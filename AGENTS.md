# Agent Instructions

## Waterlily I001D VM Workflow

For the Android 16 Bliss Waterlily ASUS I001D reconstruction work:

- If the user asks to reconstruct, restore, recreate a fresh VM, rebuild the ROM
  workspace, or recover the current ROM source state, read
  `waterlily-i001d-reconstruction/HOW_TO_RECONSTRUCT_CURRENT_VM.md` first.
  That file is marked NEVER DELETE and is the canonical restore entry point.
- VM modifications are handled directly by the agent when needed.
- Build processes are handed over to the user. Do not start long Android builds on the VM unless the user explicitly asks the agent to run that build.
- When a build is needed, provide the exact VM-side commands for the user to run, then wait for the user to report the result.
- Prefer updating the restore ledger in
  `waterlily-i001d-reconstruction/HOW_TO_RECONSTRUCT_CURRENT_VM.md` or the
  GitHub-backed clean restore repo over reviving old progress logs.
- The current VM target is `/home/premanandal1978/android/waterlily` on instance `instance-20260710-230647`.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
