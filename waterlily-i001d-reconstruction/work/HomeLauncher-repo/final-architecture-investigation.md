# Final Architecture Investigation: `com.home.launcher`

Date: 2026-07-08  
Device/ROM: ASUS I001D, BlissROM Android 14 (`Bliss-v17.8.3-I001D-UNOFFICIAL-vanilla-20260708`)  
Scope: Validate remaining architectural assumptions before ROM/framework changes.

This report intentionally does not repeat previously verified SELinux denials for `/proc/stat` and `sysfs_thermal`, signature permission grants, or current package classification except where needed to support a new conclusion.

## Executive Conclusion

`com.home.launcher` reflection succeeds because the APK is signed with the platform certificate. Platform signing causes Android to assign `hiddenApiEnforcementPolicy=0`, which disables standard ART hidden API enforcement for that app process.

The observed behavior is not caused by:

- global `hidden_api_policy` settings
- global hidden API blacklist exemptions
- `usesNonSdkApi=true`
- current package being the configured Recents component
- current package being installed as a privileged/system app

For a permanent custom ROM component, the recommended architecture is to adopt the modern Launcher3/QuickStep-style Overview pipeline: `TaskOrganizer` plus WM Shell/SystemUI `OverviewProxyService` integration. The current legacy reflection approach is suitable only as a temporary prototype.

## Runtime Hidden API Evidence

A throwaway probe module was added under:

```text
hidden-api-probe/
```

The same APK code was tested twice:

1. normal debug signing
2. re-signed with `platform.pk8` / `platform.x509.pem`

### ROM Hidden API Global State

Runtime state collected with `adb shell getprop` and `settings`:

```text
ro.build.type=user
ro.build.version.sdk=34
ro.bliss.build=Bliss-v17.8.3-I001D-UNOFFICIAL-vanilla-20260708
persist.debug.dalvik.vm.core_platform_api_policy=just-warn
hidden_api_policy=null
hidden_api_policy_pre_p_apps=null
hidden_api_policy_p_apps=null
hidden_api_blacklist_exemptions=null
```

The `core_platform_api_policy=just-warn` property is not the explanation for app-to-framework hidden API reflection. ART has separate standard hidden API enforcement and core-platform API enforcement paths.

### Normal-Signed Probe Result

Runtime package state:

```text
Package: com.home.hiddenapiprobe
Signature: debug cert
targetSdk=34
usesNonSdkApi=false
hiddenApiEnforcementPolicy=2
SELinux domain: ordinary app domain
```

Observed reflection results:

```text
FAIL ActivityTaskManager.getService -> NoSuchMethodException
OK   ServiceManager.getService -> BinderProxy
FAIL IActivityTaskManager.Stub.asInterface -> NoSuchMethodException
FAIL ActivityTaskManager.getInstance -> NoSuchMethodException
SKIP IActivityTaskManager.getRecentTasks -> no service proxy
SKIP IActivityTaskManager.registerTaskStackListener -> no service proxy
SKIP IActivityTaskManager.unregisterTaskStackListener -> no service proxy
SKIP IActivityTaskManager.removeTask -> no service proxy
SKIP IActivityTaskManager.removeAllVisibleRecentTasks -> no service proxy
SKIP IActivityTaskManager.startActivityFromRecents -> no service proxy
SKIP IActivityTaskManager.getTaskSnapshot -> no service proxy
OK   TaskInfo.taskId
OK   TaskInfo.baseIntent
FAIL TaskInfo.userId -> NoSuchFieldException
OK   TaskInfo.taskDescription
OK   TaskDescription.getLabel
FAIL TaskSnapshot.getHardwareBuffer -> NoSuchMethodException
OK   TaskSnapshot.getSnapshot
OK   TaskSnapshot.getOrientation
OK   ActivityManager.forceStopPackage lookup
```

Interpretation: with standard hidden API enforcement enabled (`2`), ART hides several framework members from reflection. Some fields/methods remain visible because they are public/unrestricted on this build.

### Platform-Signed Probe Result

Runtime package state:

```text
Package: com.home.hiddenapiprobe
Signature: platform cert, digest b4addb29
targetSdk=34
usesNonSdkApi=false
hiddenApiEnforcementPolicy=0
SELinux: u:r:platform_app:s0:c512,c768
```

Observed reflection results:

```text
OK   ActivityTaskManager.getService
OK   ServiceManager.getService
OK   IActivityTaskManager.Stub.asInterface
OK   ActivityTaskManager.getInstance
OK   IActivityTaskManager.getRecentTasks
OK   IActivityTaskManager.registerTaskStackListener
OK   IActivityTaskManager.unregisterTaskStackListener
OK   IActivityTaskManager.removeTask
OK   IActivityTaskManager.removeAllVisibleRecentTasks
FAIL IActivityTaskManager.startActivityFromRecents(int, String) -> NoSuchMethodException
OK   IActivityTaskManager.getTaskSnapshot
OK   TaskInfo.taskId
OK   TaskInfo.baseIntent
OK   TaskInfo.userId
OK   TaskInfo.taskDescription
OK   TaskDescription.getLabel
OK   TaskSnapshot.getHardwareBuffer
OK   TaskSnapshot.getSnapshot
OK   TaskSnapshot.getOrientation
OK   ActivityManager.forceStopPackage lookup
```

Interpretation: platform signing changes the app's hidden API policy to disabled (`0`), and previously hidden members become reflectable. The one failure is not enforcement; the exact reflected method signature is absent.

## ART / Framework Source Evidence

Android 14 AOSP `ApplicationInfo` defines:

```text
HIDDEN_API_ENFORCEMENT_DISABLED = 0
HIDDEN_API_ENFORCEMENT_JUST_WARN = 1
HIDDEN_API_ENFORCEMENT_ENABLED = 2
```

Relevant source behavior:

```java
private boolean isAllowedToUseHiddenApis() {
    if (isSignedWithPlatformKey()) {
        return true;
    } else if (isSystemApp() || isUpdatedSystemApp()) {
        return usesNonSdkApi() || isPackageWhitelistedForHiddenApis();
    } else {
        return false;
    }
}

public int getHiddenApiEnforcementPolicy() {
    if (isAllowedToUseHiddenApis()) {
        return HIDDEN_API_ENFORCEMENT_DISABLED;
    }
    ...
    return HIDDEN_API_ENFORCEMENT_ENABLED;
}
```

ART `hidden_api.cc` checks the runtime policy:

```text
if policy == kDisabled:
    allow access
if policy == kEnabled:
    apply hidden API deny logic
```

This matches the runtime experiment:

- normal app: `hiddenApiEnforcementPolicy=2`, restricted members hidden
- platform-signed app: `hiddenApiEnforcementPolicy=0`, restricted members visible

## Hidden API Classification

| API currently used | Runtime result for platform app | Normal app result | Classification |
|---|---:|---:|---|
| `ActivityTaskManager.getService()` | allowed | blocked | allowed because of platform identity |
| `ServiceManager.getService("activity_task")` | allowed | allowed | unrestricted on this build |
| `IActivityTaskManager.Stub.asInterface()` | allowed | blocked | allowed because of platform identity |
| `ActivityTaskManager.getInstance()` | allowed | blocked | allowed because of platform identity |
| `IActivityTaskManager.getRecentTasks()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `TaskInfo.taskId` | allowed | allowed | unrestricted on this build |
| `TaskInfo.baseIntent` | allowed | allowed | unrestricted on this build |
| `TaskInfo.userId` | allowed | blocked | allowed because of platform identity |
| `TaskInfo.taskDescription` | allowed | allowed | unrestricted on this build |
| `TaskDescription.getLabel()` | allowed | allowed | unrestricted on this build |
| `IActivityTaskManager.registerTaskStackListener()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `IActivityTaskManager.unregisterTaskStackListener()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `IActivityTaskManager.removeTask()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `IActivityTaskManager.removeAllVisibleRecentTasks()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `IActivityTaskManager.startActivityFromRecents(int, String)` | fails | not reachable after proxy block | wrong signature on Android 14 |
| `IActivityTaskManager.getTaskSnapshot()` | allowed | not reachable after proxy block | allowed because of platform identity |
| `TaskSnapshot.getHardwareBuffer()` | allowed | blocked | allowed because of platform identity |
| `TaskSnapshot.getSnapshot()` | allowed | allowed | unrestricted on this build |
| `TaskSnapshot.getOrientation()` | allowed | allowed | unrestricted on this build |
| `ActivityManager.forceStopPackage()` lookup | allowed | allowed | unrestricted lookup on this build; invocation still requires permission |
| `ITaskStackListener` proxy class | allowed | class lookup not separately blocked in probe | effectively dependent on platform-only registration path |

### Signature Bug Found

`HiddenApi.kt` currently reflects:

```kotlin
getDeclaredMethod("startActivityFromRecents", Int::class.java, String::class.java)
```

Android 14 AOSP exposes:

```java
startActivityFromRecents(int taskId, Bundle bOptions)
```

The platform-signed probe failed the `(int, String)` method lookup. This is not a hidden API denial; it is a wrong reflected signature.

## Current Recents / Overview Runtime State

`dumpsys activity recents`:

```text
mRecentsUid=10285
mRecentsComponent=ComponentInfo{com.android.launcher3/com.android.quickstep.RecentsActivity}
```

SystemUI `OverviewProxyService`:

```text
isConnected=true
mIsEnabled=true
mRecentsComponentName=ComponentInfo{com.android.launcher3/com.android.quickstep.RecentsActivity}
mQuickStepIntent=Intent { act=android.intent.action.QUICKSTEP_SERVICE pkg=com.android.launcher3 }
mBound=true
```

Launcher3/QuickStep package state:

```text
codePath=/system/system_ext/priv-app/Launcher3QuickStep
targetSdk=34
hiddenApiEnforcementPolicy=0
usesNonSdkApi=true
signatures=[b4addb29]
privateFlags include PRIVILEGED and SYSTEM_EXT
```

HOME candidates:

```text
com.android.launcher3/.uioverrides.QuickstepLauncher
com.home.launcher/.MainActivity
com.android.settings/.FallbackHome
```

Conclusion: `com.home.launcher` is a HOME candidate and can run as a platform-signed app, but it is not the official Recents/Overview provider. The ROM still routes Overview through Launcher3/QuickStep.

## Modern Overview / Recents Pipeline

AOSP `config.xml`:

```xml
<bool name="config_hasRecents">true</bool>
<string name="config_recentsComponentName" translatable="false">
    com.android.launcher3/com.android.quickstep.RecentsActivity
</string>
```

AOSP `RecentTasks`:

- loads `config_recentsComponentName`
- stores `mRecentsComponent`
- stores `mRecentsUid`
- treats that UID specially in `isCallerRecents()`
- allows the recents component to query tasks without the normal app limitations

AOSP `OverviewProxyService`:

- reads `config_recentsComponentName`
- creates `mQuickStepIntent = android.intent.action.QUICKSTEP_SERVICE`
- sets the package to the recents component package
- binds to that service as the current user
- exposes/receives `IOverviewProxy` and `ISystemUiProxy`

AOSP `TaskOrganizer`:

- calls `ITaskOrganizerController.registerTaskOrganizer()`
- receives current tasks as `TaskAppearedInfo`
- receives live callbacks:
  - `onTaskAppeared(RunningTaskInfo, SurfaceControl leash)`
  - `onTaskVanished(RunningTaskInfo)`
  - `onTaskInfoChanged(RunningTaskInfo)`
  - `onBackPressedOnTaskRoot(RunningTaskInfo)`

Live SystemUI dump confirms WM Shell is already tracking tasks through shell listeners, including `RecentTasksController`, `RecentsTransitionHandler`, and fullscreen task listeners.

## Required Migration Changes

### Application-Only Changes

These can be done without ROM changes but still rely on platform signing for hidden/platform APIs:

1. Fix `startActivityFromRecents` reflection to Android 14 signature:

   ```text
   startActivityFromRecents(int, Bundle)
   ```

2. Remove polling-first recents assumptions from `HiddenApi.kt`.
3. Create an internal task model based on `RunningTaskInfo` fields.
4. Prototype `TaskOrganizer` registration from the app process.
5. Use `TaskOrganizer` callbacks for live task list updates.
6. Keep existing UI as a consumer of a task repository rather than direct `getRecentTasks()` polling.

### ROM Configuration / Overlay Changes

Required for `com.home.launcher` to become official Overview:

1. Change `config_recentsComponentName` to a `com.home.launcher` Recents/Overview activity.
2. Ensure `config_hasRecents=true`.
3. Make `com.home.launcher` the default HOME role holder or default home activity.
4. Add a `QUICKSTEP_SERVICE` implementation in `com.home.launcher` or provide a compatible SystemUI integration.
5. Ensure SystemUI `OverviewProxyService` resolves `android.intent.action.QUICKSTEP_SERVICE` to `com.home.launcher`.

### Packaging / Permission Changes

Recommended for final ROM integration:

1. Install `com.home.launcher` as a ROM-bundled app, preferably under `system_ext/priv-app` if it replaces Launcher3QuickStep.
2. Sign with the platform certificate.
3. If privileged, add privapp permission allowlist XML for required privileged permissions.
4. Declare and grant:
   - `MANAGE_ACTIVITY_TASKS`
   - `START_TASKS_FROM_RECENTS`
   - `REMOVE_TASKS`
   - `READ_FRAME_BUFFER`
   - `STATUS_BAR`
   - `STATUS_BAR_SERVICE` if implementing QuickStep service contracts
   - `CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS` if taking over transition animation paths
   - `MONITOR_INPUT` if implementing gesture/navigation handling comparable to QuickStep

### SystemUI Integration Changes

Minimum clean path:

1. Keep SystemUI `OverviewProxyService`.
2. Point `config_recentsComponentName` at `com.home.launcher`.
3. Implement the QuickStep service/action expected by SystemUI:

   ```text
   android.intent.action.QUICKSTEP_SERVICE
   ```

4. Implement/borrow the `IOverviewProxy` and `ISystemUiProxy` contract used by Launcher3 QuickStep.
5. Verify SystemUI dump changes from:

   ```text
   mQuickStepIntent pkg=com.android.launcher3
   ```

   to:

   ```text
   mQuickStepIntent pkg=com.home.launcher
   ```

Higher-complexity path:

1. Fork SystemUI recents integration.
2. Replace OverviewProxyService assumptions directly.
3. Maintain a custom SystemUI-to-launcher contract.

This is more invasive and should be avoided unless compatibility with QuickStep contracts is not feasible.

### Framework Changes

Avoid framework changes initially. The existing AOSP extension points are sufficient:

- `config_recentsComponentName`
- `TaskOrganizer`
- WM Shell recents interfaces
- SystemUI `OverviewProxyService`

Framework changes should only be considered if:

- `TaskOrganizer` registration is blocked despite platform/privileged placement
- the desired UI requires replacing WM Shell transition behavior
- the project intentionally wants to remove QuickStep compatibility contracts

### SELinux Policy Changes

No new SELinux requirement was proven for hidden API reflection or the basic recents provider switch.

SELinux may become relevant if:

- `com.home.launcher` needs additional direct sysfs/proc reads
- the app implements services that SystemUI or WM Shell expects to bind under a more constrained domain
- Binder/service access denials appear after moving to `system_ext/priv-app`

Any SELinux changes should be driven by fresh AVC evidence after the ROM integration step.

## Ordered Migration Plan

### Phase 1: Stabilize Current Prototype

1. Fix `startActivityFromRecents(int, Bundle)`.
2. Add explicit runtime logging for every hidden API lookup and invocation.
3. Keep platform-signed standalone install for iteration.
4. Remove reliance on APIs that are only accidentally visible to normal apps.

### Phase 2: Introduce Task Model

1. Create a task repository abstraction.
2. Feed it from current `getRecentTasks()` temporarily.
3. Update UI to consume repository state.
4. Add snapshot/icon/label caching behind the repository.

### Phase 3: Prototype TaskOrganizer

1. Register a `TaskOrganizer`.
2. Populate the repository from `onTaskAppeared`, `onTaskVanished`, and `onTaskInfoChanged`.
3. Compare callback data with current `getRecentTasks()` output.
4. Use the legacy path only as fallback diagnostics.

### Phase 4: ROM-Bundle Launcher

1. Add `com.home.launcher` to the ROM build.
2. Sign with platform certificate.
3. Install in the appropriate system partition.
4. Add privapp permission allowlist if using privileged placement.
5. Make it the default HOME provider.

### Phase 5: Replace Overview Provider

1. Add a Recents/Overview activity to `com.home.launcher`.
2. Change overlay for `config_recentsComponentName`.
3. Implement `android.intent.action.QUICKSTEP_SERVICE`.
4. Verify SystemUI binds to `com.home.launcher`.
5. Verify `dumpsys activity recents` reports `mRecentsComponent=com.home.launcher/...`.

### Phase 6: Full QuickStep/WM Shell Compatibility

1. Implement required overview proxy callbacks.
2. Integrate task transition behavior.
3. Validate gesture navigation, 3-button recents, task switching, task dismissal, snapshots, split-screen/PiP edge cases, and multi-user/work-profile behavior.
4. Remove legacy reflection paths once feature parity is reached.

## Architecture Comparison

### Option 1: Continue Legacy Hidden API Reflection

Benefits:

- Fastest path from current prototype.
- Already works when platform-signed.
- Minimal ROM configuration required for basic task operations.

Costs:

- Depends on platform signing disabling hidden API enforcement.
- Fails or degrades on normal signing and stock Android 14.
- Method signatures are brittle, as shown by `startActivityFromRecents(int, String)`.
- Does not make `com.home.launcher` the official Overview provider.
- Leaves SystemUI and WM Shell bound to Launcher3/QuickStep.
- Higher Android 15/16 compatibility risk because hidden APIs and AIDL signatures can change.

### Option 2: Adopt TaskOrganizer / WM Shell / OverviewProxy Architecture

Benefits:

- Matches Android 14's modern recents architecture.
- Integrates with live task lifecycle callbacks instead of polling.
- Lets SystemUI route Overview to `com.home.launcher`.
- Uses ROM-supported extension points instead of accidental reflection behavior.
- Better Android 15/16 maintainability because it follows current Launcher3/QuickStep architecture.
- Cleanly separates app UI, task model, SystemUI binding, and WM Shell task lifecycle.

Costs:

- Requires ROM overlay/config changes.
- Requires a QuickStep-compatible service or SystemUI integration.
- Requires more implementation work than reflection.
- May require privileged packaging and additional permission allowlisting.
- May expose new SELinux issues only after integration.

## Final Recommendation

For a custom ROM where `com.home.launcher` is intended to become a permanent platform component, adopt the modern TaskOrganizer/WM Shell architecture and replace Launcher3/QuickStep through ROM configuration and SystemUI OverviewProxy integration.

The evidence supports this recommendation:

- Runtime probes prove hidden reflection works because platform signing disables enforcement, not because the APIs are stable or generally available.
- Normal signed runtime behavior proves several current APIs are blocked under standard enforcement.
- AOSP source shows platform signing is the direct reason `hiddenApiEnforcementPolicy` becomes disabled.
- Runtime dumps show SystemUI and ATMS still treat Launcher3/QuickStep as the official recents provider.
- AOSP `config_recentsComponentName`, `OverviewProxyService`, and `TaskOrganizer` provide the intended integration path.

Legacy reflection can remain temporarily as a diagnostic/prototype layer, but it should not be the long-term architecture for the ROM.
