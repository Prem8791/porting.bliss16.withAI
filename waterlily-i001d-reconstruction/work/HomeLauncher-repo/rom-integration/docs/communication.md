# Communication: First-Boot Logcat Findings

Captured from the VM after flashing HomeLauncher as a baked-in `/system/priv-app`.

## Observed Logcat

```
05:30:59.574  W  type=1400 audit(0.0:45): avc:  denied { read }
                  for name="stat" dev="proc" ino=4026532110
                  scontext=u:r:platform_app:s0:c512,c768
                  tcontext=u:object_r:proc_stat:s0 tclass=file
                  permissive=0  app=com.home.launcher

05:31:01.378  I  getRecentTasks parsed 0/0 tasks
...
05:31:13.514  I  TaskStackListener unregistered
05:31:13.534  D  why are we still using reflection? should we not
                  do the legitimate way? and did we make an selinux
                  policy for accessing the cpu parameters?
```

Two issues: **SELinux proc_stat denial** and **reflection recents returning 0 tasks**.

---

## Issue 1: SELinux `proc_stat` Denial

### Symptom

Repeating AVC denial every ~2 seconds:

```
avc: denied { read } for name="stat" dev="proc"
  scontext=u:r:platform_app:s0:c512,c768
  tcontext=u:object_r:proc_stat:s0 tclass=file
```

The app runs as the generic `platform_app` domain (no custom domain transition). The culprit is `SystemStatsProvider.getCpuUsage()` at `app/src/main/java/com/home/launcher/data/SystemStatsProvider.kt:73`:

```kotlin
val reader = BufferedReader(FileReader("/proc/stat"))
```

### Root Cause

A policy file exists at `rom-integration/sepolicy/private/platform_app_home_launcher.te`:

```
allow platform_app proc_stat:file r_file_perms;
```

**The rule existing under `rom-integration/` is not enough. It must be applied into the active ROM sepolicy and included in the flashed OTA.**

An active patch is now provided at `rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch`. This creates `system/sepolicy/private/platform_app_home_launcher.te` in the ROM tree — the same rule as before, but now delivered as a `git apply`-ready patch that targets the correct sepolicy path.

### Next Steps

1. Apply the patch in the VM AOSP tree:
   ```sh
   git apply < packages/apps/HomeLauncher/rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch
   ```
2. Verify the rule compiles into sepolicy:
   ```sh
   adb shell sesearch --allow -s platform_app -t proc_stat -c file -p read
   ```
3. If absent, rebuild the full OTA (`make otapackage`) and reflash. Rebuilding `sepolicy` alone is insufficient; the OTA must be regenerated from scratch.

---

## Issue 2: Reflection Recents Returns 0 Tasks

### Symptom

```
05:31:01.378  I  ReflectionTasksBackend: getRecentTasks parsed 0/0 tasks
05:31:13.514  I  TaskStackListener unregistered
```

The app repeatedly fetches recent tasks via hidden API reflection and gets nothing. Then the listener is unregistered (UI probably hides the recents panel).

### Root Cause

`RecentTasksRepository` (`app/src/main/java/com/home/launcher/task/RecentTasksRepository.kt:9`) defaults to `ReflectionRecentTasksBackend`:

```kotlin
RecentTasksRepository(
    context,
    backend = ReflectionRecentTasksBackend(context)
)
```

`ReflectionRecentTasksBackend.getRecentTasks()` calls `IActivityTaskManager.getRecentTasks()` via reflection. This returns zero tasks even though `dumpsys activity recents` showed tasks. The most important known cause from device logs was:

```
mRecentsComponent=ComponentInfo{com.android.launcher3/com.android.quickstep.RecentsActivity}
```

That means HomeLauncher was the HOME app, but Launcher3/Quickstep was still the system Recents owner.

Other possible causes:

- The method signature or return type has changed on the target Android 14+ build.
- Hidden API access may be blocked even for platform apps (the greylist/blacklist applies at runtime).
- The reflection path was never validated against this specific ROM branch.
- The call flags/user id are too narrow for this branch.

The `TaskOrganizerRecentTasksBackend` at `app/src/main/java/com/home/launcher/task/TaskOrganizerRecentTasksBackend.kt` exists but is a **stub** — every method returns `emptyList()`, `false`, or `null`.

Now that the app runs as a privileged `platform_app`, reflection is still a workaround, but it should not be the final architecture. A ROM-bundled app can use platform APIs if built against the platform, or it can be paired with a small framework/system service. `TaskOrganizer` alone is not a drop-in replacement for Quickstep Recents.

**Do not assume TaskOrganizer is the immediate legitimate replacement.** First verify that HomeLauncher actually becomes `mRecentsComponent`. If it does and `getRecentTasks` still returns 0/0, then debug or replace the backend.

### Verification — Is HomeLauncher the Recents Component?

After flashing an OTA where `HomeLauncherConfigOverlay` is included in `PRODUCT_PACKAGES`:

```sh
adb shell dumpsys activity recents | grep -i "mRecentsComponent\|mRecentsUid"
```

Expected:

```
mRecentsComponent=ComponentInfo{com.home.launcher/com.home.launcher.RecentsActivity}
```

If this still shows `com.android.launcher3` or `com.android.quickstep`, the overlay is not active. Check:

- `HomeLauncherConfigOverlay` is in `PRODUCT_PACKAGES` for the target product.
- The overlay's `AndroidManifest.xml` has `android:targetPackage="android"` and `android:isStatic="true"`.
- The overlay's `config.xml` defines `config_recentsComponentName`.

### If HomeLauncher IS the Recents Component

Then verify the system has tasks:

```sh
adb shell dumpsys activity recents | grep -i "Recent #\|taskId\|realActivity\|baseIntent"
```

If tasks exist but the app logs `0/0`, the reflection backend is genuinely broken. At that point:

1. Keep `ReflectionRecentTasksBackend` temporarily — log the raw return object and try broader recent-task flags.
2. Implement a real platform-integrated backend only after confirming the reflection path cannot be salvaged.
3. Keep any `TaskOrganizerRecentTasksBackend` work behind a selectable backend flag until fully validated. `TaskOrganizer` alone is not a drop-in replacement for Quickstep Recents; it requires the full WM Shell / Overview contract.

See `rom-integration/docs/taskorganizer-migration-roadmap.md` for the planned architecture.

---

## Summary

| Issue | File | Status |
|-------|------|--------|
| SELinux proc_stat denied | `rom-integration/sepolicy/private/platform_app_home_launcher.te` | Must be integrated into active ROM sepolicy and flashed |
| Reflection recents 0 tasks | `config_recentsComponentName` / `ReflectionRecentTasksBackend` | First verify HomeLauncher is the configured Recents component; then debug backend |

---

## Fixes Applied (commit pending)

The following code changes address the architecture gaps identified above:

| # | Issue | File | What changed |
|---|-------|------|-------------|
| 1 | Silent failure path | `ReflectionRecentTasksBackend.kt:19-38` | `getRecentTasks` now uses `HiddenApiBridge.call()` and logs every intermediate step: whether iAtm is null, whether invoke() succeeded, raw return type, unwrapped list size |
| 2 | No failure backoff | `SystemStatsProvider.kt:29-33,80-82,119-124` | Consecutive CPU read failures increment `consecutiveCpuDenials`. After 10 failures, `getCpuUsage()` returns -1 and `shouldThrottleCpuPoll()` returns true. `SystemStatsBar` switches to 10s interval when throttled and shows "⏸" instead of "⚙️ 0%" |
| 3 | No permission self-verification | `MainActivity.kt:101-110,128-137` | `onCreate()` now logs every privileged permission check result via `checkSelfPermission()` with tag `PermCheck` |
| 4 | No build fingerprint | `MainActivity.kt:98-100` | `onCreate()` now logs a static AOSP system-image build marker, package name, UID, SDK version |
| 5 | Main-thread polling | *(deferred)* | All pollers still run on `Handler(Looper.getMainLooper())`. Moving I/O to a background thread requires coroutines or a thread-switching refactor beyond this pass |
| 6 | Duplicate timer risk | `MainActivity.kt:69,74,84,143-144,148,153-154` | `onResume` now calls `handler.removeCallbacks(...)` before posting each timer. Runnable bodies check `pollingActive` flag before re-posting |
| 7 | Activity pause kills polling | *(documented)* | Expected lifecycle behavior; the `pollingActive` flag ensures no timer re-posts after `onPause` |
| 8 | Redundant self-filter | `MainActivity.kt:260` | Removed `.filter { it.packageName != packageName }` — `excludeFromRecents="true"` in the manifest already prevents this |
| 9 | Thermal fallback | `SystemStatsProvider.kt:128-132` | Removed `/sys/class/thermal/thermal_zone0/temp` fallback. Temperature now only reads from `BatteryManager.EXTRA_TEMPERATURE` |
| 10 | Missing build fingerprint | `MainActivity.kt` | Uses a static `aospBuild=HomeLauncher system image build` log that works in both Gradle and AOSP/Soong builds |

---

## Supplemental Analysis: Architecture Gaps

### 1. Silent Failure Path in Reflection Backend

`ReflectionRecentTasksBackend.getRecentTasks()` (`system/hiddenapi/ReflectionRecentTasksBackend.kt:19`) has no intermediate logging to distinguish an API call failure from a legitimate empty result:

```kotlin
val raw = HiddenApiBridge.invoke(service, "getRecentTasks", maxNum, RECENT_WITH_EXCLUDED, USER_CURRENT)
val taskList = unwrapParceledList(raw) ?: return emptyList<RecentTask>()
return parseRecentTasks(taskList)
```

`HiddenApiBridge.invoke()` (line 12) wraps every call in `runCatching` and returns `HiddenApiResult(invoked=false, value=null)` on any exception — `ClassNotFoundException`, `NoSuchMethodException`, `IllegalAccessException`, or `InvocationTargetException`. The only log is a `Log.w` at the method-not-found level inside `findCompatibleMethod`. The `getRecentTasks` caller never checks `invoked`, so it cannot tell whether:

- The reflection call failed entirely → `raw` is null → `0/0 tasks`
- The reflection call succeeded but returned an empty `ParceledListSlice` → `0/0 tasks`
- The reflection call returned a non-empty slice but parsing silently failed → `0/0 tasks`

**Fix**: Add `Log.i` at each stage — after invoke (log `invoked` status), after unwrapParceledList (log raw type and size), and before parse (log raw list count). This triage step is needed before any backend replacement.

### 2. No Failure Backoff on Any Poller

Three independent polling loops run simultaneously with no backoff when they fail:

| Poller | Interval | Source | What happens on failure |
|--------|----------|--------|------------------------|
| CPU stats | 2s | `SystemStatsBar.refreshRunnable` | Silently shows 0%, retries 2s later |
| Recents | 3s | `MainActivity.refreshRunnable` | Silently shows empty grid, retries 3s later |
| Notifications | 2s | `MainActivity.notificationRefreshRunnable` | Silently shows placeholder, retries 2s later |

None of them throttle or stop after repeated failures. In the logcat, the CPU/SELinux denial repeats 7 times in 12 seconds (05:30:59 to 05:31:11) with no let-up. Even after the SELinux policy is fixed, this polling architecture is wasteful — especially for recents, which polls via expensive reflection on every tick.

**Fix**: Add a failure counter. After N consecutive failures, back off the interval (e.g., 2s → 5s → 30s) or stop polling until the activity is resumed.

### 3. No Permission Self-Verification at Startup

The app never logs whether its critical privileged permissions were actually granted at runtime. The post-flash checklist (`docs/post-flash-verification-checklist.md`) documents an `adb shell dumpsys package` command for this, but the logcat shows no confirmation that `REAL_GET_TASKS`, `MANAGE_ACTIVITY_TASKS`, `START_TASKS_FROM_RECENTS`, or `REMOVE_TASKS` are enabled.

Without this, a missing `privapp-permissions-com.home.launcher.xml` inclusion — the exact case that commit c5f47aa fixed — silently cripples recents while the app appears to be running normally.

**Fix**: Add `PackageManager.checkPermission()` calls at startup and log each result. This makes the logcat self-documenting about permission state.

### 4. No Build/Commit Fingerprint in Logcat

No logcat entry identifies which source commit or build variant is running. If the VM has a stale build or a different patch set, the runtime behavior cannot be correlated with the source tree.

**Fix**: Log a static AOSP system-image build marker plus package name, UID, SDK, and Android release in `onCreate()`.

### 5. All Polling Runs on the Main Thread

All three timers execute on the main thread via `Handler(Looper.getMainLooper())`:

- `SystemStatsBar.kt:19` — `Handler(Looper.getMainLooper())`
- `MainActivity.kt:69` — `Handler(Looper.getMainLooper())`
- `MainActivity.kt:80` — `Handler(Looper.getMainLooper())`

The recents poll calls `HiddenApiBridge.invoke` → `Method.invoke` on the main thread. The CPU poll reads `/proc/stat` (blocking I/O) on the main thread. With all three firing every 2–3 seconds on the same thread, they can queue up and cause frame drops, especially during reflection or when the audit log is under denial storm pressure.

**Fix**: Move I/O and reflection work to a background thread or coroutine dispatcher. Only post UI updates back to the main thread.

### 6. Duplicate Timer Accumulation on Multiple `onResume` Calls

Each call to `onResume()` posts a new `refreshRunnable`, `notificationRefreshRunnable`, and `statsBar.start()` — but `onPause()` only removes the callbacks that were posted from the *current* resume cycle. If `onResume` fires twice without an intervening `onPause` (possible in edge cases like `singleTask` launch), duplicate timers accumulate, causing redundant work and AVC amplification.

**Fix**: Guard timer starts with a flag (similar to `statsBar.running`), or use `handler.removeCallbacks(...)` before posting.

### 7. Activity Pause Kills All Polling Mid-Interaction

The logcat shows:
```
05:31:13.514  I  TaskStackListener unregistered    (onPause)
05:31:13.534  D  visibilityChanged old=true new=false  (window hidden)
```

The activity was paused during the recents poll cycle. This means:
- `statsBar.stop()` was called → CPU polling stops
- `handler.removeCallbacks(refreshRunnable)` → recents polling stops
- `handler.removeCallbacks(notificationRefreshRunnable)` → notification polling stops
- `unregisterTaskListener()` → TaskStackListener deregistered

If the pause was transient (another activity briefly covered the launcher), all polling state is lost and must be re-established from scratch when `onResume` fires again. The recents listener re-registration on resume will trigger another `registerTaskStackListener` call, but if the reflection method has an issue, this repeats the failure cycle.

**Why was it paused?** No crash stack trace appears. Possible causes:
- A system dialog appeared (permission request, profile picker, setup wizard overlay).
- Another activity (Settings invoked from the launcher menu) briefly took focus.
- The `singleTask` launch mode re-delivered an intent, causing `onNewIntent` + lifecycle shuffle.

**Fix**: Investigate what caused the pause. If it's a transient dialog, suppress or defer it. If it's the settings activity, the launcher should expect to be paused.

### 8. Recents Self-Filter Is Redundant

`refreshRecentTasks()` at `MainActivity.kt:230` filters the task list:
```kotlin
val tasks = recentTasksRepository.getRecentTasks(30).filter { it.packageName != packageName }
```

The manifest already excludes the launcher from recents via `android:excludeFromRecents="true"` at line 43 of `AndroidManifest.xml`. The framework will never return the launcher in its own recent-task query. The app-side filter is harmless but unnecessary.

### 9. Thermal Fallback Triggers a Second SELinux Denial

`SystemStatsProvider.getTemperature()` at line 117–125 (`app/src/main/java/com/home/launcher/data/SystemStatsProvider.kt`) falls back to reading `/sys/class/thermal/thermal_zone0/temp` when the battery manager intent is unavailable:

```kotlin
val reader = BufferedReader(FileReader("/sys/class/thermal/thermal_zone0/temp"))
```

This will cause a `sysfs_thermal` AVC denial on top of the `proc_stat` denial. The deferred draft at `rom-integration/sepolicy/draft/platform_app_home_launcher.te` targets this:
```
allow platform_app sysfs_thermal:file r_file_perms;
```
but is marked "Do not enable before first boot." The first boot already happened and it's still not enabled. If battery manager intent works (it does in the logcat — the battery stat bar updates), the fallback path isn't reached, but if the intent ever fails, a second denial storm begins.

**Fix**: Either enable the `sysfs_thermal` rule alongside `proc_stat`, or remove the `thermal_zone0` fallback entirely (battery manager provides temperature via `EXTRA_TEMPERATURE`).

### 10. Missing Verification Points in the Observed Logcat

The post-flash checklist specifies several checks that were not confirmed in the captured logcat:

| Checklist Item | What to look for | In logcat? |
|---|---|---|
| Package at `/system/priv-app/` | `pm path` / `dumpsys` | Not shown |
| `hiddenApiEnforcementPolicy=0` | `dumpsys package` | Not shown |
| `pkgFlags=SYSTEM`, `privateFlags=PRIVILEGED` | `dumpsys package` | Not shown |
| `granted=true` for all privileged permissions | `dumpsys package` | Not shown |
| HOME resolution | `resolve-activity` | Not shown |
| `mRecentsComponent` | `dumpsys activity recents` | Not shown |
| AVC denials for thermal | `logcat -b all` grep | Not collected |

Without these, it is not possible to determine whether the OTA was correctly assembled. The `0/0 tasks` and `proc_stat` denial may be the *only visible symptoms* of a deeper configuration gap (e.g., missing overlay, missing privapp XML, stale OTA).
