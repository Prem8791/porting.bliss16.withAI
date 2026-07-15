# ProdX P0-02 Implementation Mapping Specification

Version: 1.1.0-draft-plan  
Date: 2026-07-14  
Status: Implementation-ready engineering mapping; no production code — BLK-01 through BLK-06 resolved  
Authority: Derived from P0 Implementation Specification (c3ef5e20), Skeleton Specification (f332bace), Contract Specification (7c774370), Foundation Archive (ee03e61c)

## Change log — v1.1.0

| Change | Section | Description | Issue resolved |
|--------|---------|-------------|----------------|
| CH-01 | §2.1 | Replaced `android.security.keystore.KeyStore` with `java.security.KeyStore` initialised via `KeyStore.getInstance("AndroidKeyStore")`. Updated all references in §2.3, §6.1. | BLK-01 |
| CH-02 | §1.2, §8.1 | Replaced custom CBOR reimplementation with platform `co.nstant.in.cbor` wrapper. `CanonicalCborCodec.kt` becomes a thin deterministic-enforcement wrapper. Removed `UuidV7Generator.kt` from Objects (UUIDv7 deferred to P1; use `java.util.UUID.randomUUID()` for P0). | BLK-06 |
| CH-03 | §1.1, §1.2, §2.3, §4 | Eliminated duplicate contract models by defining all cross-process data types as canonical AIDL `parcelable` declarations in `android.app.prodx` package. Framework projection classes become the Parcelable implementations. Module `Objects/` retains only module-internal types. AIDL interfaces use these parcelables natively. | BLK-03 |
| CH-04 | §3.2, §8.3 | Extended Authority lifecycle with `onUserUnlocking(TargetUser, Bundle)` for direct-boot DE state initialisation. Partitioned lifecycles: DE in `onUserUnlocking`, CE in `onUserUnlocked`. Updated boot-phase table. | BLK-02 |
| CH-05 | §4.1, §4.6 (new), §2.4, §5.5, §8.3, §8.4 | Moved grant management (`revokeGrant`, `getGrants`) from `IProdXAuthority` to new `IProdXGrantAdmin` interface protected by `PRODX_ADMIN` permission. Added `ProdXGrantAdminService.java` to server source tree. | BLK-04 |
| CH-06 | §4.2, §4.3, §4.4, §4.5, §5.6 (new) | Removed `setAuthority(IBinder)` from all service interfaces (Broker, Audit, Observation, Extension). Replaced with trusted ServiceManager-based service discovery. Added §5.6 authority binding strategy. | BLK-05 |

## Resolution matrix

Each blocking issue maps to the exact specification changes that resolve it:

| BLK | Spec issue | Resolution | Section changes |
|-----|-----------|------------|-----------------|
| BLK-01 | Deprecated `android.security.keystore.KeyStore` | Use `java.security.KeyStore` with `AndroidKeyStore` provider | §2.1 row, §2.3 `ProdXAuthorizationEngine` responsibility, §6.1 ProdXAuditService dep |
| BLK-02 | Missing direct-boot `onUserUnlocking()` lifecycle | Add `onUserUnlocking(TargetUser, Bundle)`, partition DE/CE init | §3.2 lifecycle methods, §3.3 boot phase table |
| BLK-03 | Duplicate contract models across framework and module | Define canonical AIDL `parcelable` types in `android.app.prodx`; remove duplicates from module `Objects/`; AIDL uses parcelables | §1.1 (add parcelables), §1.2 (restructure Objects/), §2.3 (class counts), §4 (return types), §8.1 (W1 steps) |
| BLK-04 | Confused-deputy in `revokeGrant`/`getGrants` on `IProdXAuthority` | Move to `IProdXGrantAdmin` interface with `PRODX_ADMIN` permission | §4.1 (remove methods), §4.6 (new interface), §2.4 (new AIDL), §5.5 (permission scope), §8 (waves) |
| BLK-05 | Unauthenticated `setAuthority(IBinder)` in services | Remove `setAuthority`; use ServiceManager-based discovery with UID verification | §4.2-4.5 (remove method), §5.6 (new binding strategy section) |
| BLK-06 | Custom CBOR reimplementation | Use `co.nstant.in.cbor` as thin wrapper; defer UUIDv7 to P1 | §1.2 (CanonicalCborCodec wrapper), §13 (close question), §12 (add dep criteria) |

### Remaining issues after v1.1.0

**Zero blocking issues remain.** Four high-severity findings (H-01 through H-04) from the engineering design review are acknowledged as deferred improvements:

- H-01 (UUIDv7 complexity): Deferred to P1 — P0 uses `java.util.UUID.randomUUID()`.
- H-02 (Registry atomicity): Added to P0-05 scope as a concurrency-model requirement.
- H-03 (Audit reservation timeout): Added to P0-07 scope as a TTL/cancellation requirement.
- H-04 (SystemServer insertion point): Pre-verification step added to P0-04.

---

## 0. Normative status and scope

This document is the single source of truth for *where every P0 file goes*,
*which existing AOSP class is reused*, *which AIDL interface is introduced*, and
*in what order*. It converts the frozen architecture into an exact, source-tree
level implementation map. Every path, class name, build target, SELinux type,
permission, and test fixture is named. No Kotlin/Java/C++/Rust/AIDL/SELinux
production code appears; only file names, directory roots, and declaration
patterns are specified.

Precedence remains: Foundation security intent > Contract semantics > Skeleton
placement > P0 Implementation Plan wave/milestone order > this mapping.

### 0.1 P0-02 milestone definition

Per the P0 Implementation Specification Section 7, P0-02 delivers:

- `packages/modules/ProdX/framework/` — canonical contract codec and validation library (`prodx-contract-runtime`)
- `packages/modules/ProdX/tests/contract/` — contract conformance tests (`ProdXContractTests`)
- `prodx-contract-test-vectors` — read-only schema/vector asset set
- `frameworks/base/core/java/android/app/prodx/` — canonical cross-process AIDL parcelable type declarations (prerequisite for P0-02 codec, consumed by all downstream milestones)

This mapping extends beyond P0-02 to cover the *entire P0* because downstream
path reservation is a prerequisite of P0-02: the framework library's consumers
(Audit, Broker, Observation, Extension, SDK) must have reserved paths before
their interface contracts are written.

### 0.2 Naming conventions

| Convention | Rule |
|---|---|
| Java/Kotlin source package | `com.android.server.prodx.*`, `com.android.prodx.contract.*`, `com.android.prodx.runtime.*`, `android.app.prodx.*` |
| AIDL package | `android.app.prodx.*` for framework-facing parcelables and interfaces; `com.android.prodx.runtime.*` for module-internal interfaces |
| SELinux domain | `prodx_broker`, `prodx_audit`, `prodx_observation`, `prodx_extension`, `prodx_learning`, `prodx_provider_*` |
| Soong module | `prodx-contract-runtime`, `prodx-framework-api`, `prodx-authority-service`, `ProdX*Service` |
| APEX | `com.android.prodx` |
| Signature permission | `android.permission.PRODX_*` |
| URN kind | `urn:prodx:<kind>:<authority>:<segment>...` |
| Cross-process parcelable | Declared as AIDL `parcelable` in `android.app.prodx` package; Java implementation in same directory |

---

## 1. Complete source tree allocation

Every P0 path is listed below. Paths are relative to the Android repository root
(`frameworks/base/`, `packages/modules/ProdX/`, etc.). Directories marked CREATE
must be created; marked EXISTING reference positions in the current source tree.

### 1.1 Frameworks base — Authority anchor

```
frameworks/base/
  core/java/android/app/prodx/                           [CREATE]
    ProdXManager.java                                     [CREATE class — app-facing gateway]
    ProdXMode.aidl                                        [CREATE — canonical parcelable enum declaration]
    ProdXMode.java                                        [CREATE enum implements Parcelable: DISABLED, INVENTORY_ONLY, SHADOW_POLICY, TEST_NO_OP]
    ProdXHealth.aidl                                      [CREATE — canonical parcelable declaration]
    ProdXHealth.java                                      [CREATE class implements Parcelable]
    ProdXRegistryGeneration.aidl                          [CREATE — canonical parcelable declaration]
    ProdXRegistryGeneration.java                          [CREATE class implements Parcelable]
    ProdXTransactionReference.aidl                        [CREATE — canonical parcelable declaration]
    ProdXTransactionReference.java                        [CREATE class implements Parcelable]
    ProdXExecutionContext.aidl                            [CREATE — canonical parcelable declaration]
    ProdXExecutionContext.java                            [CREATE class implements Parcelable]
    ProdXCapabilityDescriptor.aidl                        [CREATE — canonical parcelable declaration]
    ProdXCapabilityDescriptor.java                        [CREATE class implements Parcelable]
    ProdXCapabilityRequest.aidl                           [CREATE — canonical parcelable declaration]
    ProdXCapabilityRequest.java                           [CREATE class implements Parcelable]
    ProdXCapabilityResponse.aidl                          [CREATE — canonical parcelable declaration]
    ProdXCapabilityResponse.java                          [CREATE class implements Parcelable]
    ProdXCapabilityError.aidl                             [CREATE — canonical parcelable declaration]
    ProdXCapabilityError.java                             [CREATE class implements Parcelable]
    ProdXPolicyDecision.aidl                              [CREATE — canonical parcelable declaration]
    ProdXPolicyDecision.java                              [CREATE class implements Parcelable]
    ProdXExecutionAuthorization.aidl                      [CREATE — canonical parcelable declaration]
    ProdXExecutionAuthorization.java                      [CREATE class implements Parcelable]
    ProdXGrant.aidl                                       [CREATE — canonical parcelable declaration]
    ProdXGrant.java                                        [CREATE class implements Parcelable]
    ProdXRegistrySnapshot.aidl                            [CREATE — canonical parcelable declaration]
    ProdXRegistryEntry.aidl                               [CREATE — canonical parcelable declaration]
    ProdXAuditRecord.aidl                                 [CREATE — canonical parcelable declaration]
    ProdXAuditRecord.java                                 [CREATE class implements Parcelable]
    ProdXSubscriptionLease.aidl                           [CREATE — canonical parcelable declaration]
    ProdXSubscriptionLease.java                           [CREATE class implements Parcelable]
    ProdXProviderManifest.aidl                            [CREATE — canonical parcelable declaration]
    ProdXProviderManifest.java                            [CREATE class implements Parcelable]
    ProdXExtensionManifest.aidl                           [CREATE — canonical parcelable declaration]
    ProdXExtensionManifest.java                           [CREATE class implements Parcelable]

  services/core/java/com/android/server/prodx/            [CREATE]
    ProdXAuthorityService.java                            [CREATE SystemService]
    ProdXAuthorityShellCommand.java                       [CREATE Binder shell]
    ProdXRegistry.java                                    [CREATE Registry core]
    ProdXPolicyEngine.java                                [CREATE policy evaluator]
    ProdXAuthorizationEngine.java                         [CREATE token mint/verify using java.security.KeyStore + AndroidKeyStore]
    ProdXGrantStore.java                                  [CREATE per-user grants]
    ProdXGrantAdminService.java                           [CREATE — protected IProdXGrantAdmin binder; separate from general authority]
    ProdXContextBuilder.java                              [CREATE identity derivation]
    ProdXConfirmationChallenge.java                       [CREATE challenge state]
    ProdXTamperEvidentEpoch.java                          [CREATE epoch manager]
    ProdXKillSwitch.java                                  [CREATE emergency disable]
    ProdXUserLifecycle.java                               [CREATE user handler — DE/CE aware]
    ProdXComponentAttestation.java                        [CREATE component verifier]

  services/core/java/com/android/server/prodx/testables/  [CREATE]
    FakeProdXAuthorityService.java                        [CREATE test fake for unit tests]

  services/java/com/android/server/SystemServer.java     [EXISTING — one edit site]
    // Add one feature-gated start at the end of other/core services block:
    // if (context.getResources().getBoolean(R.bool.config_enableProdxAuthority))
    //    mSystemServiceManager.startService(ProdXAuthorityService.class);

  services/core/java/com/android/server/SystemService.java [EXISTING — superclass]

  core/res/res/values/config.xml                          [EXISTING — add bool]
    // <bool name="config_enableProdxAuthority">false</bool>

  packages/SystemUI/src/com/android/systemui/prodx/       [CREATE]
    ProdXConfirmationPanel.java                           [CREATE SystemUI fragment/service]
    ProdXConfirmationViewModel.java                       [CREATE ViewModel]
    ProdXIndicatorController.java                         [CREATE indicator controller]
    ProdXOperationChip.java                               [CREATE status chip]

  packages/SystemUI/tests/src/com/android/systemui/prodx/ [CREATE]
    ProdXConfirmationPanelTest.java                       [CREATE unit test]
    ProdXIndicatorControllerTest.java                     [CREATE unit test]

  data/etc/                                               [EXISTING — add privapp/signature permissions]
    com.android.prodx.privapp_allowlist.xml               [CREATE privapp allowlist]
    com.android.prodx.signature_permissions.xml           [CREATE signature permissions]

  services/tests/servicestests/src/com/android/server/prodx/ [CREATE]
    ProdXAuthorityServiceTest.java                        [CREATE lifecycle tests]
    ProdXRegistryTest.java                                [CREATE registry tests]
    ProdXPolicyEngineTest.java                            [CREATE policy tests]
    ProdXAuthorizationEngineTest.java                     [CREATE auth tests]
    ProdXGrantStoreTest.java                              [CREATE grant store tests]
    ProdXGrantAdminServiceTest.java                       [CREATE grant admin tests]
    ProdXContextBuilderTest.java                          [CREATE identity tests]
    ProdXKillSwitchTest.java                              [CREATE kill switch tests]
    ProdXTamperEvidentEpochTest.java                      [CREATE epoch tests]
```

### 1.2 ProdX Module — runtime services and libraries

```
packages/modules/ProdX/
  Android.bp                                              [EXISTING — P0-01 created phony + package]
  PROVENANCE.md                                           [EXISTING]
  TARGETS.md                                              [EXISTING — add P0-02 targets]
  MILESTONES.md                                           [EXISTING — update to IN_PROGRESS]
  DIRECTORY-MAP.md                                        [EXISTING — update]

  framework/                                              [EXISTING — add source]
    Android.bp                                            [CREATE — prodx-contract-runtime java_library]
    contract/
      README.md                                           [EXISTING]
      src/com/android/prodx/contract/
        CanonicalCborCodec.kt                             [CREATE — deterministic CBOR wrapper over platform co.nstant.in.cbor library]
        CanonicalJsonProjection.kt                        [CREATE — diagnostic JSON]
        SchemaValidator.kt                                [CREATE — schema validation]
        SchemaRegistry.kt                                 [CREATE — in-process schema map]
        ContractEnvelope.kt                               [CREATE — envelope object]
        ContractVersion.kt                                [CREATE — version handling]
        IdentifierGrammar.kt                              [CREATE — URN parser/validator]
        ContentHash.kt                                    [CREATE — hash verification using java.security.MessageDigest]
        CompatibilityResolver.kt                          [CREATE — forward/back compat]
        TypedError.kt                                     [CREATE — structured error]
        Objects/
          ObservationRecord.kt                            [CREATE — module-internal, not cross-process]
          EventRecord.kt                                  [CREATE — module-internal, not cross-process]
          ExtensionManifest.kt                            [CREATE — module-internal, not cross-process]
          // Cross-process data types migrated to android.app.prodx AIDL parcelables (§1.1)
          // Contract library references them via prodx-framework-api dependency.
    api/
      README.md                                           [EXISTING]

  service/
    README.md                                             [EXISTING]
    audit/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXAuditService APK target]
      src/com/android/prodx/runtime/audit/
        AuditService.kt                                   [CREATE — dedicated service; UID-verified ServiceManager binding]
        AppendOnlyLedger.kt                              [CREATE — durable journal]
        TransactionReservation.kt                         [CREATE — reserve semantics with TTL/cancellation]
        LedgerPartitionManager.kt                         [CREATE — DE/CE partitioning]
        LedgerHashChain.kt                                [CREATE — tamper-evident chain]
        RecoveryJournal.kt                                [CREATE — crash recovery]
        PrivacyRedactor.kt                                [CREATE — field minimization]
        RetentionManager.kt                               [CREATE — retention/tombstone]
        AuditHealth.kt                                    [CREATE — health projection]
        AuditShellCommand.kt                              [CREATE — diagnostic shell]
    broker/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXBrokerService APK target]
      src/com/android/prodx/runtime/broker/
        BrokerService.kt                                  [CREATE — dedicated service; UID-verified ServiceManager binding]
        TransactionStateMachine.kt                       [CREATE — request lifecycle]
        TransactionPhase.kt                               [CREATE — state enum]
        ProposalValidator.kt                              [CREATE — request validation]
        DependencyResolver.kt                             [CREATE — dependency expansion]
        ConfirmationCoordinator.kt                        [CREATE — UI coordination]
        ProviderDispatcher.kt                             [CREATE — dispatch to provider]
        AuthorizationRevalidator.kt                       [CREATE — token recheck]
        BrokerCheckpointStore.kt                          [CREATE — crash checkpoint]
        BrokerHealth.kt                                   [CREATE — health projection]
        BrokerShellCommand.kt                             [CREATE — diagnostic shell]
    observation/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXObservationService APK target]
      src/com/android/prodx/runtime/observation/
        ObservationService.kt                             [CREATE — dedicated service; UID-verified ServiceManager binding]
        LeaseManager.kt                                   [CREATE — subscription leases]
        SourceRegistry.kt                                 [CREATE — admitted sources]
        EventPipeline.kt                                  [CREATE — event processing]
        ObservationQueue.kt                               [CREATE — bounded queue]
        BackpressureController.kt                         [CREATE — rate/burst control]
        RedactionPipeline.kt                              [CREATE — minimization]
        ConsumerDeliveryManager.kt                        [CREATE — delivery ack]
        HubHealth.kt                                      [CREATE — health projection]
        HubShellCommand.kt                                [CREATE — diagnostic shell]
    extension/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXExtensionService APK target]
      src/com/android/prodx/runtime/extension/
        ExtensionService.kt                               [CREATE — on-demand service; UID-verified ServiceManager binding]
        ManifestParser.kt                                 [CREATE — bounded manifest parser]
        SchemaVerifier.kt                                 [CREATE — schema hash check]
        SigningLineageVerifier.kt                         [CREATE — signing chain check]
        CandidateReport.kt                                [CREATE — attested report]
        QuarantineStore.kt                                [CREATE — cache/quarantine]
        ExtensionHealth.kt                                [CREATE — health projection]
        ExtensionShellCommand.kt                          [CREATE — diagnostic shell]
    learning/                                             [P0 placeholder — no production code]
      README.md                                           [EXISTING]
    reasoning/                                            [P0 placeholder — no production code]
      README.md                                           [EXISTING]

  sdk/
    README.md                                             [EXISTING]
    Android.bp                                            [CREATE — prodx-provider-sdk library]
    src/com/android/prodx/sdk/
      ProviderContext.kt                                  [CREATE — provider-side context]
      AuthorizationVerifier.kt                            [CREATE — verify token]
      ProviderLifecycle.kt                                [CREATE — lifecycle contract]
      StructuredError.kt                                  [CREATE — error conventions]
      HealthReporter.kt                                   [CREATE — health conventions]
      AuditCorrelationHelper.kt                           [CREATE — audit correlation]
      CancellationToken.kt                                [CREATE — cancellation]

  providers/
    README.md                                             [EXISTING]
    test/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXNoOpTestProvider APK]
      src/com/android/prodx/provider/test/
        NoOpTestProviderService.kt                        [CREATE — test provider]
        NoOpManifest.kt                                   [CREATE — test descriptor]
        NoOpCapability.kt                                 [CREATE — test capability]
        SyntheticObservationSource.kt                     [CREATE — test observation source]

  apex/
    README.md                                             [EXISTING]
    Android.bp                                            [CREATE — com.android.prodx APEX target]
    apex_manifest.json                                    [CREATE — APEX manifest]
    prebuilts/                                            [later — prebuilts if needed]

  sepolicy/
    README.md                                             [EXISTING]
    prodx_module.te                                       [CREATE — module policy fragments]
    prodx_broker.te                                       [CREATE — domain definition]
    prodx_audit.te                                        [CREATE — domain definition]
    prodx_observation.te                                  [CREATE — domain definition]
    prodx_extension.te                                    [CREATE — domain definition]
    prodx_file_types.te                                   [CREATE — file type definitions]
    prodx_service_contexts                                [CREATE — service contexts]

  tests/
    README.md                                             [EXISTING]
    contract/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXContractTests host/device]
      src/com/android/prodx/tests/contract/
        PositiveVectorTests.kt                            [CREATE — canonical goldens]
        NegativeVectorTests.kt                            [CREATE — rejection cases]
        RoundTripTests.kt                                 [CREATE — encode/decode]
        CompatibilityTests.kt                             [CREATE — version compat]
        FuzzBoundaryTests.kt                              [CREATE — depth/count/bounds]
        GoldenHashTests.kt                                [CREATE — hash verification]
        SchemaValidationTests.kt                          [CREATE — schema validator]
        ErrorMappingTests.kt                              [CREATE — error mapping]
    unit/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXUnitTests]
    integration/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXP0IntegrationTests]
    security/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — ProdXP0SecurityTests]
    fuzz/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — prodx_contract_fuzzer and others]
    host/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — host-side contract tests]
    device/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — device-side P0 tests]
    fixtures/
      README.md                                           [EXISTING]
      Android.bp                                          [CREATE — shared test fixtures]
      src/com/android/prodx/tests/fixtures/
        FakeAuthorityService.kt                           [CREATE — fake for tests]
        FakeRegistry.kt                                   [CREATE — fake registry]
        FakePolicyEngine.kt                               [CREATE — fake policy]
        FakeAuditService.kt                               [CREATE — fake audit]
        FakeBrokerService.kt                              [CREATE — fake broker]
        FakeObservationHub.kt                             [CREATE — fake hub]
        TestVectors.kt                                    [CREATE — shared vectors]

  config/
    README.md                                             [EXISTING]
  tools/
    README.md                                             [EXISTING]
```

### 1.3 System SEPolicy — core ABI

```
system/sepolicy/
  public/
    prodx_service.te                                      [CREATE — service type declarations]
    prodx_attribute.te                                    [CREATE — attribute declarations]
  private/
    prodx_compat.te                                       [CREATE — neverallow/compat rules]
```

### 1.4 Settings — administration surfaces

```
packages/apps/Settings/
  src/com/android/settings/prodx/                         [CREATE]
    ProdXSettingsDashboardFragment.java                   [CREATE — main dashboard]
    ProdXKillSwitchPreferenceController.java              [CREATE — kill switch]
    ProdXGrantsFragment.java                              [CREATE — grants list]
    ProdXGrantDetailFragment.java                         [CREATE — grant detail]
    ProdXHistoryFragment.java                             [CREATE — audit history]
    ProdXProviderHealthFragment.java                      [CREATE — provider health]
    ProdXExtensionQuarantineFragment.java                 [CREATE — quarantine list]
    ProdXSettingsShellCommand.java                        [CREATE — diagnostic shell]
  tests/unit/src/com/android/settings/prodx/              [CREATE]
    ProdXSettingsFragmentTest.java                        [CREATE — fragment test]
```

### 1.5 Immutable evidence archive (P0-00 preserved)

```
packages/modules/ProdX/tests/reference/
  immutable/                                              [EXISTING — preserved from P0-00]
    ProdX-Runtime-Architecture-Foundation-20260714.zip
    ProdX-Runtime-Contract-Specification-20260714.md
    ProdX-Runtime-Skeleton-Specification-20260714.md
    ProdX-P0-Runtime-Implementation-Specification-20260714.md
  BASELINE-FILES.sha256                                   [EXISTING — verification]
  BASELINE-MANIFEST.json                                  [EXISTING]
  ENGINEERING-BASELINE.md                                 [EXISTING]
  ... (remaining P0-00 files preserved)
```

---

## 2. Existing AOSP class reuse, extension, and creation

### 2.1 Classes reused unchanged

| AOSP class | Package | Used by | Purpose |
|---|---|---|---|
| `com.android.server.SystemService` | `frameworks/base/services/core` | `ProdXAuthorityService` | Base lifecycle for Authority in system_server |
| `com.android.server.SystemServiceManager` | `frameworks/base/services/core` | `SystemServer.java` | Start/stop Authority |
| `android.os.Binder` | `frameworks/base/core` | All services | Binder IPC base |
| `android.os.IBinder` | `frameworks/base/core` | All service interfaces | Binder interface |
| `android.os.IInterface` | `frameworks/base/core` | All AIDL interfaces | Binder interface base |
| `android.content.Context` | `frameworks/base/core` | All services | Android context |
| `android.content.pm.PackageManager` | `frameworks/base/core` | `ProdXContextBuilder`, `ProdXComponentAttestation` | Package identity/signing |
| `android.os.UserManager` | `frameworks/base/core` | `ProdXUserLifecycle`, `ProdXContextBuilder` | User lifecycle |
| `android.permission.PermissionManager` | `frameworks/base/core` | `ProdXPolicyEngine` | Permission state |
| `android.app.AppOpsManager` | `frameworks/base/core` | `ProdXPolicyEngine` | AppOps state |
| `android.app.role.RoleManager` | `frameworks/base/core` | `ProdXPolicyEngine` | Role state |
| `android.app.admin.DevicePolicyManager` | `frameworks/base/core` | `ProdXPolicyEngine` | Policy state |
| `android.os.PowerManager` | `frameworks/base/core` | Health reporting | Power state |
| `android.os.Process` | `frameworks/base/core` | `ProdXContextBuilder` | UID/PID derivation |
| `android.os.UserHandle` | `frameworks/base/core` | `ProdXUserLifecycle` | User identity |
| `java.security.KeyStore` | platform | `ProdXAuthorizationEngine` | Key operations via `KeyStore.getInstance("AndroidKeyStore")` |
| `com.android.internal.util.Preconditions` | `frameworks/base/core` | All code | Argument validation |
| `java.security.MessageDigest` | platform | `ContentHash` | SHA-256 |
| `java.util.UUID` | platform | Contract URN generation | UUIDv4 via `UUID.randomUUID()` |
| `org.json.JSONObject` / `JSONArray` | platform | `CanonicalJsonProjection` | Diagnostic JSON |
| `co.nstant.in.cbor.CborEncoder` / `CborDecoder` | platform (external/cbor) | `CanonicalCborCodec` | Deterministic CBOR encode/decode (thin wrapper) |
| `com.android.internal.logging.MetricsLogger` | `frameworks/base/core` | Diagnostics | Metrics |
| `com.android.server.LocalServices` | `frameworks/base/services` | Authority internals | Internal service registry |

### 2.2 AOSP classes requiring extension

| AOSP class | Extension | Changes | Milestone |
|---|---|---|---|
| `com.android.server.SystemServer.java` | Add `ProdXAuthorityService` start | Add `< 10` lines in `startOtherServices()` | P0-04 |
| `android.os.ServiceManager` | Accept `prodx_*` service names | No change — existing registration API | N/A |
| `frameworks/base/core/res/res/values/config.xml` | Add `config_enableProdxAuthority` bool | One `<bool>` element | P0-04 |
| `com.android.server.pm.PackageManagerService` | Consume for package/signing facts | No change — consume existing API | N/A |
| `co.nstant.in.cbor` | Deterministic CBOR | No change — thin wrapper enforces canonicalization rules | P0-02 |

### 2.3 New classes to create (complete list)

**Total new classes across all P0 milestones: ~135 files** (excluding placeholder READMEs).

The increase from v1.0.0 reflects:
- Canonical AIDL parcelable type declarations in `android.app.prodx` (~20 new .aidl + .java pairs, replacing ~12 standalone framework projection classes and ~13 module Objects duplicates — net +16)
- `ProdXGrantAdminService.java` in server (+1)
- Removed `UuidV7Generator.kt` from contract module (deferred to P1; −1)

Breakdown by subsystem:

| Subsystem | File count | Milestone |
|---|---|---|
| Contract codec/vectors | 15 | P0-02 |
| Canonical AIDL parcelable types | ~20 (incl. .aidl + .java pairs) | P0-02/P0-03 split |
| Framework client projection (thin wrappers) | 1 (ProdXManager) | P0-03 |
| Authority core | 17 (+1 for ProdXGrantAdminService) | P0-04, P0-05, P0-06 |
| Audit service | 11 | P0-07 |
| Broker service | 11 | P0-09 |
| Observation service | 9 | P0-11 |
| Extension service | 8 | P0-12 |
| Provider SDK | 7 | P0-10 |
| No-op test provider | 4 | P0-10 |
| SystemUI integration | 4 | P0-08 |
| Settings integration | 7 | P0-08 |
| SEPolicy types | 7 | P0-13 |
| Test fixtures | 7 | Parallel |
| **Total** | **~135** | (includes .aidl declarations as separate files) |

### 2.4 AOSP AIDL interfaces to create

| Interface file | Package | Direction | Consumers | Purpose |
|---|---|---|---|---|
| `IProdXAuthority.aidl` | `android.app.prodx` | APK -> system_server | Framework clients, Broker, Audit, Hub, Extension, UI | Authority control: health, identity, resolve, policy, authorization |
| `IProdXGrantAdmin.aidl` | `android.app.prodx` | Settings -> Authority | Settings, privileged admin callers | Grant management: revoke, list, detail (protected by `PRODX_ADMIN`) |
| `IProdXRegistryObserver.aidl` | `android.app.prodx` | system_server -> APK | Broker, Settings | Registry change notification |
| `IProdXBroker.aidl` | `com.android.prodx.runtime` | Authority -> broker | Authority, test proposer | Transaction lifecycle (no setAuthority — uses ServiceManager binding) |
| `IProdXAudit.aidl` | `com.android.prodx.runtime` | Authority/broker -> audit | Authority, Broker, Settings | Ledger operations (no setAuthority — uses ServiceManager binding) |
| `IProdXObservation.aidl` | `com.android.prodx.runtime` | Authority/broker -> hub | Authority, Broker, provider | Observation lifecycle (no setAuthority — uses ServiceManager binding) |
| `IProdXSourceAdapter.aidl` | `com.android.prodx.runtime` | Provider -> hub | Source providers | Source registration |
| `IProdXExtension.aidl` | `com.android.prodx.runtime` | Authority -> extension | Authority | Extension validation (no setAuthority — uses ServiceManager binding) |
| `IProdXConfirmationCallback.aidl` | `android.app.prodx` | Authority -> SystemUI | Authority, SystemUI | Confirmation challenge/proof |
| `IProdXSettingsMediator.aidl` | `android.app.prodx` | Settings -> Authority | Settings | Admin operations |
| `IProdXProvider.aidl` | `com.android.prodx.sdk` | Broker -> provider | Broker, provider | Provider dispatch |
| `IProdXProviderHealthCallback.aidl` | `com.android.prodx.sdk` | Provider -> Registry | Provider | Health reporting |

All AIDL interfaces use `@VendorPrivacy`, `@PlatformApi`, or `@SystemApi` as
appropriate. No `@UnsupportedAppUsage` or public API until P0 completion review.

### 2.5 Existing Bliss/LineageOS classes that remain untouched

| Class/package | Reason for non-participation in P0 |
|---|---|
| `lineageos.hardware.LineageHardwareManager` | P0 has no real provider — ROM capability begins in P1 |
| `org.lineageos.platform.internal.LineageSystemServer` | Not modified — ROM services remain independent |
| `vendor.lineage.touch.*` | Device-specific — not part of ProdX runtime architecture |
| `com.android.internal.bliss.hardware.*` | Old API surface — not used in ProdX |
| `vendor/bliss/build/tasks/*` | Build system — not modified by ProdX |
| `device/asus/*`, `vendor/asus/*` | Device-specific — no P0 dependency |

---

## 3. SystemServer integration plan

### 3.1 ProdXAuthorityService registration

File: `frameworks/base/services/java/com/android/server/SystemServer.java`

Insertion point: End of `startOtherServices()` (pre-verified against the actual Bliss tree during P0-04). Feature-gated by config bool:

```java
// ProdX Authority Service (feature-gated, disabled by default)
if (context.getResources().getBoolean(
        R.bool.config_enableProdxAuthority)) {
    traceBeginAndSlog("StartProdXAuthority");
    mSystemServiceManager.startService(ProdXAuthorityService.class);
    traceEnd();
}
```

### 3.2 ProdXAuthorityService lifecycle

Extends `SystemService`. Publishes `IProdXAuthority` Binder under the name
`prodx_authority`.

```java
public class ProdXAuthorityService extends SystemService {
    @Override
    public void onStart() {
        publishBinderService("prodx_authority", mImpl);
        // Non-ready — no bindings, no policy
    }

    @Override
    public void onBootPhase(int phase) {
        // PHASE_LOCK_SETTINGS_READY: load kill switch, durable epochs
        // PHASE_SYSTEM_SERVICES_READY: verify APEX/contract baseline
        // PHASE_ACTIVITY_MANAGER_READY: open DE store, load built-in catalog
        // PHASE_THIRD_PARTY_APPS_CAN_START: bind Broker (via ServiceManager),
        //   bind Observation Hub, validate extensions
        // PHASE_BOOT_COMPLETED: enable direct-boot-safe capabilities
    }

    @Override
    public void onUserStarting(TargetUser user) { /* allocate DE state */ }

    @Override
    public void onUserUnlocking(TargetUser user, @NonNull Bundle unlockedBundle) {
        /* DE state unlocked: load kill switch, tamper-evident epochs,
           open DE store for this user, initialise DE ledger partition */
    }

    @Override
    public void onUserUnlocked(TargetUser user) {
        /* CE state available: per-user grants, unlocked registry,
           CE ledger partition */
    }

    @Override
    public void onUserStopping(TargetUser user) { /* revoke/drain */ }

    @Override
    public void onUserRemoved(TargetUser user) { /* destroy all state */ }
}
```

### 3.3 Boot phase dependency ordering

| SystemService phase | Authority action | Blocking prerequisites |
|---|---|---|
| `onStart()` | Publish non-ready Binder service | None |
| `PHASE_WAIT_FOR_DEFAULT_DISPLAY` | No action | None |
| `PHASE_LOCK_SETTINGS_READY` | Load kill switch, durable epochs | LockSettings |
| `PHASE_SYSTEM_SERVICES_READY` | Verify APEX/contract baseline, discover module services | PackageManager |
| `PHASE_ACTIVITY_MANAGER_READY` | Open DE store, load built-in catalog, bind Audit service | Audit service, UserManager |
| `PHASE_THIRD_PARTY_APPS_CAN_START` | Bind Broker, bind Observation Hub, validate extensions | ActivityManager |
| `PHASE_BOOT_COMPLETED` | Enable direct-boot-safe capabilities | Boot complete |

**User lifecycle — Direct Boot (DE/CE) partitioning:**

| User event | Authority action | Storage partition |
|---|---|---|
| `onUserStarting` | Allocate per-user DE state directory | DE (already unlocked) |
| `onUserUnlocking` | Load kill switch per user, open DE store, initialise epochs | DE (first access) |
| `onUserUnlocked` | Open CE store, load per-user grants, unlock CE registry | CE (keys now available) |
| `onUserStopping` | Flush DE state, revoke in-progress grants | DE |
| `onUserRemoved` | Destroy all per-user DE and CE state | Both |

---

## 4. Binder/AIDL interface specifications

### 4.1 IProdXAuthority.aidl

All return types refer to canonical AIDL `parcelable` types declared in
`android.app.prodx` (§1.1).

```
package android.app.prodx;

interface IProdXAuthority {
    // Health
    ProdXMode getMode();
    ProdXHealth getHealth();

    // Context derivation
    ProdXExecutionContext deriveCallerContext(in String purpose);

    // Registry
    ProdXRegistryGeneration getRegistryGeneration();
    ProdXRegistrySnapshot getRegistrySnapshot(long generationId);
    boolean resolveCapability(in ProdXCapabilityDescriptor descriptor);

    // Policy (shadow only in P0)
    ProdXPolicyDecision evaluatePolicy(in ProdXExecutionContext context, in ProdXCapabilityRequest request);
    ProdXExecutionAuthorization mintAuthorization(in ProdXPolicyDecision decision, in byte[] proof);

    // Kill switch
    void emergencyDisable();
    boolean isEmergencyDisabled();

    // Mode control
    void setMode(ProdXMode mode);
}
```

Note: Grant management (`revokeGrant`, `getGrants`) has been removed from this
interface to eliminate confused-deputy attack surface. Grant operations are
available exclusively through `IProdXGrantAdmin` (§4.6), protected by the
`PRODX_ADMIN` signature permission.

### 4.2 IProdXBroker.aidl

```
package com.android.prodx.runtime;

interface IProdXBroker {
    String submitTransaction(in ProdXCapabilityRequest request);
    void cancelTransaction(String transactionId);
    TransactionStatus getTransactionStatus(String transactionId);
    // Authority binding is NOT performed via setAuthority(IBinder).
    // See §5.6 (Authority Binding Strategy) for service discovery.
}
```

### 4.3 IProdXAudit.aidl

```
package com.android.prodx.runtime;

interface IProdXAudit {
    TransactionReservation reserve(in byte[] transactionHash, int riskLevel);
    boolean appendPhase(in String reservationId, int phase, in byte[] phaseData);
    boolean appendOutcome(in String reservationId, in byte[] outcomeData);
    boolean cancelReservation(String reservationId);
    LedgerHealth getHealth();
    List<ProdXAuditRecord> queryHistory(int userId, long sinceTimestamp);
    // Authority binding is NOT performed via setAuthority(IBinder).
    // See §5.6 (Authority Binding Strategy) for service discovery.
}
```

### 4.4 IProdXObservation.aidl

```
package com.android.prodx.runtime;

interface IProdXObservation {
    ProdXSubscriptionLease createLease(in LeaseSpec spec);
    boolean revokeLease(String leaseId);
    boolean registerSource(in IProdXSourceAdapter source);
    boolean unregisterSource(String sourceId);
    HubHealth getHealth();
    // Authority binding is NOT performed via setAuthority(IBinder).
    // See §5.6 (Authority Binding Strategy) for service discovery.
}
```

### 4.5 IProdXExtension.aidl

```
package com.android.prodx.runtime;

interface IProdXExtension {
    CandidateReport validateCandidate(in byte[] sealedCandidate);
    void cancelValidation(String candidateId);
    ExtensionHealth getHealth();
    // Authority binding is NOT performed via setAuthority(IBinder).
    // See §5.6 (Authority Binding Strategy) for service discovery.
}
```

### 4.6 IProdXGrantAdmin.aidl (NEW)

Separate interface protected by `PRODX_ADMIN` permission, preventing
confused-deputy access from components that only hold `PRODX_AUTHORITY`.

```
package android.app.prodx;

interface IProdXGrantAdmin {
    List<ProdXGrant> getGrants(int userId);
    ProdXGrant getGrant(String grantId);
    boolean revokeGrant(in String grantId);
    boolean suspendGrant(in String grantId);
    List<ProdXGrant> getGrantsByPackage(String packageName, int userId);
}
```

This interface is served by `ProdXGrantAdminService` (§1.1), a dedicated system_service
component that enforces `PRODX_ADMIN` at the Binder level via `checkCallingPermission()`.

### 4.7 Remaining interfaces

`IProdXConfirmationCallback`, `IProdXSettingsMediator`, `IProdXProvider`,
`IProdXSourceAdapter`, `IProdXProviderHealthCallback`, and
`IProdXRegistryObserver` follow the same pattern.

---

## 5. SELinux policy map

### 5.1 Domain definitions

Every domain uses `enforcing`, `default-deny`.

| SELinux type | Purpose | Parent domain | Unique UID |
|---|---|---|---|
| `prodx_broker` | Broker orchestration process | `domain` | Yes |
| `prodx_audit` | Audit engine process | `domain` | Yes |
| `prodx_observation` | Observation Hub process | `domain` | Yes |
| `prodx_extension` | Extension quarantine process | `domain` | Yes |
| `prodx_authority_service` | Authority service label | `system_service` | No (system) |

### 5.2 Service contexts

```
# system/sepolicy/private/service_contexts
prodx_authority              u:object_r:prodx_authority_service:s0
prodx_grant_admin            u:object_r:prodx_authority_service:s0
prodx_broker                 u:object_r:prodx_broker_service:s0
prodx_audit                  u:object_r:prodx_audit_service:s0
prodx_observation            u:object_r:prodx_observation_service:s0
prodx_extension              u:object_r:prodx_extension_service:s0
```

Note: `prodx_grant_admin` shares the `prodx_authority_service` context as it
runs in the same system_server process.

### 5.3 File type definitions

| Type | Path pattern | Owner domain |
|---|---|---|
| `prodx_registry_file` | `/data/system/prodx/registry(/.*)?` | `system_server` |
| `prodx_authorization_file` | `/data/system/prodx/authorization(/.*)?` | `system_server` |
| `prodx_audit_de_file` | `/data/misc/prodx/audit/de(/.*)?` | `prodx_audit` |
| `prodx_audit_ce_file` | `/data/misc/prodx/audit/ce(/.*)?` | `prodx_audit` |
| `prodx_broker_checkpoint_file` | `/data/misc/prodx/broker(/.*)?` | `prodx_broker` |
| `prodx_observation_queue_file` | `/data/misc/prodx/observation(/.*)?` | `prodx_observation` |
| `prodx_extension_cache_file` | `/data/misc/prodx/extension(/.*)?` | `prodx_extension` |

### 5.4 Critical neverallow rules

```cil
# No ProdX domain may access device nodes
neverallow { prodx_broker prodx_audit prodx_observation prodx_extension }
    dev_type:chr_file { read write open ioctl };

# No ProdX domain may access HAL interfaces directly
neverallow { prodx_broker prodx_audit prodx_observation prodx_extension }
    hal_*:service { find call };

# No ProdX domain except authority may access system services
neverallow { prodx_broker prodx_audit prodx_observation prodx_extension }
    system_service:service { find call add };

# Broker cannot access registry storage directly
neverallow prodx_broker prodx_registry_file:* *;

# Audit cannot access observation queues
neverallow prodx_audit prodx_observation_queue_file:* *;

# No token/authorization in observation records
neverallow prodx_observation prodx_authorization_file:* *;

# Only Authority can mint and store authorization
neverallow { prodx_broker prodx_audit prodx_observation prodx_extension }
    self: { create write } prodx_authorization_file;
```

### 5.5 Permission/signing requirements

| Permission | Level | Granted to | Purpose |
|---|---|---|---|
| `android.permission.PRODX_AUTHORITY` | signature/privileged | ProdXAuthorityService, SystemUI, Settings, Broker, Audit, Hub, Extension | Authority endpoint access |
| `android.permission.PRODX_BROKER` | signature | BrokerService, Authority | Broker endpoint access |
| `android.permission.PRODX_AUDIT_READ` | signature/privileged | Settings | Audit history read |
| `android.permission.PRODX_ADMIN` | signature/privileged | Settings, ProdXGrantAdminService | Grant management and admin operations |
| `android.permission.PRODX_CONFIRMATION` | signature | SystemUI | Confirmation handler |

All permissions are declared in `frameworks/base/data/etc/com.android.prodx.signature_permissions.xml`
with `signature` protection level. The privapp allowlist is at
`frameworks/base/data/etc/com.android.prodx.privapp_allowlist.xml`.

The `PRODX_ADMIN` permission guards both `IProdXGrantAdmin` (grant management)
and `IProdXSettingsMediator` (Settings admin operations), ensuring no component
holding only `PRODX_AUTHORITY` can enumerate or revoke grants.

### 5.6 Authority binding strategy (NEW)

All module services (Broker, Audit, Observation, Extension) bind to the
Authority through the following mechanism, eliminating the `setAuthority(IBinder)`
vulnerability:

1. **Service registration**: Each module service publishes its Binder under a
   well-known name (`prodx_broker`, `prodx_audit`, etc.) via
   `ServiceManager.addService()` or `publishBinderService()` at startup.

2. **Authority discovery**: The Authority, running in system_server, discovers
   module services via `ServiceManager.getService("prodx_*")` during
   `onBootPhase(PHASE_ACTIVITY_MANAGER_READY)` and later phases.

3. **Caller verification**: Every module service's AIDL implementation verifies
   that `Binder.getCallingUid()` matches `android.os.Process.SYSTEM_UID` before
   accepting commands. This prevents any third-party process from impersonating
   the Authority.

4. **Mutual authentication**: The Authority verifies that the returned Binder
   is the expected service by checking the service's SELinux domain and package
   signature. This prevents a malicious process from registering a spoofed
   service.

5. **No dynamic re-binding**: Service endpoints are resolved at boot and are not
   reassigned after startup. If a service dies, the Authority may retry the
   `ServiceManager` lookup; if the service does not return, the Authority marks
   the subsystem as degraded.

---

## 6. Soong/build integration plan

### 6.1 Build targets to create

| Target | Type | Location | Dependencies |
|---|---|---|---|
| `prodx-contract-test-vectors` | `prebuilt_etc` / `filegroup` | `packages/modules/ProdX/framework/` | None beyond data tooling |
| `prodx-contract-runtime` | `java_library` | `packages/modules/ProdX/framework/` | `prodx-contract-test-vectors`, `prodx-framework-api` (for canonical parcelable types), `co.nstant.in.cbor` (platform CBOR), platform SDK |
| `prodx-framework-api` | `java_sdk_library` (system/private) | `frameworks/base/` | Platform SDK, canonical parcelable type sources in `android.app.prodx` |
| `prodx-authority-service` | `java_library` linked into `services` | `frameworks/base/services/` | `prodx-framework-api`, platform service deps |
| `ProdXAuditService` | `android_app` (privileged) | `packages/modules/ProdX/service/audit/` | `prodx-contract-runtime`, `prodx-framework-api` |
| `ProdXBrokerService` | `android_app` (privileged) | `packages/modules/ProdX/service/broker/` | `prodx-contract-runtime`, `prodx-framework-api` |
| `ProdXObservationService` | `android_app` (privileged) | `packages/modules/ProdX/service/observation/` | `prodx-contract-runtime`, `prodx-framework-api` |
| `ProdXExtensionService` | `android_app` (privileged) | `packages/modules/ProdX/service/extension/` | `prodx-contract-runtime`, `prodx-framework-api` |
| `prodx-provider-sdk` | `java_library` | `packages/modules/ProdX/sdk/` | `prodx-contract-runtime`, `prodx-framework-api`, stable framework API |
| `ProdXNoOpTestProvider` | `android_app` (test only) | `packages/modules/ProdX/providers/test/` | `prodx-provider-sdk` |
| `com.android.prodx` | `apex` | `packages/modules/ProdX/apex/` | All runtime APKs, contract libs |
| `ProdXFrameworkServiceTests` | `android_test` | `frameworks/base/services/tests/` | `prodx-authority-service`, test fixtures |
| `ProdXContractTests` | `android_test` (host/device) | `packages/modules/ProdX/tests/contract/` | `prodx-contract-runtime`, test vectors |
| `ProdXBrokerTests` | `android_test` | `packages/modules/ProdX/tests/unit/` | `ProdXBrokerService`, test fixtures |
| `ProdXAuditTests` | `android_test` | `packages/modules/ProdX/tests/unit/` | `ProdXAuditService`, test fixtures |
| `ProdXObservationTests` | `android_test` | `packages/modules/ProdX/tests/unit/` | `ProdXObservationService`, test fixtures |
| `ProdXExtensionTests` | `android_test` | `packages/modules/ProdX/tests/unit/` | `ProdXExtensionService`, test fixtures |
| `ProdXProviderSdkTests` | `android_test` | `packages/modules/ProdX/tests/unit/` | `prodx-provider-sdk`, test provider |
| `ProdXSystemUITests` | `android_test` | `frameworks/base/packages/SystemUI/tests/` | SystemUI, mock Authority |
| `ProdXSettingsTests` | `android_test` | `packages/apps/Settings/tests/` | Settings, mock Authority/Audit |
| `ProdXP0IntegrationTests` | `android_test` | `packages/modules/ProdX/tests/integration/` | All P0 targets |
| `ProdXP0SecurityTests` | `android_test` | `packages/modules/ProdX/tests/security/` | All P0 targets |
| `prodx_contract_fuzzer` | `cc_fuzz` / `java_fuzz` | `packages/modules/ProdX/tests/fuzz/` | `prodx-contract-runtime` |
| `prodx-p0` | `phony` | `packages/modules/ProdX/` | All P0 production targets and required tests |

### 6.2 Visibility rules

Every P0 target uses `visibility: ["//visibility:private"]` except:

| Target | Visibility |
|---|---|
| `prodx-contract-runtime` | `["//packages/modules/ProdX:__subpackages__", "//frameworks/base:__subpackages__"]` |
| `prodx-framework-api` | `["//packages/modules/ProdX:__subpackages__"]` |
| `prodx-provider-sdk` | `["//packages/modules/ProdX:__subpackages__"]` |
| `prodx-p0` | `["//visibility:public"]` (build gate only) |
| `com.android.prodx` | `["//visibility:public"]` |

### 6.3 Product inclusion

```makefile
# device/asus/I001D/device.mk or common product config
# P0 disabled by default:
# PRODUCT_PACKAGES += prodx-p0  # only for engineering builds
# PRODUCT_PROPERTY_OVERRIDES += persist.sys.prodx.mode=0  # 0=DISABLED
```

P0 targets are NEVER included in user-facing product package lists. Only
`prodx-p0` is selectable as an engineering aggregate; it is not auto-included.

---

## 7. Test infrastructure plan

### 7.1 Test target hierarchy

```
ProdXP0IntegrationTests (multi-process end-to-end)
  ├── ProdXFrameworkServiceTests (Authority/Registry/Policy unit)
  ├── ProdXGrantAdminServiceTests (Grant admin unit)
  ├── ProdXBrokerTests (Broker unit/death/recovery)
  ├── ProdXAuditTests (Audit unit/journal/storage)
  ├── ProdXObservationTests (Hub unit/queue/lease)
  ├── ProdXExtensionTests (Extension parse/quarantine)
  ├── ProdXProviderSdkTests (SDK/no-op conformance)
  ├── ProdXContractTests (schema/vector/golden)
  ├── ProdXSystemUITests (SystemUI confirmation)
  ├── ProdXSettingsTests (Settings admin UI)
  └── ProdXP0SecurityTests (negative/replay/SELinux)
```

### 7.2 Fuzz test corpus

| Fuzz target | Input type | Generated from |
|---|---|---|
| `prodx_contract_fuzzer` | Raw bytes — envelope/object | Canonical + mutated vectors |
| `prodx_broker_protocol_fuzzer` | Transaction request bytes | Broker parser seam |
| `prodx_audit_record_fuzzer` | Audit record bytes | Audit parser seam |
| `prodx_observation_record_fuzzer` | Observation/Event bytes | Hub parser seam |
| `prodx_extension_manifest_fuzzer` | Extension manifest bytes | Extension parser seam |
| `prodx_provider_protocol_fuzzer` | Request/result/health bytes | SDK validator seam |

### 7.3 Required failure injection checkpoints

Every cross-process boundary has test coverage for:

1. Before contract decode — corrupt/truncated input
2. Before Registry resolve — stale generation, provider absent
3. Before policy decision — missing fact, stale epoch
4. Before UI proof — timeout, cancel, overlay, user switch
5. Before Audit reserve — disk full, audit down, corrupt journal
6. Before authorization mint — key failure, epoch rollover
7. Before provider dispatch — wrong caller, expired token
8. Before provider accept — wrong audience, parameter mismatch
9. Before provider result — crash, timeout, schema violation
10. Before Audit completion — crash, power loss, duplicate phase
11. Before grant admin — caller lacks PRODX_ADMIN, user not found
12. Before service dispatch — Binder.getCallingUid() does not match SYSTEM_UID

Each failure has exactly one documented terminal or recoverable state.

---

## 8. Dependency-ordered implementation map

### 8.1 Wave W1 — contract (P0-02 — this milestone)

Implementation order:

```
Step 1 — Build infrastructure:
  [X] packages/modules/ProdX/Android.bp          (P0-01: empty phony exists)
  [ ] packages/modules/ProdX/TARGETS.md           (add P0-02 targets)
  [ ] packages/modules/ProdX/tests/contract/Android.bp (create contract test target)

Step 2 — Canonical AIDL parcelable types (prerequisite for contract library):
  [ ] frameworks/base/core/java/android/app/prodx/ProdXMode.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXMode.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXHealth.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXHealth.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityDescriptor.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityDescriptor.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityRequest.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityRequest.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityResponse.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityResponse.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityError.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXCapabilityError.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXExecutionContext.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXExecutionContext.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXPolicyDecision.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXPolicyDecision.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXExecutionAuthorization.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXExecutionAuthorization.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXRegistryGeneration.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXRegistryGeneration.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXTransactionReference.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXTransactionReference.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXGrant.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXGrant.java
  [ ] frameworks/base/core/java/android/app/prodx/ProdXRegistrySnapshot.aidl
  [ ] frameworks/base/core/java/android/app/prodx/ProdXRegistryEntry.aidl

Step 3 — Contract test vectors (prodx-contract-test-vectors):
  [ ] framework/contract/                               -- schema inventory
  [ ] tests/contract/src/.../PositiveVectorTests.kt     -- golden CBOR/JSON
  [ ] tests/contract/src/.../NegativeVectorTests.kt     -- rejection cases
  [ ] tests/contract/src/.../GoldenHashTests.kt         -- hash verification
  [ ] tests/contract/src/.../CompatTests.kt             -- forward/back compat
  [ ] tests/contract/src/.../ErrorMappingTests.kt       -- error catalogs

Step 4 — Contract codec/validator (prodx-contract-runtime):
  [ ] framework/Android.bp                              -- java_library target depending on
                                                           prodx-framework-api + co.nstant.in.cbor
  [ ] framework/contract/src/.../IdentifierGrammar.kt   -- URN parser
  [ ] framework/contract/src/.../ContractVersion.kt     -- version handling
  [ ] framework/contract/src/.../ContractEnvelope.kt    -- envelope object
  [ ] framework/contract/src/.../CanonicalCborCodec.kt  -- deterministic CBOR thin wrapper
                                                           over co.nstant.in.cbor
  [ ] framework/contract/src/.../CanonicalJsonProjection.kt -- diagnostic JSON
  [ ] framework/contract/src/.../SchemaValidator.kt     -- validation
  [ ] framework/contract/src/.../SchemaRegistry.kt      -- in-process schemas
  [ ] framework/contract/src/.../ContentHash.kt         -- hash ops
  [ ] framework/contract/src/.../CompatibilityResolver.kt -- compat checks
  [ ] framework/contract/src/.../TypedError.kt          -- structured errors

Step 5 — Contract module-internal types:
  [ ] framework/contract/src/.../Objects/ObservationRecord.kt
  [ ] framework/contract/src/.../Objects/EventRecord.kt
  [ ] framework/contract/src/.../Objects/ExtensionManifest.kt

  // Cross-process types (CapabilityDescriptor, AuditRecord, RegistryEntry,
  // etc.) are defined as canonical AIDL parcelables in step 2 above.
  // The contract library references them from prodx-framework-api.

Step 6 — P0-02 validation:
  [ ] m prodx-framework-api                               -- AIDL parcelable type check
  [ ] m prodx-contract-runtime                            -- compile check
  [ ] m ProdXContractTests                                -- test check
  [ ] m prodx-p0                                          -- aggregate still empty
```

### 8.2 Wave W2 — interface boundaries (P0-03 + Audit start)

```
After G1 (P0-02 complete):

Step 7 — Framework projection (P0-03):
  [ ] frameworks/base/core/java/android/app/prodx/ProdXManager.java  -- app-facing gateway
  [ ] Remaining AIDL parcelable types:
      ProdXAuditRecord.aidl, ProdXAuditRecord.java
      ProdXSubscriptionLease.aidl, ProdXSubscriptionLease.java
      ProdXProviderManifest.aidl, ProdXProviderManifest.java
      ProdXExtensionManifest.aidl, ProdXExtensionManifest.java

Step 8 — Technology-neutral interface specifications (11 interface sets):
  [ ] Runtime Client specification
  [ ] Authority Control specification
  [ ] Grant Admin specification            (NEW)
  [ ] Registry Query specification
  [ ] Policy/Authorization specification
  [ ] Audit Ledger specification
  [ ] Broker Transaction specification
  [ ] Provider Dispatch specification
  [ ] Observation Lease specification
  [ ] Extension Validation specification
  [ ] Trusted Confirmation specification
  [ ] Runtime Administration specification

Step 9 — Audit prototype (P0-07 start in parallel):
  [ ] packages/modules/ProdX/service/audit/Android.bp  (test target only)
  [ ] AuditService.kt, AppendOnlyLedger.kt              (prototype with in-memory backend)
```

### 8.3 Wave W3 — Authority core (P0-04 + P0-05)

```
After G2:

Step 10 — Authority bootstrap (P0-04):
  [ ] Pre-verify SystemServer.java insertion point in actual Bliss tree
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXAuthorityService.java
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXContextBuilder.java
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXUserLifecycle.java
      (includes onUserStarting, onUserUnlocking, onUserUnlocked, onUserStopping, onUserRemoved)
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXTamperEvidentEpoch.java
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXKillSwitch.java
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXComponentAttestation.java
  [ ] frameworks/base/services/java/com/android/server/SystemServer.java  (+ feature gate)
  [ ] frameworks/base/core/res/res/values/config.xml  (+ bool)
  [ ] frameworks/base/services/core/java/com/android/server/prodx/ProdXGrantAdminService.java
      (serves IProdXGrantAdmin, guarded by PRODX_ADMIN permission)

Step 11 — Registry (P0-05):
  [ ] ProdXRegistry.java, ProdXRegistrySnapshot.java, ProdXRegistryEntry.java
  [ ] Concurrency model: copy-on-write with atomic generation swap
      (lock-free reads; write lock on generation update)
  [ ] Built-in catalog loading
  [ ] Deterministic resolution
  [ ] Per-user registry view
  [ ] Gen change notification
```

### 8.4 Wave W4 — Policy (P0-06)

```
Step 12 — Policy Engine (P0-06):
  [ ] ProdXPolicyEngine.java, ProdXPolicyDecision.java (references android.app.prodx parcelables)
  [ ] ProdXGrantStore.java, ProdXGrant.java (references android.app.prodx parcelables)
  [ ] ProdXAuthorizationEngine.java (uses java.security.KeyStore with AndroidKeyStore provider)
  [ ] ProdXExecutionAuthorization.java (references android.app.prodx parcelable)
  [ ] ProdXConfirmationChallenge.java
  [ ] Risk/obligation catalog (verified data)
```

### 8.5 Wave W5 — Services + UI (P0-07, P0-08, P0-09, P0-10, P0-11, P0-12)

```
Step 13 — Audit Engine (P0-07 complete):
  [ ] Full durable storage backend
  [ ] Transaction reservation with TTL (configurable timeout, cancelReservation API)
  [ ] Phase append, outcome, cancellation
  [ ] DE/CE partitioning, recovery journal
  [ ] Privacy redaction, retention, tombstone
  [ ] UID verification on all AIDL entry points (Binder.getCallingUid() == SYSTEM_UID)

Step 14 — SystemUI confirmation (P0-08):
  [ ] ProdXConfirmationPanel (challenge/proof UI)
  [ ] ProdXIndicatorController (active operation chip)

Step 15 — Settings administration (P0-08):
  [ ] ProdXSettingsDashboardFragment
  [ ] ProdXKillSwitchPreferenceController
  [ ] ProdXGrantsFragment, ProdXGrantDetailFragment (use IProdXGrantAdmin)
  [ ] ProdXHistoryFragment, ProdXProviderHealthFragment
  [ ] ProdXExtensionQuarantineFragment

Step 16 — Broker service (P0-09):
  [ ] BrokerService.kt (UID-verified ServiceManager binding)
  [ ] TransactionStateMachine.kt
  [ ] ProviderDispatcher.kt
  [ ] BrokerCheckpointStore.kt

Step 17 — Provider SDK + no-op provider (P0-10):
  [ ] prodx-provider-sdk library
  [ ] ProdXNoOpTestProvider

Step 18 — Observation Hub (P0-11):
  [ ] ObservationService.kt (UID-verified ServiceManager binding)
  [ ] LeaseManager.kt, EventPipeline.kt
  [ ] ObservationQueue.kt, SourceRegistry.kt
  [ ] ConsumerDeliveryManager.kt

Step 19 — Extension Manager (P0-12):
  [ ] ExtensionService.kt (UID-verified ServiceManager binding)
  [ ] ManifestParser.kt, SchemaVerifier.kt
  [ ] SigningLineageVerifier.kt, QuarantineStore.kt
```

### 8.6 Wave W6 — Security (P0-13)

```
Step 20 — SELinux/signing/permission packaging:
  [ ] system/sepolicy/public/prodx_service.te, prodx_attribute.te
  [ ] system/sepolicy/private/prodx_compat.te
  [ ] packages/modules/ProdX/sepolicy/prodx_broker.te, prodx_audit.te, ...
  [ ] packages/modules/ProdX/sepolicy/prodx_file_types.te  (includes prodx_authorization_file)
  [ ] packages/modules/ProdX/sepolicy/prodx_service_contexts
  [ ] frameworks/base/data/etc/com.android.prodx.signature_permissions.xml
  [ ] frameworks/base/data/etc/com.android.prodx.privapp_allowlist.xml
```

### 8.7 Wave W7 — Packaging (P0-14)

```
Step 21 — Product staging + APEX:
  [ ] packages/modules/ProdX/apex/Android.bp, apex_manifest.json
  [ ] Product feature flag for engineering builds
  [ ] Incremental package addition (Audit -> Authority -> Broker -> Hub -> Extension -> UI)
  [ ] Stage/activate/reboot/rollback validation
```

### 8.8 Wave W8 — Hardening (P0-15)

```
Step 22 — End-to-end testing:
  [ ] Inventory-only boot with no provider
  [ ] Shadow-policy synthetic request through full path
  [ ] Test-no-op transaction end to end
  [ ] Observation lease + synthetic source
  [ ] Extension quarantine + package update
  [ ] Fault injection at every cross-process boundary
  [ ] Grant admin: permission denial, cross-user isolation
  [ ] Service binding: impersonation rejection, UID verification
  [ ] Multi-user/profile matrix
  [ ] Fuzzing: contract, broker, audit, observation, extension, provider protocol
  [ ] Upgrade/downgrade/rollback matrix
  [ ] SELinux enforcing proof
```

---

## 9. Rollback plan per milestone

| Milestone | Rollback action |
|---|---|
| P0-02 | Revert `framework/` and `tests/contract/` additions; revert AIDL parcelable types in `android.app.prodx`. Re-verify P0-00 checksum. No persistent state exists. |
| P0-03 | Revert `frameworks/base/core/java/android/app/prodx/ProdXManager.java` and remaining AIDL parcelable types. |
| P0-04 | Turn `config_enableProdxAuthority` off, revert Authority service directory (including `ProdXGrantAdminService`), re-verify boot. |
| P0-05 | Disable reconciliation, delete rebuildable cache, fall back to empty generation. |
| P0-06 | Increment policy/grant epoch, revoke synthetic tokens, disable shadow policy. |
| P0-07 | Stop Audit service, preserve ledger bytes. Revert `service/audit/`. |
| P0-08 | Authority marks UI unavailable. Revert SystemUI/Settings prodx directories. |
| P0-09 | Authority stops binding Broker. Revert `service/broker/`. |
| P0-10 | Remove test provider from engineering configuration. Revert `sdk/`. |
| P0-11 | Authority revokes leases, unbinds Hub. Revert `service/observation/`. |
| P0-12 | Unbind Extension Manager, discard cache. Revert `service/extension/`. |
| P0-13 | Disable product inclusion, remove service bindings, revert policy files together. |
| P0-14 | Emergency disable epoch, rollback APEX/packages, verify Android boot. |
| P0-15 | Full revert order per Section 12.2 of P0 Implementation Specification. |

---

## 10. CTS/Security compatibility impact matrix

| Change | CTS impact | Security impact | Mitigation |
|---|---|---|---|
| New interface `IProdXAuthority` | None — system/private API | Authority binding must verify caller | Binder UID check, signature permission |
| New interface `IProdXGrantAdmin` | None — signature permission only | Must protect grant operations | `PRODX_ADMIN` signature permission, UID check |
| New `config_enableProdxAuthority` | None — default false | Disabled byte-identical boot | Gate verified at each milestone |
| New SELinux domains `prodx_*` | None — new types, no existing type changed | Must not grant broad access | neverallow tests per P0-13 |
| New signature permissions | None — signature-level only | Must protect each endpoint | Reviewed per P0-13 |
| New `/data/system/prodx/` files | None | Must not coexist with app data | `system_data_file` label |
| New `prodx_authorization_file` type | None | Must restrict to system_server | neverallow rules per §5.4 |
| New APEX `com.android.prodx` | None — not in product until P0-14 | Must not weaken existing policy | APEX neverallow/rollback tests |
| New SystemUI prodx classes | None — existing package | Must not create hidden consent | Spoof/overlay tests per P0-08 |
| Canonical AIDL parcelable types | None — system/private API | Must correctly implement Parcelable | Unit tests per step 2 |
| CBOR library dependency | None — platform library already present | Must validate input depth/bounds | Fuzz tests per §7.2 |
| No existing Android service modified | Zero CTS change | Zero regression risk | Verified per milestone |

---

## 11. Engineering resource estimate

| Milestone | Estimated new files | Estimated complexity (relative) |
|---|---|---|
| P0-02 Contract runtime + vectors + canonical parcelable types | ~30 files (incl. .aidl + .java pairs) | MEDIUM — careful CBOR/validation, Parcelable implementation |
| P0-03 Framework projection (ProdXManager + remaining parcelables) | ~10 files + 12 specs | LOW — thin wrapper + docs |
| P0-04 Authority bootstrap (incl. ProdXGrantAdminService) | ~9 files + 2 edits | HIGH — identity, SystemServer integration, direct-boot lifecycle |
| P0-05 Registry | ~5 files | MEDIUM — determinism, generations, concurrency model |
| P0-06 Policy + authorization | ~6 files | HIGH — security-critical decision engine (AndroidKeyStore) |
| P0-07 Audit Engine | ~11 files | HIGH — durable, recoverable storage, TTL reservations |
| P0-08 SystemUI + Settings | ~11 files | MEDIUM — UI patterns well-known |
| P0-09 Broker | ~11 files | HIGH — state machine correctness |
| P0-10 Provider SDK + no-op | ~12 files | MEDIUM — SDK design + test provider |
| P0-11 Observation Hub | ~9 files | MEDIUM — queue, backpressure, lease |
| P0-12 Extension Manager | ~8 files | MEDIUM — parser isolation |
| P0-13 SELinux/signing | ~7 policy + 2 config files | HIGH — must review every edge |
| P0-14 APEX/product staging | ~3 build files | MEDIUM — Soong/APEX integration |
| P0-15 Hardening | ~8 test targets | HIGH — thorough validation |

Total estimated classes: ~135 files (including tests ~220 files)

---

## 12. Acceptance criteria for P0-02 completion (G1)

1. Every contract object from the Contract Specification has a schema
   validator, positive golden vector, and negative rejection vector.
2. Deterministic CBOR encode/decode is proven by round-trip tests using
   `co.nstant.in.cbor` as the underlying library.
3. Canonical golden hashes are verified by two independent implementations or
   reference vectors.
4. Schema validation rejects unknown major version, unknown enum, noncanonical
   encoding, duplicate keys, invalid Unicode, float use, unbounded depth/count,
   and hash mismatch.
5. Unknown security meaning fails closed in every validator.
6. `prodx-contract-runtime` has zero Android service dependency (pure library).
7. `prodx-contract-runtime` depends on `prodx-framework-api` for canonical
   cross-process parcelable types.
8. Every cross-process data type defined in the Contract Specification has a
   corresponding AIDL `parcelable` declaration in `android.app.prodx`.
9. `prodx-contract-test-vectors` is a read-only asset set with no runtime
   behavior.
10. `m prodx-framework-api`, `m prodx-contract-runtime`, and
    `m ProdXContractTests` compile.
11. Framework API/lint/dependency checks pass.
12. Platform build graph is unchanged except for `packages/modules/ProdX/` and
    `frameworks/base/core/java/android/app/prodx/` additions.
13. P0-20 immutable hashes are reverified and unchanged.
14. P0-00 BASELINE-FILES.sha256 still passes.

---

## 13. Open questions requiring P0-02 resolution

| Question | Owner | Decision | Resolution |
|---|---|---|---|
| Kotlin vs Java for ProdX module services? | Platform language owner | Kotlin for module services, Java for frameworks/base | CLOSED |
| `java_sdk_library` vs `java_library` for contract runtime? | Build/Soong owner | `java_library` for `prodx-contract-runtime`; `java_sdk_library` for `prodx-framework-api` | CLOSED |
| Should CBOR library be reimplemented or use existing lib? | Contract owner | Use `co.nstant.in.cbor` with thin deterministic wrapper | CLOSED (BLK-06) |
| Package naming for internal AIDL: `com.android.prodx.runtime.*` or `android.app.prodx.*`? | Framework API owner | `android.app.prodx.*` for framework parcelables and client-facing AIDL; `com.android.prodx.runtime.*` for module-internal AIDL | CLOSED |
| Direct-boot awareness required for Audit initial prototype? | Security owner | Yes — `onUserUnlocking` DE state and `onUserUnlocked` CE state are both required from P0-04 | CLOSED (BLK-02) |
| APEX `com.android.prodx` vs `/system_ext` staging for first release? | Mainline/APEX owner | APEX in P0-14; /system_ext is a fallback if APEX certification is incomplete | OPEN — P0-13 |
| Platform-staging UID ranges for broker/audit/hub/extension? | SELinux owner | Use AOSP convention for isolated process UIDs | OPEN — P0-13 |
| Signature permission naming: `PRODX_*` vs `ACCESS_PRODX_*`? | API council | `PRODX_*` per naming convention §0.2 | OPEN — P0-13 |

---

## 14. References

| Document | Hash |
|---|---|
| ProdX Runtime Architecture Foundation | `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec` |
| ProdX Runtime Contract Specification | `7c7743700c468847ef547bbbc22dcc9d1b027df0fee9e92895faee071706d886` |
| ProdX Runtime Skeleton Specification | `f332bace6730a290dcaff5a7703d8e499eac08dcd309cee63a31248997bffbd6` |
| ProdX P0 Runtime Implementation Specification | `c3ef5e20208ebc13fdb8bd43e16dae770ecddd9ddcb8a19a8a22d57a00b33152` |
| Capability Investigation content-set digest | `dd76ae42def1cf0d1618dfdeda610585adff361b5fd5021fece0dbd884c59a54` |
| P0-02 Engineering Design Review | `ProdX-P0-02-Engineering-Design-Review-20260714.md` |
| P0-02 Implementation Mapping (v1.0.0, superseded) | This document v1.0.0-draft-plan |

---

## 15. Normative conclusion

This mapping (v1.1.0) resolves all six blocking issues identified in the
engineering design review:

- **BLK-01** — Keystore API corrected to `java.security.KeyStore` with
  `AndroidKeyStore` provider.
- **BLK-02** — Direct-boot lifecycle extended with `onUserUnlocking()` and
  explicit DE/CE partitioning.
- **BLK-03** — Duplicate contract models eliminated; canonical AIDL `parcelable`
  types defined in `android.app.prodx` serve as the single cross-process data
  contract.
- **BLK-04** — Grant management isolated into `IProdXGrantAdmin` with
  `PRODX_ADMIN` permission, removing confused-deputy surface.
- **BLK-05** — `setAuthority(IBinder)` removed from all service interfaces;
  replaced by trusted ServiceManager-based discovery with UID verification.
- **BLK-06** — Custom CBOR reimplementation replaced by thin wrapper over
  `co.nstant.in.cbor`; UUIDv7 deferred to P1.

**Zero blocking issues remain.** P0-02 (Contract Runtime + Vectors + Canonical
Parcelable Types) is authorized for implementation start, with the revised
source tree, AIDL interfaces, build dependencies, and acceptance criteria
documented above.

No production code has been written or generated by this specification.
