# ProdX P0-00 Engineering Baseline

Status: `LOCKED`  
Milestone: `P0-00 — Reference Lock, Threat Ledger, and Engineering Baseline`  
Captured: `2026-07-14T12:05:53Z`  
Android root: `/home/premanandal1978/android/waterlily`

This directory is the documentation-only control plane for ProdX engineering.
It introduces no runtime behavior, framework service, IPC, provider, policy,
build target, executable code, or product package.

## Authority order

1. Runtime Foundation and embedded Capability Investigation: security intent,
   subsystem evidence and participation boundaries.
2. Runtime Contract Specification: immutable runtime object semantics.
3. Runtime Skeleton Specification: physical Android placement.
4. P0 Runtime Implementation Specification: P0 execution sequence and gates.
5. This baseline: engineering governance, requirement identifiers, ownership,
   validation and recorded source state.

Any conflict is escalated through `GOVERNANCE.md`; implementation may not make
an undocumented local interpretation.

## Files

| File | Purpose |
|---|---|
| `REFERENCE-LOCK.md` | Immutable reference identities, hashes and Capability Investigation digest method |
| `ENGINEERING-BASELINE.md` | Android manifest, product, host, toolchain and pre-existing repository state |
| `BASELINE-MANIFEST.json` | Machine-readable baseline identity |
| `resolved-manifest.xml` | `repo manifest -r` snapshot for all repo-managed projects |
| `repo-status.txt` | Pre-P0 repo status snapshot |
| `GOVERNANCE.md` | Decision/change process, ownership, coding, review, rollback, testing and release controls |
| `DECISION-LOG.md` | Append-only engineering decision ledger |
| `MILESTONE-TRACKER.md` | G0–G7 and P0-00–P0-15 state tracking |
| `DEPENDENCY-GRAPH.md` | Frozen implementation dependency graph and parallel lanes |
| `THREAT-LEDGER.tsv` | Threats, affected boundaries, controls, owners and validation |
| `REQUIREMENTS-TRACEABILITY.tsv` | Unique normative requirements mapped to source, milestone, owner and validation |
| `VALIDATION-CATALOG.tsv` | Validation procedure identifiers and pass evidence |
| `immutable/` | Byte-identical copies of the four immutable reference artifacts |

## Lock rule

The immutable inputs are never edited. Updating one requires an approved
architecture change record, a new file/version/hash, impact analysis, affected
requirement regeneration and a new baseline generation. This P0-00 generation
is `BL-20260714-01`.

## Completion assertion

P0-00 permits P0-01 to start only after:

- all immutable hashes verify;
- every normative requirement has an ID, owner, milestone and validation;
- all critical/high threats have controls and release-blocking validation;
- Android manifest/product/toolchain/repository state is recorded;
- governance and rollback rules are accepted; and
- the post-install verification proves the only Android-tree additions are this
  documentation directory and its immutable evidence copies.

