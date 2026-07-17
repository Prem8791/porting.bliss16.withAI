# Current Project Audit

## Source Tree

Main app:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/com/home/launcher/MainActivity.kt
app/src/main/java/com/home/launcher/adapter/
app/src/main/java/com/home/launcher/data/
app/src/main/java/com/home/launcher/service/
app/src/main/java/com/home/launcher/ui/
app/src/main/res/
```

Preparation refactor adds:

```text
app/src/main/java/com/home/launcher/task/
app/src/aosp/java/com/home/launcher/system/platform/
```

Probe module retained for diagnostics:

```text
hidden-api-probe/
```

## Gradle Configuration

Current Gradle app:

- `applicationId = "com.home.launcher"`
- `compileSdk = 34`
- `minSdk = 34`
- `targetSdk = 34`
- Kotlin JVM target 17
- ViewBinding enabled
- AndroidX dependencies:
  - `androidx.core:core-ktx`
  - `androidx.appcompat:appcompat`
  - `androidx.recyclerview:recyclerview`
  - `androidx.constraintlayout:constraintlayout`
  - `androidx.lifecycle:lifecycle-runtime-ktx`

Standalone Gradle signing remains debug-signing followed by external `apksigner` platform re-signing. This is not used by the ROM-bundled module.

## AOSP Build Requirements

The AOSP module must:

- point to `app/src/main/AndroidManifest.xml`
- include Kotlin source files from `app/src/main/java/**/*.kt`
- include resources from `app/src/main/res`
- set `platform_apis: true`
- set `certificate: "platform"`
- set `privileged: true`
- include equivalent AndroidX static libraries

## Manifest Audit

The manifest declares:

- HOME activity: `.MainActivity`
- notification listener service: `.service.NotificationListener`
- privileged task permissions
- notification/calendar/package visibility/system integration permissions

Gradle-only `tools:` annotations were removed so the manifest can be consumed directly by Soong.

## Hidden API Audit

Recents operations now use the AOSP-only `PlatformRecentTasksBackend`.

The known Android 14 signature mismatch was corrected:

```text
old: startActivityFromRecents(int, String)
new: startActivityFromRecents(int, Bundle)
```

Reflection remains temporary for:

- `ActivityTaskManager.getService`
- `ServiceManager.getService`
- `IActivityTaskManager.Stub.asInterface`
- `IActivityTaskManager.getRecentTasks`
- `IActivityTaskManager.registerTaskStackListener`
- `IActivityTaskManager.unregisterTaskStackListener`
- `IActivityTaskManager.removeTask`
- `IActivityTaskManager.removeAllVisibleRecentTasks`
- `IActivityTaskManager.startActivityFromRecents`
- `IActivityTaskManager.getTaskSnapshot`
- `TaskInfo` fields
- `TaskSnapshot` methods
- `ActivityManager.forceStopPackage`

## Signing Assumption

Runtime evidence showed platform signing sets:

```text
hiddenApiEnforcementPolicy=0
SELinux domain=platform_app
```

The ROM module must use:

```text
certificate: "platform"
```

## Build Artifacts

The repository intentionally ignores:

- APK/APKS/AAB files
- idsig files
- Gradle build output
- local SDK configuration
- platform private keys
- `bundletool.jar`
