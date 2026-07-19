# Deferred SELinux Drafts

Do not apply these rules during the first ROM integration pass.

Rationale:

- The standalone app ran as `platform_app`.
- A ROM-bundled platform-signed app is also expected to run as `platform_app`, but this must be confirmed after flashing.
- The previous AVCs for `/proc/stat` and thermal sysfs are real, but new placement may expose additional denials.

First boot validation:

```sh
adb shell ps -A -o USER,PID,NAME,LABEL | grep com.home.launcher
adb logcat -d -b all | grep -i 'avc: denied' | grep 'com.home.launcher\|platform_app'
```

Only add the draft rules if the same `proc_stat:file` and `sysfs_thermal:file` denials are still present and the product owner accepts the broader `platform_app` impact.
