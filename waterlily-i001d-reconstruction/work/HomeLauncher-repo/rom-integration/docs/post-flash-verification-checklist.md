# Post-Flash Verification Checklist

## Package Placement

```sh
adb shell pm path com.home.launcher
adb shell dumpsys package com.home.launcher | grep -E 'codePath|resourcePath'
```

Expected:

```text
/system/priv-app/HomeLauncher/HomeLauncher.apk
```

## Platform Signing

```sh
adb shell dumpsys package com.home.launcher | grep -E 'signatures|hiddenApiEnforcementPolicy|usesNonSdkApi'
```

Expected:

```text
signature digest matches platform cert
hiddenApiEnforcementPolicy=0
usesNonSdkApi=false is acceptable because platform signing grants hidden API access
```

## Privileged/System Flags

```sh
adb shell dumpsys package com.home.launcher | grep -E 'pkgFlags|privateFlags'
```

Expected:

```text
pkgFlags include SYSTEM
privateFlags include PRIVILEGED
```

## SELinux Domain

```sh
adb shell ps -A -o USER,PID,NAME,LABEL | grep com.home.launcher
```

Expected:

```text
u:r:platform_app:s0:...
```

## HOME Resolution

```sh
adb shell cmd package query-activities --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Expected:

```text
com.home.launcher/.MainActivity is present and selected.
```

## Runtime Logs

```sh
adb logcat -c
adb shell am start -n com.home.launcher/.MainActivity
sleep 3
adb logcat -d -v time ReflectionTasksBackend:I HiddenApiBridge:I AndroidRuntime:E '*:S'
```

Expected:

```text
IActivityTaskManager resolved
Task listener registered
No AndroidRuntime fatal exception
```

## Permission Grants

```sh
adb shell dumpsys package com.home.launcher | grep 'granted=true'
```

Expected signature/privileged grants:

```text
REAL_GET_TASKS
MANAGE_ACTIVITY_TASKS
START_TASKS_FROM_RECENTS
REMOVE_TASKS
READ_FRAME_BUFFER
FORCE_STOP_PACKAGES
BATTERY_STATS
DEVICE_POWER
STATUS_BAR
INTERACT_ACROSS_USERS
```

## Functional Smoke Test

1. Press HOME.
2. Open Settings.
3. Press HOME again.
4. Confirm Settings appears in recent tiles.
5. Tap Settings tile.
6. Press HOME.
7. Remove Settings tile.
8. Open notification access settings from launcher settings menu.

## AVC Collection

```sh
adb logcat -d -b all | grep -i 'avc: denied' | grep -E 'platform_app|com.home.launcher'
```

Record every denial before changing policy.
