# Permissions Required for Three-Column Recents Launcher

**Date:** 2026-07-08
**Target:** Android 14 (BlissROM / Universe) — platform-signed or privileged app

---

## Quick Reference

| Area | Permission | Protection Level | Required |
|---|---|---|---|
| **Recent Apps** | | | |
| List recent/running tasks | `REAL_GET_TASKS` | signature\|system | **Yes** |
| Register task listener | `MANAGE_ACTIVITY_TASKS` | signature\|system | **Yes** |
| Resume task from recents | `START_TASKS_FROM_RECENTS` | signature\|system | **Yes** |
| Remove task from recents | `REMOVE_TASKS` | signature\|system | **Yes** |
| Capture task snapshots | `READ_FRAME_BUFFER` | signature\|system | **Yes** |
| Force-stop/kill process | `FORCE_STOP_PACKAGES` | signature\|system | **Yes** |
| Kill background processes | `KILL_BACKGROUND_PROCESSES` | normal | No (limited) |
| Get running app processes | `GET_TASKS` | deprecated | No (use REAL_) |
| **Notifications** | | | |
| Listen to notifications | `BIND_NOTIFICATION_LISTENER_SERVICE` | signature\|system | **Yes** |
| Post notifications | `POST_NOTIFICATIONS` | normal | Yes |
| **System Stats** | | | |
| Read battery stats | `BATTERY_STATS` | signature\|system | **Yes** |
| Read device power state | `DEVICE_POWER` | signature\|system | No (alternative) |
| **Calendar / Tasks** | | | |
| Read calendar | `READ_CALENDAR` | dangerous | **Yes** |
| **App Discovery** | | | |
| List all installed apps | `QUERY_ALL_PACKAGES` | normal | **Yes** |
| Get package details | `GET_PACKAGE_SIZE` | normal | No |
| **System Integration** | | | |
| Override system UI | `STATUS_BAR` | signature\|system | **Yes** |
| Multi-user recents | `INTERACT_ACROSS_USERS` | signature\|system | Optional |
| Default launcher role | `BIND_APPWIDGET` | signature\|system | Optional |
| Set as default home | `INSTALL_SHORTCUT` | normal | No |

---

## Permission Details

### 1. Recent Apps Area (Core Recents Functionality)

These are the hidden/system permissions that make the launcher behave as a true system Recents replacement:

```xml
<!-- Get real recent/running task lists (not filtered to only your app) -->
<uses-permission android:name="android.permission.REAL_GET_TASKS" />
```
- **Protection:** signature|system
- **What it enables:** `ActivityTaskManager.getRecentTasks()`, `getTasks()` — returns all tasks across the system, not just those belonging to your app. Without this, your recents list is empty or limited.
- **Note:** Replaces the deprecated `GET_TASKS` permission which only returned tasks for the calling app since API 21.

```xml
<!-- Register task stack listeners and call deeper task management APIs -->
<uses-permission android:name="android.permission.MANAGE_ACTIVITY_TASKS" />
```
- **Protection:** signature|system
- **What it enables:** `registerTaskStackListener()` — receives real-time callbacks when tasks are added, removed, switched, or changed. This is how the recent area stays live (new app opened → tile appears instantly; app closed → tile disappears instantly).

```xml
<!-- Resume/start tasks from the recents list -->
<uses-permission android:name="android.permission.START_TASKS_FROM_RECENTS" />
```
- **Protection:** signature|system
- **What it enables:** `IActivityTaskManager.startActivityFromRecents()` — tapping a recent tile resumes that task in its existing state rather than launching a new instance.

```xml
<!-- Remove tasks from the Recents list -->
<uses-permission android:name="android.permission.REMOVE_TASKS" />
```
- **Protection:** signature|system
- **What it enables:** Removing individual tasks via `removeAllVisibleRecentTasks()` or task-specific removal. This is the **Close (X) button on each tile**.

```xml
<!-- Capture real-time screenshots/snapshots of running tasks -->
<uses-permission android:name="android.permission.READ_FRAME_BUFFER" />
```
- **Protection:** signature|system
- **What it enables:** `IActivityTaskManager.getTaskSnapshot()` — gets live thumbnails of each running task for the mini tiles. Without this, tiles show generic icons or blank placeholders.

```xml
<!-- Force-stop any running application (true kill) -->
<uses-permission android:name="android.permission.FORCE_STOP_PACKAGES" />
```
- **Protection:** signature|system
- **What it enables:** `ActivityManager.forceStopPackage()` — **this is the Kill All button.** Removing from recents (`REMOVE_TASKS`) only clears the task from the recents list. The process may still run. `FORCE_STOP_PACKAGES` actually kills the process and all its services. This is what makes "Kill All" a true system-wide kill.

### 2. Notifications Area

```xml
<!-- Listen to all system notifications (badge counts + content) -->
<uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />
```
- **Protection:** signature|system (granted at manifest level), but additionally requires user grant in **Settings → Notification Access** (or ROM-side default enable)
- **What it enables:** `NotificationListenerService` — receives `onNotificationPosted()`, `onNotificationRemoved()` callbacks. Provides the app icon, badge count, title, text, and actions for every notification on the system.

```xml
<!-- Post notifications from this app -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
- **Protection:** normal (runtime-granted on Android 13+)
- **What it enables:** This app can post its own notifications (e.g., if it has its own background service).

### 3. System Stats Bar

```xml
<!-- Read detailed battery statistics -->
<uses-permission android:name="android.permission.BATTERY_STATS" />
```
- **Protection:** signature|system
- **What it enables:** `BatteryManager` with full stats (temperature, voltage, current, health). Without it, you can still get battery percentage via `BatteryManager.EXTRA_LEVEL` (no special permission needed) but temperature and detailed stats may be unavailable.

### 4. Calendar / Tasks / Today Section

```xml
<!-- Read calendar events and reminders -->
<uses-permission android:name="android.permission.READ_CALENDAR" />
```
- **Protection:** dangerous (runtime prompt on Android 6+)
- **What it enables:** Query `CalendarContract` for appointments, tasks/reminders. This is a normal runtime permission, not signature-level.

### 5. App Discovery (Alphabet Picker)

```xml
<!-- Query all installed apps (not just yours) -->
<uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```
- **Protection:** normal (granted at install time on Android 11+)
- **What it enables:** `PackageManager.getInstalledApplications()` returns all apps on the device, used to populate the alphabet-indexed app list.

### 6. System Integration

```xml
<!-- Access and potentially override status bar -->
<uses-permission android:name="android.permission.STATUS_BAR" />
```
- **Protection:** signature|system
- **What it enables:** Ability to interact with the system status bar, collapse/expand it, and read its state. Useful when your launcher replaces the standard home screen behavior.

```xml
<!-- Access tasks across all user profiles -->
<uses-permission android:name="android.permission.INTERACT_ACROSS_USERS" />
```
- **Protection:** signature|system
- **What it enables:** If the device has work profiles or multiple users, this lets the recents list show tasks from all profiles, not just the current user.
- **Status:** Optional — omit if you only support single-user.

---

## Protection Levels Explained

| Level | Meaning | Who can hold it |
|---|---|---|
| **normal** | Auto-granted at install time | Any app |
| **dangerous** | Runtime prompt (user must approve) | Any app |
| **signature** | App must be signed with the platform cert | Platform-signed apps only |
| **signature\|system** | Platform-signed OR preinstalled in `/system` | Platform-signed or privileged apps |

All recents-related permissions are **signature|system** — meaning they are **not grantable to an ordinary sideloaded APK**. The app must be either:
- Signed with the **ROM's platform certificate**, OR
- Installed as a **privileged app** under `/system/priv-app/` with a `privapp-permissions` allowlist XML

---

## Privapp Permissions Allowlist XML

If deploying as a privileged app under `/system/priv-app/`, you need an allowlist XML at:
`/system/etc/permissions/privapp-permissions-com.your.package.name.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<permissions>
    <privapp-permissions package="com.your.package.name">
        <!-- Recent Apps -->
        <permission name="android.permission.REAL_GET_TASKS"/>
        <permission name="android.permission.MANAGE_ACTIVITY_TASKS"/>
        <permission name="android.permission.START_TASKS_FROM_RECENTS"/>
        <permission name="android.permission.REMOVE_TASKS"/>
        <permission name="android.permission.READ_FRAME_BUFFER"/>
        <permission name="android.permission.FORCE_STOP_PACKAGES"/>

        <!-- Notifications -->
        <permission name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"/>

        <!-- System Stats -->
        <permission name="android.permission.BATTERY_STATS"/>

        <!-- System Integration -->
        <permission name="android.permission.STATUS_BAR"/>
        <permission name="android.permission.INTERACT_ACROSS_USERS"/>
    </privapp-permissions>
</permissions>
```

---

## Complete Manifest Declaration

For a **platform-signed standalone APK** (no allowlist needed — platform cert is sufficient):

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.your.package.name">

    <!-- ============ RECENT APPS ============ -->
    <uses-permission android:name="android.permission.REAL_GET_TASKS" />
    <uses-permission android:name="android.permission.MANAGE_ACTIVITY_TASKS" />
    <uses-permission android:name="android.permission.START_TASKS_FROM_RECENTS" />
    <uses-permission android:name="android.permission.REMOVE_TASKS" />
    <uses-permission android:name="android.permission.READ_FRAME_BUFFER" />
    <uses-permission android:name="android.permission.FORCE_STOP_PACKAGES" />

    <!-- ============ NOTIFICATIONS ============ -->
    <uses-permission android:name="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- ============ SYSTEM STATS ============ -->
    <uses-permission android:name="android.permission.BATTERY_STATS" />

    <!-- ============ TODAY (CALENDAR) ============ -->
    <uses-permission android:name="android.permission.READ_CALENDAR" />

    <!-- ============ APP DISCOVERY ============ -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

    <!-- ============ SYSTEM INTEGRATION ============ -->
    <uses-permission android:name="android.permission.STATUS_BAR" />
    <uses-permission android:name="android.permission.INTERACT_ACROSS_USERS" />

    <!-- ============ NOTIFICATION LISTENER SERVICE ============ -->
    <service
        android:name=".NotificationListener"
        android:exported="true"
        android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
        <intent-filter>
            <action android:name="android.service.notification.NotificationListenerService" />
        </intent-filter>
    </service>
</manifest>
```

---

## How to Verify Permissions Are Granted

```powershell
adb shell dumpsys package com.your.package.name | findstr /i "REAL_GET_TASKS MANAGE_ACTIVITY_TASKS START_TASKS_FROM_RECENTS REMOVE_TASKS READ_FRAME_BUFFER FORCE_STOP_PACKAGES BATTERY_STATS STATUS_BAR granted=true"
```

Or on the device:

```shell
dumpsys package com.your.package.name | grep -E "(REAL_GET_TASKS|MANAGE_ACTIVITY_TASKS|START_TASKS_FROM_RECENTS|REMOVE_TASKS|READ_FRAME_BUFFER|FORCE_STOP_PACKAGES)" | grep granted=true
```

---

## Summary: Recent Area Specifically

| Feature | Permission | Hidden API |
|---|---|---|
| Show recent tasks (live list) | `REAL_GET_TASKS` + `MANAGE_ACTIVITY_TASKS` | `ActivityTaskManager.getRecentTasks()` |
| Live tile thumbnails | `READ_FRAME_BUFFER` | `IActivityTaskManager.getTaskSnapshot()` |
| Close single app (X button) | `REMOVE_TASKS` | `ActivityTaskManager.removeAllVisibleRecentTasks()` (or per-task removal) |
| Resume app from tile | `START_TASKS_FROM_RECENTS` | `IActivityTaskManager.startActivityFromRecents()` |
| Kill All (force stop) | `FORCE_STOP_PACKAGES` | `ActivityManager.forceStopPackage()` |
| Listen for task changes | `MANAGE_ACTIVITY_TASKS` | `TaskStackListener` via `registerTaskStackListener()` |

**Without `FORCE_STOP_PACKAGES`:** Kill All only removes tasks from the recents list (superficial). Apps continue running in the background.

**Without `READ_FRAME_BUFFER`:** Tiles show app icon + label only, no live thumbnail preview.

**Without `MANAGE_ACTIVITY_TASKS`:** The recent list is static — it won't update when apps open or close elsewhere in the system.
