# Intentionally Deferred Tasks

Strikethrough items are now implemented. Remaining tasks:

<s>1. Implement a non-reflection recents backend after identifying the supported platform API.</s>
<s>2. Implement `android.intent.action.QUICKSTEP_SERVICE`.</s>
3. Enable `HomeLauncherConfigOverlay` (needs on-device QuickStep binding verification).
4. Replace Launcher3/QuickStep as official Overview provider.
5. Remove Launcher3QuickStep from product packages.
6. Add/default-grant notification listener access.
7. Add/default-grant calendar permission.
<s>8. Apply SELinux policy for `/proc/stat` and thermal sysfs.</s>
9. Replace deprecated `LocalBroadcastManager`.
<s>10. Remove legacy reflection backend from ROM builds after platform backend validation.</s>
11. Validate Android 15/16 API compatibility.
12. Add automated instrumentation tests for task lifecycle behavior.
