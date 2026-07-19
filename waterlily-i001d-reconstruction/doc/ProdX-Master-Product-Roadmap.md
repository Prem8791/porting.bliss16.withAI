# ProdX Master Product Roadmap

Status: Living product roadmap
Approved direction: 2026-07-16
Target: Android 16 Bliss ROM for ASUS I001D

## 1. Product objective

ProdX will turn reviewed Android capabilities into typed tools that an AI
assistant can use under Android-owned policy, user confirmation, audit and
process isolation. The same capability families will expose bounded
observations where Android supports them. Those observations will feed a
background security monitor, a private per-user knowledge base and a
privacy-controlled learning system.

The target is broad Android coverage, not unrestricted privilege. Raw Binder
enumeration, arbitrary shell execution, direct HAL/kernel access, credential
extraction and security-policy bypasses are never presented to the model as
tools. Every stable Android capability is recorded in the capability inventory
as exposed, planned, unavailable on I001D, or deliberately blocked with a
reason.

## 2. Relationship to the existing P0 plan

The immutable P0 specification remains the security-foundation record:

- `artifacts/ProdX-P0-Runtime-Implementation-Specification-20260714.md`

P0 defines contracts, Authority, Registry, policy, audit, trusted UI, Broker,
provider SDK, observation plumbing, extension quarantine, SELinux, packaging
and a synthetic end-to-end release gate. It deliberately excludes a real AI
model and Learning Engine.

This document is the living product roadmap above P0. It does not rewrite or
silently change the immutable P0 evidence. P0 phase IDs remain useful for
traceability, while the six product programs in Section 5 continue beyond P0.

## 3. Non-negotiable architecture

```text
User
  |
  v
AI reasoning host
  | typed proposal; no Android privilege
  v
Secure Broker
  | identity + policy + grant + confirmation + audit
  v
Capability provider family
  | narrow reviewed Android API access
  v
Android OS

Android events
  |
  v
Observation adapters -> Security monitor -> Alerts/recommendations
                          |
                          v
                 Private knowledge base
                          |
             eligible + consented records only
                          v
                 Isolated learning system
                          |
                evaluated model/preferences
                          v
                    AI reasoning host
```

Rules:

1. The model proposes; it never calls Android services or providers directly.
2. The Broker owns transaction state but owns no broad Android capability.
3. Each provider receives only the permissions needed for its declared tools.
4. Control, observation, knowledge and learning data use separate contracts.
5. Security decisions remain deterministic and Android-owned; learned output
   cannot grant permission, lower risk or bypass confirmation.
6. Personal knowledge and learning are per-user, encrypted, inspectable,
   exportable and deletable.
7. Missing, dead, stale, incompatible or unverified components fail closed.

## 4. Honest current baseline

The successful scoped build proves that services, SystemUI, Settings and the
dedicated ProdX UI/Settings test targets compile together. It does not prove a
working assistant or an end-to-end device transaction.

| P0 area | Current state | Required before acceptance |
|---|---|---|
| P0-00/01 reference and repository | Established | Keep immutable evidence and update only living trackers |
| P0-02 contracts | Meaningful implementation and tests | Complete bounds/fuzz/conformance coverage |
| P0-03 framework projection | Compiles | Correct defensive copies, bounds and final client transaction API |
| P0-04 Authority | Compiles and is feature-gated | Boot/device validation, identity hardening and service labels |
| P0-05 Registry | Basic in-memory catalog | Durable/reconciled provider identity and generations |
| P0-06 policy/authorization | Partial and unsafe for execution | Fix mode enforcement, grants, token claims, keystore and replay |
| P0-07 Audit | Meaningful ledger core | Product install, typed binding, multi-user and Authority/Broker integration |
| P0-08 trusted UI/Settings | Compiles | Fix dialog/auth/emergency behavior and run device security/accessibility tests |
| P0-09 Broker | Skeleton | Real Binder service and deterministic transaction state machine |
| P0-10 SDK/no-op provider | Skeleton | Working authorization verifier and bindable no-effect provider |
| P0-11 Observation | Skeleton | Real leases, sources, queues, redaction and synthetic delivery |
| P0-12 Extension | Skeleton | Bounded parser, signing verification and quarantine-only admission |
| P0-13 security packaging | Partial declarations | Exact UID/domain/service/file policy and enforcing negative tests |
| P0-14 product/APEX | Placeholder | Installable product graph, boot/update/rollback path |
| P0-15 integration | Placeholder tests | Meaningful no-op end-to-end, fault, security and production-absence tests |

## 5. Six permanent product programs

### Program A — Android Capability Fabric

Purpose: convert Android functions and knowledge surfaces into versioned typed
tools and bounded observation sources.

Deliverables:

- machine-readable capability inventory with owner, Android API, permission,
  risk, confirmation, data class, user/profile behavior and device support;
- provider SDK with strict input/output validation and authorization checking;
- isolated provider families rather than one omnipotent provider;
- separate control and observation contracts where a capability supports both;
- deterministic health, cancellation, timeout and update behavior;
- conformance tests proving undeclared Android access is denied.

Capability waves:

1. Safe device tools: flashlight, volume/media, application launch, alarms,
   timers and read-only device health.
2. Connectivity and system state: Wi-Fi, Bluetooth, display, battery, storage,
   notification and approved settings surfaces.
3. Personal organization: calendar, contacts, reminders, downloads and files,
   with field-level data minimization and confirmation for writes.
4. Communications: dialer, messaging and notification actions, using Android
   roles/default-app policy and explicit confirmation.
5. Context and sensors: location, motion, audio/camera state and selected sensor
   summaries, with privacy indicators, sampling budgets and background limits.
6. ROM/OEM administration: reviewed Bliss, Lineage and ASUS functions. Factory,
   radio-debug and destructive device-administration primitives remain blocked
   unless separately designed, tested and approved.

Completion measure: every investigated Android/I001D capability has a recorded
status, and every exposed tool passes provider conformance and negative-access
tests.

### Program B — Secure Tool Broker

Purpose: provide the single AI-facing transaction doorway and safely coordinate
all tool execution.

Deliverables:

- submit, query, cancel and accepted-async transaction APIs;
- request/schema/purpose/dependency validation before policy evaluation;
- immutable transaction state machine and idempotency handling;
- exact provider component/version/signature/UID resolution;
- authorization bound to caller, user, purpose, capability, parameters,
  provider, transaction, audience, expiry, policy/grant/registry epochs and
  confirmation proof;
- Audit reservation before effects and terminal/unknown recovery afterward;
- timeout, cancellation, Binder death, reboot and partial-outcome recovery;
- structured results suitable for AI reasoning without leaking hidden data.

Completion measure: a synthetic proposer can execute and cancel the no-op tool
end to end, while wrong callers, stale tokens, replay and provider substitution
are denied.

### Program C — AI Reasoning and Assistant Runtime

Purpose: understand user intent, select tools, plan bounded workflows and hold a
conversation without owning Android privilege.

Deliverables:

- unprivileged reasoning host and per-session isolated model worker;
- model asset/version/compatibility management;
- minimized context assembly and explicit session resource limits;
- tool-catalog projection containing only tools available to the current user;
- structured tool proposal, clarification, result and error loop;
- explanation and confirmation copy derived from trusted capability metadata,
  never arbitrary model prose;
- model crash/timeout fallback that leaves Android and Broker state safe;
- evaluation suite for tool selection, planning, hallucinated tools, prompt
  injection, unsafe proposals and recovery.

Completion measure: natural-language requests reliably produce correct typed
tool proposals, and the model cannot call Android, Authority or providers
outside the Broker.

### Program D — Observation and Security Monitor

Purpose: continuously observe approved Android security/health signals, detect
suspicious behavior and report understandable incidents.

Deliverables:

- Authority-issued, time-limited observation leases;
- source adapters for package changes, permissions/AppOps, accessibility,
  overlays, microphone/camera/location use, process/service behavior, network
  summaries, authentication failures, device health and ProdX activity;
- bounded queues, rate limits, backpressure, gap markers and restart handling;
- deterministic rule engine for known dangerous states and sequences;
- AI-assisted correlation and explanation that cannot override deterministic
  severity or directly take privileged action;
- incident timeline with source provenance, confidence and recommended action;
- notifications and Settings controls for acknowledge, quarantine proposal,
  revoke proposal, ignore rule and false-positive feedback;
- strict data minimization: metadata by default, content only through a separate
  explicit consent/policy path.

Completion measure: controlled rogue-app scenarios generate timely incidents,
normal workloads remain bounded, and monitor/AI failure cannot block Android.

### Program E — Private Growing Knowledge Base

Purpose: give the assistant durable, trustworthy memory without turning all
device data into an uncontrolled model database.

Deliverables:

- encrypted per-user storage with CE/DE separation;
- typed knowledge records for preferences, entities, workflows, outcomes,
  device facts and security incidents;
- source reference, timestamp, confidence, sensitivity, retention, consent and
  supersession on every record;
- hybrid exact/structured/semantic retrieval with provenance returned alongside
  every result;
- conflict handling, correction, expiry and deduplication;
- user-facing inspect, search, edit, export, delete and reset controls;
- profile/user isolation and deletion on user/profile removal;
- no authorization token, credential or raw unrestricted audit stream stored as
  knowledge.

Completion measure: the assistant remembers approved facts across reboot,
explains where each fact came from and reliably forgets deleted information.

### Program F — Learning and Personalization

Purpose: improve assistant behavior over time using eligible outcomes and user
feedback while preserving user control and security invariants.

Deliverables:

- learning eligibility and explicit consent policy;
- immutable learning records derived from minimized knowledge/audit references;
- preference/ranking adaptation before any model-weight training;
- isolated learning workers scheduled under charging, thermal, storage and
  resource policy;
- candidate model/preference versioning, provenance and reproducibility;
- offline evaluation, safety regression, quality threshold and rollback before
  activation;
- protection against feedback poisoning and one app dominating learning;
- user controls to pause learning, exclude sources, inspect learned preferences,
  undo updates and erase learned state.

Completion measure: repeated approved feedback measurably improves a held-out
evaluation, while security decisions and permission requirements remain
unchanged and rollback restores the previous behavior.

## 6. P0 acceleration and recovery plan

P0 must finish quickly without hiding defects behind phase labels. The goal is
one secure synthetic vertical slice, not the full product.

### Lane 1 — Foundation correctness

- Correct authorization keystore creation, action/audience claims, replay state
  and bounded parsing.
- Enforce disabled/inventory/shadow/test modes consistently.
- Add a real grant-creation path with user, package, capability and expiry
  binding.
- Persist emergency-disable epoch/state across restart.
- Fix trusted-dialog dismissal, make denial immediate, define fresh-auth policy
  and make emergency stop permission-correct.
- Add missing Binder service labels and exact permission callers.

Gate: focused unit tests fail before each correction and pass afterward;
`services`, `SystemUI`, Settings and dedicated tests build together.

### Lane 2 — Synthetic runtime vertical slice

- Bind Authority to the Audit service and expose health/readiness.
- Implement the Broker Binder service and minimal complete state machine.
- Implement provider authorization verification and the bindable no-op provider.
- Implement synthetic Observation lease/source/queue/delivery.
- Implement quarantine-only Extension validation for one synthetic manifest.

Gate: one no-op request and one synthetic observation complete end to end; no
real Android effect or dynamic extension execution exists in P0.

### Lane 3 — Product/security/test closure

- Install P0 services only on the engineering product in dependency order.
- Complete exact service/file/domain SELinux rules without broad allow rules.
- Replace placeholder unit, integration, security, device and fuzz tests with
  assertions against real boundaries.
- Boot enforcing, kill each process at every transaction phase and verify
  fail-closed recovery.
- Prove the no-op provider is absent from the production-like package graph.

Gate: P0-15 end-to-end and negative-security catalog passes on a booted image.

### Faster build discipline

1. Build only the owner module and its tests during red/green development.
2. Batch compatible interface changes before the consolidated framework build.
3. Do not clean `out/` for ordinary source corrections.
4. Run one consolidated scoped build at each lane gate.
5. Build and flash a full ROM only at boot/product/security gates.
6. Keep a forward dependency scan after every compile failure so retries expose
   new work rather than one missing import at a time.
7. Update `progress.md` with the exact failed target, root cause, correction and
   next user-run build command.

### P0 exit definition

P0 is complete only when all of the following are true:

- Authority boots and remains optional to normal Android operation.
- A synthetic client completes one no-op transaction through Broker, policy,
  trusted confirmation when required, authorization, provider and Audit.
- A synthetic observation traverses lease, source, redaction, queue and
  consumer delivery without authorizing an action.
- A synthetic extension remains quarantined and cannot load code.
- Emergency disable persists and invalidates grants/tokens/leases.
- Enforcing SELinux denies every undeclared edge.
- Tests exercise real code; placeholder assertions do not count.
- Production mode contains no model, learner, real provider or test provider.

## 7. Product delivery milestones after P0

| Milestone | User-visible result | Programs involved |
|---|---|---|
| M1 Assistant vertical slice | Conversational assistant operates five safe device tools | A, B, C |
| M2 Capability wave 2 | Connectivity/system-state control and observation | A, B, C |
| M3 Security monitor MVP | Background rules detect and report controlled rogue-app scenarios | A, D, E |
| M4 Durable assistant memory | Approved preferences/facts survive reboot with source and deletion controls | C, E |
| M5 Personalization | Feedback improves ranking/tool choice with evaluation and rollback | C, E, F |
| M6 Sensitive capability waves | Personal organization, communications and context tools under stronger policy | A, B, C, D |
| M7 Broad I001D coverage | Capability inventory is closed: exposed, unavailable or deliberately blocked | All |
| M8 Product release | Full ROM update/rollback, privacy, security, performance and recovery gates pass | All |

Milestones are acceptance-driven rather than calendar-driven. Capability
families may proceed in parallel only after their shared Broker, authorization,
observation and provider contracts are frozen and tested.

## 8. Progress reporting

Each program and milestone uses these states:

- `NOT_STARTED`
- `IN_PROGRESS`
- `REVIEW`
- `DEVICE_VALIDATION`
- `COMPLETE`
- `BLOCKED`
- `ROLLED_BACK`

`COMPLETE` requires its stated behavior and tests, not merely source files or a
successful compile. `progress.md` remains the chronological execution log. This
roadmap remains the authoritative statement of product scope and acceptance.

## 9. Immediate next decision

The next implementation document will be the P0 Foundation Recovery plan. It
will break Lane 1 into test-first corrections with exact files, interfaces,
commands and acceptance outputs. Lane 2 begins only after the corrected
authorization and confirmation contracts are frozen.
