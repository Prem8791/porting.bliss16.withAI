# ProdX Engineering Governance

Baseline: `BL-20260714-01`

## 1. Control principles

1. Frozen architecture precedes implementation convenience.
2. Android remains the authority for execution, permissions, AppOps, policy,
   identity, authentication, user/profile isolation and hardware.
3. No milestone begins without satisfied entry criteria and assigned reviewers.
4. Every normative change maps to a requirement, validation, owner and gate.
5. Every source change is independently revertible and leaves Android bootable
   with ProdX disabled.
6. Security-relevant unknowns fail closed and block the affected gate.
7. No provider or extension can grant itself trust, risk, privilege or readiness.
8. Evidence is immutable and content-addressed; corrections append/supersede.

## 2. Ownership and RACI

| Owner ID | Accountable scope | Mandatory reviews |
|---|---|---|
| OWN-ARCH | Architecture authority and reference precedence | Architecture changes, boundary changes, milestone exit |
| OWN-CONTRACT | Contract/schema/canonicalization/version compatibility | G1, interface object use, schema/vector changes |
| OWN-FRAMEWORK | Framework API/SystemServer/Authority/Registry/Policy | G2, SystemServer integration, multi-user lifecycle |
| OWN-RUNTIME | Broker/Hub/Extension dedicated services | State machines, death/recovery, resource bounds |
| OWN-AUDIT | Audit storage/integrity/recovery/retention | Audit-before-effect, migrations, power-loss behavior |
| OWN-SDK | Provider Framework/SDK/conformance | Provider boundary, verifier, test no-op provider |
| OWN-SYSTEMUI | Trusted confirmation/authentication/indicator UX | Anti-spoofing, lifecycle, accessibility-security |
| OWN-SETTINGS | Administration/grants/history/kill switch UX | User/admin scope, absent-runtime behavior |
| OWN-SECURITY | Threat model, Binder/permission/signing/SELinux and fuzzing | G0, G2, G3, G6, exception review |
| OWN-PRIVACY | Classification, minimization, consent, retention/deletion | Contract fields, Hub/Audit/learning boundaries, G6 |
| OWN-MULTIUSER | User/profile/direct-boot/cross-profile correctness | Authority, stores, UI, providers, G6 |
| OWN-BUILD | Target visibility, Soong/product/Mainline/APEX integration | G3, update/rollback, installed inventory |
| OWN-TEST | Validation catalog, coverage, fault/fuzz/regression evidence | Every milestone exit, G5–G7 |
| OWN-RELEASE | Gate status, evidence hashes, rollback readiness | G7 and emergency disable drill |
| OWN-ROMOEM | Bliss/Lineage/OEM/Treble boundary liaison | Future ROM/device seams; confirms no P0 dependency |

Named human assignees are recorded in the engineering issue tracker. The stable
Owner IDs remain in requirements and tests so personnel changes do not rewrite
the baseline. One person may not solely approve both implementation and
security/release evidence for a high/critical boundary.

## 3. Milestone tracking

`MILESTONE-TRACKER.md` is append-only for completed transitions. Allowed states:
`NOT_STARTED`, `READY`, `IN_PROGRESS`, `BLOCKED`, `REVIEW`, `COMPLETE`,
`ROLLED_BACK`. A state transition records date, evidence links/hashes, owner and
approver. `COMPLETE` requires all entry/exit requirements and validation rows.

No later milestone may mark an earlier incomplete prerequisite as “assumed.” A
test fake may unblock parallel development only after the logical interface is
frozen and must never ship in a production package graph.

## 4. Decision log

All nontrivial engineering choices use `DECISION-LOG.md` IDs `DEC-####` and
include context, immutable constraints, options, decision, consequences,
requirements affected, validation, rollback and approvals. Decisions may
clarify implementation but cannot override a frozen reference.

Supersession appends a new decision that cites the old ID. History is never
edited to hide the previous decision.

## 5. Architecture change process

Architecture changes use IDs `ACP-####` and must contain:

1. reason and evidence;
2. affected reference sections, objects, repositories, processes and trust
   boundaries;
3. threat/privacy/multi-user/update/rollback impact;
4. compatibility and migration analysis;
5. requirement/validation/decision diffs;
6. alternatives and least-authority analysis;
7. implementation and rollback sequencing;
8. approvals from OWN-ARCH, OWN-SECURITY, OWN-CONTRACT and every affected owner;
9. a new immutable artifact/version/hash; and
10. a new baseline generation.

Emergency security disablement may stop runtime behavior immediately, but it
does not permit an architecture change without retrospective ACP review before
reenablement.

## 6. Requirement management

`REQUIREMENTS-TRACEABILITY.tsv` is normative. IDs never change meaning or get
reused. A requirement can be `ACTIVE`, `SUPERSEDED` or `RETIRED`; the latter two
require an ACP and replacement/rationale. Every implementation change cites all
affected IDs. Every test cites validation IDs, and every validation maps back to
one or more requirements.

A requirement without owner, milestone or validation blocks readiness. A test
without a requirement is supplemental and cannot replace release evidence.

## 7. Rollback policy

- Each milestone has a tested, documented rollback to the last complete gate.
- ProdX must remain feature-disabled independently of module/package health.
- Rollback order is: stop new authority, increment epochs/revoke, drain Broker,
  revoke Hub leases, stop extension work, checkpoint Audit, unbind services,
  rollback package/APEX/product delta, verify stale-object rejection and Android
  regression.
- Never broaden policy, enable permissive SELinux, silently erase Audit, reuse
  authorization, or interpret unknown/newer data to make rollback succeed.
- Binary, schema, policy and persistent-format compatibility are reviewed as one
  rollback unit.
- If safe rollback cannot read required evidence, effects remain disabled.

## 8. Coding and source standards for later milestones

P0-00 adds no source. When source is authorized:

- follow the owning AOSP repository language/style/API/lint rules;
- use descriptive immutable types and closed schemas; avoid generic maps,
  bundles, intents, URIs, paths, reflection and arbitrary callbacks;
- make bounds, timeouts, cancellation, idempotency, threading and error behavior
  explicit;
- derive identity at trusted boundaries and never trust caller-supplied UID,
  user, package, permission, policy or confirmation;
- avoid shared mutable cross-process files and global static authority;
- minimize/redact before persistence/logging/IPC;
- use no debug bypass in production paths;
- keep production and test-only targets/configuration structurally separate;
- add tests before or with behavior; and
- keep changes small, owner-scoped and independently revertible.

Warnings, lint suppressions, broad permissions, platform shared UID, new hidden
API use and SELinux allows require explicit owner/security rationale. Generated
code must derive from reviewed schemas and be reproducible.

## 9. Security review gates

| Gate | Minimum security evidence |
|---|---|
| G0 | Immutable references, threat ledger, prohibited primitives and owners locked |
| G1 | Schema/canonical vectors, parser bounds, unknown-version failure and fuzz plan |
| G2 | Caller/callee identity, size/thread/death/version/error rules for each IPC; no generic primitive |
| G3 | Exact UID/domain/service/file/permission/signing graph; undeclared edges denied |
| G4 | Component unit/negative/death/resource tests and zero unresolved high findings |
| G5 | End-to-end synthetic transaction/observation/extension quarantine; no real capability |
| G6 | Fuzz, replay, multi-user, privacy, crash/power-loss, downgrade/rollback and red-team evidence |
| G7 | Production inventory absence, emergency disable, rollback drill and independent release approval |

Critical/high threat validation is release-blocking. Exceptions require an ACP;
time-limited waivers cannot authorize a security-boundary bypass.

## 10. Testing strategy

Testing layers are contract vectors, host unit/property tests, parser/protocol
fuzzing, component device tests, framework/SystemUI/Settings tests, multi-process
integration, SELinux/signing/permission negatives, multi-user/direct-boot,
fault/power-loss/resource injection, upgrade/downgrade/rollback, platform
regression and production installed-inventory verification.

Every cross-process phase is tested immediately before and after decode,
resolution, policy, proof, audit reserve, authorization, dispatch, result,
queue/ack, extension attest and persistence checkpoint. Test evidence records
target, build/source/reference hashes, product, seed/corpus, command, result,
duration and artifact hash.

Tests must prove absence as well as success: no real provider, no broad Binder
access, no direct HAL/kernel/native socket, no cross-user leak, no code loading,
no token in observations/learning and no test mode/no-op provider in production.

## 11. Release gates

G7 requires:

- G0–G6 complete with immutable evidence;
- no open critical/high security/privacy correctness issue;
- all requirements `PASS` or explicitly not applicable to P0 by frozen scope;
- API/dependency/installed-file/permission/SELinux and test inventories reviewed;
- product defaults `DISABLED` or approved `INVENTORY_ONLY`;
- no real/test provider, test mode, public API, model or learning component in
  production-like image;
- demonstrated module/package and framework-disable rollback;
- Android boots and passes regression with ProdX absent/disabled/rolled back;
- emergency disable drill; and
- OWN-ARCH, OWN-SECURITY, OWN-PRIVACY, OWN-BUILD, OWN-TEST and OWN-RELEASE signoff.

