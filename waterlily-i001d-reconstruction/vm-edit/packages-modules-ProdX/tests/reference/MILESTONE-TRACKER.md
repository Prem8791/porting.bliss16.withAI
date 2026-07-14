# ProdX Milestone and Gate Tracker

Baseline: `BL-20260714-01`

## Gates

| Gate | State | Required milestone/evidence | Owner |
|---|---|---|---|
| G0 Reference lock | `COMPLETE` | P0-00 documents, hashes, threat/requirement/validation ledgers | OWN-ARCH / OWN-SECURITY |
| G1 Contract lock | `NOT_STARTED` | P0-02 schemas/vectors/conformance | OWN-CONTRACT |
| G2 Interface lock | `NOT_STARTED` | P0-03 interface ownership and security review | OWN-FRAMEWORK / OWN-SECURITY |
| G3 Process/build lock | `NOT_STARTED` | P0-13 UID/domain/service/file/signing graph | OWN-BUILD / OWN-SECURITY |
| G4 Subsystem unit-ready | `NOT_STARTED` | Individual subsystem exits | Component owners |
| G5 Shadow integration | `NOT_STARTED` | P0-15 end-to-end synthetic paths | OWN-RUNTIME / OWN-TEST |
| G6 Security/recovery | `NOT_STARTED` | P0-15 fault/fuzz/multi-user/rollback evidence | OWN-SECURITY / OWN-PRIVACY |
| G7 P0 freeze | `NOT_STARTED` | Final acceptance and production absence proof | OWN-RELEASE |

## Milestones

| Milestone | State | Dependencies | Accountable owner | Completion evidence |
|---|---|---|---|---|
| P0-00 Reference lock/baseline | `COMPLETE` | Immutable references | OWN-ARCH | This directory; post-install verification |
| P0-01 Repository/test skeleton | `NOT_STARTED` | P0-00/G0 | OWN-BUILD / OWN-TEST | Pending |
| P0-02 Contract runtime/vectors | `NOT_STARTED` | P0-01 | OWN-CONTRACT | Pending |
| P0-03 Framework projection/interface design | `NOT_STARTED` | P0-02/G1 | OWN-FRAMEWORK | Pending |
| P0-04 Authority Service bootstrap | `NOT_STARTED` | P0-02/P0-03/G2 | OWN-FRAMEWORK | Pending |
| P0-05 Registry/reconciler | `NOT_STARTED` | P0-02/P0-04 | OWN-FRAMEWORK | Pending |
| P0-06 Policy/grants/authorization | `NOT_STARTED` | P0-02/P0-04/P0-05 | OWN-FRAMEWORK / OWN-SECURITY | Pending |
| P0-07 Audit Engine | `NOT_STARTED` | P0-02 | OWN-AUDIT | Pending |
| P0-08 SystemUI/Settings | `NOT_STARTED` | P0-04/P0-06/P0-07 | OWN-SYSTEMUI / OWN-SETTINGS | Pending |
| P0-09 Broker | `NOT_STARTED` | P0-02/P0-05/P0-06/P0-07/G2 | OWN-RUNTIME | Pending |
| P0-10 Provider Framework/SDK/no-op | `NOT_STARTED` | P0-02/P0-03/P0-06/P0-09 | OWN-SDK | Pending |
| P0-11 Observation/Event | `NOT_STARTED` | P0-02/P0-05/P0-06/P0-07/P0-10 | OWN-RUNTIME | Pending |
| P0-12 Extension quarantine | `NOT_STARTED` | P0-02/P0-04/P0-05 | OWN-RUNTIME / OWN-SECURITY | Pending |
| P0-13 Security packaging | `NOT_STARTED` | P0-04/P0-07/P0-09/P0-11/P0-12/G2 | OWN-SECURITY / OWN-BUILD | Pending |
| P0-14 Product/APEX integration | `NOT_STARTED` | All component G4/P0-13/G3 | OWN-BUILD | Pending |
| P0-15 Shadow integration/hardening | `NOT_STARTED` | P0-00–P0-14 | OWN-TEST / OWN-RELEASE | Pending |

State changes must append a dated entry below and cite immutable evidence.

## Transition history

| UTC date | Item | From | To | Evidence | Approver roles |
|---|---|---|---|---|---|
| 2026-07-14 | P0-00 | `IN_PROGRESS` | `COMPLETE` | `BL-20260714-01` documentation and verification | OWN-ARCH, OWN-SECURITY, OWN-RELEASE |
| 2026-07-14 | G0 | `NOT_STARTED` | `COMPLETE` | Locked references, requirement/threat/validation ledgers | OWN-ARCH, OWN-SECURITY |

