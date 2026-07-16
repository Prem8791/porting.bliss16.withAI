# ProdX Engineering Decision Log

Entries are append-only. Superseding a decision creates a new entry.

## DEC-0001 — Lock the authoritative reference set

- Status: `ACCEPTED`
- Date: `2026-07-14`
- Owners: `OWN-ARCH`, `OWN-SECURITY`, `OWN-CONTRACT`
- Context: Implementation requires a single immutable architecture baseline.
- Decision: Lock REF-FOUNDATION, REF-CONTRACT, REF-SKELETON and REF-P0 by the
  SHA-256 values in `REFERENCE-LOCK.md`; lock the embedded 44-file Capability
  Investigation by deterministic content-set digest.
- Consequence: Any change requires a new version/hash, ACP and baseline.
- Rollback: Revert only this documentation baseline; no runtime exists.
- Validation: `VAL-REF-001`, `VAL-REF-002`.

## DEC-0002 — Use role-stable reviewer ownership IDs

- Status: `ACCEPTED`
- Date: `2026-07-14`
- Owners: `OWN-ARCH`, `OWN-RELEASE`
- Context: Named personnel may change during a multi-milestone platform project.
- Decision: Requirements and validations use stable owner IDs; the issue tracker
  maps them to named humans before work begins.
- Consequence: Ownership remains traceable across staffing changes.
- Rollback: Replace through a superseding governance decision.
- Validation: `VAL-GOV-002`.

## DEC-0003 — Preserve the pre-existing dirty Android checkout

- Status: `ACCEPTED`
- Date: `2026-07-14`
- Owners: `OWN-RELEASE`, `OWN-ROMOEM`
- Context: The source tree already contains I001D reconstruction changes in
  bionic, build prebuilts and the kernel.
- Decision: Record them as baseline state and do not clean, reset, modify or
  attribute them to ProdX.
- Consequence: P0 verification compares only the allowed ProdX documentation
  delta and checks the pre-existing status remains unchanged.
- Rollback: Remove only P0-00 documentation; preserve pre-existing work.
- Validation: `VAL-BAS-003`, `VAL-BAS-004`.

## DEC-0004 — Capability Investigation is a locked content set inside Foundation

- Status: `ACCEPTED`
- Date: `2026-07-14`
- Owners: `OWN-ARCH`, `OWN-TEST`
- Context: Investigation evidence is curated inside the Foundation ZIP rather
  than stored as a separate archive.
- Decision: Identify it by the Foundation archive hash, internal manifest hashes
  and the deterministic 44-entry aggregate digest.
- Consequence: Investigation provenance is independently verifiable without
  duplicating 83.9 MB of uncompressed evidence.
- Rollback: A new evidence archive/content set requires ACP and re-baseline.
- Validation: `VAL-REF-002`.

## DEC-0005 — P0-00 permits documentation-only Android-tree additions

- Status: `ACCEPTED`
- Date: `2026-07-14`
- Owners: `OWN-ARCH`, `OWN-BUILD`, `OWN-SECURITY`
- Context: P0-00 must establish governance without runtime/build behavior.
- Decision: The sole allowed tree delta is
  `packages/modules/ProdX/tests/reference/` containing non-executable governance,
  status, manifest and immutable reference artifacts.
- Consequence: Any source/interface/policy/manifest/build/service/provider or
  executable file is a milestone failure.
- Rollback: Remove the directory.
- Validation: `VAL-BAS-005`, `VAL-BAS-006`.

## Open decisions

None. P0-00 closes all decisions required to enter P0-01. Later implementation
decisions use the template and process in `GOVERNANCE.md`.

