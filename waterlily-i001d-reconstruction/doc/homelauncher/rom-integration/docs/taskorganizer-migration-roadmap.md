# TaskOrganizer / WM Shell Migration Roadmap

Do not implement TaskOrganizer in the first ROM-bundled pass. The first objective is to run `com.home.launcher` from `/system/priv-app` with platform signing and a clean compatibility boundary.

## Prepared Interfaces

The app now uses:

```text
RecentTasksRepository
RecentTasksBackend
RecentTask
TaskListenerRegistration
```

Temporary backend:

```text
ReflectionRecentTasksBackend
```

Future backend placeholder:

```text
TaskOrganizerRecentTasksBackend
```

## Future TaskOrganizer Backend Responsibilities

The future backend should:

1. Register `TaskOrganizer`.
2. Seed current tasks from `registerOrganizer()` returned `TaskAppearedInfo`.
3. Maintain an in-memory task map keyed by `taskId`.
4. Update the task map from:
   - `onTaskAppeared`
   - `onTaskVanished`
   - `onTaskInfoChanged`
   - snapshot-related callbacks if available on the target branch
5. Expose the same `RecentTasksBackend` methods to the UI.
6. Keep reflection backend enabled behind a debug flag until parity is proven.

## Coexistence Strategy

1. Start with reflection backend as default.
2. Add a developer setting or build-time flag to select TaskOrganizer backend.
3. Run both backends in diagnostic mode and compare task IDs/order/package names.
4. Switch UI reads to TaskOrganizer after parity.
5. Keep reflection only for emergency fallback.
6. Remove reflection after Android 15/16 validation.

## Required ROM/SystemUI Work For Official Overview

1. Implement `android.intent.action.QUICKSTEP_SERVICE` in `com.home.launcher`.
2. Implement the Overview proxy contracts used by SystemUI.
3. Enable `config_recentsComponentName` overlay.
4. Validate `OverviewProxyService` binds to `com.home.launcher`.
5. Validate WM Shell recents and transition handlers continue to receive task lifecycle events.

## Non-Goals For First Pass

- no custom WM Shell fork
- no framework patches
- no SystemUI contract replacement
- no SELinux policy changes before first boot evidence
