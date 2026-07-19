# VM Operations Runbook

Quick commands for the Bliss I001D VM, HomeLauncher sync, OTA build, download, flash, and post-flash checks.

## VM Details

```text
Project: customrom-501702
Zone: us-south1-b
Instance: instance-20260707-045005
User: premanandal1978
ROM root: ~/android/bliss-I001D
HomeLauncher path: ~/android/bliss-I001D/packages/apps/HomeLauncher
```

## Sync HomeLauncher From GitHub

Run on the VM:

```bash
cd ~/android/bliss-I001D/packages/apps/HomeLauncher
git fetch origin main
git reset --hard origin/main
git clean -fd
git rev-parse HEAD
```

Expected remote:

```text
https://github.com/Prem8791/homelauncher.git
```

## Apply HomeLauncher ROM Integration

Run from ROM root:

```bash
cd ~/android/bliss-I001D
```

Preferred, once patch files are fixed:

```bash
git apply packages/apps/HomeLauncher/rom-integration/patches/0007-integrate-home-launcher-product-mk.patch
git apply packages/apps/HomeLauncher/rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch
git apply packages/apps/HomeLauncher/rom-integration/patches/0009-keep-userdebug-build-identity.patch
```

Manual fallback:

```bash
grep -n "home_launcher_product.mk" device/asus/I001D/bliss_I001D.mk || echo '$(call inherit-product, packages/apps/HomeLauncher/rom-integration/product/home_launcher_product.mk)' >> device/asus/I001D/bliss_I001D.mk
```

```bash
mkdir -p system/sepolicy/private
printf '# HomeLauncher CPU stats\nallow platform_app proc_stat:file r_file_perms;\n' > system/sepolicy/private/platform_app_home_launcher.te
```

Verify:

```bash
grep -RIn "home_launcher_product.mk" device/asus/I001D/bliss_I001D.mk
grep -RIn "allow platform_app proc_stat:file" system/sepolicy/private/platform_app_home_launcher.te
grep -n "TARGET_BUILD_VARIANT" device/asus/I001D/bliss_I001D.mk
```

## Lunch Correct Target

Run from ROM root:

```bash
cd ~/android/bliss-I001D
source build/envsetup.sh
lunch bliss_I001D-ap2a-userdebug
```

Confirm the summary shows:

```text
TARGET_PRODUCT=bliss_I001D
TARGET_ARCH=arm64
TARGET_BUILD_VARIANT=userdebug
```

Guard against stale build environment:

```bash
echo "$TARGET_PRODUCT $TARGET_BUILD_VARIANT $TARGET_RELEASE"
```

Expected:

```text
bliss_I001D userdebug ap2a
```

## Build OTA

```bash
m otapackage -j$(nproc)
```

Success line:

```text
#### build completed successfully
```

Output:

```text
out/target/product/I001D/bliss_I001D-ota.zip
```

Check ZIP on VM:

```bash
unzip -t out/target/product/I001D/bliss_I001D-ota.zip | tail -5
sha256sum out/target/product/I001D/bliss_I001D-ota.zip
```

Before downloading or flashing, verify the generated build properties are still
`userdebug`. Do not flash if these report `user`:

```bash
grep -RhsE '^(ro\.build\.type|ro\.system\.build\.type|ro\.debuggable|ro\.build\.flavor)=' \
  out/target/product/I001D/{system,system_ext,product,vendor}/build.prop
```

Expected userdebug indicators include:

```text
ro.build.type=userdebug
ro.system.build.type=userdebug
ro.debuggable=1
ro.build.flavor=bliss_I001D-userdebug
```

Also inspect OTA metadata:

```bash
unzip -p out/target/product/I001D/bliss_I001D-ota.zip META-INF/com/android/metadata | grep -E 'post-build|post-build-incremental'
```

For a true userdebug build, `post-build` must contain `:userdebug/` and should
not contain `:user/release-keys`.

## Download OTA To Windows

Run in Windows PowerShell from the local update folder, for example:

```powershell
cd "D:\asus flashing\custom\update"
```

Download with a descriptive filename:

```powershell
gcloud compute scp --project customrom-501702 --zone us-south1-b premanandal1978@instance-20260707-045005:/home/premanandal1978/android/bliss-I001D/out/target/product/I001D/bliss_I001D-ota.zip .\bliss_I001D-ota-update.zip
```

Verify hash locally:

```powershell
certutil -hashfile .\bliss_I001D-ota-update.zip SHA256
```

## Flash Update

Use recovery sideload:

```text
Boot recovery
Apply update from ADB
```

Then from Windows PowerShell:

```powershell
adb sideload .\bliss_I001D-ota-update.zip
```

For same ROM/base updates:

```text
Do not wipe data unless boot or app state is broken.
```

## Post-Flash Verification

Check app path:

```powershell
adb shell pm path com.home.launcher
```

Expected:

```text
package:/system/priv-app/HomeLauncher/HomeLauncher.apk
```

Check privileged permissions:

```powershell
adb shell dumpsys package com.home.launcher | findstr /i "REAL_GET_TASKS MANAGE_ACTIVITY_TASKS START_TASKS_FROM_RECENTS REMOVE_TASKS READ_FRAME_BUFFER STATUS_BAR FORCE_STOP_PACKAGES INTERACT_ACROSS_USERS granted"
```

Check HomeLauncher overlay installed:

```powershell
adb shell pm list packages | findstr /i HomeLauncherConfigOverlay
adb shell cmd overlay list | findstr /i HomeLauncher
```

Check Recents owner:

```powershell
adb shell dumpsys activity recents | findstr /i "mRecentsComponent mRecentsUid"
```

Expected:

```text
mRecentsComponent=ComponentInfo{com.home.launcher/com.home.launcher.RecentsActivity}
```

Check CPU SELinux denial is gone:

```powershell
adb logcat -d | findstr /i "proc_stat com.home.launcher avc"
```

Expected:

```text
No output.
```

Check HomeLauncher diagnostics:

```powershell
adb logcat -c
adb shell am start -n com.home.launcher/.MainActivity
Start-Sleep -Seconds 10
adb logcat -d -s HomeLauncher PermCheck ReflectionTasksBackend SystemStats NotifDiag
```

## Save VM Local Build Fixes

Run before risky sync/reset operations:

```bash
cd ~/android/bliss-I001D
mkdir -p ~/vm-current-diffs
git -C device/asus/sm8150-common diff --binary > ~/vm-current-diffs/sm8150-common.patch
git -C prebuilts/sdk diff --binary > ~/vm-current-diffs/prebuilts-sdk.patch
git -C device/asus/I001D diff --binary > ~/vm-current-diffs/I001D.patch
cp -a packages/apps/HomeLauncher ~/vm-current-diffs/HomeLauncher-full-backup
ls -lh ~/vm-current-diffs
```

## Known Required ROM Build Fixes

These fixes were part of the successful vanilla build state:

```text
device/asus/sm8150-common:
- obsolete Dolby include removed/disabled
- duplicate SELinux genfs_contexts wakeup entries fixed
- broken VINTF XML fixed

prebuilts/sdk:
- AndroidX minSdkVersion workaround from 19 to 21

device/asus/I001D:
- local build workarounds if still present in VM patch
```

Saved patch location:

```text
~/vm-current-diffs/
~/bliss-I001D-local-patches/
```

## Git LFS Check

If WebView or large prebuilts are missing:

```bash
cd ~/android/bliss-I001D
git lfs version
repo forall -c 'git lfs pull'
```

Do not comment out WebView as a permanent fix if the real issue is missing Git LFS content.

## Useful Device Commands

Current slot:

```powershell
fastboot getvar current-slot
```

Switch slot:

```powershell
fastboot --set-active=a
fastboot --set-active=b
```

Temporary boot image:

```powershell
fastboot boot .\boot.img
```

Temporary boot recovery:

```powershell
fastboot boot .\recovery.img
```

## tmux

List sessions:

```bash
tmux ls
```

Attach:

```bash
tmux attach -t bliss
```

Detach:

```text
Ctrl+b, then d
```

