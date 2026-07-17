# Windows Mobile 5.0 Home Screen & Today Screen: A Technical Retrospective

**Date:** 2026-07-08
**Status:** Draft — pending refinement

---

## Table of Contents

1. [Overview](#overview)
2. [OS Architecture & Editions](#os-architecture--editions)
3. [Pocket PC: The Today Screen](#pocket-pc-the-today-screen)
   - [Layout](#layout)
   - [Plugin Architecture](#plugin-architecture)
   - [Default Plugins](#default-plugins)
   - [Themes](#themes)
4. [Smartphone: The Homescreen](#smartphone-the-homescreen)
   - [XML-Based Layout](#xml-based-layout)
   - [Plugin Panels](#plugin-panels)
   - [Navigation Model](#navigation-model)
5. [Start Menu & Launcher](#start-menu--launcher)
6. [Notification System](#notification-system)
7. [Comparison: Pocket PC vs Smartphone](#comparison-pocket-pc-vs-smartphone)
8. [Customization Ecosystem](#customization-ecosystem)
9. [ASCII Art Diagrams](#ascii-art-diagrams)
10. [Sources](#sources)

---

## Overview

Windows Mobile 5.0 (codenamed "Magneto") was released by Microsoft on May 9, 2005 as the successor to Windows Mobile 2003. It was built on the Windows CE 5.0 kernel and introduced persistent storage (flash memory), removing the need to reserve battery power for volatile RAM retention. The OS shipped in two distinct editions — **Pocket PC** (touchscreen/stylus) and **Smartphone** (non-touch, hardware-key navigation) — each with a fundamentally different home screen paradigm.

Windows Mobile 5.0 reached mainstream support end on October 12, 2010, and extended support ended on October 13, 2015.

---

## OS Architecture & Editions

| Aspect | Details |
|---|---|
| Kernel | Windows CE 5.0 |
| Codename | "Magneto" |
| Release | May 9, 2005 |
| RAM minimum | 64 MB |
| CPU | ARM-compatible (Intel XScale, Samsung, TI OMAP) |
| .NET | .NET Compact Framework 1.0 SP3 |
| Synchronization | ActiveSync 4.2 (15% faster sync) |
| Persistent storage | Yes — flash memory, no data loss on battery drain |

### Two Editions

| Feature | Pocket PC | Smartphone |
|---|---|---|
| Input | Touchscreen + stylus | Hardware keys only (d-pad, numeric keypad, soft keys) |
| Screen | 240×320 (QVGA) or higher | 176×220 (QCIF) or 240×320 (QVGA) |
| Home screen | **Today Screen** | **Homescreen** |
| Layout engine | Plugin-based (DLL, registered in registry) | XML-defined layout (.home.xml files) |
| Start Menu | Tap top-left corner | Hardware "Start" key |

---

## Pocket PC: The Today Screen

The Today Screen was the default home screen on Pocket PC devices. It was a vertically scrolling information dashboard composed of **plugin items** — DLL-based components that each rendered a small, updateable information pane.

### Layout

```
┌─────────────────────────────────────┐
│  Start     12:45          🖪 🔍  │  ← Title bar (always visible)
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📅  Wednesday, Jul 8      │   │  ← Owner Info plugin
│  │  "Work"                     │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📅  Appointments           │   │  ← Calendar plugin
│  │  • Meeting w/ Sarah 2pm    │   │     (tap to open calendar)
│  │  • Dentist 4:30pm           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✉  Messages               │   │  ← Messaging plugin
│  │  • (3) Inbox: John         │   │     (tap to open email/SMS)
│  │  • (1) SMS: Mom            │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  ✓  Tasks                   │   │  ← Tasks plugin
│  │  ☐ Buy groceries           │   │     (tap to open tasks)
│  │  ☐ Call plumber            │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  📌  Owner: Alice Smith    │   │  ← Owner Info plugin
│  │  "Company: Acme Corp"      │   │     (contact info)
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│  📞  💬  🌐  📷              │  ← Soft key bar / launcher strip
│  [Phone] [Contacts] [IE]     │     (configurable)
└─────────────────────────────────────┘
```

### Plugin Architecture

Each Today Screen plugin was a **native Windows DLL** implementing a specific COM-like interface with exported functions at fixed ordinals:

| Ordinal | Function | Purpose |
|---|---|---|
| 240 | `InitializeCustomItem` | **Required** — creates child window, sets height, renders content |
| 241 | `CustomItemOptionsDlgProc` | **Optional** — provides an Options dialog for user configuration |

#### Lifecycle

1. **Registration:** The DLL is registered under `HKLM\Software\Microsoft\Today\Items\<PluginName>` with values for DLL path, type (always 4 for custom), enabled state, selectability, and options support.
2. **Initialization:** The shell calls `InitializeCustomItem`, passing a `TODAYLISTITEM` structure. The plugin creates a child window and sets its height (`cyp`).
3. **Refresh:** Every **2 seconds**, the Today screen sends `WM_TODAYCUSTOM_QUERYREFRESHCACHE` to determine if data has changed. The plugin should respond `TRUE` only if repainting is needed. `WM_TODAYCUSTOM_CLEARCACHE` forces all plugins to release cached data.
4. **Interaction:** `WM_LBUTTONUP` is sent on tap — the plugin typically launches the associated full application.
5. **Unload:** `WM_TODAYCUSTOM_CLEARCACHE` is sent before the plugin is unloaded, allowing cleanup.

#### Registry Structure

```
HKLM\Software\Microsoft\Today\Items\
├── Calendar
│   ├── DLL = "\windows\today.dll"
│   ├── Type = 4 (DWORD)
│   ├── Enabled = 1 (DWORD)
│   ├── Options = 1 (DWORD)
│   ├── Selectability = 1 (DWORD)    // 0=not selectable, 1=shell handles
│   │                                 // 2=plugin handles up/down
│   └── Flags = 0 (DWORD)
├── Messages
│   ├── DLL = "\windows\tmail.dll"
│   └── ...
├── Tasks
├── Owner Info
└── <Third-party plugins>
```

### Default Plugins

| Plugin | Source DLL | Description |
|---|---|---|
| Owner Info | `t today.dll` | User name, company, job title, phone number |
| Calendar | `t today.dll` | Upcoming appointments (date, time, subject) |
| Messages | `tmail.dll` | Unread count by account (Exchange, POP3, SMS) |
| Tasks | `t today.dll` | Active task list with checkboxes |
| Pocket MSN | `msnapps.dll` | MSN Messenger status, Hotmail alerts (new in WM5.0) |
| Weather | (OEM/carrier) | Current conditions, forecast (device-dependent) |

### Themes

Themes were packaged as `.tsk` files containing:
- Background image (watermark)
- Color scheme (text, highlight, rule colors)
- Font selections

Users could select themes via **Settings → Today** on Pocket PC. The API exposed `TODAYDRAWWATERMARKINFO` for plugins to render transparent backgrounds properly.

Third-party themes (`.tsk` or `.hme` files) could be installed to `\My Documents` and selected from the settings panel. Theme files were forbidden from containing `CESetup.dll` — only graphics, colors, and plugin references were allowed.

---

## Smartphone: The Homescreen

The Smartphone edition had no touchscreen, so its homescreen used a **hardware-key-navigable** layout defined in **XML files** (`.home.xml` stored in `\Application Data\Home\`). The layout was a vertically stacked sequence of **plugin panels**, each acting as a "page" or "section" that the user navigated through with the d-pad.

### XML Layout Structure

```xml
<home>
  <title lang="0x0409">Default Homescreen</title>

  <plugin clsid="{CLSID-of-plugin}">
    <!-- Plugin-specific configuration -->
  </plugin>

  <plugin clsid="{CLSID-of-next-plugin}">
    ...
  </plugin>
</home>
```

### Built-in Plugin CLSIDs

| Plugin | CLSID | Description |
|---|---|---|
| CClock | `{....}` | Large digital/analog clock + date |
| CAppointments | `{....}` | Next appointment summary |
| CHome | `{....}` | Notifications aggregator (missed calls, voicemail, email, SMS) |
| CMyPhotos | `{....}` | Background photo + photo browser shortcut |
| CMessage | `{....}` | Message center (off by default on most layouts) |
| CMusic | `{....}` | Now-playing music control + album art |
| Settings | `{....}` | Quick toggles (ring profile, Bluetooth, brightness) |
| IconBar | `{....}` | Signal bars, battery meter, connectivity icons |

### Navigation Model (Smartphone)

No touchscreen — all navigation via hardware keys:

```
┌─────────────────────────────────────┐
│                                     │
│  ┌─────────────────────────────┐   │
│  │         CLOCK               │   │  ← Current panel (focused)
│  │        10:45 AM             │   │     Up/Down cycles through
│  │      Wednesday, Jul 8       │   │     panels in the plugin list
│  │                             │   │
│  │    ╔═══════════════════╗    │   │
│  │    ║  [Missed Call]    ║    │   │  ← Sub-items (highlighted)
│  │    ║  [SMS: Mom]       ║    │   │     Left/Right navigates
│  │    ║  [Email: Work]    ║    │   │     sub-items within panel
│  │    ╚═══════════════════╝    │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│ [   Left Soft Key  ] [Right Soft Key]│  ← Context-dependent
│     "Contacts"         "Calendar"    │
└─────────────────────────────────────┘
```

### Smartphone Homescreen Layout (Default)

```
┌─────────────────────────────────────┐
│  📶 ▂▃▄▅  🔋  ▐   ⏰ 10:45   │  ← Icon bar (carrier, signal, battery, time)
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │   ╔═══════════════════════╗ │   │
│  │   ║                       ║ │   │  ← CClock (clock panel)
│  │   ║      10:45 AM         ║ │   │
│  │   ║                       ║ │   │
│  │   ╚═══════════════════════╝ │   │
│  │   Wednesday, Jul 8          │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   📅 Appointments          │   │  ← CAppointments
│  │   • Meeting w/ Sarah 2pm  │   │
│  │   • Dentist 4:30pm         │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   📬 Notifications         │   │  ← CHome (notifications)
│  │   • (1) Missed call        │   │
│  │   • (3) New email          │   │
│  │   • (1) SMS from Mom       │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   🖼 My Photos             │   │  ← CMyPhotos
│  │   [current wallpaper]      │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│ [     Contacts    ] [   Calendar   ]│  ← Soft keys (customizable)
└─────────────────────────────────────┘
```

---

## Start Menu & Launcher

Both editions featured a **Start Menu** as the primary application launcher:

### Pocket PC

- **Access:** Tap the "Start" text in the top-left title bar
- **Layout:** A dropdown panel appears with:
  - Recently used applications (MRU list) at top
  - Program groups below
  - Settings, Help, and Run... at bottom
  - Tap outside to dismiss

```
┌────────── Start ──────────┐
│  📞 Phone                  │  ← MRU (most recently used)
│  🌐 Internet Explorer      │
│  📧 Messaging              │
│  ──────────────────────    │
│  🎮 Games                  │  ← Program groups
│  📁 Office Mobile          │
│  🛠 Utilities              │
│  ──────────────────────    │
│  ⚙ Settings                │
│  ❓ Help                    │
│  ▶ Run...                  │
└────────────────────────────┘
```

### Smartphone

- **Access:** Press hardware "Start" key
- **Layout:** Full-screen grid or list (OS-version dependent):
  - Alphabetical program list
 - Most-recently-used programs at top
  - Left/Right to navigate tabs (Programs, Settings, etc.)
  - OK/Select to launch

---

## Notification System

### Pocket PC

- **Title bar icons:** New email, SMS, missed call indicators appear in the top bar
- **Popup notifications:** Toast-style popups for incoming calls, SMS, reminders
- **LED:** Single-color flashing LED (if hardware supported)
- **Sound:** Configurable per-event ringtones and alerts

### Smartphone

- **Notification panel (CHome):** Built into homescreen — shows missed calls, voicemail, SMS, email counts in a scrollable list
- **Icon bar:** Signal strength, battery, connectivity, notification indicators at screen top
- **Popup notifications:** "Caller ID" style popup on incoming call/SMS (can be dismissed with hardware key)
- **LED:** Multi-color LED for different event types (if hardware supported)

---

## Comparison: Pocket PC vs Smartphone

| Aspect | Pocket PC (Today Screen) | Smartphone (Homescreen) |
|---|---|---|
| Input method | Stylus / touch | Hardware keys (d-pad, numeric, soft keys) |
| Home screen engine | DLL-based plugins (C++) | XML-based panels with CLSID |
| Plugin format | Native DLL exporting ordinal 240+241 | COM CLSID registered in registry |
| Refresh cycle | Every 2 seconds (`WM_TODAYCUSTOM_QUERYREFRESHCACHE`) | Event-driven (notification changes) |
| Focus/selection | Tap to activate item | Up/Down between panels, Left/Right within panel |
| Themes | `.tsk` files (bitmap + colors) | `.home.xml` + `.tsk` (layout + skin) |
| Customization | Install plugin DLL, registry entry | Drop `.home.xml` in `\Application Data\Home\` |
| Developer API | Win32 `todaycmn.h` + `InitializeCustomItem` | XML schema + COM plugin registration |
| Soft keys | Title bar buttons | Two hardware soft keys (context-sensitive) |
| Application launcher | Start Menu (tap top-left) | Hardware "Start" key |

---

## Customization Ecosystem

The extensible nature of the WM5.0 homescreen spawned a vibrant third-party ecosystem:

### Third-Party Today Screen Plugins (Pocket PC)

- **SPB Pocket Plus** — Tabbed Today Screen, enhanced launcher
- **Facade** — Skinnable Today Screen with large fonts for QVGA
- **WisBar** — Custom title bars, start menu replacements
- **Oxios** — Lightweight Today plugins (tasks, battery, memory)
- **PhoneAlarm** — Profile management with Today overlay

### Third-Party Homescreen Layouts (Smartphone)

- **Sliding Panels** (HTC, later WM6.1 standard) — Horizontal panel swiping
- **CHome Configurator** — GUI tool to reorder/enable/disable panels
- **Custom `.home.xml` files** — Community-created layouts shared on XDA-Developers
- **IconBar plugins** — Custom signal/battery/notification icons

### Registry-Based Tweaks

WM5.0 allowed extensive registry-level customization:

```
HKLM\Software\Microsoft\Today\Items\          → Enable/disable/reorder plugins
HKCU\ControlPanel\Home\Scheme                  → Active theme (.tsk file)
HKCU\ControlPanel\Home\BgImage                 → Background image path
HKLM\Security\CHome\DefaultSettings\           → Plugin order (Smartphone)
HKLM\Software\Microsoft\CHome\                 → CHome behavior (Sliding Panels)
```

---

## ASCII Art Diagram: Pocket PC Today Screen (Full)

```
┌──────────────────────────────────────────────────────┐
│  Start        Windows Mobile 5.0        12:45   🖪🔍│  ← Title bar
│                   Pocket PC                        │     (Notifications + battery)
├──────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  📅  Wednesday, July 08, 2026               │    │  ← Owner Info
│  │  Alice Smith                                 │    │
│  │  Acme Corp                                   │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  ✓  Tasks (3 active)                        │    │  ← Tasks plugin
│  │  ☐ Complete expense report         High    │    │
│  │  ☐ Buy groceries                   Normal  │    │
│  │  ☐ Call plumber                     Normal  │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  📅  Appointments                           │    │  ← Calendar plugin
│  │  2:00 PM ─── Meeting w/ Sarah  ─── Office  │    │
│  │  4:30 PM ─── Dentist ──────────── 123 Main │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  ✉  Messages                                │    │  ← Messaging plugin
│  │  Inbox (Exchange):   3 unread               │    │
│  │  SMS/MMS:            1 unread               │    │
│  │  Outlook Express:    0 unread               │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  📶  Signal: ▂▃▄▅▆▇█   Battery: ████78%    │    │  ← Device Info plugin
│  │  Storage:  ████████░░ 45% free              │    │     (third-party)
│  │  Memory:   ██████░░░░ 38% free              │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  --- (scroll for more plugins if installed) ---      │
│                                                       │
├──────────────────────────────────────────────────────┤
│  [  Phone  ] [Contacts] [  IE  ] [  📷  ] [  ⚙  ] │  ← Launcher bar
│                                                       │  (often OEM-customized)
└──────────────────────────────────────────────────────┘
```

---

## ASCII Art Diagram: Smartphone Homescreen (Default Layout)

```
┌──────────────────────────────────────────────────────┐
│  📶 ▂▃▄▅▆▇█   T-Mobile    🔋 ████   🕐 12:45 PM   │  ← Icon bar (always visible)
├──────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │                                             │    │
│  │           ╔═══════════════════╗             │    │
│  │           ║     12:45 PM     ║             │    │  ← CClock panel
│  │           ║                  ║             │    │     (large, centered)
│  │           ╚═══════════════════╝             │    │
│  │          Wednesday, July 8                  │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  📅  Next Appointment                       │    │  ← CAppointments panel
│  │  ─────────────────────────────              │    │
│  │  Meeting w/ Sarah                           │    │
│  │  Office · 2:00 PM - 3:00 PM                │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  📬  Notifications (4)                      │    │  ← CHome panel
│  │  ─────────────────────────────              │    │
│  │  📞  1 Missed Call                   12:30  │    │
│  │  ✉   3 New Email                     11:15  │    │
│  │  💬   1 SMS from Mom                  10:45  │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  🖼  My Photos                              │    │  ← CMyPhotos panel
│  │  ─────────────────────────────              │    │
│  │  [ Current wallpaper image displayed ]      │    │
│  │  Press OK to browse                        │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │  ♪  Music                                    │    │  ← CMusic panel (if enabled)
│  │  ─────────────────────────────              │    │
│  │  Now Playing:                                 │    │
│  │  "Bohemian Rhapsody" - Queen                 │    │
│  │  ⏪  ⏸  ⏩                                  │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
├──────────────────────────────────────────────────────┤
│  [  Contacts  ]                  [  Calendar  ]     │  ← Soft keys (context aware)
│  (Left soft key)                 (Right soft key)    │
└──────────────────────────────────────────────────────┘
```

---

## ASCII Art Diagram: Navigation Model (Smartphone)

```
┌─────────────── HARDWARE KEY NAVIGATION ──────────────┐
│                                                        │
│   UP / DOWN  =  Cycle between plugin panels            │
│                                                        │
│         ┌───────────────┐                              │
│         │   CClock      │  ← Panel 1 (focused)         │
│         │  (highlight)  │                              │
│         └───────┬───────┘                              │
│                 │  UP/DOWN                              │
│         ┌───────┴───────┐                              │
│         │ CAppointments │  ← Panel 2                   │
│         └───────┬───────┘                              │
│                 │                                       │
│         ┌───────┴───────┐                              │
│         │  CHome        │  ← Panel 3                   │
│         │ ┌───┬───┬───┐ │  ← LEFT/RIGHT to select      │
│         │ │📞 │✉  │💬 │ │      sub-items               │
│         │ └───┴───┴───┘ │                              │
│         └───────┬───────┘                              │
│                 │                                       │
│         ┌───────┴───────┐                              │
│         │ CMyPhotos     │  ← Panel 4                   │
│         └───────┬───────┘                              │
│                 │                                       │
│         ┌───────┴───────┐                              │
│         │  CMusic       │  ← Panel 5                   │
│         └───────────────┘                              │
│                                                        │
│   OK / ACTION  =  Select highlighted item/panel        │
│   LEFT SOFT KEY =  Context action (varies by panel)    │
│   RIGHT SOFT KEY = Context action (varies by panel)    │
│   START KEY     =  Open Start Menu (app launcher)      │
│   BACK KEY      =  Go back / dismiss                   │
│                                                        │
└────────────────────────────────────────────────────────┘
```

---

## Sources

- Wikipedia: Windows Mobile 5.0 (https://en.wikipedia.org/wiki/Windows_Mobile_5.0)
- Microsoft Docs: "How to: Install and Register a Custom Today Screen Item" (https://learn.microsoft.com/en-us/previous-versions/ms847161(v=msdn.10))
- Microsoft Docs: "Writing a Custom Today Screen Item" (https://learn.microsoft.com/en-us/previous-versions/aa458857(v=msdn.10))
- Microsoft Docs: "Home and Today Screen Guidelines" (https://learn.microsoft.com/en-us/previous-versions/ms847158(v=msdn.10))
- Microsoft Docs: "Home Configuration Service Provider" (https://learn.microsoft.com/en-us/previous-versions/aa455917(v=msdn.10))
- Microsoft News Center: "Microsoft Releases Windows Mobile 5.0" (https://news.microsoft.com/2005/05/10/microsoft-releases-windows-mobile-5-0/)
- XDA Developers: "How To: Customize the Sliding Panels Homescreen" (https://xdaforums.com/t/how-to-customize-the-sliding-panels-homescreen.397334/)
- Windows Central: "Howto: Customize your WM5 Smartphone-Edition Today Screen" (https://www.windowscentral.com/howto-customize-your-wm5-smartphone-edition-today-screen)
- Embedded.com: "Getting Started With Windows Mobile Application Development" (2006)
- MSDN Archive: "Designed for Windows Mobile Software Application Handbook for Smartphone" (May 2004)
- StackOverflow: "How to change windows mobile homescreen programmatically"
- XDA Developers: "CHome Configurator for WM6.1" (background on CHome architecture)
