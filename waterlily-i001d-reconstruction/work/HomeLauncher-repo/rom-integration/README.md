# HomeLauncher ROM Integration Package

Purpose: prepare `com.home.launcher` for conversion from a standalone platform-signed APK into a ROM-bundled privileged platform app.

This package is intentionally not applied to an AOSP tree yet. It contains reviewed source patches, build snippets, product configuration snippets, privapp allowlist XML, deferred SELinux drafts, and execution checklists for the Cloud VM phase.

## Mandatory For First ROM-Bundled Launcher Boot

1. Copy this project into `packages/apps/HomeLauncher`.
2. Use the root `packages/apps/HomeLauncher/Android.bp`.
3. Add `permissions/privapp-permissions-com.home.launcher.xml` and the matching `prebuilt_etc` module.
4. Add `HomeLauncher` and `privapp-permissions-com.home.launcher` to the product package list.
5. Build and flash `system.img`.
6. Set `com.home.launcher/.MainActivity` as the HOME activity if Launcher3 remains installed.

## Optional Future Work

1. Enable the overlay that points `config_recentsComponentName` to `com.home.launcher`.
2. Implement `android.intent.action.QUICKSTEP_SERVICE`.
3. Replace the reflection recent-tasks backend with TaskOrganizer/WM Shell.
4. Apply SELinux policy only after first boot confirms the same AVCs are still required.

## Files

- `../Android.bp`: active Soong module for `HomeLauncher`.
- `aosp/Android.bp.template`: reference copy for external AOSP integrations; keep the `.template` suffix so Soong does not parse a duplicate module.
- `aosp/permissions/privapp-permissions-com.home.launcher.xml`: privileged permission allowlist.
- `aosp/overlays/HomeLauncherConfigOverlay/`: optional static overlay for future Overview replacement.
- `product/home_launcher_product.mk`: product makefile additions.
- `sepolicy/draft/`: deferred SELinux policy snippets.
- `docs/`: architecture, permission audit, migration plan, and execution checklists.
- `patches/`: generated source/build patch files.
