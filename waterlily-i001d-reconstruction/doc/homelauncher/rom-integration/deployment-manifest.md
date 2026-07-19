# Deployment Package Manifest

## 1. Concise Architecture Summary

File:

```text
rom-integration/docs/architecture-summary.md
```

## 2. Every Required Source Patch

File:

```text
rom-integration/patches/0001-source-refactor-hidden-api-compat.patch
```

This patch contains the app-side refactor:

- removes direct `HiddenApi` usage
- introduces `RecentTasksBackend`
- introduces `RecentTasksRepository`
- adds the platform `ActivityTaskManager` recents backend
- fixes `startActivityFromRecents(int, Bundle)`
- adds TaskOrganizer backend placeholder
- removes Gradle-only manifest `tools:` annotations

## 3. Every Required Build System Patch

Files:

```text
Android.bp
rom-integration/aosp/Android.bp.template
rom-integration/patches/0002-add-aosp-home-launcher-module.patch
```

## 4. Every Required Product Configuration Patch

Files:

```text
rom-integration/product/home_launcher_product.mk
rom-integration/patches/0004-add-product-packages.patch
```

## 5. Every Required Privapp Allowlist

Files:

```text
rom-integration/aosp/permissions/privapp-permissions-com.home.launcher.xml
rom-integration/patches/0003-add-privapp-permissions.patch
```

## 6. Draft SELinux Policy Files

Files:

```text
rom-integration/sepolicy/draft/README.md
rom-integration/sepolicy/draft/platform_app_home_launcher.te
rom-integration/patches/0006-deferred-selinux-proc-thermal.patch
```

Status: deferred. Do not enable before first ROM-bundled boot and fresh AVC collection.

## 7. Exact Cloud VM Execution Checklist

File:

```text
rom-integration/docs/cloud-vm-execution-checklist.md
```

## 8. Post-Flash Verification Checklist

File:

```text
rom-integration/docs/post-flash-verification-checklist.md
```

## 9. Expected First-Boot Failures

File:

```text
rom-integration/docs/expected-first-boot-failures.md
```

## 10. Intentionally Deferred Tasks

File:

```text
rom-integration/docs/deferred-tasks.md
```

## Additional Supporting Audits

Files:

```text
rom-integration/docs/current-project-audit.md
rom-integration/docs/permission-audit.md
rom-integration/docs/taskorganizer-migration-roadmap.md
```

## Optional Future Overlay

Files:

```text
rom-integration/aosp/overlays/HomeLauncherConfigOverlay/
rom-integration/patches/0005-optional-recents-component-overlay.patch
```

Status: deferred. The QuickStep service contract (`app/src/main/aidl/.../IOverviewProxy.aidl`,
`ISystemUiProxy.aidl` + `QuickStepService.kt`) is now implemented. The overlay can be
re-enabled in `home_launcher_product.mk` once the service binding is verified on-device.

## TaskOrganizer Migration

Files:

```text
frameworks/base/core/java/android/window/TaskOrganizer.java
app/src/aosp/java/com/home/launcher/system/platform/PlatformRecentTasksBackend.kt
```

Status: redirected. Android 14 `TaskOrganizer` exposes organizer registration,
root/child task control, and `ActivityManager.RunningTaskInfo` callbacks, but it
does not expose recents operations such as `getRecentTasks`, `removeTask`, or
`startActivityFromRecents`. Privileged ROM builds use the formal
`ActivityTaskManager` / `IActivityTaskManager` platform API via
`PlatformRecentTasksBackend`. The legacy reflection backend has been removed from
the app module.

## SELinux Policy

Files:

```text
rom-integration/patches/0008-add-active-home-launcher-proc-stat-sepolicy.patch
rom-integration/sepolicy/private/platform_app_home_launcher.te
```

Status: ready to apply. Add to `system/sepolicy/private/` in AOSP tree before rebuild.
