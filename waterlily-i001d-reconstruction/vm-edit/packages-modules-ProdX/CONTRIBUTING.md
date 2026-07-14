# Contribution policy

All changes must identify a requirement, milestone, owner role, validation evidence, and rollback boundary. Architectural contracts may not be altered through implementation changes; use the architecture change process in the locked P0-00 governance record.

Before review:

1. Verify `tests/reference/BASELINE-FILES.sha256`.
2. Confirm the change is inside the active milestone's allowed write set.
3. Confirm no forbidden artifact or future-milestone target was introduced.
4. Run the milestone's static checks and obtain the required build/test evidence.
5. Update the live milestone record and implementation report without editing locked evidence.

P0-01 accepts documentation, metadata, directory placeholders, and the two build declarations in the root `Android.bp`. It rejects source files, generated files, binaries, archives, Binder definitions, manifests, policy, installable modules, product inclusions, and runtime configuration.

Reviews are role-based until named maintainers are assigned. Required roles are defined in `OWNERSHIP.md`; a contributor cannot approve their own security- or contract-sensitive change.
