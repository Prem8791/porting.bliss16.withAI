# Architecture Summary

## Current State

`com.home.launcher` is a ROM-bundled privileged platform app using AndroidX UI libraries. The app declares HOME intent filters and privileged task-management permissions. Recent-task behavior is implemented through the AOSP-only platform backend.

The source-level refactor in this preparation pass changes the architecture from direct `HiddenApi` calls to this boundary:

```text
MainActivity / RecentAppsAdapter
      -> RecentTasksRepository
      -> RecentTasksBackend
          -> PlatformRecentTasksBackend        Android 14 platform backend
```

The platform backend is isolated under:

```text
app/src/aosp/java/com/home/launcher/system/platform/
```

Task-facing interfaces are under:

```text
app/src/main/java/com/home/launcher/task/
```

## Target First ROM Integration

Target first ROM state:

```text
/system/priv-app/HomeLauncher/HomeLauncher.apk
package=com.home.launcher
certificate=platform
privileged=true
hiddenApiEnforcementPolicy=0
SELinux domain=platform_app
HOME activity=com.home.launcher/.MainActivity
```

The first ROM integration is not a full Overview/QuickStep replacement. It makes the launcher a privileged platform app and uses formal ActivityTaskManager platform APIs for recents.

## Target Final Architecture

Final architecture should add Overview/QuickStep integration through:

```text
TaskOrganizer / WM Shell callbacks
  -> RecentTasksRepository
      -> launcher UI
```

SystemUI Overview should eventually bind to `com.home.launcher` through:

```text
config_recentsComponentName
OverviewProxyService
android.intent.action.QUICKSTEP_SERVICE
IOverviewProxy / ISystemUiProxy
```

## Mandatory Changes

- Add Soong `android_app` with `platform_apis: true`, `certificate: "platform"`, and `privileged: true`.
- Include AndroidX static libraries needed by the current app.
- Add privapp permission allowlist.
- Add product package entries.
- Set the launcher as default HOME after flashing if Launcher3 remains installed.

## Optional Future Improvements

- Remove or replace Launcher3QuickStep.
- Enable `HomeLauncherConfigOverlay`.
- Implement QuickStep service compatibility.
- Validate `PlatformRecentTasksBackend` on-device.
- Replace deprecated `LocalBroadcastManager`.
