# Android Three-Column Recents Launcher — Design Specification

**Date:** 2026-07-08
**Status:** Draft — awaiting confirmation

---

## Screen Layout Overview

```
┌────────────────────────────────────────────────────────────────┐
│  STATUS BAR (Android system)                                    │
│  ⏰ 9:41  ▂▃▄▅▆▇  📶  🔋  4G                               │
├──────────┬─────────────────────────────────────┬───────────────┤
│          │                                     │               │
│   A · B  │  ┌─ NOTIFICATIONS + TODAY ──────┐  │  N · O        │
│   C · D  │  │  📱  📧  💬  📷              │  │  P · Q        │
│   E · F  │  │  [3]  [1]  [2]  [0]          │  │  R · S        │
│   G · H  │  │  ┌──────────────────────┐    │  │  T · U        │
│   I · J  │  │  │ "Meeting at 2pm"    │    │  │  V · W        │
│   K · L  │  │  │ "Buy milk" (todo)   │    │  │  X · Y        │
│   M · #  │  │  └──────────────────────┘    │  │  Z · *        │
│          │  └────────────────────────────────┘  │               │
│   (tap)  │                                     │   (tap)       │
│   opens  │  ┌─ RECENT APPS (75%) ──────────┐  │   opens       │
│   app    │  │  ┌──────┐ ┌──────┐ ┌──────┐  │  │   app         │
│   list   │  │  │  [X] │ │  [X] │ │  [X] │  │  │   list        │
│   for    │  │  │      │ │      │ │      │  │  │               │
│   this   │  │  │  📰  │ │  🗺  │ │  🎵  │  │  │               │
│   letter │  │  │      │ │      │ │      │  │  │               │
│          │  │  │ News │ │ Maps │ │Music │  │  │               │
│          │  │  └──────┘ └──────┘ └──────┘  │  │               │
│          │  │  ┌──────┐ ┌──────┐ ┌──────┐  │  │               │
│          │  │  │  [X] │ │  [X] │ │  [X] │  │  │               │
│          │  │  │      │ │      │ │      │  │  │               │
│          │  │  │  🛒  │ │  ✉   │ │  ☁   │  │  │               │
│          │  │  │      │ │      │ │      │  │  │               │
│          │  │  │ Shop  │ │Email │ │Cloud │  │  │               │
│          │  │  └──────┘ └──────┘ └──────┘  │  │               │
│          │  │  ↕ scrollable (vertical)  ↕    │  │               │
│          │  │  (continuous, no wrap)          │  │               │
│          │  │         [ KILL ALL APPS ]      │  │               │
│          │  └────────────────────────────────┘  │               │
│          │                                     │               │
│          │  ┌─ SYSTEM STATS (5%) ──────────┐  │               │
│          │  │  🔋 85%  🧠 2.4/8GB  🌡 42°C│  │               │
│          │  └────────────────────────────────┘  │               │
├──────────┴─────────────────────────────────────┴───────────────┤
│  HOME BUTTON  ●  ●  ●                                          │
└────────────────────────────────────────────────────────────────┘
```

---

## Column Breakdown

### Left Column — Alphabet Picker (A–M + #)
- Vertical list: A, B, C, D, E, F, G, H, I, J, K, L, M, #
- Tap any letter → opens dynamic window overlay showing all apps starting with that letter (ordered alphabetically)
- Tapping `#` shows all apps whose names start with a digit/number
- Tapping a letter also dims/highlights the corresponding letter on the right column

### Right Column — Alphabet Picker (N–Z + *)
- Vertical list: N, O, P, Q, R, S, T, U, V, W, X, Y, Z, *
- Tapping `*` shows all user-favourited apps (starred/bookmarked)
- Same behavior as left column — opens dynamic app list overlay

### Central Column — Three Rows

#### Row 1: Notifications + Today (20% of central height)

**Notifications section:**
- Shows app icons with unread count badges (e.g., 📱[3], 📧[1], 💬[2])
- **Tapping an app icon:**
  1. All other notification icons visually collapse/hide
  2. The tapped app's notifications expand inline as a stacked list (like iOS power pill/notification expansion)
  3. Each expanded notification shows: app icon, title, message body, timestamp
  4. Swipe left on a notification to dismiss it
  5. Tap a notification to open the app to that specific item
  6. Tap outside the expanded area or tap the icon again to collapse back to icon-only view
- If no notifications: section shows "No new notifications" text

**Today section (same row, right side of row):**
- Next calendar appointment (time + title)
- Active tasks/todos (checkbox list, max 3 visible)
- Reminders for the day
- Tapping any item opens the respective app

#### Row 2: Recent Apps / Active Frames (75% of central height)

- **Layout:** 3-column grid of live minimized app tiles
- **Tile aspect ratio:** Mirrors the device's current orientation (landscape apps → landscape tiles, portrait → portrait) — NOT square like BlackBerry 10
- **Tile content:** Live/refreshing thumbnail of the app's current state
- **Close button:** Top-right corner of each tile (X button)
- **Scrollable:** **Vertically scrollable** — always, regardless of tile count. The 3-column grid scrolls vertically as a single continuous column group. This is the only scroll direction; tiles never scroll horizontally.
- **Kill All button:** Fixed at the bottom of this section — closes all recent apps
- **Tap tile:** Resumes the app to full screen
- **Long-press tile:** Opens app info / pin to recents

#### Row 3: System Stats Bar (5% of central height)

- Live-updating system statistics displayed in a compact horizontal bar:
  - 🔋 Battery percentage
  - 📶 Wi-Fi signal strength
  - 🧠 RAM usage (used/total)
  - ⚙️ CPU usage %
  - 🌡 Temperature
  - 💾 Storage free
- Updates in real time (1-second refresh or better)
- Tapping opens full device monitor / task manager

---

## Interaction Flow: Alphabet → App List

```
User taps 'G' on left column

┌────────────────────────────────────────────────────────────────┐
│                     ┌──────────────────┐                       │
│                     │  Apps starting   │                       │
│                     │  with "G"        │                       │
│                     │                  │                       │
│                     │  Gallery     📷  │                       │
│                     │  Gmail       📧  │                       │
│                     │  Google      🌐  │                       │
│                     │  Google Maps 🗺  │                       │
│                     │  Google Play 🛍  │                       │
│                     │  GroupMe     💬  │                       │
│                     │                  │                       │
│                     │  [ scrollable ]  │                       │
│                     │                  │                       │
│                     │  [ Close ]       │                       │
│                     └──────────────────┘                       │
│                                                                 │
│  A · B                    [Recent Apps area dimmed behind       │
│  C · D                     the overlay popup]                   │
│  E · F                                                          │
│  G (highlighted)                                                │
│  ...                                                             │
└────────────────────────────────────────────────────────────────┘

Tapping an app → launches the app (app is added to recents)
Tapping Close → dismisses overlay, returns to home screen
```

---

## Interaction Flow: Notification Expansion

```
Step 1: Icon-only view (default)
┌──────────────────────────────────────┐
│  📱(3)  📧(1)  💬(2)  📷(0)        │
└──────────────────────────────────────┘

Step 2: User taps 📱(3)
┌──────────────────────────────────────┐
│  📱(3)                               │  ← Other icons hidden
│  ┌────────────────────────────┐      │
│  │ John Smith                │      │  ← Expanded stack
│  │ "Meeting moved to 3pm"    │      │
│  │ 12:30 PM  ——— [ swipe to  │      │
│  │              dismiss  ]   │      │
│  ├────────────────────────────┤      │
│  │ Sarah                     │      │
│  │ "Can you review this?"    │      │
│  │ 11:45 AM  ——— [ swipe ]   │      │
│  ├────────────────────────────┤      │
│  │ Mom                       │      │
│  │ "Call me when you can"    │      │
│  │ 10:15 AM  ——— [ swipe ]   │      │
│  └────────────────────────────┘      │
└──────────────────────────────────────┘

Step 3: Tap 📱(3) again or tap background
┌──────────────────────────────────────┐
│  📱(3)  📧(1)  💬(2)  📷(0)        │  ← Collapsed back
└──────────────────────────────────────┘
```

---

## Data Sources Required (Android Side)

| Feature | Android API / Permission |
|---|---|
| Recent tasks list | `ActivityTaskManager.getRecentTasks()`, `REAL_GET_TASKS`, `MANAGE_ACTIVITY_TASKS` |
| Live task thumbnails | `IActivityTaskManager.getTaskSnapshot()`, `READ_FRAME_BUFFER` |
| Task lifecycle events | `TaskStackListener` / `registerTaskStackListener()` |
| Close task | `removeAllVisibleRecentTasks()`, `REMOVE_TASKS` |
| Resume task | `startActivityFromRecents()`, `START_TASKS_FROM_RECENTS` |
| Notifications + badges | `NotificationListenerService` + `BIND_NOTIFICATION_LISTENER_SERVICE` |
| Notification expansion | `Notification.Builder` style expansion (listening service side) |
| Calendar | `CalendarContract` content provider + `READ_CALENDAR` |
| Tasks/Todos | `CalendarContract.Reminders` or Google Tasks API |
| Installed apps list | `PackageManager.getInstalledApplications()` + `QUERY_ALL_PACKAGES` |
| System stats | `BatteryManager`, `ActivityManager.MemoryInfo`, `/proc/stat`, `/sys/class/thermal/` |

---

## Key Design Principles

1. **Alphabet-first app discovery** — Fast letter-indexed app launching, no scrolling through grids
2. **Live dashboard** — Active apps continue running and show real-time state in mini tiles
3. **Notification power pill** — Tap-to-expand notifications with iOS-style inline interaction
4. **System stats always visible** — Bottom bar gives glanceable device health
5. **Orientation-aware tiles** — Recent apps tiles match the app's actual aspect ratio, not forced to square
6. **One-tap kill all** — Clear all recent apps from the recents area
7. **Today glance** — Calendar, tasks, and reminders always visible in the top section

---

**Is this correct?** Please confirm the layout, proportions, and interactions match your vision, or tell me what to adjust.
