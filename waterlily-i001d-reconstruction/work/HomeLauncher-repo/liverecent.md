# Live Recent Cards — Real-time Surface Feed for Recents Grid

## Goal

Replace the current static `Bitmap` snapshots (polled every 3s from `getTaskSnapshot()`) with **live, real-time surface content** per task card — the recents grid should show apps as they actually look right now.

## Current Architecture

```
Every 3000ms: getTaskSnapshot(taskId) ──→ Bitmap (frozen) ──→ ImageView.setImageBitmap()
         ↑                              ↑
   poll interval                  system captured this at task-switch time
```

## Target Architecture

```
Continuous: ──→ SurfaceControl per card ──→ SurfaceView rendering live GPU content
                    ↑
              SurfaceControl.mirrorSurface()
              or
              SurfaceControl.mirrorDisplay() + crop
```

---

## Two Approaches

### Approach A: TaskOrganizer + SurfaceControl.mirrorSurface() (Recommended)

Uses the modern `TaskOrganizer` pipeline that QuickStep/Launcher3 uses. This delivers a `SurfaceControl` leash per task directly from WindowManagerService.

#### Prerequisites

| Requirement | Status |
|-------------|--------|
| Platform-signed APK in `/system/priv-app` | ✅ Already done |
| `TaskOrganizer` registration with WMS | ❌ Not yet; the stub exists at `TaskOrganizerRecentTasksBackend.kt` but is unimplemented |
| `MANAGE_ACTIVITY_TASKS` | ✅ Already declared |
| AOSP build (`platform_apis: true`) | ✅ `Android.bp` exists |
| `config_recentsComponentName` set to this app | ❗ Investigated but caused SystemUI gesture breakage |

#### Implementation

**New file: `animation/LiveTileLayer.kt`** — per-card surface host:

```
LiveTileLayer(SurfaceView)
│
├── Attach phase:
│       TaskOrganizer.onTaskAppeared(taskInfo, taskSurfaceControl)
│           ↓
│       mirror = SurfaceControl.mirrorSurface(taskSurfaceControl)
│           ↓
│       childSurfaceControl = SurfaceControl.Builder()
│           .setParent(mirror)
│           .setName("live-recent-${taskId}")
│           .build()
│           ↓
│       surfaceView.getSurfaceControl().reparent(childSurfaceControl)
│
├── Update phase:
│       TaskOrganizer.onTaskInfoChanged(taskInfo)
│           ↓
│       mirror.transaction { setCropRect(taskBounds) }.apply()
│
└── Detach phase:
        TaskOrganizer.onTaskVanished(token)
            ↓
        mirror.release()
        childSurfaceControl.release()
```

**Modified file: `task/RecentTasksBackend.kt`** — add surface access:

```kotlin
interface RecentTasksBackend {
    // Existing methods...
    fun getTaskSurface(taskId: Int): SurfaceControl?  // NEW
}
```

**Modified file: `adapter/RecentAppsAdapter.kt`** — replace `ImageView` with `SurfaceView`:

```kotlin
// In TileViewHolder:
private val liveSurface: SurfaceView = itemView.findViewById(R.id.liveSurface)

fun bindSurface(surfaceControl: SurfaceControl?) {
    if (surfaceControl != null) {
        // Reparent mirrored surface to this SurfaceView
        val svSc = liveSurface.surfaceControl
        SurfaceControl.Transaction().apply {
            svSc?.let { reparent(surfaceControl, it) }
            show(surfaceControl)
            apply()
        }
        thumbnail.visibility = View.GONE
        liveSurface.visibility = View.VISIBLE
    } else {
        // Fall back to static thumbnail
        liveSurface.visibility = View.GONE
        thumbnail.visibility = View.VISIBLE
    }
}
```

**Modified layout: `item_recent_tile.xml`** — add `SurfaceView` alongside `ImageView`:

```xml
<SurfaceView
    android:id="@+id/liveSurface"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:visibility="gone" />
```

**Modified file: `MainActivity.kt`** — use `TaskOrganizer` instead of polling:

```kotlin
private fun initLiveRecents() {
    taskOrganizer = TaskOrganizer(executor) { taskInfo, surfaceControl ->
        // onTaskAppeared — assign surface to tile
        val tile = recentAppsAdapter.getTileByTaskId(taskInfo.taskId)
        tile?.bindSurface(surfaceControl)
    }
    taskOrganizer.register()
    pollingActive = false  // Stop the 3s poll
}
```

#### Phases

| Phase | What | Effort |
|-------|------|--------|
| 1 | Implement `TaskOrganizer` registration in the AOSP source set | 1–2 days |
| 2 | Add per-card `SurfaceView` + surface reparenting | 1 day |
| 3 | Remove snapshot polling, replace with surface-based rendering | 0.5 day |
| 4 | Handle edge cases (task bounds change, orientation, background tasks) | 1 day |

#### Risks

| Risk | Mitigation |
|------|------------|
| `TaskOrganizer` callbacks blocked by SELinux or permission checks | Already platform-signed + privileged; test with `dmesg \| grep avc` |
| SurfaceControl mirrorSurface() requires `CAPTURE_VIDEO_OUTPUT` | Add to `privapp-permissions` allowlist |
| `mRecentsComponent` conflict — SystemUI disables gestures when set to this app | Documented in `rom-integration/docs/communication.md`; requires `QuickStepService` binding |
| Surface reparenting causes rendering artifacts | Use `SurfaceControl.Transaction` synchronization; test per-device |

---

### Approach B: Display Mirror + Per-Task Crop (Simpler, Works Now)

Mirror the entire display via `SurfaceControl.mirrorDisplay()` and crop per-task bounds, assigning each card a viewport into the shared mirror. All cards share ONE mirrored surface but display different crop regions.

#### Implementation Sketch

**`animation/DisplayMirrorHost.kt`** — singleton:

```kotlin
class DisplayMirrorHost(context: Context) {
    private val displayMirror = SurfaceControl.mirrorDisplay(Display.DEFAULT_DISPLAY)

    // Each card gets a SurfaceControl that is a child of displayMirror
    // with crop set to that task's window bounds.

    fun createTileViewport(taskBounds: Rect): SurfaceControl {
        val vp = SurfaceControl.Builder()
            .setParent(displayMirror)
            .setName("viewport-${System.identityHashCode(taskBounds)}")
            .build()
        SurfaceControl.Transaction().apply {
            setCropRect(vp, taskBounds)
            setLayer(vp, 1)
            show(vp)
            apply()
        }
        return vp
    }

    fun updateTileViewport(surface: SurfaceControl, taskBounds: Rect) {
        SurfaceControl.Transaction().apply {
            setCropRect(surface, taskBounds)
            apply()
        }
    }

    fun release() {
        displayMirror.release()
    }
}
```

#### Task bounds source

Track task bounds using either:
- `ActivityTaskManager.getService().getTaskBounds(taskId)` via hidden AIDL (current method, works)
- `RunningTaskInfo.configuration.windowConfiguration.getBounds()` from polling `getRecentTasks()`

#### Pros vs Cons

| | Approach A (TaskOrganizer) | Approach B (Display Mirror) |
|---|---|---|
| **Latency** | Zero — true live surface | Near-zero (reflects entire display) |
| **Correctness** | Shows only the task | Shows ALL screen content (overlays, dialogs, nav bar) |
| **Complexity** | High — full TaskOrganizer pipeline | Medium — one mirror + per-card viewports |
| **Scaling** | Per-task surfaces, heavy | One surface, lightweight |
| **Limitation** | Needs full WM Shell integration | Shows whatever is on screen, not per-task isolation |

---

## Render Pipeline Comparison

```
Current (static):
  SystemSnapshot ──→ Bitmap ──→ ImageView.drawBitmap()
                                   ↑
                              CPU decode each frame

Approach A (live per-task):
  Task Surface (GPU) ──→ SurfaceControl.mirrorSurface() ──→ SurfaceView (GPU)
                                                               ↑
                                                          Zero-copy, GPU-only

Approach B (display mirror):
  Entire Display (GPU) ──→ SurfaceControl.mirrorDisplay() ──→ SurfaceControl.child(crop)
                                                               └── SurfaceView per card
                                                               ↑
                                                          Single GPU copy per frame
```

## Recommendation

**Approach A** (TaskOrganizer + mirrorSurface) is the long-term correct solution — it's what QuickStep/Launcher3 uses. However, it requires the full AOSP integration path (WM Shell, `QuickStepService`, `mRecentsComponent` handling), which the project's own research documents describe as complex and currently blocked.

**Approach B** (Display Mirror + crop) can be prototyped immediately with existing platform signing and does not require any new AOSP integration. The trade-off is that cards show a cropped view of the full display (including nav bar, status bar, etc.) rather than isolated per-task content.

### Suggested Path

1. **Prototype Approach B first** (2–3 days) — get live display mirror working in the recents area
2. **Then migrate to Approach A** after TaskOrganizer integration is unblocked

---

## File Manifest

| File | Action | Purpose |
|------|--------|---------|
| `animation/LiveTileLayer.kt` | **Create** | SurfaceView wrapper with surface reparenting logic |
| `animation/DisplayMirrorHost.kt` | **Create** (Approach B) | Display mirror singleton + viewport factory |
| `task/TaskOrganizerRecentTasksBackend.kt` | **Implement** (Approach A) | Register TaskOrganizer, dispatch surfaces |
| `adapter/RecentAppsAdapter.kt` | **Modify** | Replace ImageView → SurfaceView for live tiles |
| `res/layout/item_recent_tile.xml` | **Modify** | Add `SurfaceView` to tile layout |
| `MainActivity.kt` | **Modify** | Wire live surface pipeline, disable polling |

## Testing

- Verify each card shows live app content (open a clock app and watch the seconds tick)
- Verify recents grid updates when tasks change position in the stack
- Measure GPU memory vs current snapshot approach
- Test with 10+ recent tasks
- Verify no SELinux denials: `adb logcat -b events | grep avc`
