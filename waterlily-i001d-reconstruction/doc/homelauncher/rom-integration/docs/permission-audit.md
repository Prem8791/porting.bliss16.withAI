# Permission Audit

| Permission | Class | Current purpose | ROM-bundled expectation | Allowlist / policy notes |
|---|---|---|---|---|
| `android.permission.REAL_GET_TASKS` | signature/privileged, framework dependent | retrieve complete recent tasks | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.MANAGE_ACTIVITY_TASKS` | signature/privileged, hidden API dependent | task stack listener and future TaskOrganizer | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.START_TASKS_FROM_RECENTS` | signature/privileged, hidden API dependent | resume existing tasks | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.REMOVE_TASKS` | signature/privileged, hidden API dependent | remove tasks from recents | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.READ_FRAME_BUFFER` | signature/privileged, hidden API dependent | task snapshots | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.FORCE_STOP_PACKAGES` | signature/privileged, hidden API dependent | force-stop app packages | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` | signature, framework mediated | notification listener declaration | service binding is framework-controlled; user/default grant still matters | include in privapp allowlist for strict builds |
| `android.permission.POST_NOTIFICATIONS` | dangerous runtime | notification permission if app posts notifications later | not automatically granted unless default permission grant is added | no privapp allowlist; user or default grant required |
| `android.permission.BATTERY_STATS` | signature/privileged | battery/system stats | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.DEVICE_POWER` | signature/privileged | power/settings integration | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.READ_CALENDAR` | dangerous runtime | Today calendar panel | not automatically granted | grant manually or add default-permission-grant policy later |
| `android.permission.QUERY_ALL_PACKAGES` | normal/special app visibility | app discovery | expected because declared | no privapp allowlist |
| `android.permission.STATUS_BAR` | signature/privileged | system integration | expected after platform signing and privileged placement | include in privapp allowlist |
| `android.permission.INTERACT_ACROSS_USERS` | signature/privileged | multi-user/system integration | expected after platform signing and privileged placement | include in privapp allowlist |

## Expected Immediately After ROM Integration

Expected granted:

- `REAL_GET_TASKS`
- `MANAGE_ACTIVITY_TASKS`
- `START_TASKS_FROM_RECENTS`
- `REMOVE_TASKS`
- `READ_FRAME_BUFFER`
- `FORCE_STOP_PACKAGES`
- `BATTERY_STATS`
- `DEVICE_POWER`
- `STATUS_BAR`
- `INTERACT_ACROSS_USERS`
- `QUERY_ALL_PACKAGES`

May require user/default grant:

- `READ_CALENDAR`
- `POST_NOTIFICATIONS`
- notification listener access

May require SELinux after validation:

- direct `/proc/stat`
- direct `/sys/class/thermal/.../temp`

May require framework/SystemUI work later:

- official Overview/Recents provider behavior
- gesture navigation / QuickStep binding
- TaskOrganizer transition animation parity
