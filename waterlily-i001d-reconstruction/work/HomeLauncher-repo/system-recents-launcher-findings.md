# System Recents Launcher Findings

Date: 2026-07-08

## Context

Target device:

- ASUS ROG Phone II / I001D
- BlissROM Android 14 / Universe
- Custom ROM build booted successfully on slot `_a`

Built OTA:

```text
out/target/product/I001D/bliss_I001D-ota.zip
SHA256: 068927dacee89854d76a681274302c473447835f7df6ca99e197bd56084aa0cb
```

The ROM was built from public Bliss/device sources with local build fixes saved separately as patches.

## Goal

Build a custom launcher/home screen that can behave like a system-level Recents interface, similar in spirit to BlackBerry 10:

- Show open/recent apps
- Receive live task updates
- Resume existing tasks
- Remove tasks from recents
- Show task thumbnails/snapshots
- Access notifications for richer home-screen state

## Key Finding

For true Android Recents/task control, this is not just a normal launcher problem.

It requires Android hidden/system APIs around:

- `ActivityTaskManager`
- `IActivityTaskManager`
- `TaskStackListener`
- task snapshots
- recent/running task APIs

Platform signing can solve many permission checks, but it does not automatically make hidden APIs easy to compile against from a normal Android Studio app.

## Important AOSP APIs

Relevant hidden APIs:

```java
ActivityTaskManager.getRecentTasks(...)
ActivityTaskManager.getTasks(...)
ActivityTaskManager.registerTaskStackListener(...)
ActivityTaskManager.unregisterTaskStackListener(...)
ActivityTaskManager.removeAllVisibleRecentTasks()
IActivityTaskManager.startActivityFromRecents(...)
IActivityTaskManager.getTaskSnapshot(...)
```

These APIs are marked `@hide` in AOSP, so a normal SDK app will not see them directly.

Practical access options:

- Use reflection
- Copy/use hidden AIDL stubs carefully
- Build inside the ROM with platform APIs
- Create a small privileged/system service inside the ROM and let the launcher talk to it

## Permissions Needed

Likely permissions for a real Recents replacement:

```xml
<uses-permission android:name="android.permission.REAL_GET_TASKS" />
<uses-permission android:name="android.permission.MANAGE_ACTIVITY_TASKS" />
<uses-permission android:name="android.permission.START_TASKS_FROM_RECENTS" />
<uses-permission android:name="android.permission.REMOVE_TASKS" />
<uses-permission android:name="android.permission.READ_FRAME_BUFFER" />
<uses-permission android:name="android.permission.STATUS_BAR" />
<uses-permission android:name="android.permission.INTERACT_ACROSS_USERS" />
```

Meaning:

- `REAL_GET_TASKS`: get real recent/running task lists instead of limited app-only results.
- `MANAGE_ACTIVITY_TASKS`: register task listeners and call deeper task-management APIs.
- `START_TASKS_FROM_RECENTS`: resume/start existing recent tasks.
- `REMOVE_TASKS`: remove tasks from the Recents list.
- `READ_FRAME_BUFFER`: access task snapshots/thumbnails.
- `STATUS_BAR`: useful when replacing overview/system navigation behavior.
- `INTERACT_ACROSS_USERS`: useful if dealing with multi-user/work-profile state.

## Platform Signing

If the APK is signed with the same platform certificate used by the custom ROM, Android should treat it as platform-signed.

That can grant signature permissions when declared in the manifest.

Expected to work for standalone platform-signed APK testing:

```text
REAL_GET_TASKS
MANAGE_ACTIVITY_TASKS
START_TASKS_FROM_RECENTS
REMOVE_TASKS
READ_FRAME_BUFFER
STATUS_BAR
```

But there are caveats:

- Hidden APIs are still hidden from normal SDK compilation.
- Some behavior may check whether the app is the configured Recents component.
- Some behavior may require default launcher/home role.
- Some paths may check UID, role, package, or SystemUI identity, not just permissions.
- Notification access is separate and usually needs user approval or ROM-side default grant.

## Privileged App Versus Standalone APK

Standalone APK, platform-signed:

- Good for first testing.
- Can receive signature permissions.
- Installed with `adb install`.
- May still need reflection/hidden stubs.

Privileged app:

- Installed under `/system/priv-app`, `/product/priv-app`, or equivalent.
- May need `privapp-permissions` allowlist XML.
- Better match for final ROM integration.

Final bundled ROM app:

```bp
android_app {
    name: "Home",
    srcs: ["src/**/*.java"],
    platform_apis: true,
    certificate: "platform",
    privileged: true,
}
```

Then add to product packages:

```make
PRODUCT_PACKAGES += Home
```

## Notification Access

For notifications, use `NotificationListenerService`:

```xml
<service
    android:name=".HomeNotificationListener"
    android:label="Home"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

This usually still requires:

```text
Settings > Notification access
```

or ROM-side configuration to enable by default.

## Recommended Development Path

1. Build a normal launcher prototype:
   - Default home launcher
   - App grid/list
   - Notification listener
   - Usage-stats based approximate recents

2. Platform-sign the APK:
   - Use the same platform cert as the custom ROM.
   - Install with `adb install`.
   - Declare the task/recents permissions.

3. Test actual permission grants:

```powershell
adb shell dumpsys package your.package.name | findstr /i "REAL_GET_TASKS MANAGE_ACTIVITY_TASKS START_TASKS_FROM_RECENTS REMOVE_TASKS READ_FRAME_BUFFER STATUS_BAR granted=true"
```

4. Test hidden API access:
   - Reflection first for quick experiment.
   - If reflection is painful, build against platform APIs inside the ROM tree.

5. If standalone APK hits system walls:
   - Move to `/system/priv-app` or `/product/priv-app`.
   - Add privapp permission allowlist XML.
   - Reboot and test again.

6. For final product:
   - Bundle launcher into ROM.
   - Make it the default home/recents component if needed.
   - Consider a small system service if direct app access is blocked by UID/role checks.

## Practical Conclusion

The desired launcher is possible on a self-built ROM, but the real Recents functionality is system-level Android work.

Best first experiment:

```text
Platform-signed standalone APK + declared recents/task permissions + hidden API calls.
```

Best final architecture:

```text
ROM-bundled platform app, probably privileged, possibly paired with a small system service.
```

