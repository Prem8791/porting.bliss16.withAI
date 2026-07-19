# Cloud VM Execution Checklist

Run these steps in order on the AOSP/Bliss build VM. Do not skip validation.

## 1. Copy Project Into AOSP Tree

Edit/copy:

```sh
mkdir -p packages/apps/HomeLauncher
rsync -a --delete \
  --exclude .git \
  --exclude .gradle \
  --exclude .idea \
  --exclude build \
  --exclude '*/build' \
  --exclude '*.apk' \
  --exclude '*.apks' \
  --exclude '*.aab' \
  --exclude platform.pk8 \
  --exclude platform.x509.pem \
  /path/to/home/ packages/apps/HomeLauncher/
```

Expected outcome:

```text
packages/apps/HomeLauncher/app/src/main/AndroidManifest.xml exists
packages/apps/HomeLauncher/app/src/main/java/... exists
```

Rollback:

```sh
rm -rf packages/apps/HomeLauncher
```

Validate:

```sh
test -f packages/apps/HomeLauncher/app/src/main/AndroidManifest.xml
find packages/apps/HomeLauncher/app/src/main/java -name '*.kt' | wc -l
```

## 2. Add Soong Module

Use the root module that ships with this project. Do not leave a second file named
`Android.bp` under `rom-integration/aosp`, because Soong parses every nested
`Android.bp` file and duplicate module definitions will fail the build.

Edit/copy only if the root module is missing:

```sh
cp packages/apps/HomeLauncher/rom-integration/aosp/Android.bp.template \
   packages/apps/HomeLauncher/Android.bp
mkdir -p packages/apps/HomeLauncher/permissions
cp packages/apps/HomeLauncher/rom-integration/aosp/permissions/privapp-permissions-com.home.launcher.xml \
   packages/apps/HomeLauncher/permissions/
```

Expected outcome:

```text
HomeLauncher android_app module exists
privapp-permissions-com.home.launcher prebuilt_etc module exists
```

Rollback:

```sh
rm packages/apps/HomeLauncher/Android.bp
rm -rf packages/apps/HomeLauncher/permissions
```

Validate:

```sh
grep -n 'name: "HomeLauncher"' packages/apps/HomeLauncher/Android.bp
grep -n 'certificate: "platform"' packages/apps/HomeLauncher/Android.bp
grep -n 'privileged: true' packages/apps/HomeLauncher/Android.bp
```

## 3. Apply Product Configuration Patch

Apply the patch that adds the `inherit-product` line to `device/asus/I001D/bliss_I001D.mk`:

```sh
git apply < packages/apps/HomeLauncher/rom-integration/patches/0007-integrate-home-launcher-product-mk.patch
```

This includes `HomeLauncher` and `privapp-permissions-com.home.launcher` in `PRODUCT_PACKAGES` via the `home_launcher_product.mk` makefile fragment.

Expected outcome:

```text
Inherit line present in device/asus/I001D/bliss_I001D.mk
HomeLauncher selected in PRODUCT_PACKAGES
privapp allowlist XML selected in PRODUCT_PACKAGES
```

Rollback:

```sh
patch -R < packages/apps/HomeLauncher/rom-integration/patches/0007-integrate-home-launcher-product-mk.patch
```

Validate:

```sh
grep -n "home_launcher_product" device/asus/I001D/bliss_I001D.mk
grep -R "HomeLauncher" device vendor -n | head
```

## 3.5. Apply Active SELinux Policy Patch

Apply the patch that adds the `proc_stat` rule to active ROM sepolicy:

```sh
git apply < packages/apps/HomeLauncher/rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch
```

This creates `system/sepolicy/private/platform_app_home_launcher.te` with:

```
allow platform_app proc_stat:file r_file_perms;
```

Expected outcome:

```text
File exists at system/sepolicy/private/platform_app_home_launcher.te
Rule compiled into sepolicy after full build
```

Rollback:

```sh
rm system/sepolicy/private/platform_app_home_launcher.te
```

Validate after build:

```sh
adb shell sesearch --allow -s platform_app -t proc_stat -c file -p read
# Expect: allow platform_app proc_stat:file { read }
```

## 3.6. Preserve Userdebug Build Identity

Apply the patch that prevents the stock ASUS `user/release-keys` fingerprint
from overriding userdebug runtime identity:

```sh
git apply < packages/apps/HomeLauncher/rom-integration/patches/0009-keep-userdebug-build-identity.patch
```

This keeps the stock fingerprint override only for actual `user` builds. Without
this, the image files can contain `ro.build.type=userdebug` while runtime
`getprop ro.build.type` still reports `user`.

Validate:

```sh
grep -n "TARGET_BUILD_VARIANT" device/asus/I001D/bliss_I001D.mk
grep -n "BUILD_FINGERPRINT" device/asus/I001D/bliss_I001D.mk
```

## 4. Build Only The Launcher

Command:

```sh
source build/envsetup.sh
lunch bliss_I001D-ap2a-userdebug
m HomeLauncher privapp-permissions-com.home.launcher
```

Expected outcome:

```text
out/target/product/I001D/system/priv-app/HomeLauncher/HomeLauncher.apk
out/target/product/I001D/system/etc/permissions/privapp-permissions-com.home.launcher.xml
```

Rollback:

```sh
m clean-HomeLauncher
```

Validate:

```sh
ls -l out/target/product/I001D/system/priv-app/HomeLauncher/HomeLauncher.apk
ls -l out/target/product/I001D/system/etc/permissions/privapp-permissions-com.home.launcher.xml
```

If AndroidX modules are missing:

```sh
grep -R "name: \"androidx.recyclerview" -n prebuilts frameworks packages | head
```

Then adjust `static_libs` names in `packages/apps/HomeLauncher/Android.bp`.

## 5. Generate system.img

Command:

```sh
m systemimage
```

Expected outcome:

```text
out/target/product/I001D/system.img updated
```

Validate build variant before flashing. Stop here if any active image property
reports `user`:

```sh
grep -RhsE '^(ro\.build\.type|ro\.system\.build\.type|ro\.debuggable|ro\.build\.flavor)=' \
  out/target/product/I001D/{system,system_ext,product,vendor}/build.prop
```

Expected userdebug indicators:

```text
ro.build.type=userdebug
ro.system.build.type=userdebug
ro.debuggable=1
ro.build.flavor=bliss_I001D-userdebug
```

Rollback:

```text
Revert product makefile and HomeLauncher module changes, then rebuild systemimage.
```

Validate:

```sh
ls -lh out/target/product/I001D/system.img
```

## 6. Flash Only System Partition

Command:

```sh
adb reboot bootloader
fastboot flash system out/target/product/I001D/system.img
fastboot reboot
```

Expected outcome:

```text
Device boots with updated /system.
```

Rollback:

```sh
fastboot flash system /path/to/known-good/system.img
fastboot reboot
```

Validate:

```sh
adb wait-for-device
adb shell getprop sys.boot_completed
```

## 7. Verify System Installation

Commands:

```sh
adb shell pm path com.home.launcher
adb shell dumpsys package com.home.launcher | grep -E 'codePath|resourcePath|pkgFlags|privateFlags|versionCode|targetSdk|hiddenApi|usesNonSdkApi|signatures'
```

Expected:

```text
package:/system/priv-app/HomeLauncher/HomeLauncher.apk
pkgFlags include SYSTEM
privateFlags include PRIVILEGED
signature digest matches platform cert
hiddenApiEnforcementPolicy=0
```

Rollback:

```sh
Flash known-good system.img.
```

## 8. Set/Confirm Default HOME

If Launcher3 remains installed, set HomeLauncher manually for validation:

```sh
adb shell cmd package set-home-activity com.home.launcher/.MainActivity
```

Validate:

```sh
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb shell input keyevent KEYCODE_HOME
```

Expected:

```text
Resolved HOME is com.home.launcher/.MainActivity
Home key opens HomeLauncher
```

Rollback:

```sh
adb shell cmd package set-home-activity com.android.launcher3/.uioverrides.QuickstepLauncher
```

## 9. Confirm Privileged Permission Grants

Command:

```sh
adb shell dumpsys package com.home.launcher | grep -E 'REAL_GET_TASKS|MANAGE_ACTIVITY_TASKS|START_TASKS_FROM_RECENTS|REMOVE_TASKS|READ_FRAME_BUFFER|FORCE_STOP_PACKAGES|BATTERY_STATS|DEVICE_POWER|STATUS_BAR|INTERACT_ACROSS_USERS'
```

Expected:

```text
Each listed permission has granted=true.
```

Rollback:

```text
Fix privapp allowlist or manifest, rebuild HomeLauncher and systemimage, flash again.
```

## 10. Confirm Hidden API Behavior

Commands:

```sh
adb shell am force-stop com.home.launcher
adb logcat -c
adb shell am start -n com.home.launcher/.MainActivity
sleep 3
adb logcat -d -v time HiddenApiBridge:I ReflectionTasksBackend:I AndroidRuntime:E '*:S'
```

Expected:

```text
IActivityTaskManager resolved
getRecentTasks parsed N/M tasks
no fatal exception
```

Rollback:

```text
If hiddenApiEnforcementPolicy is not 0, verify platform certificate and package flags.
```

## 11. Validate Launcher Functionality

Commands:

```sh
adb shell input keyevent KEYCODE_HOME
adb shell am start -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.android.settings/.Settings
adb shell input keyevent KEYCODE_HOME
adb logcat -d -v time | grep -E 'HomeLauncher|ReflectionTasksBackend|AndroidRuntime|FATAL EXCEPTION'
```

Expected:

```text
Home screen appears.
App list works.
Recent task tiles update.
Task resume/remove operations do not crash.
```

## 12. Collect AVC Denials

Commands:

```sh
adb shell ps -A -o USER,PID,NAME,LABEL | grep com.home.launcher
adb logcat -d -b all | grep -i 'avc: denied' | grep -E 'com.home.launcher|platform_app'
```

Expected:

```text
No unexpected denials.
Known proc_stat/sysfs_thermal denials may recur.
```

Rollback:

```text
Do not disable SELinux. Document denials and decide whether to apply narrow draft policy.
```

## 13. Document Results

Create/update:

```text
packages/apps/HomeLauncher/rom-integration/results/<date>-first-rom-boot.md
```

Record:

- build target and commit
- files edited
- image flashed
- package path
- signature/permission state
- HOME resolver result
- hidden API logs
- AVC denials
- functional failures
