# Expected First-Boot Failures / Risks

## AndroidX Soong Module Names May Differ

Symptom:

```text
module "androidx.recyclerview_recyclerview" not found
```

Action:

```sh
grep -R 'name: "androidx.recyclerview' prebuilts frameworks packages -n
```

Update `static_libs` names in `packages/apps/HomeLauncher/Android.bp`.

## HOME May Not Be Default Automatically

If Launcher3 remains installed, Android may keep Launcher3 as HOME.

Validation:

```sh
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Temporary action:

```sh
adb shell cmd package set-home-activity com.home.launcher/.MainActivity
```

Final action:

- remove Launcher3QuickStep from the product, or
- add a product-specific preferred activity/default role solution.

## Notification Listener May Not Be Enabled

The service can be present but disabled until user/default grant.

Temporary action:

```text
Settings > Notification access > HomeLauncher
```

Final action:

- add a default notification listener setting during provisioning, if desired.

## Calendar Permission May Not Be Granted

`READ_CALENDAR` is dangerous/runtime.

Temporary action:

```text
Grant via Settings.
```

Final action:

- add default permission grant policy if calendar integration is required out of box.

## Direct CPU/Thermal Reads May Still Fail

Expected AVC types:

```text
platform_app -> proc_stat:file
platform_app -> sysfs_thermal:file
```

Action:

- collect fresh AVCs
- apply deferred SELinux draft only if still needed

## Official Overview Still Points At Launcher3

Until the optional overlay and QuickStep service are implemented:

```text
OverviewProxyService mRecentsComponentName=com.android.launcher3/com.android.quickstep.RecentsActivity
```

This is expected for the first pass.
