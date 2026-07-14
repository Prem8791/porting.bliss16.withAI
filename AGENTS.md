# Agent Instructions

## Waterlily I001D VM Workflow

For the Android 16 Bliss Waterlily ASUS I001D reconstruction work:

- VM modifications are handled directly by the agent when needed.
- Build processes are handed over to the user. Do not start long Android builds on the VM unless the user explicitly asks the agent to run that build.
- When a build is needed, provide the exact VM-side commands for the user to run, then wait for the user to report the result.
- Keep `waterlily-i001d-reconstruction/progress.md` updated as reconstruction, VM edits, blockers, and build handoffs change.
- The current VM target is `/home/premanandal1978/android/waterlily` on instance `instance-20260710-230647`.
