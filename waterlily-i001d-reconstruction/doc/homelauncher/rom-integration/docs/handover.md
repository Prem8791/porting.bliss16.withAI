# Handover — Session 2

## Current State

`com.home.launcher` runs as a privileged platform app from `/system/priv-app/` on the VM (Bliss OS I001D, Android 14). The app is set as the default HOME activity.

## What Was Done This Session

1. **Patches 0007/0008 fixed for `git apply` flow** — hunk headers were malformed (0007 claimed 3 original lines but only provided 2; 0008 had bare `@@` with no line counts). Both now apply cleanly from the ROM root. Verified with `git apply --check` on mock repo.

2. **Two analyses performed:**
   - **A: Reflection replacement analysis** — Identified `android.app.task.TaskOrganizer` (API 31+, public SDK) as the best replacement for 4/5 recents operations. `getTaskSnapshot()` and `forceStopPackage()` still need reflection or `platform_apis: true` direct calls.
   - **B: Swipe-to-home broken analysis** — Determined root cause of gesture nav failure.

## Key Findings

### A. Reflection → TaskOrganizer

| Operation | Current | Replacement | Status |
|-----------|---------|-------------|--------|
| `getRecentTasks()` | `IActivityTaskManager.getRecentTasks()` via reflection | `TaskOrganizer.getRecentTasks()` | Public API, no reflection |
| `registerTaskStackListener()` | `IActivityTaskManager.registerTaskStackListener()` via Proxy reflection | `TaskOrganizer.registerListener(TaskListener)` | Callback-driven, no polling |
| `removeTask()` / `removeAllVisibleRecentTasks()` | `IActivityTaskManager.removeTask()` via reflection | `TaskOrganizer.removeRecentTasks(taskIds)` | Public API |
| `startActivityFromRecents()` | `IActivityTaskManager.startActivityFromRecents()` via reflection | `TaskOrganizer.startActivityFromRecents()` | Public API |
| `getTaskSnapshot()` | `IActivityTaskManager.getTaskSnapshot()` via reflection | **No TaskOrganizer equivalent** | Needs reflection or `platform_apis: true` + direct AIDL |
| `forceStopPackage()` | `ActivityManager.forceStopPackage()` via reflection | **No TaskOrganizer equivalent** | Needs reflection |

`compileSdk = 34` already includes `TaskOrganizer` in the public SDK. `MANAGE_ACTIVITY_TASKS` is already declared and granted. The stub `TaskOrganizerRecentTasksBackend` at `app/.../task/TaskOrganizerRecentTasksBackend.kt` needs implementation.

### B. Overlay Broke Gesture Navigation (CRITICAL)

**Root cause**: `home_launcher_product.mk` includes `HomeLauncherConfigOverlay` in `PRODUCT_PACKAGES`. This overlay sets `config_recentsComponentName` to `com.home.launcher/com.home.launcher.RecentsActivity`. SystemUI's `OverviewProxyService` detects the change and tries to bind via `QUICKSTEP_SERVICE` / `IOverviewProxy` / `ISystemUiProxy` contracts. The app has no such service. **SystemUI disables all gesture navigation** — swipe-to-home, swipe-to-recents, and back gesture all break.

**Documentation already warned against this**: `deployment-manifest.md:120` says *"Do not add to PRODUCT_PACKAGES until com.home.launcher implements the QuickStep/Overview service contract."* `deferred-tasks.md:7` lists enabling the overlay as deferred. `architecture-summary.md:75` lists it as optional future work.

### C. CPU Stats Not Working

`SystemStatsProvider.getCpuUsage()` reads `/proc/stat` via plain `FileReader` (not reflection). SELinux denies `proc_stat:file { read }` for `platform_app`. Patch 0008 adds the allow rule, but the OTA must be rebuilt for it to take effect. After 10 consecutive denials, the backoff permanently returns `-1` (shows `⏸` on the stats bar).

## Required Actions

### Immediate (blockers)

1. **Remove `HomeLauncherConfigOverlay` from `home_launcher_product.mk`** — Without this, gesture navigation is broken. The overlay stays in the repo as a reference; just don't include it in PRODUCT_PACKAGES.

2. **Rebuild OTA on VM** with the fixed product config:
   ```sh
   make otapackage -j$(nproc)
   ```
   Then reflash and verify gesture navigation works.

### High Priority

3. **Implement `TaskOrganizerRecentTasksBackend`** — Replace reflection for `getRecentTasks()`, `removeTask()`, `removeAllVisibleRecentTasks()`, `startActivityFromRecents()`, and `registerTaskChangeListener()`. Keep `HiddenApiBridge` only for `getTaskSnapshot()` and `forceStopPackage()`.

4. **Switch `RecentTasksRepository` default** from `ReflectionRecentTasksBackend` to `TaskOrganizerRecentTasksBackend`.

### Medium Priority

5. **Verify SELinux rule takes effect** — After OTA rebuild with patch 0008 applied, confirm with:
   ```sh
   adb shell sesearch --allow -s platform_app -t proc_stat -c file -p read
   ```
   Should show: `allow platform_app proc_stat:file { read }`

### Low Priority / Future

6. **QuickStep service contract** — Implement `QUICKSTEP_SERVICE` + `IOverviewProxy`/`ISystemUiProxy` if full Overview replacement is desired. Only then can `HomeLauncherConfigOverlay` be re-enabled.

7. **Remove reflection completely** — After TaskOrganizer covers the API gap and QuickStep service is implemented, delete `HiddenApiBridge.kt` and `ReflectionRecentTasksBackend.kt`.

## Open Questions

- Does `ActivityManager.getTaskSnapshot()` exist as a hidden API on Android 14? If so, it can replace `IActivityTaskManager.getTaskSnapshot()` with `platform_apis: true` at compile time.
- Can `forceStopPackage()` be replaced by `UsageStatsManager` or `AppOpsManager` approaches on Android 14+?

## Files Changed This Session

```
rom-integration/patches/0007-integrate-home-launcher-product-mk.patch  (hunk fix)
rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch  (hunk fix + comment)
rom-integration/docs/cloud-vm-execution-checklist.md  (updated steps)
rom-integration/docs/communication.md  (0008 reference)
rom-integration/docs/implementation-report.md  (ROM Integration + Template Rename sections)
```
