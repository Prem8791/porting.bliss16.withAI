# Live milestone state

The immutable starting state is `tests/reference/MILESTONE-TRACKER.md`. Later transitions are recorded here so baseline hashes remain valid.

| Milestone | State | Evidence |
|---|---|---|
| P0-00 | COMPLETE | `tests/reference/POST-INSTALL-VERIFICATION.md` |
| P0-01 | COMPLETE | `docs/engineering/P0-01-IMPLEMENTATION-REPORT.md`; user-confirmed `m prodx-p0` and `m nothing` success on 2026-07-14 |
| P0-02 and later | NOT_STARTED | no writes authorized |

Allowed states are `NOT_STARTED`, `IN_PROGRESS`, `REVIEW`, `COMPLETE`, `BLOCKED`, and `ROLLED_BACK`. P0-01 moved to `COMPLETE` after both required graph-validation commands succeeded. Later milestones require separate authorization and entry-gate validation.
