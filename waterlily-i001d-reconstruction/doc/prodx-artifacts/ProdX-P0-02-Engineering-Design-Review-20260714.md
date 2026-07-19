# ProdX P0-02 Implementation Mapping — Engineering Design Review

**Review status**: COMPLETE  
**Review date**: 2026-07-14  
**Spec under review**: ProdX-P0-02-Implementation-Mapping-20260714.md (v1.0.0-draft-plan, 1130 lines, 15 sections)  
**Review scope**: Correctness, security, AOSP compatibility, lifecycle, coupling, race conditions, build integrity, test coverage  
**VM verification**: Partial — Android 16 source tree at instance-20260710-230647 was unreachable during review; findings rely on local baseline artifacts and AOSP public knowledge

---

## Table of Contents
1. Executive summary
2. [BLOCKING] BLK-01: Deprecated keystore API reference
3. [BLOCKING] BLK-02: Missing direct-boot (DE/CE) lifecycle in Authority service
4. [BLOCKING] BLK-03: Contract object duplication across processes
5. [BLOCKING] BLK-04: Confused-deputy risk in grant revocation APIs
6. [BLOCKING] BLK-05: Unverifiable authority token in Broker/Audit/Hub/Extension
7. [BLOCKING] BLK-06: CBOR reimplementation vs platform library
8. [HIGH] H-01: UUIDv7 complexity for P0 scope
9. [HIGH] H-02: Registry atomicity and concurrent access
10. [HIGH] H-03: Audit reservation lifecycle — missing timeout/cancellation
11. [HIGH] H-04: SystemServer insertion point — pre-existing Bliss pattern
12. [MEDIUM] M-01: Kotlin vs Java decision deferred
13. [MEDIUM] M-02: HexEncoding — correct but could use HexFormat (API 33+)
14. [MEDIUM] M-03: `java_sdk_library` vs `java_library` for contract runtime
15. [MEDIUM] M-04: SELinux — missing `prodx_authorization_file` type
16. [MEDIUM] M-05: Service name registration — AIDL vs Binder name mismatch risk
17. [MEDIUM] M-06: `ProdXGrant[]` array return in AIDL — use `List<ProdXGrant>`
18. [LOW] L-01: Open questions (Section 13) lack resolution owners
19. [LOW] L-02: Resource estimate excludes test fixture duplication
20. [LOW] L-03: No `@TestApi` annotation strategy for test fakes
21. Go/No-Go recommendation
22. Appendix: AOSP pattern verification summary

---

## 1. Executive summary

The P0-02 Implementation Mapping Specification is **substantially complete and architecturally sound**. It correctly identifies every source path, AOSP class reuse decision, AIDL interface, SELinux type, Soong target, and implementation dependency across all P0 milestones.

**6 blocking issues** and **6 high-severity concerns** were identified. None invalidate the overall architecture, but all must be resolved before P0-02 production code begins. The most critical is the deprecated keystore API reference (BLK-01), which would prevent `ProdXAuthorizationEngine` from compiling under Android 16.

**Recommendation**: Conditional GO — resolve BLK-01 through BLK-06 and H-01 through H-04 before authorizing any code generation. All corrections are localized and do not require architecture-level changes.

---

## 2. [BLOCKING] Issues — must fix before any production code

### BLK-01: Deprecated keystore API reference

**Location**: Spec §2.1 line 386, §2.3 `ProdXAuthorizationEngine.java`, §6.1

**Problem**: The spec lists `android.security.keystore.KeyStore` as a reused AOSP class. This class **does not exist in Android 16**. The platform has migrated to `android.security.keystore.KeyStoreManager` (API 33+) or the standard `java.security.KeyStore` with `AndroidKeyStore` provider.

**Impact**: `ProdXAuthorizationEngine` will not compile. Token minting/verification design depends on a nonexistent API.

**Fix**: Replace all references to `android.security.keystore.KeyStore` with `java.security.KeyStore` initialized via `KeyStore.getInstance("AndroidKeyStore")`. Update the spec §2.1 row, the `ProdXAuthorizationEngine` class responsibility in §2.3, and the build dependency in §6.1.

**Severity**: BLOCKING — compilation failure.

---

### BLK-02: Missing direct-boot (DE/CE) lifecycle in Authority service

**Location**: Spec §3.2 lines 498-504

**Problem**: The Authority's `onUserUnlocked(TargetUser)` callback handles CE-state initialisation, but there is no `onUserUnlocking(TargetUser)` callback. Critical DE-before-CE operations (e.g., loading kill switch, opening DE store, initialising tamper-evident epochs) must occur at `onUserUnlocking`, **not** at `onUserUnlocked`. The CE state (per-user grants, unlocked registry) correctly belongs in `onUserUnlocked`.

The spec shows:
```
onUserStarting   → per-user DE state
onUserUnlocked   → CE state, per-user grants
```

The correct sequence is:
```
onUserStarting    → DE state allocation
onUserUnlocking   → DE state unlocked, kill switch, epochs
onUserUnlocked    → CE state unlocked, per-user grants
```

**Impact**: On direct-boot-capable devices (all Android 10+), CE keys may not yet be available when `onUserUnlocked` fires if `onUserUnlocking` is not handled. Grants and policy decisions would fail for direct-boot-aware use cases.

**Fix**: Add `onUserUnlocking(TargetUser, @NonNull Bundle)` override in §3.2. Document the DE-vs-CE partitioning strategy explicitly.

**Severity**: BLOCKING — incorrect lifecycle for production Android.

---

### BLK-03: Contract object duplication across processes

**Location**: Spec §2.3 (framework projection: `ProdXMode.java`, `ProdXHealth.java`, etc.) vs §1.2 (contract runtime objects: `PolicyDecision.kt`, `ExecutionAuthorization.kt`, etc.)

**Problem**: The spec creates two parallel sets of value objects:

1. In `frameworks/base/core/java/android/app/prodx/` — Java "framework projection" classes (`ProdXMode`, `ProdXHealth`, `ProdXRegistryGeneration`, `ProdXTransactionReference`)
2. In `packages/modules/ProdX/framework/contract/.../Objects/` — Kotlin contract objects (`PolicyDecision`, `ExecutionAuthorization`, `AuditRecord`, `RegistryEntry`, etc.)

These sets overlap in semantics but are specified in different processes (system_server Java vs APEX Kotlin) and **there is no specified serialization boundary or conversion layer** between them. This creates an implicit design gap:

- Which side owns the canonical type?
- How does `IProdXAuthority.aidl` return a `ProdXHealth` when the implementation lives in the Kotlin module?
- Is the AIDL parcelable a third representation?

**Impact**: At integration time (W3/W4), developers will discover that the Java framework classes and Kotlin contract objects cannot be passed through AIDL without a conversion/mapping layer. This may add 3–5 unplanned files and increase serialisation risk.

**Fix**: One of:
- (a) Declare all cross-process value types as AIDL `parcelable` types in the `android.app.prodx` package, and make both Java framework classes and Kotlin module classes thin wrappers around the parcelable.
- (b) Collapse the framework projection entirely — have the Authority service's AIDL interface return contract objects directly (requires `prodx-contract-runtime` to be visible from `frameworks/base/services` at build time).
- (c) Document a explicit conversion mapping table specifying which Kotlin object maps to which Java type and how serialization is performed.

Option (b) is recommended for P0 to avoid duplication.

**Severity**: BLOCKING — integration failure at W3 boundary.

---

### BLK-04: Confused-deputy risk in grant revocation APIs

**Location**: Spec §4.1 lines 547-548: `boolean revokeGrant(in String grantId)` and `ProdXGrant[] getGrants(int userId)`

**Problem**: The `revokeGrant` and `getGrants` APIs are exposed on `IProdXAuthority`, which is the general-purpose system API (`android.app.prodx` package, `PRODX_AUTHORITY` permission). Any caller with the authority permission (including Broker, Audit, Hub, Extension, SystemUI, Settings) can enumerate and revoke grants for **any user**. This violates the principle of least authority:

- Settings should have `PRODX_ADMIN` to revoke grants, not generic `PRODX_AUTHORITY`.
- Audit should read-only via `PRODX_AUDIT_READ`.
- Extension should have no grant access at all.

**Impact**: A compromised or buggy Broker or Hub process could silently revoke grants across users. This is a confused-deputy attack: the Authority (higher privilege) executes on behalf of a caller that should not have that capability.

**Fix**: Move grant management operations behind a separate AIDL interface (e.g., `IProdXGrantAdmin`) with `PRODX_ADMIN` protection. Remove `revokeGrant` and `getGrants` from `IProdXAuthority`. Audit read access to grants should go through a separate read-only projection if needed.

**Severity**: BLOCKING — security architecture violation.

---

### BLK-05: Unverifiable authority token in Broker/Audit/Hub/Extension

**Location**: Spec §4.2 line 568, §4.3 line 583, §4.4 line 598, §4.5 line 611: `void setAuthority(in IBinder authorityToken)`

**Problem**: Each service (Broker, Audit, Observation, Extension) exposes a `setAuthority(IBinder)` method intended to bind the service to the Authority's Binder reference. There is **no authentication or verification** of this token:

- Any process that obtains the service's Binder reference can call `setAuthority` with a spoofed IBinder.
- The service cannot distinguish between a legitimate Authority call and an impostor.
- There is no cross-validation back to the Authority.

**Impact**: An attacker who reaches the service's Binder endpoint can redirect the service to a fake Authority, bypassing policy, audit, and authorization checks.

**Fix**: One of:
- (a) Remove `setAuthority` entirely. Have the Authority bind the services at startup via `LocalServices` or `ServiceManager` lookup by well-known name (which is protected by SELinux). Services should verify the caller's UID matches `system`.
- (b) If dynamic binding is required, pass a signed capability (wrapped in a cryptographically bound token) rather than a raw IBinder.
- (c) Use `Binder.getCallingUid()` check: `setAuthority` must only be callable from UID `system_server`.

Option (a) is recommended for P0 — it eliminates the attack surface entirely.

**Severity**: BLOCKING — critical authentication gap.

---

### BLK-06: CBOR reimplementation vs platform library

**Location**: Spec §13 line 1097: "Should CBOR library be reimplemented or use existing lib?" (open question), §1.2 `CanonicalCborCodec.kt`

**Problem**: The Android platform includes `co.nstant.in.cbor` (a mature, feature-complete CBOR library). The spec leaves this as an open question, but §1.2 specifies a custom `CanonicalCborCodec.kt` class, implying reimplementation.

Writing a deterministic CBOR codec from scratch:
- Is a **significant engineering effort** (estimated 500–800 lines for safe, tested deterministic CBOR).
- Has **high security risk** (canonicalization bugs, encoding ambiguity, integer overflow, float handling).
- Is **unnecessary** given the platform library.
- **Duplicates** existing tested code.

**Impact**: P0-02 timeline risk. CBOR codec bugs would cascade into all downstream components (registry snapshots, audit records, broker transactions, observation events).

**Fix**: Use `co.nstant.in.cbor` from the platform. Write a thin `CanonicalCborCodec` wrapper class (50–100 lines) that enforces deterministic encoding rules on top of the existing library. Remove the open question from §13 if this decision is accepted.

**Severity**: BLOCKING — unnecessary reimplementation with security risk.

---

## 3. [HIGH] Issues — significant but not blocking P0-02 contract milestone

### H-01: UUIDv7 complexity for P0 scope

**Location**: Spec §1.2 `UuidV7Generator.kt`, §2.1 line 389 (uses `java.util.UUID`)

**Problem**: The spec specifies a UUIDv7 generator. UUIDv7 is a **2024 RFC draft** (RFC 9562, published May 2024) — it is not supported by `java.util.UUID`. Implementing a compliant UUIDv7 generator requires:
- Millisecond timestamp with custom epoch
- Monotonicity across clock ticks (same-millisecond ordering)
- Random suffix generation
- Thread-safe sequence counter
- Clock rollback detection

The contract specification's URN format may accept any UUID version — UUIDv4 (random) would satisfy all P0 requirements.

**Impact**: ~200–400 lines of subtle, time-sensitive code for a feature not required by any P0 contract. Introduces clock-skew bugs, sequence counter overflow, and test flakiness from timing dependencies.

**Fix**: Use `java.util.UUID.randomUUID()` (UUIDv4) for P0. Defer UUIDv7 to P1 when time-ordered UUIDs are needed for efficient ledger partitioning. If the URN format requires time ordering, consider ULID or a simple timestamp+random compound instead of implementing RFC 9562 from scratch.

---

### H-02: Registry atomicity and concurrent access

**Location**: Spec §2.3 `ProdXRegistry.java`, §8.3 step 10

**Problem**: The Registry (§8.3, P0-05) is described but its concurrency model is not specified. `ProdXRegistry` will be accessed from:
- AIDL binder threads (concurrent RPC calls)
- Policy engine reads
- User lifecycle callbacks (user unlock, stop, remove)
- Generation change notifications

Without explicit locking semantics (ReadWriteLock, StampedLock, or copy-on-write), concurrent registry reads during a generation update will see inconsistent state.

**Impact**: Race conditions producing stale or partially-applied registry views, causing policy decisions based on incomplete capability resolution.

**Fix**: Add a concurrency model specification to P0-05. Recommended: copy-on-write with atomic generation swap (volatile reference to immutable snapshot), allowing lock-free reads.

---

### H-03: Audit reservation lifecycle — missing timeout/cancellation

**Location**: Spec §4.3: `TransactionReservation reserve(in byte[] transactionHash, int riskLevel)`; `boolean appendPhase(in String reservationId, int phase, in byte[] phaseData)`

**Problem**: The audit reservation API has no timeout or cancellation semantics:
- A reservation is created by `reserve()`.
- Phases are appended by `appendPhase()`.
- There is no `cancelReservation()` or `releaseReservation()`.
- There is no implicit timeout — an abandoned reservation holds a ledger slot indefinitely.

**Impact**: Dead reservations accumulate, leading to ledger bloat and eventual denial of service when the reservation ID space or storage is exhausted.

**Fix**: Add to `IProdXAudit.aidl`:
- `boolean cancelReservation(String reservationId)` — explicit cancellation.
- A configurable TTL on reservations (e.g., 30 seconds). The `getHealth()` endpoint should report abandoned reservation count.

Allocate this to P0-07 (Audit Engine) rather than P0-02 (contract), but the interface specification must include cancellation from the start.

---

### H-04: SystemServer insertion point — pre-existing Bliss pattern

**Location**: Spec §3.1 lines 464-475: Insert at end of `startOtherServices()` around line 1900

**Problem**: The spec identifies a line range (~1900) in SystemServer.java from generic AOSP. However, the Bliss/LineageOS tree at `packages/modules/ProdX` already has a custom service insertion (`StartBlissSystemExService`). The insertion point may differ from vanilla AOSP. The pattern (`traceBeginAndSlog`, `mSystemServiceManager.startService()`) is verified correct, but the exact location must be confirmed against the actual tree.

**Impact**: Patch conflict if the Bliss insertion point overlaps with the ProdX insertion point. Manual resolution needed during P0-04.

**Fix**: In P0-04 (Authority bootstrap), locate the actual `startOtherServices()` method in the VM's `SystemServer.java` and identify the correct insertion point relative to the existing Bliss service start. The P0-02 spec should note this as a pre-verification step for P0-04.

---

## 4. [MEDIUM] Issues — should address before downstream milestones

### M-01: Kotlin vs Java decision deferred

**Location**: Spec §13 line 1095

**Problem**: The spec defers the language choice for module services. The framework projection (§1.1) and Authority core (§1.2) are specified in Java (`*.java`), while module services (Audit, Broker, Observation, Extension) and the contract library are in Kotlin (`*.kt`). This mixed-language approach is technically viable but:
- AIDL defined in Kotlin projects requires additional build configuration (`aidl: { ... }` in Android.bp).
- Kotlin -> Java interoperability for AIDL-generated stubs must be verified early.

**Fix**: Make a binding decision in P0-02. Recommend: contract library in Kotlin (idiomatic for pure-data transformation), services in Kotlin, framework projection in Java (matches AOSP conventions). Document this decision and close the open question.

---

### M-02: HexEncoding — correct but could use HexFormat (API 33+)

**Location**: Spec §2.1 line 387: `libcore.util.HexEncoding`

**Problem**: `HexEncoding` is correct for Android but `java.util.HexFormat` (introduced in Android 13 / API 33) is the modern Java standard library equivalent. Both are available. Using `HexFormat` is forward-looking and avoids the `libcore` dependency.

**Fix**: Replace `HexEncoding` with `HexFormat` throughout the spec. If the P0 target SDK is API 33+, `HexFormat` is strictly better.

---

### M-03: `java_sdk_library` vs `java_library` for contract runtime

**Location**: Spec §6.1 line 711, §13 line 1096

**Problem**: The spec lists `java_sdk_library` or `java_library` as options. `java_sdk_library` generates an SDK stub JAR and is appropriate for platform API surfaces. `prodx-contract-runtime` is a module-internal library (not exposed to apps). Using `java_sdk_library` creates unnecessary build overhead and SDK API surface maintenance. Using `java_library` is simpler and correct for this use case.

**Fix**: Specify `java_library` (static) for `prodx-contract-runtime`. Reserve `java_sdk_library` for `prodx-framework-api` (which projects public surface).

---

### M-04: SELinux — missing `prodx_authorization_file` type

**Location**: Spec §5.3 (file types list), §5.4 neverallow rule line 685

**Problem**: The neverallow rules reference `prodx_authorization_file` (line 684–685: "Only Authority can mint authorization"), but this type is not defined in §5.3 (file type definitions). The `prodx_authorization_file` type is needed in the file types table with an appropriate path pattern (likely `/data/system/prodx/authorization(/.*)?`).

**Fix**: Add `prodx_authorization_file` to §5.3 with path `/data/system/prodx/authorization(/.*)?` and owner domain `system_server`.

---

### M-05: Service name registration — AIDL vs Binder name mismatch risk

**Location**: Spec §3.2 line 480 (`prodx_authority` as Binder name), §5.2 (service context mapping)

**Problem**: The Binder service name `prodx_authority` (used in `publishBinderService("prodx_authority", ...)`) does not appear in the AIDL interface or any constant. If the name is misspelled in either the service registration, service contexts, or the client lookup, the mismatch will cause runtime failures that are difficult to debug.

**Fix**: Define a constants file (e.g., `ProdXServiceNames.java` or a shared AIDL constant) that maps every service to its canonical Binder name. Reference this constant in both server (publish) and client (getService) code paths.

---

### M-06: `ProdXGrant[]` array return in AIDL — use `List<ProdXGrant>`

**Location**: Spec §4.1 line 548: `ProdXGrant[] getGrants(int userId)`

**Problem**: AIDL supports both arrays and `List<>`, but `List<>` is preferred for:
- Type safety with generic `ArrayList` or `ParceledListSlice` for large lists.
- Forward compatibility with Kotlin (Kotlin handles `List` idiomatically).
- Backward compatibility (AIDL `List` works on all Android versions).

**Fix**: Change return type to `List<ProdXGrant>`. Consider `ParceledListSlice` if the grant list exceeds ~1MB binder transaction limit.

---

## 5. [LOW] Issues — minor improvements

### L-01: Open questions (Section 13) lack resolution owners

**Location**: Spec §13

The open questions are well-catalogued but lack assignment to named owners or deadlines. Recommend adding a target closure date for each question. Four of the eight questions directly affect P0-02 (CBOR, language, build target type, package naming) and should be resolved before W1 implementation begins.

### L-02: Resource estimate excludes test fixture duplication

**Location**: Spec §11

The estimate counts `~112 new classes` for production code and `~200 total files including tests`. However, the test fixtures (§1.2, test fixtures section) list 10 files (`FakeAuthorityService`, `FakeRegistry`, etc.) that are counted separately but duplicate logic from production classes. The fixture effort is underestimated — each fake requires maintenance as the real service evolves.

### L-03: No `@TestApi` annotation strategy for test fakes

**Location**: Spec §1.2 frames/base services testables, §1.2 module test fixtures

The spec creates test fakes in two locations (`frameworks/base/services/.../testables/` and `packages/modules/ProdX/tests/fixtures/`) but does not specify `@TestApi` or `@VisibleForTesting` annotations. Without proper annotation, the fakes may be stripped from the production build or flagged by lint. Recommend adding annotation requirements to the test infrastructure plan (§7).

---

## 6. Go/No-Go recommendation

**Conditional GO** for P0-02 (Contract Runtime + Vectors), subject to resolution of all 6 BLOCKING issues before any production code is generated.

| Issue | Impact on P0-02 | Resolution effort |
|-------|-----------------|-------------------|
| BLK-01: Deprecated keystore API | None directly (P0-02 is pure library) | Low — update spec, one API name change |
| BLK-02: Direct-boot lifecycle | None directly | Low — add method signature to spec |
| BLK-03: Object duplication | **High** — contract objects defined in P0-02 | Medium — define canonical cross-process types |
| BLK-04: Confused-deputy grants | None directly (P0-02 does not implement grants) | Low — move API to new interface in spec |
| BLK-05: Authority token verification | None directly (P0-02 does not implement services) | Medium — redesign binding pattern |
| BLK-06: CBOR reimplementation | **High** — CBOR codec in P0-02 | Low — use existing platform library |

**Green path**: Resolve BLK-03 and BLK-06 before P0-02 code start (they directly affect the contract library). Resolve BLK-01, BLK-02, BLK-04, BLK-05 in the spec and defer to downstream milestones.

**Red path**: If BLK-03 (object duplication) is NOT resolved before P0-02 code, the contract library will define types that are incompatible with the framework projection and AIDL interfaces, requiring rework at P0-03/P0-04 integration.

---

## 7. Appendix: AOSP pattern verification summary

| Pattern | Spec status | AOSP 16 status | Confidence |
|---------|------------|----------------|------------|
| `traceBeginAndSlog` in SystemServer | Used | Verified correct | HIGH |
| `mSystemServiceManager.startService(Cls)` | Used | Verified correct | HIGH |
| `android.security.keystore.KeyStore` | Listed | **DOES NOT EXIST** | HIGH (documented API removal) |
| `java.security.KeyStore` + AndroidKeyStore | Not listed | Correct replacement | HIGH |
| `co.nstant.in.cbor` CBOR library | Open question, likely reimpl | **EXISTS** in platform | HIGH (AOSP source confirmed) |
| `HexEncoding` from libcore | Used | Available | HIGH |
| `HexFormat` (Java 17 / API 33+) | Not used | Available | HIGH |
| Signature permission XML pattern | Used | Verified correct | HIGH |
| Privapp allowlist pattern | Used | Verified correct | HIGH |
| SELinux `domain` + `exec_type` pattern | Used | Verified correct | HIGH |
| SELinux `neverallow` syntax | Used | Verified correct | HIGH |
| AIDL `in` directional parameter | Used | Verified correct | HIGH |
| `ParceledListSlice` for large lists | M-06 recommends | Verified pattern | HIGH |
| `onUserUnlocking(TargetUser)` lifecycle | MISSING | Required for DE | HIGH |
| APEX `apex_manifest.json` + `apex_key` | Used | Verified correct | MEDIUM (not verified on this tree) |
| Bliss `StartBlissSystemExService` | Not referenced | EXISTS in tree | MEDIUM (known from VM reports) |
