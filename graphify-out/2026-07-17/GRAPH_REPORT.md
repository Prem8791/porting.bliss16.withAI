# Graph Report - porting  (2026-07-17)

## Corpus Check
- 426 files · ~358,764 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 4200 nodes · 7541 edges · 388 communities (189 shown, 199 thin omitted)
- Extraction: 95% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 318 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `95e09928`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- inParm Touch Calibration PR2-1
- ASUS I001D Device Parameters
- Android 16 Bliss Reconstruction
- Project Governance & Decisions
- ProdX Blocking Issues (BLK)
- inParm Calibration PR1
- inParm Calibration PR2-2
- ProdX Core Services
- HomeLauncher SELinux & Platform
- HomeLauncher TaskOrganizer
- HomeLauncher Backend Architecture
- inParm Config Parameters
- ProdX Engineering Governance
- ProdX Reference Specifications
- SELinux & Boot Failures
- HomeLauncher Integration
- TaskOrganizer Migration Plan
- HomeLauncher Gesture Navigation
- RecentTasks Backend
- ProdX P0 Specifications
- VM Build Workflow
- Diagnostic Verification
- ProdX Split Architecture
- ProdX Contribution & Ownership
- ProdX Milestone Tracking
- HomeLauncher Animation Engine
- porting_handsoff_md_handsoff
- porting_readme_md_readme
- porting_takeover_md_takeover
- waterlily-i001d-reconstruction_agents_md_agents_md
- waterlily-i001d-reconstruction_baseline_device_asus_i001d_readme_md_android_14_base
- waterlily-i001d-reconstruction_baseline_device_asus_i001d_readme_md_device_info
- libadsprpc.so
- libcdsprpc.so
- libfastcvopt.so
- liblistenjni.so / liblistensoundmodel2.so
- libnpu.so / libhta_*
- libOpenCL.so
- libprotobuf-cpp-full/lite.so
- libsdsprpc.so
- unnhal-acc-hta.so
- waterlily-i001d-reconstruction_handoff_vm-current_current-overlay-deletions_txt_overlay_deletions
- waterlily-i001d-reconstruction_handoff_vm-current_current-overlay-files_txt_overlay_files
- waterlily-i001d-reconstruction_handoff_vm-current_snapshot-metadata_txt_snapshot_metadata
- waterlily-i001d-reconstruction_handoff_vm-current_vm-project-status_txt_vm_project_status
- waterlily-i001d-reconstruction_prodx-master-product-roadmap_md_roadmap
- waterlily-i001d-reconstruction_progress_md_prodx_framework_contracts
- waterlily-i001d-reconstruction_progress_md_prodx_services
- waterlily-i001d-reconstruction_progress_md_prodx_systemui_integration
- waterlily-i001d-reconstruction_progress_md_progress
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_docs_engineering_p0-01-implementation-report_md_p0_01_report
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_docs_engineering_readme_md_engineering_docs
- ProdX Documentation
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_sepolicy_readme_p0_01_milestone
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_sepolicy_readme_prodx_sepolicy
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_audit_ledger
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_authority_binding
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_p0_07_milestone
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_prodx_audit_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_prodxauditservice
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_prodxaudittests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_sha256_digest
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_audit_readme_system_uid_boundary
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_broker_readme_prodx_broker_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_extension_readme_prodx_extension_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_learning_readme_prodx_learning_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_observation_readme_prodx_observation_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_readme_prodx_service_boundaries
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_service_reasoning_readme_prodx_reasoning_service
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_contract_readme_prodx_contract_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_device_readme_prodx_device_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_fixtures_readme_prodx_test_fixtures
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_fuzz_readme_prodx_fuzz_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_host_readme_prodx_host_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_integration_readme_prodx_integration_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_readme_prodx_test_hierarchy
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_reference_immutable_prodx-p0-runtime-implementation-specification-20260714_md_p0_implementation
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_reference_immutable_prodx-runtime-contract-specification-20260714_md_contract_spec
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_reference_immutable_prodx-runtime-skeleton-specification-20260714_md_skeleton_spec
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_security_readme_prodx_security_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tests_unit_readme_prodx_unit_tests
- waterlily-i001d-reconstruction_vm-edit_current-overlay_packages_modules_prodx_tools_readme_prodx_engineering_tools
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_blackberry-10-home-screen-research_md_active_frames
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_blackberry-10-home-screen-research_md_blackberry_10
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_blackberry-10-home-screen-research_md_blackberry_hub
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_blackberry-10-home-screen-research_md_peek_flow
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_implementation-plan_md_impl_plan
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_required-permissions_md_permissions_catalog
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_three-column-recents-launcher-design_md_hidden_apis
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_three-column-recents-launcher-design_md_three_column_launcher
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_windows-mobile-5-home-screen-research_md_homescreen_navigation
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_windows-mobile-5-home-screen-research_md_today_screen_plugin
- waterlily-i001d-reconstruction_work_homelauncher-repo_research_windows-mobile-5-home-screen-research_md_windows_mobile_5
- Gradle configuration
- waterlily-i001d-reconstruction_work_homelauncher-repo_rom-integration_sepolicy_draft_readme_avc_denials_procstat_sysfsthermal
- waterlily-i001d-reconstruction_work_homelauncher-repo_rom-integration_sepolicy_draft_readme_com_home_launcher
- waterlily-i001d-reconstruction_work_homelauncher-repo_rom-integration_sepolicy_draft_readme_first_boot_validation
- waterlily-i001d-reconstruction_work_homelauncher-repo_rom-integration_sepolicy_draft_readme_homelauncher_deferred_sepolicy_drafts
- waterlily-i001d-reconstruction_work_homelauncher-repo_rom-integration_sepolicy_draft_readme_platform_app_context
- prodx-provider-sdk
- ProdX SELinux Domain Architecture
- ContractEnvelope
- P0 Execution Modes: DISABLED, INVENTORY_ONLY, SHADOW_POLICY, TEST_NO_OP
- ProdX Repository Directory Map
- ProdX Runtime
- ProdX Build Target List
- DisplayMirrorHost (Approach B)
- System Recents Launcher Findings
- SystemServer
- Context
- AudioFxService
- Intent
- ATMS_decoded.java
- BiometricsFingerprint
- SoundRecorderService
- ContractVersion
- HandwaveSensor
- SchemaValidator
- ProdXConfirmationBridge
- ProdXRegistry
- TouchKeyHandler
- ErrorCode
- ProdXAuthorityService
- ProdXGrantStore
- ProdXCapabilityRequest
- ProdXConfirmationController
- NonNull
- Context.java
- FakeBrokerService
- ProdXManager
- ProdXGrantListPreferenceController
- AppListOverlay.kt
- .getContentResolver
- ActivityRecord
- Parcelable
- ProdXCapabilityDescriptor
- MainActivity
- gpt-utils.cpp
- Final Architecture Investigation: `com.home.launcher`
- DozeUtils
- EventRecord
- CapabilityInventory
- .enforceTaskPermission
- Uri
- ProdXPolicyDecision
- Lights
- .getResources
- MorphingEngine
- RecentAppsAdapter
- ProdXExecutionAuthorization
- ProdXSettingsMediatorService
- What You Must Do When Invoked
- SecurityMonitor
- ProdXRegistryEntry
- FakeProdXAuthorityService
- DomainCode
- .enforceNotIsolatedCaller
- DozeSettingsFragment.java
- ObservationQueue
- TransactionStateMachine
- SourceRegistry
- TouchscreenGestureSettings.java
- RecentTasks
- Live Recent Cards — Real-time Surface Feed for Recents Grid
- HealthReporter
- ProviderState
- AppendOnlyLedger
- ConfirmationCoordinator.kt
- RecentTasks_decoded.java
- FileOutputStream
- .getPackageManager
- CanonicalCborCodec
- BackpressureController
- ProdXMode
- ContractCanonicalCborCodecTest
- MorphingEngine — Dynamic Island-style Animation Engine
- Task
- PlatformRecentTasksBackend
- TestApi
- VisibleForTesting
- ProdXIndicatorController
- JsonParser
- LeaseManager
- ObservationService
- Deployment Package Manifest
- LedgerHashChain
- build_hybrid_boot.py
- SystemSettingsStore
- ConsumerDeliveryManager
- HubShellCommand
- Build, Sign & Install
- .startActivity
- UnavailableRecentTasksBackend
- gpt_disk
- ProdXAuditRecord
- .getSharedPreferences
- ErrorCategory
- BrokerCheckpointStore
- RedactionPipeline
- TouchscreenGesture
- ProdXHealth
- AuditCorrelationHelper
- FileLedgerBackend
- TransactionPhase
- RuleEngine.kt
- TouchscreenGesture
- AsusParts
- AuditService
- ContractIdentifierGrammarTest
- FakeAuthorityService
- CollapsingToolbarBaseActivity
- .revalidate
- FakeSecureSettingsRepository
- Lifecycle
- MainSettingsFragment
- extract_payload.py
- ProdXExtensionManifest
- ProdXSubscriptionLease
- SyntheticObservationSource
- ProposalValidator
- .dump
- .getRecentTasksImpl
- SleepTokenAcquirerImpl
- FakeSystemSettingsRepository
- AppendOnlyLedgerTest
- BrokerShellCommand
- GloveMode
- GloveMode
- graphify reference: extra exports and benchmark
- parse_payload3.py
- ProdXConfirmationChallenge
- NoOpTestProviderService
- CancellationToken
- .copy
- State
- DependencyResolver
- 10. `getRecentTasks()` Framework Execution Path
- dng_xmp_sdk::PackageForJPEG
- SparseBooleanArray
- NoOpCapabilities
- AuthorizationVerifier.kt
- ContractEnvelopeTest
- LegacyCameraProviderImpl_2_5.cpp
- dng_string
- SystemSettingSwitchPreference.java
- parse_payload2.py
- patch_boot_ramdisk_property.py
- ProdXCapabilityActivity
- ExtensionService
- SystemStatsBar
- 11. Decision Matrix
- 9. Summary: Application Privilege Level & Capability Mapping
- graphify reference: query, path, explain
- ProdXSettingsFailClosedTest
- PipelineStage
- QuickStepService
- AsusPartsTileService
- extract-files.sh
- setup-makefiles.sh
- extract_boot_v2.py
- ProdXDeveloperOptionsFragment
- QuarantineStore
- Investigation Report: `com.home.launcher` Runtime Environment & Framework Analysis
- 12. Final Verification: Remaining Uncertainties
- 2. Runtime Identity
- 5. SELinux Mode & Denials
- 6. `/proc/stat` and `/sys/class/thermal` Accessibility
- HomeLauncher ROM Integration Package
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- graphify reference: incremental update and cluster-only
- dng_xmp_private
- init.qcom.early_boot.sh
- init.qcom.post_boot.sh
- parse_payload.py
- parse
- ManifestParser
- SchemaVerifier
- SigningLineageVerifier
- 1. APK Installation & Classification
- 3. Permissions
- 7. Hidden API / Reflection Usage Audit
- 8. Build Configuration
- CBS_init
- dng_xmp_sdk::GetStringList
- dng_local_string
- dng_xmp_sdk
- graphify.js
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- extract-files.sh
- update_data
- init.qti.chg_policy.sh
- init.qti.chg_policy.sh
- qtigetprop
- qtisetprop
- State handoff
- debug_manifest.py
- scan_payload.py
- ProdXAmbientDisplayController
- ProdXKeyguardMonitor
- ProdXNotificationController
- ProdXQuickSettingsTile
- ProdXStatusBarController
- ProdXScreensaverPreferenceController
- ExtensionManifest
- ObservationRecord
- AuditShellCommand
- ExtensionShellCommand
- ProdXDeviceSideTest
- FakeAuditService
- FakePolicyEngine
- FakeRegistry
- ProdXFuzzParcelables
- ProdXFuzzServiceApi
- ProdXHostSideTest
- ProdXAuditIntegrationTest
- ProdXAuthorityIntegrationTest
- ProdXExtensionIntegrationTest
- ProdXGrantAdminIntegrationTest
- ProdXAuthorizationBoundaryTest
- ProdXPermissionEnforcementTest
- ProdXSELinuxTest
- ProdXSideChannelLeakTest
- ProdXAuthorizationEngineTest
- ProdXGrantStoreTest
- ProdXKillSwitchTest
- ProdXModeTest
- ProdXParcelableTest
- ProdXPolicyEngineTest
- ProdXRegistryTest
- instructions.md
- dng_xmp_sdk::InitializeSDK
- dng_xmp_sdk::CountArrayItems
- dng_xmp_sdk::IteratePaths
- extraction-spec.md
- TouchscreenGestureConstants.java
- init.class_main.sh
- init.qcom.sh
- init.qti.dcvs.sh
- setup-makefiles.sh
- init.qti.qcv.sh
- LOCAL-STATE.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- CandidateReport.kt
- ExtensionHealth.kt
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md
- README.md

## God Nodes (most connected - your core abstractions)
1. `ActivityTaskManagerService` - 319 edges
2. `Context` - 274 edges
3. `LocalService` - 114 edges
4. `RecentTasks` - 86 edges
5. `MainActivity` - 71 edges
6. `SystemServer` - 56 edges
7. `ProdXManager` - 35 edges
8. `BiometricsFingerprint` - 33 edges
9. `TouchKeyHandler` - 33 edges
10. `FakeBrokerService` - 32 edges

## Surprising Connections (you probably didn't know these)
- `inParm16` --semantically_similar_to--> `Parameter Cluster A (R/G/B Gain Base)`  [INFERRED] [semantically similar]
  waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm16.txt → waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm1.txt
- `inParm17` --semantically_similar_to--> `Parameter Cluster A (R/G/B Gain Base)`  [INFERRED] [semantically similar]
  waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm17.txt → waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm1.txt
- `inParm18` --semantically_similar_to--> `Parameter Cluster A (R/G/B Gain Base)`  [INFERRED] [semantically similar]
  waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm18.txt → waterlily-i001d-reconstruction/baseline/vendor_asus_I001D/proprietary/vendor/etc/inparm_pr1/inParm1.txt
- `HomeLauncher ROM Integration` --part_of--> `Android 16 Bliss Waterlily ASUS I001D Reconstruction`  [0.85]
  waterlily-i001d-reconstruction/work/HomeLauncher-repo/rom-integration/README.md → .agents/instructions.md
- `Android 16 Bliss Waterlily ASUS I001D Reconstruction` --references--> `VM State Checkpoint 2026-07-16`  [0.85]
  .agents/instructions.md → waterlily-i001d-reconstruction/handoff/README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **inParm PR1 Camera Calibration Parameter Set** — waterlily_i001d_reconstruction_baseline_vendor_asus_i001d_proprietary_vendor_etc_inparm_pr1_inparm_parameter_set, waterlily_i001d_reconstruction_baseline_vendor_asus_i001d_proprietary_vendor_etc_inparm_pr1_parameter_cluster_a, waterlily_i001d_reconstruction_baseline_vendor_asus_i001d_proprietary_vendor_etc_inparm_pr1_parameter_cluster_b, waterlily_i001d_reconstruction_baseline_vendor_asus_i001d_proprietary_vendor_etc_inparm_pr1_parameter_cluster_c [INFERRED 0.95]

## Communities (388 total, 199 thin omitted)

### Community 0 - "inParm Touch Calibration PR2-1"
Cohesion: 0.08
Nodes (30): asus_i001d_device, inparm_calibration_coefficients, inparm_config_flag_line124, inparm_curve_array, inparm_mode_flag_line9, inparm_pr2_1_parameter_set, inparm_rotation_matrices, inparm_timing_parameters (+22 more)

### Community 1 - "ASUS I001D Device Parameters"
Cohesion: 0.08
Nodes (24): ASUS I001D Waterlily, Audio DSP tuning parameters, inParm10.txt, inParm11.txt, inParm12.txt, inParm13.txt, inParm14.txt, inParm15.txt (+16 more)

### Community 2 - "Android 16 Bliss Reconstruction"
Cohesion: 0.08
Nodes (26): Android 16 Bliss Waterlily ASUS I001D Reconstruction, VM Build Handoff Policy, libqti-perfd-client.so, Qualcomm SM8150 Platform, Workstation State Checkpoint, ProdX Module Tree, VM State Checkpoint 2026-07-16, ArcSoft Camera Libraries (+18 more)

### Community 3 - "Project Governance & Decisions"
Cohesion: 0.15
Nodes (13): Bliss OS for ASUS I001D, Waterlily I001D reconstruction project, DEC-0001: Lock authoritative reference set, DEC-0002: Role-stable reviewer ownership IDs, DEC-0003: Preserve pre-existing dirty Android checkout, DEC-0004: Capability Investigation inside Foundation, DEC-0005: P0-00 permits documentation-only additions, ProdX P0-00 Engineering Baseline (+5 more)

### Community 4 - "ProdX Blocking Issues (BLK)"
Cohesion: 0.10
Nodes (23): BLK-01: Deprecated Keystore API, BLK-02: Missing Direct-Boot DE/CE Lifecycle, BLK-03: Contract Object Duplication, BLK-04: Confused-Deputy Grant Revocation, BLK-05: Unverifiable Authority Token, BLK-06: CBOR Reimplementation vs Platform Library, P0-02 Blocking Issues (BLK-01 through BLK-06), ProdX P0-02 Engineering Design Review (+15 more)

### Community 5 - "inParm Calibration PR1"
Cohesion: 0.08
Nodes (25): inParm1, inParm10, inParm11, inParm12, inParm13, inParm14, inParm15, inParm16 (+17 more)

### Community 6 - "inParm Calibration PR2-2"
Cohesion: 0.02
Nodes (37): ActivityClientController, ActivityStartController, ActivityTaskManagerInternal, AnrController, AppOpsManager, AppWarnings, BackNavigationController, ClientLifecycleManager (+29 more)

### Community 7 - "ProdX Core Services"
Cohesion: 0.04
Nodes (15): ActivityAssistInfo, ActivityMetricsLaunchObserverRegistry, ActivityServiceConnectionsHolder, ActivityTokens, AppTask, PackageConfig, PackageConfigurationUpdater, ScreenObserver (+7 more)

### Community 8 - "HomeLauncher SELinux & Platform"
Cohesion: 0.20
Nodes (10): Cloud VM Build/Test Infrastructure, platform_app SELinux Domain, seapp_contexts Precedence: seinfo=platform beats isPrivApp=true, com.home.launcher, HiddenApi.kt Reflection Layer, isVisibleRecentTask() HOME/RECENTS Filter, Platform Signing for Hidden API Access, REAL_GET_TASKS Permission Grant (+2 more)

### Community 9 - "HomeLauncher TaskOrganizer"
Cohesion: 0.29
Nodes (7): Reflection recents returning 0 tasks, HiddenApiBridge, ReflectionRecentTasksBackend, Deferred tasks list, TaskOrganizer (android.app.task API), Reflection-to-TaskOrganizer replacement analysis, TaskOrganizer migration roadmap

### Community 10 - "HomeLauncher Backend Architecture"
Cohesion: 0.29
Nodes (7): com.home.launcher, PlatformRecentTasksBackend, RecentTasksRepository, Soong android_app module config, Hidden API audit, Privileged permission table for com.home.launcher, Post-flash verification checklist

### Community 11 - "inParm Config Parameters"
Cohesion: 0.05
Nodes (71): condition_variable, Effect, Effect_1_1, EffectStrength, IVibrator, perform_cb, queue, Status (+63 more)

### Community 12 - "ProdX Engineering Governance"
Cohesion: 0.33
Nodes (6): P0 Implementation Dependency Graph, ProdX Engineering Governance, Owner IDs and RACI matrix, Security review gates G0-G7, Milestone and Gate Tracker, P0-00 through P0-15 milestones

### Community 13 - "ProdX Reference Specifications"
Cohesion: 0.33
Nodes (6): ProdX P0-00 README, Immutable Reference Lock, REF-CONTRACT: Runtime Contract Specification, REF-FOUNDATION: Runtime Architecture Foundation, REF-P0: P0 Implementation Specification, REF-SKELETON: Runtime Skeleton Specification

### Community 14 - "SELinux & Boot Failures"
Cohesion: 0.33
Nodes (6): SELinux proc_stat denial, SystemStatsProvider.getCpuUsage(), Expected first-boot failures, Patch 0008-add-active-home-launcher-proc-stat-sepolicy, CPU failure backoff mechanism, Denial storm suppression

### Community 15 - "HomeLauncher Integration"
Cohesion: 0.08
Nodes (18): ActivityOptions, AssistContent, AssistStructure, BackgroundStartPrivileges, IAssistDataReceiver, IIntentSender, IRecentsAnimationRunner, PendingIntentRecord (+10 more)

### Community 16 - "TaskOrganizer Migration Plan"
Cohesion: 0.40
Nodes (5): Architecture Comparison: Legacy Reflection vs TaskOrganizer, HomeLauncher Six-Phase Migration Plan, TaskOrganizer Modern Recents Pipeline, Legacy Hidden API Reflection Approach, LiveTileLayer (Approach A - TaskOrganizer)

### Community 17 - "HomeLauncher Gesture Navigation"
Cohesion: 0.50
Nodes (4): HomeLauncherConfigOverlay, Overview/QuickStep integration, Gesture navigation broken by overlay, Patch 0007-integrate-home-launcher-product-mk

### Community 18 - "RecentTasks Backend"
Cohesion: 0.67
Nodes (3): PlatformRecentTasksBackend, RecentTasksBackend, Android 14 TaskOrganizer

### Community 19 - "ProdX P0 Specifications"
Cohesion: 0.67
Nodes (3): ProdX P0 Runtime Implementation Specification, ProdX Runtime Contract Specification, ProdX Runtime Skeleton Specification

### Community 108 - "ProdX Runtime"
Cohesion: 0.40
Nodes (4): Authoritative references, Build participation, Namespace reservations, ProdX Runtime

### Community 112 - "SystemServer"
Cohesion: 0.05
Nodes (30): ActivityThread, CrashInfo, DataLoaderManagerService, DisplayManagerService, Dumpable, EntropyMixer, LongArray, PackageManagerService (+22 more)

### Community 113 - "Context"
Cohesion: 0.03
Nodes (14): AssetManager, AttributionSource, AutofillClient, ClassLoader, ComponentCallbacks, Deprecated, Drawable, FileInputStream (+6 more)

### Community 114 - "AudioFxService"
Cohesion: 0.07
Nodes (27): AudioDeviceInfo, AudioOutputChangedCallback, AudioOutputChangeListener, Binder, DevicePreferenceManager, EffectSet, MasterConfigControl, MaterialSwitch (+19 more)

### Community 115 - "Intent"
Cohesion: 0.12
Nodes (13): BroadcastOptions, IntentSender, RequiresPermission, SuppressWarnings, SystemApi, UnsupportedAppUsage, UserHandle, BroadcastReceiver (+5 more)

### Community 116 - "ATMS_decoded.java"
Cohesion: 0.04
Nodes (37): ActivityManagerInternal, AppTimeTracker, BackAnimationAdapter, BackNavigationInfo, GuardedBy, IActivityClientController, IPackageManager, IScreenCaptureObserver (+29 more)

### Community 117 - "BiometricsFingerprint"
Cohesion: 0.08
Nodes (48): BiometricsFingerprint, authenticate, BiometricsFingerprint::BiometricsFingerprint(), cancel, enroll, enumerate, ErrorFilter, getAuthenticatorId (+40 more)

### Community 118 - "SoundRecorderService"
Cohesion: 0.09
Nodes (17): DeathRecipient, LifecycleService, Messenger, Notification, SoundRecording, UiStatus, BroadcastReceiver, Handler (+9 more)

### Community 119 - "ContractVersion"
Cohesion: 0.12
Nodes (6): CompatibilityResolver, parse(), VersionRange, ContractVersion, ContractCompatibilityResolverTest, ContractVersionTest

### Community 120 - "HandwaveSensor"
Cohesion: 0.07
Nodes (23): IntentFilter, SensorEventListener, Service, DozeService, BroadcastReceiver, IBinder, Intent, Override (+15 more)

### Community 121 - "SchemaValidator"
Cohesion: 0.16
Nodes (7): compute(), ContentHash, ByteArray, verify(), SchemaRegistry, ByteArray, TestVectors

### Community 122 - "ProdXConfirmationBridge"
Cohesion: 0.06
Nodes (18): CustomizationProvider, Subcomponent, SystemUIDialog, SysUIComponent, SysUISingleton, Builder, ReferenceSysUIComponent, ProdXAuthAdapter (+10 more)

### Community 123 - "ProdXRegistry"
Cohesion: 0.05
Nodes (22): ReadWriteLock, Creator, Override, Parcel, ProdXProviderManifest, Creator, Override, Parcel (+14 more)

### Community 124 - "TouchKeyHandler"
Cohesion: 0.07
Nodes (24): Activity, AudioManager, CameraManager, DeviceKeyHandler, Handler, KeyEvent, PowerManager, SparseIntArray (+16 more)

### Community 125 - "ErrorCode"
Cohesion: 0.05
Nodes (36): ErrorCategory, CONTRACTUAL, CRYPTO, SCHEMA, TRANSIENT, ErrorCode, BAD_EXTENSION_PAYLOAD, COMPATIBILITY_VIOLATION (+28 more)

### Community 126 - "ProdXAuthorityService"
Cohesion: 0.06
Nodes (10): Override, Stub, TargetUser, ProdXAuthorityService, PackageManager, ProdXComponentAttestation, ProdXKillSwitch, ProdXTamperEvidentEpoch (+2 more)

### Community 127 - "ProdXGrantStore"
Cohesion: 0.09
Nodes (10): Stub, Creator, Override, Parcel, ProdXGrant, Override, ProdXGrantAdminService, ArrayMap (+2 more)

### Community 128 - "ProdXCapabilityRequest"
Cohesion: 0.06
Nodes (19): IProdXProvider, SigningInfo, BrokerCheckpointStore, CheckpointEntry, BrokerHealth, BrokerService, IBinder, Intent (+11 more)

### Community 129 - "ProdXConfirmationController"
Cohesion: 0.10
Nodes (13): Challenge, Choice, ALLOW, DENY, ByteArray, ProdXConfirmationController, State, CANCELLED (+5 more)

### Community 130 - "NonNull"
Cohesion: 0.08
Nodes (10): ContextParams, Display, DisplayContext, FlaggedApi, NonNull, ServiceConnection, UiContext, BindServiceFlags (+2 more)

### Community 131 - "Context.java"
Cohesion: 0.12
Nodes (22): ContentCaptureClient, CursorFactory, DatabaseErrorHandler, DisplayAdjustments, IServiceConnection, LongDef, RavenwoodKeepPartialClass, SQLiteDatabase (+14 more)

### Community 132 - "FakeBrokerService"
Cohesion: 0.08
Nodes (5): FakeBrokerService, FakeTransaction, ByteArray, ProdXCapabilityRequest, ProdXBrokerIntegrationTest

### Community 133 - "ProdXManager"
Cohesion: 0.12
Nodes (5): IProdXSettingsMediator, IProdXAuthority, IProdXConfirmationCallback, IProdXRegistryObserver, ProdXManager

### Community 134 - "ProdXGrantListPreferenceController"
Cohesion: 0.06
Nodes (10): ProdXAuditLogPreferenceController, ProdXGrantListPreferenceController, ProdXHealthPreferenceController, ByteArray, ProdXModePreferenceController, Bundle, ByteArray, PreferenceFragmentCompat (+2 more)

### Community 135 - "AppListOverlay.kt"
Cohesion: 0.13
Nodes (20): ShortcutInfo, AppEntry, AppIndex, AppListAdapter, AppListOverlay, getPublishedShortcuts(), Activity, ImageView (+12 more)

### Community 136 - ".getContentResolver"
Cohesion: 0.09
Nodes (8): ConfigurationInfo, ContentObserver, Configuration, ContentResolver, Uri, UserIdInt, SettingObserver, UpdateConfigurationResult

### Community 137 - "ActivityRecord"
Cohesion: 0.08
Nodes (6): IVoiceInteractionSession, IVoiceInteractor, PictureInPictureParams, TransitionController, ActivityRecord, FileDescriptor

### Community 138 - "Parcelable"
Cohesion: 0.27
Nodes (4): Creator, Override, Parcel, ProdXCapabilityError

### Community 139 - "ProdXCapabilityDescriptor"
Cohesion: 0.08
Nodes (13): Creator, Override, Parcel, ProdXCapabilityDescriptor, Creator, Override, Parcel, ProdXCapabilityRequest (+5 more)

### Community 140 - "MainActivity"
Cohesion: 0.11
Nodes (6): AppCompatActivity, LauncherApps, Bundle, ContentObserver, RecyclerView, MainActivity

### Community 141 - "gpt-utils.cpp"
Cohesion: 0.15
Nodes (26): add_lun_to_update_list(), blk_rw(), map, string, vector, get_dev_path_from_partition_name(), get_scsi_node_from_bootdevice(), gpt2_set_boot_chain() (+18 more)

### Community 142 - "Final Architecture Investigation: `com.home.launcher`"
Cohesion: 0.07
Nodes (29): Application-Only Changes, Architecture Comparison, ART / Framework Source Evidence, Current Recents / Overview Runtime State, Executive Conclusion, Final Architecture Investigation: `com.home.launcher`, Final Recommendation, Framework Changes (+21 more)

### Community 143 - "DozeUtils"
Cohesion: 0.11
Nodes (10): BroadcastReceiver, SharedPreferences, BootCompletedReceiver, Intent, Override, Override, DozeUtils, Sensor (+2 more)

### Community 144 - "EventRecord"
Cohesion: 0.30
Nodes (4): EventPipeline, ByteArray, EventRecord, PipelineResult

### Community 145 - "CapabilityInventory"
Cohesion: 0.09
Nodes (6): NoOpManifest, CapabilityInventory, InventoryEntry, CapabilityManifest, ProviderManifest, RiskLevel

### Community 146 - ".enforceTaskPermission"
Cohesion: 0.11
Nodes (5): ITaskStackListener, PictureInPictureUiState, RootTaskInfo, TaskDescription, Rect

### Community 147 - "Uri"
Cohesion: 0.12
Nodes (5): CheckResult, PermissionMethod, PermissionResult, IBinder, Uri

### Community 148 - "ProdXPolicyDecision"
Cohesion: 0.13
Nodes (10): Parcelable, PrivateKey, PublicKey, SecureRandom, Creator, Override, Parcel, ProdXPolicyDecision (+2 more)

### Community 149 - "Lights"
Cohesion: 0.15
Nodes (21): BnLights, HwLight, led_type, HwLightState, ScopedAStatus, string, vector, HwLightState (+13 more)

### Community 150 - ".getResources"
Cohesion: 0.12
Nodes (10): ColorInt, ColorStateList, ExportedProperty, RavenwoodKeep, TypedArray, AttributeSet, PackageManager, Resources (+2 more)

### Community 151 - "MorphingEngine"
Cohesion: 0.14
Nodes (10): FrameLayout, IntArray, Interpolator, MotionEvent, ValueAnimator, MorphConfig, Rect, View (+2 more)

### Community 152 - "RecentAppsAdapter"
Cohesion: 0.11
Nodes (8): ImageButton, ImageView, RecyclerView, TextView, ViewGroup, RecentAppsAdapter, RecentTaskTile, TileViewHolder

### Community 153 - "ProdXExecutionAuthorization"
Cohesion: 0.12
Nodes (3): EventSummary, FakeObservationHub, ProdXObservationIntegrationTest

### Community 154 - "ProdXSettingsMediatorService"
Cohesion: 0.11
Nodes (7): Creator, Override, Parcel, ProdXAuditRecord, AuthorityActions, Override, ProdXSettingsMediatorService

### Community 155 - "What You Must Do When Invoked"
Cohesion: 0.08
Nodes (24): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+16 more)

### Community 156 - "SecurityMonitor"
Cohesion: 0.12
Nodes (8): IncidentRecord, IncidentSeverity, CRITICAL, HIGH, INFO, LOW, MEDIUM, SecurityMonitor

### Community 158 - "FakeProdXAuthorityService"
Cohesion: 0.10
Nodes (8): Creator, Override, Parcel, ProdXExecutionAuthorization, FakeProdXAuthorityService, IProdXConfirmationCallback, IProdXRegistryObserver, Override

### Community 159 - "DomainCode"
Cohesion: 0.09
Nodes (23): DomainCode, CAPABILITY_DISABLED, CAPABILITY_NOT_FOUND, EXECUTION_FAILED, GRANT_EXPIRED, GRANT_NOT_FOUND, GRANT_REVOKED, IDENTITY_BINDING_FAILED (+15 more)

### Community 162 - "ObservationQueue"
Cohesion: 0.13
Nodes (3): ByteArray, ObservationQueue, QueueEntry

### Community 163 - "TransactionStateMachine"
Cohesion: 0.14
Nodes (4): Result, ByteArray, TransactionRecord, TransactionStateMachine

### Community 164 - "SourceRegistry"
Cohesion: 0.13
Nodes (4): IProdXSourceAdapter, IBinder, SourceEntry, SourceRegistry

### Community 165 - "TouchscreenGestureSettings.java"
Cohesion: 0.12
Nodes (13): ListPreference, MenuItem, OnPreferenceStartFragmentCallback, TouchscreenGesture, Bundle, Override, Preference, PreferenceFragment (+5 more)

### Community 166 - "RecentTasks"
Cohesion: 0.12
Nodes (7): PointerEventListener, TaskPersister, ActivityTaskSupervisor, ComponentName, Resources, TaskChangeNotificationController, RecentTasks

### Community 168 - "Live Recent Cards — Real-time Surface Feed for Recents Grid"
Cohesion: 0.10
Nodes (19): Approach A: TaskOrganizer + SurfaceControl.mirrorSurface() (Recommended), Approach B: Display Mirror + Per-Task Crop (Simpler, Works Now), Current Architecture, File Manifest, Goal, Implementation, Implementation Sketch, Live Recent Cards — Real-time Surface Feed for Recents Grid (+11 more)

### Community 169 - "HealthReporter"
Cohesion: 0.16
Nodes (8): HealthRecord, HealthReporter, HealthSeverity, CRITICAL, DEGRADED, ERROR, OK, WARNING

### Community 170 - "ProviderState"
Cohesion: 0.16
Nodes (8): ProviderLifecycle, ProviderState, ACTIVE, CREATED, DESTROYED, ERROR, REGISTERED, SUSPENDED

### Community 171 - "AppendOnlyLedger"
Cohesion: 0.18
Nodes (4): AppendOnlyLedger, ByteArray, LedgerPartitionManager, RetentionManager

### Community 172 - "ConfirmationCoordinator.kt"
Cohesion: 0.14
Nodes (14): ProdXExecutionContext, Approved, ConfirmationCoordinator, ConfirmationResult, ConfirmationState, Denied, Failed, ByteArray (+6 more)

### Community 173 - "RecentTasks_decoded.java"
Cohesion: 0.15
Nodes (8): ActivityManagerService, ActivityTaskManager, ActivityInfo, ApplicationInfo, Bitmap, IBinder, Nullable, SparseArray

### Community 174 - "FileOutputStream"
Cohesion: 0.05
Nodes (29): CollapsingToolbarBaseActivity, Dialog, DialogFragment, DialogInterface, FileOutputStream, MainSwitchPreference, OnCheckedChangeListener, OnPreferenceChangeListener (+21 more)

### Community 178 - "ProdXMode"
Cohesion: 0.10
Nodes (15): createFromParcel(), describeContents(), fromValue(), Override, Parcel, newArray(), ProdXMode, DISABLED (+7 more)

### Community 180 - "MorphingEngine — Dynamic Island-style Animation Engine"
Cohesion: 0.12
Nodes (16): 1. `animation/MorphingEngine.kt` (~250 lines), 2. `animation/SpringInterpolator.kt` (~40 lines), 3. `animation/MorphConfig.kt` (~30 lines), Architecture, Caller Examples, Edge Cases, Files, Future Considerations (+8 more)

### Community 182 - "PlatformRecentTasksBackend"
Cohesion: 0.22
Nodes (4): ActivityManager, Bitmap, ComponentName, PlatformRecentTasksBackend

### Community 183 - "TestApi"
Cohesion: 0.12
Nodes (7): AutofillOptions, CanBeALL, CanBeCURRENT, ContentCaptureOptions, SuppressLint, TestApi, UserIdInt

### Community 184 - "VisibleForTesting"
Cohesion: 0.13
Nodes (3): UserInfo, ActivityRecord, VisibleForTesting

### Community 185 - "ProdXIndicatorController"
Cohesion: 0.17
Nodes (3): ByteArray, ProdXIndicatorController, ProdXIndicatorControllerTest

### Community 187 - "LeaseManager"
Cohesion: 0.22
Nodes (3): AuthorityPolicy, Lease, LeaseManager

### Community 188 - "ObservationService"
Cohesion: 0.15
Nodes (6): IBinder, Intent, Service, ObservationService, RuleDefinition, RuleEngine

### Community 189 - "Deployment Package Manifest"
Cohesion: 0.12
Nodes (15): 10. Intentionally Deferred Tasks, 1. Concise Architecture Summary, 2. Every Required Source Patch, 3. Every Required Build System Patch, 4. Every Required Product Configuration Patch, 5. Every Required Privapp Allowlist, 6. Draft SELinux Policy Files, 7. Exact Cloud VM Execution Checklist (+7 more)

### Community 190 - "LedgerHashChain"
Cohesion: 0.34
Nodes (5): DataInputStream, DataOutputStream, ByteArray, LedgerHashChain, LedgerRecord

### Community 191 - "build_hybrid_boot.py"
Cohesion: 0.29
Nodes (14): Path, align(), build_hybrid(), decode_varint(), extract_partition(), extract_payload(), field_values(), first_varint() (+6 more)

### Community 192 - "SystemSettingsStore"
Cohesion: 0.18
Nodes (3): PreferenceDataStore, ContentResolver, SystemSettingsStore

### Community 193 - "ConsumerDeliveryManager"
Cohesion: 0.21
Nodes (4): ConsumerDeliveryManager, ConsumerEndpoint, DeliveryRecord, EventRecord

### Community 194 - "HubShellCommand"
Cohesion: 0.20
Nodes (3): HubHealth, HubHealthProvider, HubShellCommand

### Community 195 - "Build, Sign & Install"
Cohesion: 0.13
Nodes (14): ADB path, AOSP / Soong Kotlin Compatibility Checks, Apksigner path, Build, Build, Sign & Install, Change Propagation Workflow, Combined quick cycle (incremental), Current VM / No-Flash Test Path (+6 more)

### Community 197 - "UnavailableRecentTasksBackend"
Cohesion: 0.06
Nodes (10): Bitmap, RecentTasksRepository, RecentTask, Bitmap, RecentTasksBackend, createBestBackend(), Bitmap, RecentTasksRepository (+2 more)

### Community 198 - "gpt_disk"
Cohesion: 0.14
Nodes (14): gpt_disk, block_size, devpath, hdr, hdr_bak, hdr_bak_crc, hdr_crc, is_initialized (+6 more)

### Community 199 - "ProdXAuditRecord"
Cohesion: 0.27
Nodes (4): ContractEnvelope, create(), Extension, ByteArray

### Community 200 - ".getSharedPreferences"
Cohesion: 0.16
Nodes (3): LinearLayout, TextView, View

### Community 201 - "ErrorCategory"
Cohesion: 0.15
Nodes (12): categorizeError(), ErrorCategory, AUTHENTICATION, AUTHORIZATION, EXECUTION, INTERNAL, NETWORK, POLICY_VIOLATION (+4 more)

### Community 202 - "BrokerCheckpointStore"
Cohesion: 0.24
Nodes (4): BackgroundActivityStartCallback, CompatibilityInfo, ApplicationInfo, Nullable

### Community 203 - "RedactionPipeline"
Cohesion: 0.20
Nodes (3): EventRecord, RedactionPipeline, RedactionRule

### Community 204 - "TouchscreenGesture"
Cohesion: 0.22
Nodes (10): getSupportedGestures_cb, ITouchscreenGesture, Gesture, Return, GestureInfo, map, TouchscreenGesture, getSupportedGestures (+2 more)

### Community 205 - "ProdXHealth"
Cohesion: 0.19
Nodes (4): Creator, Override, Parcel, ProdXHealth

### Community 206 - "AuditCorrelationHelper"
Cohesion: 0.31
Nodes (3): AuditContext, AuditCorrelationHelper, CorrelationChain

### Community 207 - "FileLedgerBackend"
Cohesion: 0.29
Nodes (5): FileLedgerBackend, InMemoryLedgerBackend, ByteArray, LedgerBackend, RecoveryResult

### Community 208 - "TransactionPhase"
Cohesion: 0.15
Nodes (9): TransactionPhase, AUTHORIZATION, CANCELLED, COMPLETION, CONFIRMATION, DISPATCH, FAILED, PROPOSAL (+1 more)

### Community 209 - "RuleEngine.kt"
Cohesion: 0.27
Nodes (10): And, ConditionResult, Not, Or, PatternMatch, RuleCondition, RuleEvaluationContext, RuleMatch (+2 more)

### Community 210 - "TouchscreenGesture"
Cohesion: 0.24
Nodes (10): BnTouchscreenGesture, Gesture, ScopedAStatus, vector, GestureInfo, map, TouchscreenGesture, getSupportedGestures (+2 more)

### Community 211 - "AsusParts"
Cohesion: 0.27
Nodes (4): Creator, Override, Parcel, ProdXCapabilityResponse

### Community 212 - "AuditService"
Cohesion: 0.19
Nodes (5): AuditService, ByteArray, IBinder, Intent, Service

### Community 214 - "FakeAuthorityService"
Cohesion: 0.27
Nodes (4): Creator, Override, Parcel, ProdXTransactionReference

### Community 215 - "CollapsingToolbarBaseActivity"
Cohesion: 0.33
Nodes (5): AuditHealth, CORRUPT, HEALTHY, READ_ONLY, UNAVAILABLE

### Community 216 - ".revalidate"
Cohesion: 0.41
Nodes (9): ProdXToken, audienceMismatch(), AuthorizationRevalidator, epochMismatch(), expired(), invalid(), ProdXExecutionAuthorization, RevalidationResult (+1 more)

### Community 217 - "FakeSecureSettingsRepository"
Cohesion: 0.24
Nodes (3): SecureSettingsRepository, FakeSecureSettingsRepository, Flow

### Community 218 - "Lifecycle"
Cohesion: 0.22
Nodes (4): SystemService, TargetUser, WindowManagerGlobalLock, Lifecycle

### Community 220 - "extract_payload.py"
Cohesion: 0.25
Nodes (10): decode_field(), decode_varint(), extract_from_zip(), find_partition_in_payload(), parse_protobuf_fields(), Extract boot.img from OTA payload.bin inside zip files., Extract boot.img from an OTA zip's payload.bin., Decode a protobuf field, return (field_number, wire_type, value, new_offset) (+2 more)

### Community 221 - "ProdXExtensionManifest"
Cohesion: 0.24
Nodes (4): Creator, Override, Parcel, ProdXExtensionManifest

### Community 222 - "ProdXSubscriptionLease"
Cohesion: 0.24
Nodes (4): Creator, Override, Parcel, ProdXSubscriptionLease

### Community 223 - "SyntheticObservationSource"
Cohesion: 0.26
Nodes (4): ByteArray, EventRecord, StructuredSyntheticRecord, SyntheticObservationSource

### Community 224 - "ProposalValidator"
Cohesion: 0.29
Nodes (5): invalid(), ProdXCapabilityRequest, ProposalValidator, valid(), ValidationResult

### Community 226 - ".getRecentTasksImpl"
Cohesion: 0.22
Nodes (3): ArraySet, ParceledListSlice, RecentTaskInfo

### Community 227 - "SleepTokenAcquirerImpl"
Cohesion: 0.22
Nodes (5): ActivityInterceptorCallback, SleepToken, SleepTokenAcquirer, SparseArray, SleepTokenAcquirerImpl

### Community 228 - "FakeSystemSettingsRepository"
Cohesion: 0.27
Nodes (3): SystemSettingsRepository, FakeSystemSettingsRepository, Flow

### Community 229 - "AppendOnlyLedgerTest"
Cohesion: 0.27
Nodes (3): ByteArray, PrivacyRedactor, AppendOnlyLedgerTest

### Community 231 - "GloveMode"
Cohesion: 0.33
Nodes (5): BnGloveMode, ScopedAStatus, GloveMode, getEnabled, setEnabled

### Community 232 - "GloveMode"
Cohesion: 0.33
Nodes (5): IGloveMode, Return, GloveMode, isEnabled, setEnabled

### Community 233 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 234 - "parse_payload3.py"
Cohesion: 0.33
Nodes (8): decode_varint(), parse_group_fields(), parse_partition_from_group(), Parse payload manifest using protobuf groups for PartitionUpdate., Skip one protobuf field, return new offset., Parse fields within a group., Parse PartitionUpdate group fields., skip_field()

### Community 236 - "NoOpTestProviderService"
Cohesion: 0.22
Nodes (4): IBinder, Intent, Service, NoOpTestProviderService

### Community 239 - "State"
Cohesion: 0.22
Nodes (6): State, ACTIVE, CANCELLED, COMPLETED, EXPIRED, TransactionReservation

### Community 240 - "DependencyResolver"
Cohesion: 0.33
Nodes (3): DependencyGraph, DependencyNode, DependencyResolver

### Community 242 - "10. `getRecentTasks()` Framework Execution Path"
Cohesion: 0.22
Nodes (9): 10.1 The Initial Hypothesis (Refuted), 10.2 Complete Call Flow, 10.3 Filtering Pipeline (in order), 10.4 `isVisibleRecentTask()` — the Actual Gate, 10.5 What `mTasks` Actually Contains, 10.6 How QuickStep Actually Gets Tasks, 10.7 Why the First Call Showed "1 raw task", 10.8 Conclusion: getRecentTasks() Works Correctly (+1 more)

### Community 243 - "dng_xmp_sdk::PackageForJPEG"
Cohesion: 0.29
Nodes (8): AutoPtr, dng_host, dng_memory_allocator, dng_memory_block, uint32, dng_xmp_sdk::PackageForJPEG(), dng_xmp_sdk::Parse(), dng_xmp_sdk::Serialize()

### Community 246 - "NoOpCapabilities"
Cohesion: 0.39
Nodes (3): ByteArray, NoOpCapabilities, NoOpCapability

### Community 247 - "AuthorizationVerifier.kt"
Cohesion: 0.43
Nodes (5): AuthorizationResult, AuthorizationVerifier, Denied, Granted, ByteArray

### Community 251 - "LegacyCameraProviderImpl_2_5.cpp"
Cohesion: 0.33
Nodes (5): DeviceState, hidl_bitfield, CameraProvider<LegacyCameraProviderImpl_2_5>, Return, LegacyCameraProviderImpl_2_5::notifyDeviceStateChange()

### Community 252 - "dng_string"
Cohesion: 0.29
Nodes (7): dng_string, dng_xmp_sdk::GetAltLangDefault(), dng_xmp_sdk::GetNamespacePrefix(), dng_xmp_sdk::GetString(), dng_xmp_sdk::GetStructField(), dng_xmp_sdk::SetAltLangDefault(), dng_xmp_sdk::SetString()

### Community 253 - "SystemSettingSwitchPreference.java"
Cohesion: 0.38
Nodes (4): SwitchPreferenceCompat, AttributeSet, Override, SystemSettingSwitchPreference

### Community 254 - "parse_payload2.py"
Cohesion: 0.43
Nodes (6): decode_varint(), parse_partition_update(), Parse OTA payload properly - extract boot partition info from protobuf manifest., Skip one protobuf field, return new offset., Parse PartitionUpdate message, return (name, size, operations_list, new_offset)., skip_field()

### Community 255 - "patch_boot_ramdisk_property.py"
Cohesion: 0.52
Nodes (6): align(), legacy_boot_id(), main(), patch_boot(), patch_newc_file(), Patch one property in a boot v0 gzip/newc ramdisk and emit a raw boot image.

### Community 256 - "ProdXCapabilityActivity"
Cohesion: 0.38
Nodes (4): Activity, Bundle, LinearLayout, ProdXCapabilityActivity

### Community 257 - "ExtensionService"
Cohesion: 0.33
Nodes (4): ExtensionService, IBinder, Intent, Service

### Community 260 - "11. Decision Matrix"
Cohesion: 0.29
Nodes (7): 11.1 SELinux Restrictions, 11.2 Permission Restrictions, 11.3 Framework Filtering Restrictions, 11.4 Other Restrictions, 11.5 What Baking Into `/system_ext/priv-app/` Would Change, 11.6 Actual Priority Order for Fixes, 11. Decision Matrix

### Community 261 - "9. Summary: Application Privilege Level & Capability Mapping"
Cohesion: 0.29
Nodes (7): 9.1 Current State, 9.2 What Works (and Why), 9.3 What Doesn't Work (and Why), 9.4 What Would Change vs Baked-in System App, 9.5 What Would Be Required for `/proc/stat` Access, 9.6 Hidden API Dependency Summary for ROM Integration, 9. Summary: Application Privilege Level & Capability Mapping

### Community 262 - "graphify reference: query, path, explain"
Cohesion: 0.33
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 265 - "PipelineStage"
Cohesion: 0.33
Nodes (6): PipelineStage, DELIVER, QUEUE, RECEIVE, REDACT, VALIDATE

### Community 266 - "QuickStepService"
Cohesion: 0.33
Nodes (4): IBinder, Intent, Service, QuickStepService

### Community 268 - "AsusPartsTileService"
Cohesion: 0.50
Nodes (3): TileService, AsusPartsTileService, Override

### Community 269 - "extract-files.sh"
Cohesion: 0.40
Nodes (4): DEVICE, DEVICE_COMMON, extract-files.sh script, VENDOR

### Community 270 - "setup-makefiles.sh"
Cohesion: 0.40
Nodes (4): DEVICE, DEVICE_COMMON, setup-makefiles.sh script, VENDOR

### Community 273 - "ProdXDeveloperOptionsFragment"
Cohesion: 0.40
Nodes (3): Bundle, PreferenceFragmentCompat, ProdXDeveloperOptionsFragment

### Community 275 - "Investigation Report: `com.home.launcher` Runtime Environment & Framework Analysis"
Cohesion: 0.40
Nodes (4): 4.1 Policy State, 4.2 Observed Logcat, 4. Hidden API Enforcement, Investigation Report: `com.home.launcher` Runtime Environment & Framework Analysis

### Community 276 - "12. Final Verification: Remaining Uncertainties"
Cohesion: 0.40
Nodes (5): 12.1 SELinux Domain Assignment — Complete Decision Chain, 12.2 Hidden API Enforcement — Why Reflection Succeeds, and What Would Fail on Stock, 12.3 QuickStep Recents Pipeline — How Modern Recents Actually Works, 12.4 Feature Classification — Complete Matrix, 12. Final Verification: Remaining Uncertainties

### Community 277 - "2. Runtime Identity"
Cohesion: 0.40
Nodes (5): 2.1 Process Information, 2.2 SELinux Context, 2.3 Why `platform_app` Domain?, 2.4 Capabilities of `platform_app` Domain, 2. Runtime Identity

### Community 278 - "5. SELinux Mode & Denials"
Cohesion: 0.40
Nodes (5): 5.1 Mode, 5.2 Denials — `/proc/stat`, 5.3 Denials — `/sys/class/thermal`, 5.4 Cross-Domain Comparison, 5. SELinux Mode & Denials

### Community 279 - "6. `/proc/stat` and `/sys/class/thermal` Accessibility"
Cohesion: 0.40
Nodes (5): 6.1 SELinux Labels, 6.2 Accessibility Tests, 6.3 Root Cause Analysis, 6.4 Minimal Policy Fix, 6. `/proc/stat` and `/sys/class/thermal` Accessibility

### Community 280 - "HomeLauncher ROM Integration Package"
Cohesion: 0.40
Nodes (4): Files, HomeLauncher ROM Integration Package, Mandatory For First ROM-Bundled Launcher Boot, Optional Future Work

### Community 281 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 282 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 283 - "graphify reference: incremental update and cluster-only"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

### Community 284 - "dng_xmp_private"
Cohesion: 0.50
Nodes (3): SXMPMeta, dng_xmp_private, fMeta

### Community 285 - "init.qcom.early_boot.sh"
Cohesion: 0.83
Nodes (3): set_density_by_fb(), set_perms(), init.qcom.early_boot.sh script

### Community 286 - "init.qcom.post_boot.sh"
Cohesion: 0.83
Nodes (3): configure_memory_parameters(), configure_read_ahead_kb_values(), init.qcom.post_boot.sh script

### Community 287 - "parse_payload.py"
Cohesion: 0.67
Nodes (3): decode_varint(), parse_manifest_fields(), Parse OTA payload protobuf manifest using google.protobuf. Dynamically compile t

### Community 288 - "parse"
Cohesion: 0.29
Nodes (5): Result, parse(), parse(), parse(), ParsedIdentifier

### Community 292 - "1. APK Installation & Classification"
Cohesion: 0.50
Nodes (4): 1.1 APK Path and Partition, 1.2 PackageManager Classification, 1.3 Signing & Certificate, 1. APK Installation & Classification

### Community 293 - "3. Permissions"
Cohesion: 0.50
Nodes (4): 3.1 All Permissions (Requested & Granted), 3.2 Runtime Permissions (User-granted), 3.3 AppOps, 3. Permissions

### Community 294 - "7. Hidden API / Reflection Usage Audit"
Cohesion: 0.50
Nodes (4): 7.1 Complete Inventory, 7.2 Categorization, 7.3 Additional Framework Usage (Non-Hidden, But Privileged), 7. Hidden API / Reflection Usage Audit

### Community 295 - "8. Build Configuration"
Cohesion: 0.50
Nodes (4): 8.1 Gradle Configuration, 8.2 AndroidManifest Configuration, 8.3 Post-Build Re-signing, 8. Build Configuration

### Community 297 - "dng_xmp_sdk::GetStringList"
Cohesion: 0.67
Nodes (3): dng_abort_sniffer, dng_string_list, dng_xmp_sdk::GetStringList()

### Community 298 - "dng_local_string"
Cohesion: 0.67
Nodes (3): dng_local_string, dng_xmp_sdk::GetLocalString(), dng_xmp_sdk::SetLocalString()

### Community 299 - "dng_xmp_sdk"
Cohesion: 0.67
Nodes (3): dng_xmp_sdk, dng_xmp_sdk::dng_xmp_sdk(), dng_xmp_sdk::ReplaceXMP()

### Community 304 - "update_data"
Cohesion: 0.67
Nodes (3): update_data, lun_list, num_valid_entries

## Knowledge Gaps
- **562 isolated node(s):** `RiskLevel`, `OK`, `DEGRADED`, `WARNING`, `ERROR` (+557 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **199 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Context` connect `Context` to `NonNull`, `Context.java`, `ProdXManager`, `inParm Calibration PR2-2`, `ProdXCapabilityDescriptor`, `MainActivity`, `DozeUtils`, `Uri`, `.getResources`, `ProdXSettingsMediatorService`, `TouchscreenGestureSettings.java`, `FileOutputStream`, `.getPackageManager`, `TestApi`, `UnavailableRecentTasksBackend`, `.getSharedPreferences`, `AuditService`, `Lifecycle`, `SystemServer`, `AudioFxService`, `Intent`, `ATMS_decoded.java`, `HandwaveSensor`, `TouchKeyHandler`, `SystemSettingSwitchPreference.java`, `ProdXAuthorityService`, `ProdXGrantStore`?**
  _High betweenness centrality (0.205) - this node is a cross-community bridge._
- **Why does `ActivityTaskManagerService` connect `inParm Calibration PR2-2` to `.enforceNotIsolatedCaller`, `SleepTokenAcquirerImpl`, `.startActivity`, `RecentTasks`, `ProdX Core Services`, `.getContentResolver`, `ActivityRecord`, `BrokerCheckpointStore`, `FileOutputStream`, `HomeLauncher Integration`, `SystemServer`, `Context`, `.enforceTaskPermission`, `ATMS_decoded.java`, `.getResources`, `Lifecycle`, `ProdXGrantStore`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Why does `LedgerPartitionManager` connect `AppendOnlyLedger` to `AuditService`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **What connects `RiskLevel`, `OK`, `DEGRADED` to the rest of the system?**
  _562 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `inParm Touch Calibration PR2-1` be split into smaller, more focused modules?**
  _Cohesion score 0.08045977011494253 - nodes in this community are weakly interconnected._
- **Should `ASUS I001D Device Parameters` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._
- **Should `Android 16 Bliss Reconstruction` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._