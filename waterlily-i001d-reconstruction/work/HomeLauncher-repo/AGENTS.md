# Build, Sign & Install

## Build
```
.\gradlew assembleDebug
```

## AOSP / Soong Kotlin Compatibility Checks

When changing Kotlin source that may later be built inside Android 14 AOSP/Soong:

- Do not rely on Gradle-only Kotlin inference for Android platform generic APIs.
- Use explicit generic types for every `findViewById` call, for example `findViewById<TextView>(R.id.title)` or `itemView.findViewById<ImageView>(R.id.icon)`.
- After edits, scan for untyped view lookups:
```
rg --pcre2 "findViewById\((?!<)" app hidden-api-probe
```
- Treat any match as a source compatibility issue unless it is Java code or is otherwise proven safe for the AOSP Kotlin compiler.
- Prefer type-safe calls over unsafe casts. Do not change application behavior while fixing Soong/Kotlin compatibility issues.
- Run a clean local build after compatibility fixes:
```
.\gradlew clean assembleDebug
```

## Sign with platform keys
```
apksigner sign --key platform.pk8 --cert platform.x509.pem --out app\build\outputs\apk\debug\app-platform-signed.apk app\build\outputs\apk\debug\app-debug.apk
```
Keys are in the project root: `platform.pk8` and `platform.x509.pem`.

## Fresh install (uninstall first)
```
adb uninstall com.home.launcher
adb install app\build\outputs\apk\debug\app-platform-signed.apk
```

## Incremental install (replace)
```
adb install -r app\build\outputs\apk\debug\app-platform-signed.apk
```

## Restart (after install)
```
adb shell am force-stop com.home.launcher && adb shell am start -n com.home.launcher/.MainActivity
```

## Combined quick cycle (incremental)
```
.\gradlew assembleDebug && apksigner sign --key platform.pk8 --cert platform.x509.pem --out app\build\outputs\apk\debug\app-platform-signed.apk app\build\outputs\apk\debug\app-debug.apk && adb install -r app\build\outputs\apk\debug\app-platform-signed.apk && adb shell am force-stop com.home.launcher && adb shell am start -n com.home.launcher/.MainActivity
```

## ADB path
`C:\Users\Steno\AppData\Local\Android\Sdk\platform-tools\adb.exe`

## Apksigner path
`C:\Users\Steno\AppData\Local\Android\Sdk\build-tools\36.1.0\apksigner.bat`

# Change Propagation Workflow

Before starting any new code or resource change, ask the user which numbered propagation path to use. Treat "third and fourth" as path 3 plus GitHub:

1. Local only: edit and verify in `D:\Personal\AntigravityProjects\home`.
2. Local + VM: edit locally, verify, then sync only the changed source/resource files to the cloud VM AOSP tree. Stop after verifying the VM files.
3. Local + VM + device: edit and verify locally, sync and verify the VM files, then stop and hand the Soong build command to the user. After the user confirms the VM build completed, download the platform APK, install it with `adb install -r`, restart HomeLauncher, and check immediate crash logs.
4. GitHub too: after the user explicitly selects this option, commit only the intended files and push to `origin/main`. This can be combined with paths 1, 2, or 3.

Do not assume every change should be pushed to GitHub or installed on the device. Ask first.

## VM Build Handoff Policy

- Never run `m HomeLauncher` or any other VM/Soong build command on the user's behalf.
- The agent owns local implementation, local checks, source sync, and verification that local and VM source files match.
- Once the VM is prepared, stop and provide the exact build commands to the user.
- Continue only after the user confirms the build is done.
- After confirmation for path 3, the agent owns APK download, `adb install -r`, launcher restart, process verification, and crash-log inspection.
- Do not ask the user to manually download or install the APK unless an actual access or device blocker prevents the agent from doing it.

## Current VM / No-Flash Test Path

Cloud VM:
```
project: customrom-501702
zone: us-south1-b
instance: instance-20260707-045005
user: premanandal1978
ROM root: /home/premanandal1978/android/bliss-I001D
HomeLauncher path: /home/premanandal1978/android/bliss-I001D/packages/apps/HomeLauncher
```

Local `gcloud` path:
```
C:\Program Files (x86)\Google\Cloud SDK\google-cloud-sdk\bin\gcloud.cmd
```

Recommended no-flash loop for platform/AOSP-sensitive launcher changes:

1. Edit files locally.
2. Run the Kotlin compatibility scan:
```
rg --pcre2 "findViewById\((?!<)" app hidden-api-probe
```
If `rg` is unavailable, use an equivalent PowerShell `Select-String` scan.
3. Run a local build:
```
.\gradlew assembleDebug
```
For compatibility-sensitive Kotlin changes, run:
```
.\gradlew clean assembleDebug
```
4. Sync only the changed source/resource files to the matching paths under:
```
/home/premanandal1978/android/bliss-I001D/packages/apps/HomeLauncher
```
5. Stop and hand the following AOSP platform build commands to the user. Do not run them:
```
cd ~/android/bliss-I001D
source build/envsetup.sh
lunch bliss_I001D-ap2a-userdebug
m HomeLauncher
```
6. Wait for the user to confirm that the VM build completed.
7. For path 3, download the rebuilt platform APK:
```
gcloud compute scp --project customrom-501702 --zone us-south1-b premanandal1978@instance-20260707-045005:/home/premanandal1978/android/bliss-I001D/out/target/product/I001D/system/priv-app/HomeLauncher/HomeLauncher.apk .\HomeLauncher-system.apk
```
8. Install over the existing system app without flashing:
```
adb install -r .\HomeLauncher-system.apk
adb shell am force-stop com.home.launcher
adb shell am start -n com.home.launcher/.MainActivity
```
9. Verify the launcher process and check for immediate crashes:
```
adb shell pidof com.home.launcher
adb logcat -d -t 250 | findstr /i "AndroidRuntime FATAL HomeLauncher"
```

## GitHub Commit / Push Policy

Only commit and push when the user explicitly asks.

Before committing:

- Review `git status --short`.
- Exclude unrelated/generated files unless the user explicitly wants them.
- Prefer committing only intentional launcher source/resource changes.
- Verify VM status separately if the VM tree was synced.

GitHub remote:
```
origin https://github.com/Prem8791/homelauncher.git
```
