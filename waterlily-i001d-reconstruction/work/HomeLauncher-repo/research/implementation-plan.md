# Implementation Plan — Three-Column Recents Launcher

**Target:** ASUS ROG Phone II / BlissROM Android 14  
**App type:** Platform-signed standalone APK → eventual ROM-bundled privileged app  
**Package:** `com.home.launcher` (placeholder)

---

## Phase 0 — Project Scaffolding

### 0.1 Create Android project
- New project: Android Studio, Package `com.home.launcher`, minSdk 34, no activity template
- Disable AndroidX `windowSplashScreenAnimatedIcon` (we are the home screen)
- Add `AGENTS.md` noting build commands, lint, test conventions

### 0.2 Configure manifest with all permissions
- File: `AndroidManifest.xml`
- Declare every permission from `required-permissions.md` — even if not yet used
- Register `NotificationListenerService` stub
- Set `android:sharedUserId="android.uid.system"` for system-level integration (optional, may cause issues)

### 0.3 Set up build for platform signing
- Place `platform.pk8` and `platform.x509.pem` in `signing/`
- Configure `build.gradle.kts` signingConfig to use these
  ```kotlin
  signingConfigs {
      create("platform") {
          storeFile = file("signing/platform.pk8")
          // Use a keystore converted from platform.pk8 + platform.x509.pem
      }
  }
  ```
  **Note:** pk8/pem are not directly usable by Android Studio. Convert them to PKCS12:
  ```bash
  openssl pkcs8 -inform DER -nocrypt -in platform.pk8 -out platform.pem
  openssl pkcs12 -export -in platform.x509.pem -inkey platform.pem -out platform.p12
  keytool -importkeystore -destkeystore platform.jks -srckeystore platform.p12 -srcstoretype PKCS12
  ```

### 0.4 Install verification
- Build unsigned APK → sign with platform key → `adb install`
- Verify permissions granted:
  ```powershell
  adb shell dumpsys package com.home.launcher | findstr "granted=true"
  ```

---

## Phase 1 — Skeleton Layout (No System APIs)

**Goal:** Render the static three-column UI. No hidden APIs yet — placeholder data only.

### 1.1 Create root layout
- `res/layout/activity_main.xml` — three-column `LinearLayout` (horizontal) or `ConstraintLayout`
- Weight distribution: left 10%, center 80%, right 10%
- Status bar at top (system-drawn, no need for custom)

**Left column:**
- `LinearLayout` vertical with 14 buttons/textviews: A–M + #
- Tap handler placeholder (Toast)

**Right column:**
- Same: N–Z + *
- Tap handler placeholder

**Center column — 3 sub-rows (vertical LinearLayout with weights):**
- Row 1 (20% height) — two sub-sections: Notifications (left) + Today (right)
- Row 2 (75% height) — `RecyclerView` with 3-column `GridLayoutManager`, placeholder tiles
- Row 3 (5% height) — horizontal `LinearLayout`, static text labels

### 1.2 Implement RecyclerView adapter (stub)
- `RecentAppsAdapter` with `GridLayoutManager(3)` (vertical)
- Each tile: `FrameLayout` + `ImageView` (thumbnail placeholder) + `ImageButton` (close X)
- Close button: stub that removes item from local list
- Kill All button: `Button` below RecyclerView, stub that clears adapter

### 1.3 Verify layout on device
- Install and check proportions look correct
- Adjust weights and margins

**Deliverable:** Static three-column layout with placeholder data, scrolling RecyclerView, close/kill-all stubs.

---

## Phase 2 — Hidden API Access Layer

**Goal:** Create a safe access layer for all hidden/system APIs before wiring to UI.

### 2.1 Choose approach: Reflection wrapper
Create `HiddenApi.java` — static methods using reflection to access `ActivityTaskManager`, `IActivityTaskManager`, `TaskStackListener`.

```java
public class HiddenApi {
    private static final ActivityTaskManager atm =
        (ActivityTaskManager) getSystemService("activity_task");

    // Reflection call to ActivityTaskManager.getRecentTasks()
    public static List<RecentTaskInfo> getRecentTasks(int maxNum, int flags) { ... }

    // Reflection to register a TaskStackListener
    public static void registerTaskStackListener(TaskStackListener listener) { ... }

    // etc.
}
```

### 2.2 Core hidden APIs to wrap

| Method | Return | Purpose |
|---|---|---|
| `ActivityTaskManager.getRecentTasks(int, int)` | `List<RecentTaskInfo>` | Get recent task list |
| `ActivityTaskManager.registerTaskStackListener(TaskStackListener)` | `void` | Live task updates |
| `ActivityTaskManager.unregisterTaskStackListener(...)` | `void` | Cleanup |
| `ActivityTaskManager.removeAllVisibleRecentTasks()` | `void` | Kill All (recents only) |
| `IActivityTaskManager.startActivityFromRecents(int, ...)` | `int` | Resume task |
| `IActivityTaskManager.getTaskSnapshot(int, boolean)` | `TaskSnapshot` | Live thumbnail |

### 2.3 TaskStackListener stub
```java
public class LauncherTaskListener extends TaskStackListener {
    @Override public void onTaskStackChanged() { }
    @Override public void onTaskAdded(int taskId) { }
    @Override public void onTaskRemoved(int taskId) { }
    @Override public void onTaskMovedToFront(int taskId) { }
}
```

### 2.4 Force-stop via reflection
```java
public static void forceStopPackage(String packageName) {
    ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    Method m = am.getClass().getMethod("forceStopPackage", String.class);
    m.invoke(am, packageName);
}
```

### 2.5 Test each API independently
- Write small unit tests or adb-backed verification for each call
- Confirm `getRecentTasks()` returns data (even if empty)
- Confirm `registerTaskStackListener` doesn't throw SecurityException

**Deliverable:** `HiddenApi.java` with all hidden methods callable and tested on device.

---

## Phase 3 — Recent Apps Area (Live)

**Goal:** Wire the 75% center section to real system recent tasks.

### 3.1 Fetch and display recent tasks
- Call `HiddenApi.getRecentTasks(Integer.MAX_VALUE, 0)` in `onResume()`
- Map `RecentTaskInfo` → tile view:
  - `taskDescription.icon` → app icon
  - `taskDescription.label` → app name
  - `baseIntent` → launch intent for resume
  - `persistentId` → task ID for snapshot/close

### 3.2 Live thumbnails via getTaskSnapshot
- For each tile, call `HiddenApi.getTaskSnapshot(taskId, false)` to get `TaskSnapshot`
- `TaskSnapshot.getSnapshot()` → `GraphicBuffer` → convert to `Bitmap`
- Set on tile's `ImageView`
- Refresh on a timer (every 1–2s) or on `onTaskStackChanged`

### 3.3 Close single app
- X button calls `HiddenApi.removeAllVisibleRecentTasks()` with specific task
  - **Caveat:** `removeAllVisibleRecentTasks()` removes ALL. For single-task removal, may need lower-level `IActivityTaskManager.removeTask(int)` via reflection.
- Remove item from adapter + notifyDataSetChanged

### 3.4 Resume app from tile
- Tap tile → `HiddenApi.startActivityFromRecents(taskId, ...)`
- Alternative: fallback to `context.startActivity(task.baseIntent)` if reflection fails

### 3.5 Kill All button
- Iterate all visible tasks → call `HiddenApi.forceStopPackage(packageName)` on each
- Then call `HiddenApi.removeAllVisibleRecentTasks()` to clear recents list
- **Warning:** `forceStopPackage` is destructive — unloads services, clears alarms. Show confirmation dialog.

### 3.6 Live updates via TaskStackListener
- Register `LauncherTaskListener` on startup
- On `onTaskAdded`, `onTaskRemoved`, `onTaskStackChanged` → refresh adapter
- Register in `onStart()`, unregister in `onStop()`

### 3.7 Scroll performance
- `RecyclerView` + `GridLayoutManager(3, VERTICAL)`
- Thumbnail loading with `Glide` or manual `Bitmap` pool to avoid GC thrash
- Paginate `getRecentTasks` if list is long (>50)

**Deliverable:** Live recent apps grid with thumbnails, close, resume, kill-all, real-time task updates.

---

## Phase 4 — Alphabet Picker (Left + Right Columns)

**Goal:** The A–Z, #, * columns open an app list overlay.

### 4.1 Load app index
- `PackageManager.getInstalledApplications(0)` + `MATCH_ALL`
- Filter out system packages that should be hidden (optional: configurable exclude list)
- Build map: `HashMap<Character, List<ApplicationInfo>>` — keyed by first letter of app label (uppercased)
- Separate entries starting with digit → `#` bucket
- Tap `*` → show favourites (use shared prefs or a boolean metadata flag)

### 4.2 Alphabet column tap handling
- Tap letter → show overlay `PopupWindow` or `DialogFragment` or `Fragment` (covers center section)
- List in the popup: `RecyclerView` with simple vertical list, sorted alphabetically
- Each item: app icon + label
- Tap item → launch app via `context.startActivity(packageManager.getLaunchIntentForPackage(pkg))`
- Close overlay on: tap outside, tap Close button, tap letter again

### 4.3 Dual-column highlight sync
- When user taps letter on left, dim/highlight corresponding letter on right (N->Z remain unaffected, just visual feedback)
- No special cross-column navigation needed beyond visual sync

**Deliverable:** Left/right alphabet columns that open scrollable app lists. App launch works.

---

## Phase 5 — Notifications + Today (Top 20%)

**Goal:** Wire the top row to `NotificationListenerService` and `CalendarContract`.

### 5.1 Notification listener service
- `NotificationListener.kt` extending `NotificationListenerService`
- Override `onNotificationPosted(StatusBarNotification)` and `onNotificationRemoved(...)`
- Maintain in-memory map: `SparseArray<StatusBarNotification>` keyed by notification ID
- Expose active notifications to UI via callback or `LiveData`/`Flow`

### 5.2 Badge icons row
- UI: horizontal row of app icons with circular badge counts
- For each unique package in active notifications, show one icon + badge
- Order by recency (most recent notification first)
- Track expanded state — only one app expanded at a time

### 5.3 Inline expansion (iOS power pill)
- Tap app icon: hide all other icons, expand the tapped app's section
- Each notification shown as a card: icon, title, body, timestamp, swipe-to-dismiss
- Swipe → `cancelNotification(key)` on the listener service
- Tap notification → `context.startActivity(intent)` where intent comes from `StatusBarNotification.getNotification().contentIntent`
- Tap icon again or tap background → collapse back to icon-only row

### 5.4 Today section
- Query `CalendarContract.Instances` for current day's events
- Query `CalendarContract.Reminders` for active tasks
- Display: next appointment (time + title), up to 3 tasks with checkboxes (visual only — marking done requires deeper integration or launching the calendar app)
- Tap item → open Calendar app with `Intent.ACTION_VIEW` on that event

**Deliverable:** Live notification icons with expand/collapse power pill. Today section shows calendar + tasks.

---

## Phase 6 — System Stats Bar (Bottom 5%)

**Goal:** Real-time system statistics in the bottom bar.

### 6.1 Battery
- Register `BroadcastReceiver` for `Intent.ACTION_BATTERY_CHANGED`
- Extract: `level`, `scale`, `temperature`, `status` (charging/discharging)
- Display: `🔋 85%` + charging icon when plugged

### 6.2 RAM
- `ActivityManager.MemoryInfo` via `ActivityManager.getMemoryInfo()`
- `totalMem` / `availMem` → used = total - avail → percentage
- Display: `🧠 3.2/8.0 GB`

### 6.3 CPU
- Read `/proc/stat` — parse first line for total CPU ticks, calculate delta
- Display: `⚙️ 34%`
- **Note:** This is best-effort. On modern Android with isolated processes, overall CPU% is approximate.

### 6.4 Temperature
- `BatteryManager.EXTRA_TEMPERATURE` (in tenths of °C) or /sys/class/thermal/thermal_zone*
- Display: `🌡 42°C`

### 6.5 Polling strategy
- `Handler.postDelayed(runnable, 2000)` — update every 2 seconds
- Cancel on `onPause()`, restart on `onResume()`

### 6.6 Tap to expand
- Tap stats bar → open Android's built-in battery settings or a custom detail dialog

**Deliverable:** Live-updating system stats bar with battery, RAM, CPU, temperature.

---

## Phase 7 — Polish & UX

**Goal:** Make it feel like a production home screen.

### 7.1 Set as default launcher
- Declare intent filter in manifest:
  ```xml
  <activity android:name=".MainActivity" ...>
      <intent-filter>
          <action android:name="android.intent.action.MAIN" />
          <category android:name="android.intent.category.HOME" />
          <category android:name="android.intent.category.DEFAULT" />
      </intent-filter>
  </activity>
  ```
- First boot: system prompt "Which app do you want to use as home?"
- Test with `adb shell am start -c android.intent.category.HOME -a android.intent.action.MAIN`

### 7.2 Override recents overview (SystemUI hook)
- **This is the hardest part.** The stock Recents overview (SystemUI) will still show on gesture nav or 3-button recents.
- Options:
  1. **Replace SystemUI Recents** — requires modifying SystemUI in ROM source (not a standalone APK thing)
  2. **Disable stock recents** via `settings put global overview_of_most_recently_used_apps 0` (may not work on all ROM)
  3. **Embrace both** — our launcher shows recents in the home screen; the system recents still exist as a fallback

### 7.3 Animations
- Tile appear/disappear: fade + scale
- Alphabet overlay: slide in from side
- Notification expand: smooth height transition ( `TransitionManager.beginDelayedTransition()` or `animateLayoutChanges`)

### 7.4 Edge cases
- **Zero recent apps:** Show "No recent apps" placeholder instead of empty grid
- **Thumbnail loading failure:** Fallback to app icon
- **Permission denial:** Show snackbar explaining which permission is missing
- **Alphabet overlay with 0 results:** Show "No apps starting with X" message

**Deliverable:** Polished launcher with home screen intent filter, animations, edge case handling.

---

## Phase 8 — Privileged App & ROM Integration

**Goal:** Move from standalone APK to system-privileged app for full access.

### 8.1 Create privapp permissions XML
- `packages/services/Launcher/privapp-permissions-com.home.launcher.xml`
- Include all permissions from `required-permissions.md`

### 8.2 Create Android.bp
```bp
android_app {
    name: "HomeLauncher",
    srcs: ["src/**/*.java", "src/**/*.kt"],
    platform_apis: true,
    certificate: "platform",
    privileged: true,
    required: ["privapp-permissions-com.home.launcher"],
}
```

### 8.3 Add to product packages
```makefile
PRODUCT_PACKAGES += HomeLauncher
PRODUCT_COPY_FILES += \
    packages/services/Launcher/privapp-permissions-com.home.launcher.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/permissions/privapp-permissions-com.home.launcher.xml
```

### 8.4 Set as default home in overlay
- In device overlay `frameworks/base/core/res/res/values/config.xml`:
  ```xml
  <string-array name="config_defaultHome" translatable="false">
      <item>com.home.launcher</item>
  </string-array>
  ```

### 8.5 Option: Disable stock SystemUI recents
- Modify `packages/SystemUI/src/com/android/systemui/recents/RecentsImplementation.java` to no-op if our launcher is default
- OR: replace the recents key/gesture to launch our activity instead

**Deliverable:** ROM-flashed build where our launcher is the default home and recents provider.

---

## Phase 9 — Testing & Verification

### 9.1 Permission checklist
```powershell
adb shell dumpsys package com.home.launcher | findstr "granted=true"
```
Expected: `REAL_GET_TASKS`, `MANAGE_ACTIVITY_TASKS`, `START_TASKS_FROM_RECENTS`, `REMOVE_TASKS`, `READ_FRAME_BUFFER`, `FORCE_STOP_PACKAGES`, `BATTERY_STATS`, `STATUS_BAR` all show `granted=true`.

### 9.2 Functional tests
| Test | Pass criteria |
|---|---|
| Open app, go home → tile appears | New tile in recents grid within 1s |
| Tap tile → app resumes | App returns to previous state |
| X button → app removed | Tile gone, app removed from system recents |
| Kill All → all apps removed | Grid empty, recents cleared, processes stopped (check `ps`) |
| Tap letter → app list shows | Correct apps for that letter |
| Notification arrives → badge shows | Icon + count appears within seconds |
| Tap notification icon → expand | Other icons hide, notifications stack shows |
| Swipe notification → dismissed | `cancelNotification()` called, count decrements |
| Stats bar → numbers update | RAM, battery, CPU change over time |

### 9.3 Stability
- Run for 24h with normal usage — no ANRs, no crashes
- Test with 50+ recent apps open — RecyclerView should scroll smoothly
- Test notification storm (50 notifications at once) — UI should not freeze

---

## Dependency Graph

```
Phase 0 (Scaffolding)
    │
    ▼
Phase 1 (Static Layout) ──────┐
    │                          │
    ▼                          ▼
Phase 2 (Hidden API Layer)   Phase 4 (Alphabet Picker)
    │                          │
    ▼                          │
Phase 3 (Recent Apps Live) ───┤
    │                          │
    ▼                          ▼
Phase 5 (Notifications) ─── Phase 7 (Polish)
    │
    ▼
Phase 6 (System Stats) ─── Phase 7 (Polish)
    │
    ▼
Phase 8 (ROM Integration)
    │
    ▼
Phase 9 (Testing)
```

Phases 4, 5, 6 can be built in parallel after Phase 1 is done, since they don't depend on each other.

---

## Key Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Hidden API reflection breaks in Android 14 U | Check AOSP source for API 34 changes. Use `@SuppressLint("SoonBlockedPrivateApi")` and test early. |
| `getTaskSnapshot` returns null | Fallback to app icon from `PackageManager`. Snapshot may be null if app is not visible or if hardware acceleration issue. |
| `forceStopPackage` kills our own process if we kill system apps | Whitelist: never kill our own package or SystemUI. |
| Notification listener not granted | Guide user to Settings → Notification Access on first run. Include `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` intent. |
| Recents overview (SystemUI) conflicts with our recents | Accept both coexist in Phase 7. Only replace SystemUI recents in Phase 8 with ROM source changes. |
| Platform key not available / lost | Keep backup of `platform.pk8` and `platform.x509.pem`. Without them, signature permissions cannot be granted. |
