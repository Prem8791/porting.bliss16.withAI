# Android 16 I001D Reconstruction Progress

Date: 2026-07-14

## Current Goal

Maintain the booting Bliss Waterlily Android 16 reconstruction for ASUS I001D,
validate the latest post-SELinux build, finish the remaining runtime/audio and
release-signing work, and prepare a reliable local Ubuntu build environment.

## Canonical State Snapshot

This file is the live source of truth for the reconstruction work. If other notes disagree, trust this section and the latest checkpoints below.

Current stable state:

- Latest successful and retained build:
  `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713-cpu-sepolicy.zip`.
- Size: `1,482,821,089` bytes.
- SHA-256:
  `967be99ea20d2114ab2e00e63c99fc4ecbc521b2efe59921186d2f00fe095844`.
- The local build is byte-identical to the current VM output at
  `out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713.zip`.
- The device boots. HomeLauncher CPU reading was proven functional and the
  SELinux policy correction is included in this latest build; it still requires
  a post-flash runtime confirmation on the retained image.
- Audio remains a separate open issue. The latest live trace identified Jelly
  source/track attenuation (`-25 dB`) rather than a failed HAL or amplifier;
  compare against ringtone/local playback before changing mixer or calibration.
- Release signing remains open. Generate and back up owner-controlled private
  release keys outside the VM before wiring release signing.
- The authoritative ProdX architecture investigation is preserved in
  `artifacts/ProdX-Runtime-Architecture-Foundation-20260714.zip`, SHA-256
  `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec`.
- The implementation-ready, architecture-only immutable contract design is
  `artifacts/ProdX-Runtime-Contract-Specification-20260714.md`, SHA-256
  `7c7743700c468847ef547bbbc22dcc9d1b027df0fee9e92895faee071706d886`.
  It is the single source of truth for ProdX runtime object semantics; no
  production code or Android transport/interface design has begun.
- The physical Android placement blueprint is
  `artifacts/ProdX-Runtime-Skeleton-Specification-20260714.md`, SHA-256
  `f332bace6730a290dcaff5a7703d8e499eac08dcd309cee63a31248997bffbd6`.
  It is the single source of truth for runtime repositories, modules, processes,
  SELinux domains, startup, recovery, updates, and provider-family placement;
  Binder/interface and production implementation design has not begun.
- The authoritative master P0 engineering plan is
  `artifacts/ProdX-P0-Runtime-Implementation-Specification-20260714.md`, SHA-256
  `c3ef5e20208ebc13fdb8bd43e16dae770ecddd9ddcb8a19a8a22d57a00b33152`.
  It defines the deterministic implementation sequence, targets, dependencies,
  interfaces, tests, gates and rollback from an empty Android tree; it contains
  no production source or build/security/interface artifacts.
- ProdX P0-00 is complete on the VM. The locked engineering baseline is at
  `/home/premanandal1978/android/waterlily/packages/modules/ProdX/tests/reference`.
  Its `BASELINE-FILES.sha256` manifest has SHA-256
  `24731f2c4d85c5595bd108720b891c1b95c70c8c372d1a0ae5667ad3f6831ae9`.
  G0 is complete; P0-01 is the next permitted ProdX milestone.
- The VM is Ubuntu Server 22.04.5 LTS with 12 vCPUs and 62 GiB RAM. The planned
  local HP EliteBook build host has a Ryzen 7 8840U, one removable 16 GiB
  DDR5-5600 SO-DIMM, a second memory slot, and firmware-reported 64 GiB maximum.
  A 64 GiB RAM and 1 TB-or-larger SSD configuration is recommended.
- All older reports, TODO notes, boot/debug artifacts, build ZIPs and duplicate
  architecture working trees are superseded by this canonical file, the latest
  ROM, and the curated ProdX archive.

Open work, in priority order:

1. Flash/identify the retained build and verify HomeLauncher CPU stats under
   enforcing SELinux.
2. Re-test speaker volume with Jelly and an independent ringtone/local source.
3. Resolve only evidence-backed remaining runtime issues discovered on that
   image; do not revive superseded planning queues automatically.
4. Establish owner-controlled release keys and signing/backup procedure.
5. Prepare the local Ubuntu build host or continue full builds on the VM.

Do not rely on older checkpoint text for current status without checking this snapshot first.

## Required Missing Source Inputs

The official Bliss 19.6 I001D build references six private source projects:

- `device/asus/I001D`
- `device/asus/sm8150-common`
- `hardware/asus`
- `kernel/asus/I001D`
- `vendor/asus/I001D`
- `vendor/asus/sm8150-common`

Private official upstreams are under `StudioKeys-Dumps` and are not currently accessible. Until access is granted, the working path is reconstruction from public Android 14 `universe` baselines plus OTA/myBliss extracted artifacts.

## Local State

- Workspace handover: `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\handover.md`
- Local staging root: `D:\tmp\waterlily-i001d-reconstruction`
- myBliss analysis clone: `D:\tmp\myBliss-analysis`
- Relevant upstream/reference repo: `https://github.com/Prem8791/myBliss`

Local baseline repositories are present in both:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\baseline`
- `D:\tmp\waterlily-i001d-reconstruction\baseline`

Usable Windows baseline check:

- `device_asus_I001D`: present at `66473d931666769a627e65c6ee24b7f05fa61425`
- `device_asus_sm8150-common`: present at `6cae628f8f24c75eac1d2a1759ac31df6dc25d77`
- `vendor_asus_I001D`: present at `90c9d99e15b4df3e81fbba89ecdccfb29d5f98ba`
- `vendor_asus_sm8150-common`: present at `b5f485169923f41bf3588df159e1351f5f4edb1b`
- `kernel_asus_I001D`: `.git` exists, but working tree is effectively empty on Windows because checkout failed on reserved path `aux.c`

## myBliss Artifact State

`D:\tmp\myBliss-analysis\android16` contains:

- Findings/report material
- Official build manifest fragments
- Partial patches for `device/asus/I001D`, `device/asus/sm8150-common`, fstab, init, VINTF, kernel config, and vendor manifest
- Extracted Android 16 device/common source-like files under `android16\device\asus`
- Android 16 permissions, ramdisk contexts, VINTF fragments, init rc files, sepolicy snapshots, kernel config, and vendor path/hash inventories
- HomeLauncher backup and integration material

The partial patch files alone are not sufficient for a full port. The extracted `android16\device\asus` tree is the more useful reconstruction input.

## VM State

VM:

- External IP: `8.230.119.36`
- GCE instance: `instance-20260710-230647`
- Zone: `us-south1-a`
- Project: `customrom-501702`
- SSH user from gcloud: `home`
- Target user home on VM: `/home/premanandal1978`
- `home` has passwordless sudo

Current VM capacity observed:

- Root filesystem: 485G total, 186G used, 300G available
- RAM: 62Gi total, about 61Gi available
- Swap: none

This is better than the older analysis note. RAM now meets the Bliss recommendation, but available disk is still below the 500G recommendation for a full source/build workflow.

VM checkout:

- Waterlily platform checkout exists at `/home/premanandal1978/android/waterlily`
- It has platform directories such as `art`, `bionic`, `build`, `frameworks`, `hardware`, `vendor`
- It currently does not have `.repo/local_manifests`
- It currently does not have `device/asus`, `vendor/asus`, or `kernel/asus`
- It has reference material at `/home/premanandal1978/android/waterlily/reference-material`

Populated VM baseline paths:

- `/home/premanandal1978/android/waterlily/device/asus/I001D` at `66473d931666769a627e65c6ee24b7f05fa61425`
- `/home/premanandal1978/android/waterlily/device/asus/sm8150-common` at `6cae628f8f24c75eac1d2a1759ac31df6dc25d77`
- `/home/premanandal1978/android/waterlily/vendor/asus/I001D` at `90c9d99e15b4df3e81fbba89ecdccfb29d5f98ba`
- `/home/premanandal1978/android/waterlily/vendor/asus/sm8150-common` at `b5f485169923f41bf3588df159e1351f5f4edb1b`
- `/home/premanandal1978/android/waterlily/kernel/asus/I001D` at `368dd4099045c66ae294f4a9d3717d615920c329`

The kernel checkout is valid on the VM/Linux. The Windows kernel baseline remains unusable because of the reserved `aux.c` path issue.

VM reference material currently contains only the reduced bundle:

- `findings.android16.md`
- `bringup-i001d-to-waterlily.sh`
- `local_manifests/waterlily-i001d.xml`
- patch files

It does not yet show the full extracted `android16/device/asus` artifact set in the Waterlily tree.

Update:

- Uploaded the full local `D:\tmp\myBliss-analysis\android16` artifact set to the VM.
- VM destination: `/home/premanandal1978/android/waterlily/reference-material-full/android16`
- Verified size on VM: about `11M`

## Current Status

Not ready yet.

The Waterlily platform base exists on the VM, but the six I001D-specific missing source inputs are not populated in the checkout. The next step is to locate or upload the full extracted myBliss Android 16 artifacts, then reconstruct/populate the missing project paths in the VM Waterlily tree in small groups.

## Next Actions

1. Check whether the VM still has the full OTA extraction or full `myBliss` artifact tree somewhere outside `reference-material`. Done: not present.
2. If not present, upload the local full `D:\tmp\myBliss-analysis\android16` artifact tree to the VM. Done.
3. Populate `device/asus/I001D` and `device/asus/sm8150-common` first, because they define the product and service surface. In progress: public baselines cloned.
4. Reconstruct or stub `hardware/asus` only after identifying what depends on it.
5. Recreate vendor trees from public Android 14 vendor baselines plus Android 16 blob inventories and available OTA blobs.
6. Clone/check out the kernel on Linux/VM, then apply Android 16 kernel config evidence.
7. Mark the missing sources ready only after the six paths exist in the Waterlily checkout and their basic product discovery/build parsing passes.

## 2026-07-11 Update

Progress since the previous checkpoint:

- Uploaded full `myBliss-analysis/android16` artifact bundle to VM at `/home/premanandal1978/android/waterlily/reference-material-full/android16`.
- Cloned public Android 14 `universe` baselines into the VM Waterlily tree:
  - `device/asus/I001D`
  - `device/asus/sm8150-common`
  - `vendor/asus/I001D`
  - `vendor/asus/sm8150-common`
  - `kernel/asus/I001D`
- Applied initial Android 16 reconstruction changes:
  - I001D product/lunch patch
  - I001D fstab from Android 16 OTA reference
  - sm8150-common init.target changes
  - sm8150-common VINTF manifest and fragment copy
  - major service package migrations in `msmnile.mk`
- Reconstructed the missing AIDL ASUS touch service as `vendor.lineage.touch-service.asus_msmnile` using current Lineage AIDL interfaces and old I001D Goodix sysfs paths.
- Disabled obsolete HIDL vibrator service module `android.hardware.vibrator@1.2-service.I001D`; Android 16 uses `vendor.qti.hardware.vibrator.service`.
- Removed stale missing `vendor/bliss/config/device_framework_matrix.xml` matrix reference from `BoardConfigCommon.mk`.
- Verified `lunch bliss_I001D bp4a userdebug` succeeds and reports Bliss Waterlily / Android 16 / BP4A config.

Current blocker:

- Targeted module build `m vendor.lineage.touch-service.asus_msmnile` reaches Soong analysis but fails on `vendor.lineage.health-service.default` because `lineage_health.charging_control_supports_bypass` is being typed as bool while the module select expects string-like branches.

Current readiness state:

- Missing sources are not ready yet.
- Product discovery works.
- Main remaining source-readiness pass is clearing Soong/module analysis and then checking product package resolution.

## 2026-07-11 Kati Package Validation Blocker

The targeted module build reached Kati package validation and failed because `device/asus/I001D/bliss_I001D.mk` includes `device/asus/sm8150-common/msmnile.mk`, which still lists Android 14-era package names that no longer exist in the Android 16 Waterlily tree.

Missing/offending packages reported:

- `AntHalService-Soong`
- `android.hardware.audio.sounddose-vendor-impl`
- `android.hardware.vibrator@1.2-service.I001D`
- `android.hidl.base@1.0_system`
- `android.hidl.manager@1.0_system`
- `gralloc.msmnile`
- `hwcomposer.msmnile`
- `libOmxAacEnc`
- `libOmxAmrEnc`
- `libOmxEvrcEnc`
- `libOmxG711Enc`
- `libOmxQcelp13Enc`
- `libdisplayconfig.qti.vendor`
- `libjson.vendor`
- `libxml.vendor`
- `memtrack.msmnile`
- `sound_trigger.primary.msmnile`

Next action: remove or replace these stale package names in `device/asus/sm8150-common/msmnile.mk`, then rerun `m vendor.lineage.touch-service.asus_msmnile`.

## 2026-07-11 Stale Package Cleanup

Checked VM file `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/msmnile.mk` directly.

After the user's attempted cleanup, three stale entries remained:

- `sound_trigger.primary.msmnile`
- `android.hidl.base@1.0_system`
- `libOmxEvrcEnc`

Removed those entries on the VM and verified the full previous offending-entry grep now returns no matches.

Next handoff command: rerun targeted module build `m vendor.lineage.touch-service.asus_msmnile` from the Waterlily tree.

## 2026-07-11 VM File Check Before Build Handoff

Checked `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/msmnile.mk` directly on the VM.

State:

- Previous stale package grep now returns no matches.
- Fixed one Make syntax hazard left by stale package removal: `tinymix \` was changed to `tinymix` so `AUDIO_HAL_DIR := ...` is not accidentally continued into `PRODUCT_PACKAGES`.

Build task handed to user: rerun targeted module build from `/home/premanandal1978/android/waterlily`.

## 2026-07-11 Single Remaining Stale Package

User reran targeted build after `msmnile.mk` cleanup. Kati now reports only one missing PRODUCT_PACKAGES entry:

- `android.hardware.vibrator@1.2-service.I001D`

The module itself was disabled in `device/asus/I001D/vibrator/Android.bp`, but a product makefile still references it. Android 16 path uses `vendor.qti.hardware.vibrator.service`; remove the old HIDL I001D vibrator package reference from device product makefiles and rerun the targeted touch build.

## 2026-07-11 Agent Workflow Rule

User instruction recorded for future agents:

- Agent handles VM file/source modifications directly.
- Agent does not run Android build processes by default.
- For builds, agent provides exact VM-side commands to the user and waits for reported output.
- Keep this progress file updated as reconstruction state changes.

Instruction files added:

- `AGENTS.md`
- `.agents/instructions.md`

## 2026-07-11 Old I001D Vibrator Product Reference Removed

Handled VM modification directly per agent workflow rule.

Change:

- Removed obsolete `android.hardware.vibrator@1.2-service.I001D` PRODUCT_PACKAGES block from `/home/premanandal1978/android/waterlily/device/asus/I001D/device.mk`.

Verification:

- `grep -R android.hardware.vibrator@1.2-service.I001D -n device/asus/I001D/*.mk device/asus/sm8150-common/*.mk` returns no matches.

Next build handoff:

- User should rerun `m vendor.lineage.touch-service.asus_msmnile`.

## 2026-07-11 Git LFS Check

Checked VM Waterlily checkout for Git LFS state.

Observed:

- `git-lfs` is installed on the VM: `git-lfs/3.0.2`.
- Bliss Waterlily manifest README says to initialize with `repo init ... --git-lfs`.
- `.repo/manifests/.git/config` and `.repo/manifests.git/config` contain `repo.git-lfs = true`.
- The same config also has LFS smudge/process set to `--skip`.

Conclusion:

- The checkout appears initialized with repo LFS awareness, but this does not prove all LFS objects are hydrated in every project.
- We have not explicitly run a full `repo sync --force-sync` / LFS pull audit during this reconstruction pass.
- If future build failures show tiny pointer files or missing large binary assets, run an LFS hydration/audit pass before treating the source tree as complete.

## 2026-07-11 Removed VNDK v29 libc++ Copy

Handled VM modification directly.

Build failure was:

- Missing `prebuilts/vndk/v29/arm64/arch-arm64-armv8-a/shared/vndk-sp/libc++.so`, needed by vendor `libc++.so` copy.

Change:

- Removed stale Android 14 `PRODUCT_COPY_FILES` stanza from `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/msmnile.mk`:
  - `prebuilts/vndk/v29/.../libc++.so:$(TARGET_COPY_OUT_VENDOR)/lib64/libc++.so`

Reason:

- Waterlily checkout has VNDK v31/v32/v33/v34, not v29.
- Directly copying old VNDK libc++ into vendor is an Android 14-era compatibility workaround and should not be blindly remapped to a newer VNDK path.

Next build handoff:

- User should rerun `m vendor.lineage.touch-service.asus_msmnile`.

## 2026-07-11 AIDL Touch Service Build Passed

User reran targeted build:

```bash
m vendor.lineage.touch-service.asus_msmnile
```

Result:

- Build completed successfully.
- Reconstructed AIDL ASUS touch service now compiles in the Waterlily tree.

Notes:

- Build still prints duplicate-install warnings for some vendor prebuilts, e.g. `libdynproc.so` and `vendor.qti.hardware.camera.device@1.0.so`. These are warnings, not current blockers.

Next validation handoff:

- Run `m nothing` to expose remaining product/package/Soong issues without attempting a full ROM image build.

## 2026-07-11 `m nothing` Passed

User ran broader validation:

```bash
m nothing
```

Result:

- Build completed successfully.
- Makefiles were read successfully.
- Product discovery, Soong analysis, and Kati/Make parsing are coherent enough for the reconstructed I001D Waterlily sources.

Remaining warnings:

- Duplicate install warnings still appear for vendor prebuilts, including `vendor.qti.hardware.camera.device@1.0.so` and earlier `libdynproc.so`.
- These warnings are not current blockers but should be reconciled during vendor blob cleanup.

Current readiness state:

- Initial missing device tree/common tree source reconstruction is now build-parse ready.
- The reconstructed AIDL ASUS touch service builds.
- Remaining source readiness areas are vendor blob reconciliation, kernel/config/module reconciliation, duplicate prebuilt cleanup, and then actual image/ROM build validation.

## 2026-07-11 Kernel Toolchain Blocker

User ran limited image build and hit kernel toolchain failures:

- Missing Clang path: `prebuilts/clang/host/linux-x86/clang-r416183b1/bin/clang`
- Kernel wrapper also failed under Python 3 due Python 2 syntax: `print >> sys.stderr`
- Build log also showed missing GCC path in wrapper error handling, but current VM checkout contains the expected GCC 4.9 prebuilts.

Handled VM modifications directly:

- Changed `device/asus/sm8150-common/BoardConfigCommon.mk`:
  - `TARGET_KERNEL_CLANG_VERSION := r416183b1`
  - to `TARGET_KERNEL_CLANG_VERSION := r574158`
- Patched `kernel/asus/I001D/scripts/gcc-wrapper.py` Python 2 print syntax to Python 3:
  - `print(args[0] + ':', e.strerror, file=sys.stderr)`

Verified on VM:

- `prebuilts/clang/host/linux-x86/clang-r574158/bin/clang` exists and is executable.
- `prebuilts/gcc/linux-x86/aarch64/aarch64-linux-android-4.9/bin/aarch64-linux-android-gcc` exists and is executable.
- `prebuilts/gcc/linux-x86/arm/arm-linux-androideabi-4.9/bin/arm-linux-androideabi-gcc` exists and is executable.

## 2026-07-11 Build Run Sessions

Two build issues were found and fixed on the VM during the `m bootimage vendorimage` run:

### Fix 1: `struct sched_param` redefinition (bionic/kernel header conflict)

**Error**: `bionic/libc/include/sched.h:99` redefined `struct sched_param` after `<linux/sched/types.h>` already defined it via `generated_kernel_includes`.

**Root cause**: bionic's `sched.h` lacks a guard on its `struct sched_param` definition. When `generated_kernel_includes` (from `vendor/bliss/build/soong`) appear first in the include path, the kernel UAPI header defines it, then bionic redefines it.

**Fix**: Wrapped `struct sched_param { ... };` in `bionic/libc/include/sched.h:99` with `#ifndef _LINUX_SCHED_TYPES_H` / `#endif`, matching the kernel header's own guard macro.

### Fix 2: Duplicate SELinux type `vendor_nfc_prop`

**Error**: SELinux policy compilation failed with "Duplicate declaration of type" for `vendor_nfc_prop`.

**Root cause**: The Waterlily platform already declares `vendor_nfc_prop` as a generic vendor NFC property type with extended attributes (`property_type, vendor_property_type, vendor_internal_property_type`). The device-specific `device/asus/sm8150-common/sepolicy/vendor/property.te` also declared it as a bare `property_type`.

**Fix**: Removed the duplicate `type vendor_nfc_prop, property_type;` declaration from `device/asus/sm8150-common/sepolicy/vendor/property.te:29`. No other types in that file were duplicated.

### Fix 3: Duplicate genfscon entries (entire device genfs_contexts file redundant)

**Error**: SELinux policy compilation failed with "duplicate entry for genfs entry" for `sysfs /devices/platform/soc/c440000.qcom,spmi/spmi-0/spmi0-04/c440000.qcom,spmi:qcom,pm8150l@4:qcom,power-on@800/wakeup` in `device/asus/sm8150-common/sepolicy/vendor/genfs_contexts:169`.

**Root cause**: All 79 genfscon entries in the device's `genfs_contexts` file were already provided by the Waterlily platform's generic vendor sepolicy. The file was entirely redundant.

**Fix**: Emptied `device/asus/sm8150-common/sepolicy/vendor/genfs_contexts`.

### Fix 4: Kernel assembler `.file` directive (Clang r574158 + GCC 4.9 binutils)

**Error**: `kernel/exit.o` assembly error — "file number less than one" / "junk at end of line" in `.file` directives.

**Root cause**: Clang r574158 generates `.file <num> <dir> <filename>` (3-operand) DWARF 5 directives. The kernel `Makefile:513` passes `-no-integrated-as`, forcing Clang to use the old GCC 4.9 GNU assembler 2.27, which only supports the 2-operand form.

**Attempted fix**: Commented out `-no-integrated-as` in `kernel/asus/I001D/Makefile:513` to use Clang's integrated assembler.

**Outcome**: This broke the VDSO assembly (`arch/arm64/kernel/vdso/gettimeofday.S`) which uses `.macro` keyword arguments not supported by Clang's integrated assembler.

**Final fix**: Reverted the `-no-integrated-as` removal. Instead added `KBUILD_CFLAGS += -gdwarf-4` to force DWARF 4 output, which uses the older 2-operand `.file` format compatible with GNU as 2.27.

### Fix 5: Missing `vendor.asus.motor@1.0` HIDL interface

**Error**: `host_init_verifier` rejects camera init RC — `vendor.asus.motor@1.0::IRotateCameraInterface` not in known `hidl_interfaces`.

**Root cause**: The `android.hardware.camera.provider@2.4-service_64.rc` references `vendor.asus.motor@1.0::IRotateCameraInterface`, but the `vendor.asus.motor@1.0` HIDL interface is part of the missing `hardware/asus` project and was never created in the Waterlily tree.

**Fix**: Removed the `interface vendor.asus.motor@1.0::IRotateCameraInterface default` line from the prebuilt RC file. Prebuilt `vendor.asus.motor@1.0.so` and `libAsusMotor_hidl.so` libraries remain in the vendor tree for runtime use if something binds to the service directly.

### Fix 6: Invalid `PRIVATE_BUILD_DESC` / `TARGET_DEVICE` / `PRODUCT_NAME` build prop overrides

**Error**: `Key "PRIVATE_BUILD_DESC" isn't a valid prop override` and later `Key "TARGET_DEVICE" isn't a valid prop override` during `build.prop` generation.

**Root cause**: `device/asus/I001D/bliss_I001D.mk` used `PRODUCT_BUILD_PROP_OVERRIDES` to set `PRIVATE_BUILD_DESC`, `TARGET_DEVICE`, and `PRODUCT_NAME`. In Waterlily (Android 16), the `gen_build_prop.py` tool validates that all override keys exist in the Soong `product_config.json`. None of these keys are recognized.

**Fix**: Removed both `PRODUCT_BUILD_PROP_OVERRIDES` blocks from `device/asus/I001D/bliss_I001D.mk`. The build system auto-populates these values.

### Fix 7: `-Wdefault-const-init-field-unsafe` Clang warning-as-error

**Error**: `kernel/exit.o` and other files — `[-Werror,-Wdefault-const-init-field-unsafe]` about anonymous unions with const members in `READ_ONCE()` macros.

**Root cause**: Clang r574158 implements `-Wdefault-const-init-field-unsafe` (object with const member left uninitialized during default init). The kernel's `__READ_ONCE` macro in `include/linux/compiler.h` creates anonymous unions with `const typeof(x) __val`, triggering this on zero-initialization. The `-Werror` flag promotes it to a hard error.

**Fix**: Added `KBUILD_CFLAGS += $(call cc-option, -Wno-default-const-init-field-unsafe,)` to `kernel/asus/I001D/Makefile`.

### Fix 8: `-Wdefault-const-init-var-unsafe` Clang warning-as-error

**Error**: `techpack/audio/asoc/codecs/wcd9xxx-core.c` — `[-Werror,-Wdefault-const-init-var-unsafe]` related to `const unsigned long` default initialization.

**Root cause**: Clang r574158 also flags `-Wdefault-const-init-var-unsafe` for default-initialized variables with const type. This is another `READ_ONCE`-related pattern.

**Fix**: Added `KBUILD_CFLAGS += $(call cc-option, -Wno-default-const-init-var-unsafe,)` to `kernel/asus/I001D/Makefile`.

### Fix 9: `-Wunused-but-set-variable` not suppressed for Clang builds

**Error**: Multiple kernel files — `[-Werror,-Wunused-but-set-variable]`, including `techpack/audio/asoc/codecs/wcd934x/wcd934x.c:2750` and `drivers/vservices/session.c:1908`.

**Root cause**: The kernel's Makefile had `KBUILD_CFLAGS += $(call cc-disable-warning, unused-but-set-variable)` inside the `else` branch of `ifeq ($(cc-name),clang)`. This meant the suppression was only applied for GCC builds, not Clang. Clang r574158 now emits this warning.

**Fix**: Added a second unconditional `KBUILD_CFLAGS += $(call cc-disable-warning, unused-but-set-variable)` after the `ifeq ($(cc-name),clang)` / `ifeq ($(ld-name),lld)` block ends, so it applies to both Clang and GCC.

Next build handoff:

- User should rerun limited image build: `m bootimage vendorimage`.
- If many incremental kernel objects remain from failed attempts, clean first: `rm -rf out/target/product/I001D/obj/KERNEL_OBJ`.

## 2026-07-11 Kernel Header and Enum Warning Fixes

User reran limited image build and reported two failures:

1. Userspace/vendor compile failure in `hardware/qcom-caf/sm8150/display/gralloc/gr_adreno_info.cpp`:
   - Android 16 bionic defines `struct sched_param` in `bionic/libc/include/sched.h`.
   - Generated kernel UAPI header also exported `struct sched_param` from `linux/sched/types.h`.
   - Result: redefinition of `sched_param`.

2. Kernel build failure in `techpack/audio/asoc/codecs/wcd_cpe_services.c`:
   - Old Qualcomm code triggers `-Wimplicit-enum-enum-cast`.
   - Clang treats it as error due `-Werror`.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/include/uapi/linux/sched/types.h`:
  - Wrapped `struct sched_param` with `#ifndef __ANDROID__` so Android userspace uses bionic's definition.
- Updated `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/BoardConfigCommon.mk`:
  - Added `TARGET_KERNEL_ADDITIONAL_FLAGS += KCFLAGS=-Wno-error=implicit-enum-enum-cast`.
- Removed generated kernel include output:
  - `out/soong/.intermediates/vendor/bliss/build/soong/generated_kernel_includes`
  - This forces regenerated headers to pick up the patched UAPI header.

Next build handoff:

- User should rerun `m bootimage vendorimage`.

## 2026-07-12 First device flash / AVB failure

Build artifacts were copied locally under:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts`

Generated package:

- `Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260711.zip`
- SHA256: `c00f0053da2e0045664120f7d1d36e6ddf59b9487241e355550a5d70d1e9c5f2`

Extracted images:

- `boot.img`
- `dtbo.img`
- `vbmeta.img`

User test results:

- `fastboot boot boot.img` reached ASUS logo briefly and restarted.
- Full OTA sideload produced bootloop with ASUS logo flashing briefly.
- Device reported `avb verify fail`.
- Bootloader is unlocked.
- Current slot is `b`.
- `fastboot getvar all` confirms slotted `boot_a/b`, `dtbo_a/b`, and `vbmeta_a/b` partitions.

Important AVB finding:

- Local `vbmeta.img` starts with AVB magic `AVB0`.
- Earlier `avbtool info_image` showed generated `vbmeta.img` has AVB flags `3`, meaning verification and hashtree are already disabled in the image.
- User's command `fastboot --disable-verity --disable-verification flash vbmeta_b vbmeta.img` failed with `Failed to find AVB_MAGIC at offset: 0`.
- This appears to be fastboot's vbmeta patching path failing, not proof that the image is invalid.

Conservative next flashing route:

- Do not touch firmware-critical partitions (`abl`, `xbl`, `modem`, `tz`, `hyp`, `persist`, ASUS key partitions).
- Avoid `--disable-verity --disable-verification` because the built `vbmeta.img` is already disabled and those flags trigger fastboot patching.
- Flash only the active slot's Android boot-chain images first:
  - `vbmeta`
  - `boot`
  - `dtbo`

Recommended command sequence to user:

```powershell
fastboot --set-active=b
fastboot flash vbmeta vbmeta.img
fastboot flash boot boot.img
fastboot flash dtbo dtbo.img
fastboot reboot
```

If plain `fastboot flash vbmeta vbmeta.img` fails or does not clearly target slot `b`, stop and inspect before trying explicit partition variants.

## 2026-07-12 Post-vbmeta flash bootloop

User flashed generated `vbmeta.img` to slot `b` as a plain image, without fastboot's `--disable-verity --disable-verification` patch flags.

Result:

- Boot behavior remained the same: short ASUS logo flash followed by reboot/bootloop.

Current interpretation:

- Do not continue random flashing.
- If `avb verify fail` is no longer shown, the failure likely moved past AVB into early boot/kernel/ramdisk/device-tree/init.
- If `avb verify fail` is still shown, inspect slot/vbmeta state before changing source.

Next safe diagnostic handoff:

```powershell
fastboot getvar current-slot
fastboot getvar slot-retry-count:b
fastboot getvar slot-unbootable:b
fastboot getvar slot-successful:b
```

Then temporarily boot recovery/TWRP, without flashing recovery:

```powershell
fastboot boot twrp-3.7.1_12-0-I001D.img
```

If recovery boots and ADB is available, pull early crash logs:

```powershell
adb wait-for-device
adb shell ls -la /sys/fs/pstore /proc/last_kmsg /sys/fs/ramoops
mkdir bootloop-logs
adb pull /sys/fs/pstore bootloop-logs\pstore
adb pull /proc/last_kmsg bootloop-logs\last_kmsg.txt
adb pull /sys/fs/ramoops bootloop-logs\ramoops
```

If the device becomes hard to recover from slot `b`, use slot `a` as the known previously successful slot:

```powershell
fastboot --set-active=a
fastboot reboot
```

## 2026-07-12 TWRP read-only diagnostics

User confirmed device was in TWRP. Agent performed read-only diagnostics only:

- `adb devices` showed `K9AIGF00U2343U3 recovery`.
- Pulled logs/artifacts into:
  - `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\bootloop-logs\20260712-062638`

Pulled files:

- `recovery.log`
- `recovery-dmesg.txt`
- `data-recovery-log.gz`
- `recovery.fstab`
- `storage.fstab`
- `vbmeta_b_device.img`
- `boot_b_device.img`
- `dtbo_b_device.img`

Unavailable/empty failed-boot logs:

- `/sys/fs/pstore` missing in TWRP session.
- `/proc/last_kmsg` missing.
- `/sys/fs/ramoops` missing.
- `/proc/last_logcat` could not be read and resulted in a zero-byte local file.
- No useful `/data/system/dropbox`, tombstone, or ANR records were present.

Verified device state:

- `ro.boot.slot_suffix=_b`
- `ro.boot.verifiedbootstate=orange`
- `vbmeta_b_device.img` SHA256 exactly matches local `artifacts\vbmeta.img`:
  - `ec7913b3effddf24d45b2786c2d56c6225105b3e85675cd2cd1fb016deb4eca1`
- `boot_b_device.img` does not match local `artifacts\boot.img`:
  - device: `e979aa846a9d0538de088c25e6f9e5cfb94ed9db54839b1769dd6655affa2562`
  - local: `95e88ebb3479605d4377fbaa842bd182b7ddfca181e10b4feafa9e9dd7e88bd2`
- `dtbo_b_device.img` is full partition size `25165824`; local `dtbo.img` is `8388608`.
- First `8388608` bytes of `dtbo_b_device.img` still do not match local `dtbo.img`.
- Boot headers for local `boot.img` and pulled `boot_b_device.img` match in basic fields, but binary differences begin around offset `38912392`.
- DTBO headers match, but binary differences begin around offset `6570055`.

Read-only mounted slot `b` partitions:

```sh
mount -o ro -t ext4 /dev/block/by-name/system_b /tmp/slotb/system
mount -o ro -t ext4 /dev/block/by-name/vendor_b /tmp/slotb/vendor
```

Findings:

- `system_b` and `vendor_b` are Android 16-ish images (`ro.build.version.sdk=36`, `ro.build.version.release=16`).
- Build fingerprints still carry ASUS stock-looking fingerprint strings, likely from inherited vendor/device props.

Current interpretation:

- There is no preserved kernel panic/logcat from the failed Android boot.
- The phone's slot `b` contains our generated `vbmeta_b`, and system/vendor appear updated to Android 16.
- `boot_b` and `dtbo_b` on-device do not match the local artifacts being inspected.
- If AVB error is still shown, the boot-chain image mismatch remains suspicious.
- If AVB error is gone and it simply reboots, the issue may be early kernel/DTBO/init and needs a way to capture ramoops/pstore or serial/last_kmsg support.

No flash/write operation was performed during this diagnostic pass.

## 2026-07-12 TWRP copied log follow-up

User said the TWRP-copied log should be in `data/media`.

Read-only ADB checks showed:

- `/data` is mounted as f2fs and has free space.
- `/data/media` does not exist from the ADB shell view.
- `/data/media/0` does not exist.
- `/sdcard` exists but is empty and not backed by `/data/media`.
- The only visible persisted TWRP files are under `/data/recovery`.

Pulled/decompressed:

- `/data/recovery/log.gz` -> `bootloop-logs\20260712-062638\data-recovery-log.txt`

Relevant TWRP log findings:

- TWRP thinks data is decrypted and configured as internal storage:
  - `Is_Encrypted Is_Decrypted Has_Data_Media`
  - `Storage_Path: /data/media`
  - `Symlink_Mount_Point: /sdcard`
- But it also logs:
  - `Unable to open '/data/media/TWRP/BACKUPS/K9AIGF00U2343U3'`
  - `Unable to open '/data/media/TWRP/BACKUPS'`
- Therefore the TWRP "copy log to data/media" action did not produce a file visible to ADB in the current recovery shell.

Current usable logs remain:

- `recovery.log`
- `recovery-dmesg.txt`
- `data-recovery-log.txt`
- pulled slot images for `vbmeta_b`, `boot_b`, `dtbo_b`

## 2026-07-12 Rebuild with vbmeta flags=3 downloaded

User made another VM build with vbmeta flags set to `3`.

Agent verification on VM:

```sh
out/host/linux-x86/bin/avbtool info_image --image out/target/product/I001D/vbmeta.img
```

Relevant verified output:

- `Minimum libavb version: 1.0`
- `Algorithm: SHA256_RSA4096`
- `Flags: 3`
- `Release String: 'avbtool 1.3.0'`
- Hash descriptors present for:
  - `boot`
  - `dtbo`
- Hashtree descriptors present for:
  - `system`
  - `vendor`

Because the image was AVB-valid and flags were confirmed as `3`, agent downloaded new artifacts from VM to:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts`

Previous local artifacts were backed up before replacement to:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts\previous-20260712-100106`

New local artifact hashes:

- `boot.img`
  - SHA256: `085472bc71d07de590868725b585094e54c112542020b0e6c4f95b37742562a9`
  - Size: `100663296`
- `dtbo.img`
  - SHA256: `cecf67752fef822241012f3738822f6f3cc892291ef2c11082965e4a88dbf82c`
  - Size: `8388608`
- `vbmeta.img`
  - SHA256: `e1ce0eca74e2b2482b8f9b0eaeebea609273c9d3c6d59443d6dfe90869adcfb7`
  - Size: `65536`
  - Local header starts with `AVB0`
- `Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip`
  - SHA256: `f826e04bd8e310ca63ff3ae70de7b625fb2acc839eb5efe38f6e4fee32b21435`
  - Size: `1380780007`

Fresh `/data/recovery` logs were also pulled from TWRP to:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\bootloop-logs\20260712-095950-data-recovery`

Pulled and decompressed:

- `log.gz` -> `log.txt`
- `last_log.gz` -> `last_log.txt`
- `recovery.fstab`
- `storage.fstab`

Log review:

- TWRP sees slot `_b`.
- TWRP property `ro.product.ab_ota_partitions=boot,system,vendor,vbmeta,dtbo`.
- TWRP logs internal storage as `/data/media`, but also reports it cannot open TWRP backup folders under `/data/media`.
- These are recovery logs, not failed Android boot kernel logs.

## 2026-07-12 Same bootloop after flags=3 rebuild

User reported the rebuilt `20260712` package still has the same bootloop.

Local device visibility check at that moment:

- `adb devices`: no device
- `fastboot devices`: no device

Artifact analysis:

- Generated build `boot.img`:
  - Android boot image header v0
  - kernel size `23236994`
  - ramdisk size `15666098`
  - SHA256 `085472bc71d07de590868725b585094e54c112542020b0e6c4f95b37742562a9`
- TWRP image that successfully boots recovery:
  - Android boot image header v2
  - kernel size `15678757`
  - ramdisk size `26708229`
  - SHA256 `13e79333986c236f899b40b45396e74bd65661d56090c964d8ba988a99e32898`

Interpretation:

- The generated boot image uses the compiled source kernel from `kernel/asus/I001D`.
- The only kernel currently known to boot on this phone is the TWRP kernel.
- Because `fastboot boot boot.img` previously restarted before Android userspace, the compiled kernel/DTB is now the primary suspect.

Created local RAM-only isolation test image:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts\boot-hybrid-twrp-kernel.img`
- SHA256: `9a3bdf5d0d0ac7c82869661a0b9fd2ff5da61982aa3b40da21214e9797631238`

Hybrid composition:

- Kernel payload from known-booting `twrp-3.7.1_12-0-I001D.img`
  - kernel SHA256: `373f6c51ce7e7292703eb9968565df082ba9d9732b237c77c6840c100c9f14c8`
  - size: `15678757`
- Ramdisk payload from rebuilt Android 16 `boot.img`
  - ramdisk SHA256: `193855ce4ae7622bb93e4ec0a6e89878f1741adf3391241253fba8004d098c68`
  - size: `15666098`
- Header/cmdline mostly kept from Android 16 `boot.img`.
- This is intended for `fastboot boot` only, not flashing.

Next user handoff:

```powershell
fastboot boot boot-hybrid-twrp-kernel.img
```

Expected interpretation:

- If it gets past the instant ASUS-logo reboot, the compiled source kernel/DTB is the blocker and the source fix should switch the device tree to a known-good prebuilt kernel path or repair the kernel config/toolchain.
- If it still reboots instantly, investigate Android 16 ramdisk/init/first-stage mount/dtbo interaction next.

Follow-up:

- User reported the first hybrid still gave `avb verify fail`.
- Root cause: the first hybrid was manually rebuilt and did not have an AVB hash footer.
- Verified built `boot.img` footer requirements with VM `avbtool`:
  - partition name `boot`
  - partition size `100663296`
  - algorithm `NONE`
  - footer version `1.0`

Created corrected AVB-footered hybrid:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\artifacts\boot-hybrid-twrp-kernel-avb.img`
- SHA256: `98070df46d33a53ab0e798ee5e8d9b2cd47bdcfe3b6c0f238ec201ce7287ee10`

VM `avbtool info_image` on corrected hybrid:

- Footer version: `1.0`
- Image size: `100663296`
- Original image size: `31350784`
- VBMeta offset: `31350784`
- VBMeta size: `704`
- Algorithm: `NONE`
- Hash descriptor partition name: `boot`

Corrected next RAM-only test command:

```powershell
fastboot boot boot-hybrid-twrp-kernel-avb.img
```

Do not use the unfootered `boot-hybrid-twrp-kernel.img` for testing.

## 2026-07-12 Fastboot evidence after corrected hybrid failure

User reported corrected hybrid did not boot and device stayed/returned to fastboot.

Agent collected read-only fastboot evidence to:

- `D:\AndroidProjects\porting\waterlily-i001d-reconstruction\bootloop-logs\20260712-103229-fastboot-hybrid-fail`

Commands run:

- `fastboot devices`
- `fastboot getvar current-slot`
- `fastboot getvar slot-retry-count:a`
- `fastboot getvar slot-retry-count:b`
- `fastboot getvar slot-unbootable:a`
- `fastboot getvar slot-unbootable:b`
- `fastboot getvar slot-successful:a`
- `fastboot getvar slot-successful:b`
- `fastboot getvar unlocked`
- `fastboot getvar secure`
- `fastboot getvar all`
- `fastboot oem device-info`
- `fastboot oem get_build_version`

Key results:

- Device visible: `K9AIGF00U2343U3 fastboot`
- Current slot: `b`
- Slot retry count:
  - `a: 7`
  - `b: 6`
- Slot unbootable:
  - `a: no`
  - `b: no`
- Slot successful:
  - `a: yes`
  - `b: no`
- Bootloader unlocked: `yes`
- Secure boot: `yes`
- Critical unlocked: `false`
- ABL: `CS1(00049)-1-WW-user`
- Build version from OEM command: `WW_ZS660KL-17.0240.2108.103-0`
- OEM device-info:
  - `AVB Verity : Enable`
  - `Verify vbmeta ret : 5`

Interpretation of `Verify vbmeta ret : 5`:

- AOSP `AvbSlotVerifyResult` enum value 5 is `AVB_SLOT_VERIFY_RESULT_ERROR_PUBLIC_KEY_REJECTED`.
- This points to bootloader/key acceptance, not a kernel panic.
- It is consistent with ASUS ABL rejecting the vbmeta public key even though the bootloader reports `Device unlocked: true`.
- `Device critical unlocked: false` is suspicious and may be relevant to ASUS accepting custom top-level vbmeta.

Agent attempted a no-flash `fastboot boot twrp-3.7.1_12-0-I001D.img` to read partitions/pstore after collecting fastboot evidence.

Result:

- TWRP image sent successfully.
- Fastboot returned `FAILED (Status read failed (Too many links))`.
- After waiting, the device was not visible in either `adb devices` or `fastboot devices`.

No flash/write command was run.

## 2026-07-12 Analysis pass: is this Android 16 or port/build failure?

User asked whether the same bootloop is caused by Android 16 flashing, a wrong build component, or failed porting.

Evidence reviewed:

- Fastboot/OEM state after failed boot:
  - `Device unlocked: true`
  - `Device critical unlocked: false`
  - `AVB Verity : Enable`
  - `Verify vbmeta ret : 5`
- AOSP `AvbSlotVerifyResult` enum:
  - value `5` = `AVB_SLOT_VERIFY_RESULT_ERROR_PUBLIC_KEY_REJECTED`
- Build AVB config:
  - `BOARD_AVB_ENABLE := true`
  - common config adds `--set_hashtree_disabled_flag`
  - common config adds `--flags 2`
  - device config adds `--flags 3`
- Generated `vbmeta.img`:
  - valid AVB metadata
  - `Algorithm: SHA256_RSA4096`
  - `Public key (sha1): 2597c218aae470a130f61162feaae70afd97f011`
  - `Flags: 3`
  - descriptors for `boot`, `dtbo`, `system`, `vendor`
- Generated `boot.img` and `dtbo.img`:
  - valid AVB hash footers
  - algorithm `NONE`
  - partition names `boot` and `dtbo`

Conclusion:

- The current observed blocker is before Android 16 userspace.
- `Verify vbmeta ret : 5` means the ASUS bootloader/ABL is rejecting the top-level vbmeta public key.
- `Flags: 3` disables verification/hashtree behavior only after the top-level vbmeta is accepted/processed; it does not by itself make ASUS ABL trust an unknown vbmeta signing key.
- The port may still have later kernel/init/device bugs, but the current evidence does not reach that stage reliably.
- The build is structurally valid AVB, but it is likely packaged with a vbmeta signing/key model this ASUS bootloader state will not accept.
- `Device critical unlocked: false` is suspicious and may explain why the bootloader rejects a custom top-level vbmeta despite normal `Device unlocked: true`.

Conservative next direction:

- Do not keep modifying/flashing random partitions.
- Preserve known-good slot `a`.
- Next source-side fix should focus on AVB packaging/bootloader acceptance before kernel or Android 16 runtime debugging:
  - determine whether this device/ABL requires critical unlock for custom vbmeta; or
  - determine whether Bliss/Lineage I001D builds normally avoid replacing top-level vbmeta; or
  - find the device-specific accepted AVB/key workflow from a working I001D custom ROM package.

## 2026-07-11 Limited image build success

User reran:

- `m bootimage vendorimage`

Result:

- Build completed successfully.
- This clears the limited boot/vendor image compile milestone.

Non-fatal warnings observed:

- `mv: Needs 2 arguments`
- `depmod` warnings about missing module metadata under:
  - `out/target/product/I001D/obj/PACKAGING/depmod_vendor_intermediates/lib/modules/0.0`
- `vendor.img` is exactly at the configured size limit:
  - current: `1073741824`
  - limit: `1073741824`

Current roadmap status:

- Missing source reconstruction is complete enough for Android 16 cooking.
- Device/kernel/vendor trees now pass the limited image build.
- Next stage is broader ROM/package build bring-up, where likely remaining failures will be packaging, image size, sepolicy/VINTF edge cases, or full-target app/framework integration issues.

## 2026-07-11 Package target correction

User tried:

- `m bacon`

Result:

- Build failed quickly because `bacon` is not a target in this Waterlily/Bliss tree:
  - `FAILED: ninja: unknown target 'bacon', did you mean 'chkcon'?`

Inspection:

- `vendor/bliss/build/tasks/blissify.mk` defines:
  - `.PHONY: blissify`
  - `blissify: $(DEFAULT_GOAL) $(INTERNAL_OTA_PACKAGE_TARGET)`
- Generated ninja also contains standard target:
  - `otapackage -> out/target/product/I001D/bliss_I001D-ota.zip`

Next build handoff:

- Preferred Bliss package command: `m blissify`
- Standard OTA-only alternative: `m otapackage`

## 2026-07-11 AsusParts old Lineage hardware Java API removal

User ran the broader package build and reported Java compile errors in:

- `device/asus/sm8150-common/AsusParts/src/org/blissroms/settings/asusparts/touch/TouchscreenGestureSettings.java`

Symptoms:

- Missing `com.android.internal.bliss.hardware.LineageHardwareManager`
- Missing `com.android.internal.bliss.hardware.TouchscreenGesture`
- 25 Java errors total.

Root cause:

- AsusParts came from an older Lineage/Bliss generation that used internal Java hardware APIs for touchscreen gestures.
- The Android 16 Waterlily tree does not provide those Java classes.
- The device already has a reconstructed native/AIDL touch service and the same Goodix sysfs gesture nodes are known from `device/asus/I001D/touch/TouchscreenGesture.cpp`.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/AsusParts/src/org/blissroms/settings/asusparts/touch/TouchscreenGestureSettings.java`.
- Removed imports/use of:
  - `LineageHardwareManager`
  - framework `TouchscreenGesture`
- Added a local private `TouchscreenGesture` model in the settings fragment.
- Added local supported gestures matching the reconstructed touch service:
  - W, S, e, C, Z, V, swipe up
  - Same keycodes and Goodix sysfs paths as `device/asus/I001D/touch/TouchscreenGesture.cpp`.
- Replaced `LineageHardwareManager.setTouchscreenGestureEnabled(...)` calls with direct `FileUtils.setValue(...)` sysfs writes.
- Replaced feature detection with checking whether any known gesture sysfs node exists.
- Verified no remaining `LineageHardwareManager`, `com.android.internal.bliss.hardware`, or `FEATURE_TOUCHSCREEN_GESTURES` references under AsusParts source.

Next build handoff:

- User should rerun `m blissify`.

## 2026-07-11 AsusParts Android 16 framework constant cleanup

User reran the broader package build and AsusParts failed again, now in:

- `device/asus/sm8150-common/AsusParts/src/org/blissroms/settings/asusparts/touch/TouchKeyHandler.java`

Missing Android 16 symbols:

- `Intent.ACTION_SCREEN_CAMERA_GESTURE`
- `Settings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK`

Handled VM modifications directly:

- Replaced old camera gesture intent constant with Android's available `Intent.ACTION_CAMERA_BUTTON`.
- Added local string key:
  - `touchscreen_gesture_haptic_feedback`
- Replaced old `Settings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK` symbol usage with the local key.

## 2026-07-11 AsusParts vs RogParts analysis

Question:

- Is `RogParts` a replacement for `AsusParts`?
- Are both bundled?

Findings:

- No `RogParts` app/module exists in the active device/vendor tree.
- Only `AsusParts` exists:
  - `device/asus/sm8150-common/AsusParts`
- Only `AsusParts` is bundled as a device parts app:
  - `device/asus/sm8150-common/msmnile.mk: PRODUCT_PACKAGES += AsusParts`
- The `Rog2` references are resource runtime overlays, not a device-parts app replacement:
  - `FrameworksResOverlay`
  - `SettingsRes`
  - `SystemUIRog2`
  - `SettingsProviderRog2`
- These overlays are bundled from:
  - `device/asus/I001D/device.mk`

Conclusion:

- We are not bundling both AsusParts and RogParts.
- `AsusParts` is the only device parts application currently included.
- `Rog2` entries are device-specific overlays for framework/settings/SystemUI behavior and branding/config, not a replacement app.

## 2026-07-12 AsusParts package-wide static pass

Question:

- Has a full advanced analysis pass been done on AsusParts?

Answer:

- Before this point, only targeted fixes had been done for build errors and a narrow check for old `LineageHardwareManager` usage.
- A package-wide static pass was then performed over `device/asus/sm8150-common/AsusParts`.

Scope checked:

- Complete AsusParts file list.
- Java imports.
- Hidden/internal API references.
- Old Lineage/CyanogenMod references.
- Settings constants.
- Doze/ambient display integration.
- Touch gesture/sysfs integration.
- Manifest/package wiring.
- Product inclusion and possible `RogParts` replacement.

Findings:

- No `RogParts` replacement app exists in the active tree.
- AsusParts is the only bundled device parts app.
- `Rog2` references are RRO overlays, not a replacement package.
- No remaining `LineageHardwareManager`, `com.android.internal.bliss.hardware`, or `FEATURE_TOUCHSCREEN_GESTURES` references were found after the earlier patch.
- Remaining notable hidden/internal APIs:
  - `com.android.internal.os.DeviceKeyHandler` in `TouchKeyHandler`.
  - `com.android.internal.R.bool.config_dozeAlwaysOnEnabled` in `DozeUtils`.
  - `com.android.internal.widget.PreferenceImageView` in custom preference layouts.
- These are plausible for a privileged platform app using `LOCAL_PRIVATE_PLATFORM_APIS := true`, but may still surface as build/runtime issues depending on Android 16 framework compatibility.
- Touch gesture sysfs paths in AsusParts now match the reconstructed native touch service paths for:
  - W, S, e, C, Z, V, swipe up.
- The older HIDL-era touch service RC file still contains per-letter chmod/chown lines, while the current AIDL RC only has `zenmotion`, `swipeup`, and `dclick`; runtime permission consistency should be revisited if gestures do not work on device.
- AsusParts still exposes a camera motor calibration preference targeting:
  - package `com.asus.motork`
  - activity `com.asus.motork.MainActivity`
  This depends on whether the proprietary Motork app is present in vendor/product.

Current assessment:

- AsusParts is being ported, not replaced.
- Current build blockers in AsusParts are Android 16 API drift from old Lineage/Bliss code.
- The next `m blissify` run is still needed to confirm there are no more compile-time API misses.

## 2026-07-12 VINTF duplicate boot HAL fix

User reran broader package build and reported `check_vintf_vendor` failure:

- Duplicate `android.hardware.boot` AIDL HAL instance:
  - `IBootControl/default` from `/vendor/etc/vintf/manifest.xml`
  - `IBootControl/default` from `/vendor/etc/vintf/manifest/boot-service.qti.xml`

Root cause:

- The device VINTF merge inputs included a device-side boot fragment:
  - `device/asus/sm8150-common/vintf/manifest/boot-service.qti.xml`
- The current Android 16 Qualcomm bootctrl module also installs its own fragment:
  - `hardware/qcom-caf/bootctrl/aidl/boot-service.qti.xml`
- The device fragment was being merged into the main vendor manifest, while the module fragment was installed as a separate fragment, creating a duplicate HAL instance.

Handled VM modifications directly:

- Removed `android.hardware.boot` from:
  - `device/asus/sm8150-common/vintf/manifest.xml`
- Removed duplicate device-side fragment:
  - `device/asus/sm8150-common/vintf/manifest/boot-service.qti.xml`
- Removed stale generated outputs so the next build regenerates/checks cleanly:
  - `out/target/product/I001D/vendor/etc/vintf/manifest.xml`
  - `out/target/product/I001D/obj/PACKAGING/check_vintf_all_intermediates/check_vintf_vendor.log`

Rationale:

- Keep the module-owned Qualcomm Android 16 bootctrl VINTF fragment as the single provider.
- Do not duplicate the same HAL in the device main manifest.

Next build handoff:

- User should rerun `m blissify`.

## 2026-07-12 VINTF duplicate power HAL fix

User reran broader package build and reported `check_vintf_vendor` failure:

- Duplicate `android.hardware.power` AIDL HAL instance:
  - `IPower/default` from `/vendor/etc/vintf/manifest.xml`
  - `IPower/default` from `/vendor/etc/vintf/manifest/power.xml`

Root cause:

- Same pattern as the boot HAL duplicate.
- The device VINTF main manifest carried `android.hardware.power`.
- The device also had a copied `power.xml` fragment whose comment showed it originated from:
  - `vendor/qcom/opensource/power/power.xml`
- The module-owned/current Qualcomm power HAL fragment should be the single provider.

Handled VM modifications directly:

- Removed `android.hardware.power` from:
  - `device/asus/sm8150-common/vintf/manifest.xml`
- Removed duplicate device-side copied fragment:
  - `device/asus/sm8150-common/vintf/manifest/power.xml`
- Removed stale generated outputs:
  - `out/target/product/I001D/vendor/etc/vintf/manifest.xml`
  - `out/target/product/I001D/obj/PACKAGING/check_vintf_all_intermediates/check_vintf_vendor.log`

Forward check:

- Ran a source VINTF duplicate scan for HAL `format/name/fqname` collisions between:
  - `device/asus/sm8150-common/vintf/manifest.xml`
  - `device/asus/sm8150-common/vintf/manifest/*.xml`
- No remaining main-manifest vs fragment duplicates were reported.

Next build handoff:

- User should rerun `m blissify`.

## 2026-07-12 VINTF duplicate light HAL generated-state cleanup

User reran broader package build and reported `check_vintf_vendor` failure:

- Duplicate `android.hardware.light` AIDL HAL instance:
  - `ILights/default` from `/vendor/etc/vintf/manifest.xml`
  - `ILights/default` from `/vendor/etc/vintf/manifest/lights-asus_msmnile.xml`

Inspection:

- Current source main manifest no longer contains `android.hardware.light`.
- Source fragment still correctly declares the light HAL:
  - `device/asus/sm8150-common/vintf/manifest/lights-asus_msmnile.xml`
- The duplicate `android.hardware.light` entry existed only in the generated vendor manifest from the prior build state:
  - `out/target/product/I001D/vendor/etc/vintf/manifest.xml`

Handled VM modifications directly:

- Removed stale generated VINTF outputs/intermediates:
  - `out/target/product/I001D/vendor/etc/vintf/manifest.xml`
  - `out/target/product/I001D/obj/PACKAGING/check_vintf_all_intermediates`
  - `out/target/product/I001D/obj/ETC/vendor_manifest.xml_intermediates`
  - `out/target/product/I001D/obj/ETC/manifest.xml_intermediates`

Forward check:

- `device/asus/sm8150-common/vintf` now only reports `android.hardware.light` in:
  - `manifest/lights-asus_msmnile.xml`
- Recent duplicate HALs `android.hardware.boot` and `android.hardware.power` no longer appear in the device VINTF source tree.

Next build handoff:

- User should rerun `m blissify`.

### Follow-up: remove VINTF manifest fragment wildcard

User reran `m blissify` and the duplicate `android.hardware.light` conflict returned after regeneration.

Deeper root cause:

- `device/asus/sm8150-common/BoardConfigCommon.mk` used:
  - `DEVICE_MANIFEST_FILE += $(wildcard $(DEVICE_PATH_COMMON)/vintf/manifest/*.xml)`
- That merged every VINTF fragment into `/vendor/etc/vintf/manifest.xml`.
- The same fragments are also installed as standalone files under `/vendor/etc/vintf/manifest/` by Soong/prebuilt modules.
- Result: main manifest and fragment directory both declare the same HAL instances.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/BoardConfigCommon.mk`:
  - Kept only the base main manifest:
    - `DEVICE_MANIFEST_FILE += $(DEVICE_PATH_COMMON)/vintf/manifest.xml`
  - Removed the wildcard merge of `vintf/manifest/*.xml`.
- Cleared stale VINTF outputs/intermediates again:
  - `out/target/product/I001D/vendor/etc/vintf/manifest.xml`
  - `out/target/product/I001D/obj/PACKAGING/check_vintf_all_intermediates`
  - `out/target/product/I001D/obj/ETC/vendor_manifest.xml_intermediates`
  - `out/target/product/I001D/obj/ETC/manifest.xml_intermediates`

Forward check:

- Re-ran source VINTF duplicate scan between:
  - `device/asus/sm8150-common/vintf/manifest.xml`
  - `device/asus/sm8150-common/vintf/manifest/*.xml`
- No duplicate HAL `format/name/fqname` entries were found.

Next build handoff:

- User should rerun `m blissify`.

## 2026-07-12 Full Bliss package build success

User reran:

- `m blissify`

Result:

- Build completed successfully.
- Bliss package generated:
  - `out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260711.zip`
- SHA256:
  - `c00f0053da2e0045664120f7d1d36e6ddf59b9487241e355550a5d70d1e9c5f2`
- Size:
  - `1.2G`

Milestone status:

- Full Android 16 Bliss package build is complete.
- Missing source reconstruction and compile/package bring-up are now past the primary build milestone.
- Remaining work moves from source reconstruction/build bring-up to validation:
  - artifact inspection
  - signing/metadata sanity checks
  - flash/boot testing
  - runtime hardware validation
  - cleanup/documentation of local source patches

## 2026-07-12 First runtime boot test failure

User tested the built package by ADB sideload and reported:

- Bootloop.
- ASUS logo flashes for roughly 0.5 seconds, then device restarts.

Interpretation:

- This is earlier than Android framework/userspace.
- Most likely failure area is one of:
  - kernel panic very early
  - DTB/DTBO mismatch
  - boot image ramdisk/fstab/init issue
  - AVB/vbmeta/slot handoff issue
  - vendor/system partition mismatch after sideload
- Less likely to be caused by AsusParts/framework/package Java changes because the device does not reach Android boot animation.

Next requested diagnostic data:

- Recovery sideload result text.
- `fastboot getvar current-slot`
- `fastboot getvar all`
- pstore/ramoops/last_kmsg from recovery if available.

## 2026-07-11 GSI broken symbol version workaround

User reran the limited image build and the kernel link still failed, now with relative CRC relocation:

- `WARNING: EXPORT symbol "gsi_write_channel_scratch" [vmlinux] version generation failed`
- `ld.lld: error: relocation R_AARCH64_PREL32 cannot be used against symbol '__crc_gsi_write_channel_scratch'`

Interpretation:

- The previous `MODULE_REL_CRCS` fix is active because the relocation changed from `R_AARCH64_ABS32` to `R_AARCH64_PREL32`.
- The remaining problem is specific to one exported symbol whose version CRC is not generated.
- `Module.symvers` showed:
  - `gsi_write_channel_scratch2_reg` has a valid CRC.
  - `gsi_write_channel_scratch3_reg` has a valid CRC.
  - `gsi_write_channel_scratch` has `0x00000000`.
- The likely trigger is genksyms failing on the packed-union prototype:
  - `int gsi_write_channel_scratch(unsigned long chan_hdl, union __packed gsi_channel_scratch val)`.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/drivers/platform/msm/gsi/gsi.c`:
  - Commented out `EXPORT_SYMBOL(gsi_write_channel_scratch);`.
  - Added a source comment noting genksyms cannot version the packed-union prototype with the current toolchain and all current users are built-in.
- Removed stale kernel outputs that could retain the old export-table entry:
  - `drivers/platform/msm/gsi/gsi.o`
  - related `built-in.o` aggregate objects up through `drivers/built-in.o`
  - top-level `built-in.o`
  - `vmlinux`
  - `Module.symvers`

Risk:

- If a loadable external module later needs `gsi_write_channel_scratch`, this export will need a cleaner ABI-preserving fix.
- Current I001D config shows GSI/IPA users are built into the kernel, so this is acceptable for build bring-up.

Next build handoff:

- User should rerun `m bootimage vendorimage`.

## 2026-07-11 Disable kernel modversions to stop GSI genksyms failures

User reran the limited image build and the same genksyms/LLD failure moved to another GSI export:

- `gsi_query_channel_db_addr`

Conclusion:

- The issue is broader than the GSI channel-scratch helper family.
- Qualcomm GSI exports are repeatedly failing version generation under the current modern Clang/LLD + old kernel combination.
- Continuing to comment exports one by one is risky and unnecessary for build bring-up.

Handled VM modifications directly:

- Disabled kernel module version CRC generation for active I001D config:
  - `/home/premanandal1978/android/waterlily/kernel/asus/I001D/arch/arm64/configs/vendor/I001D_defconfig`
  - Changed from `CONFIG_MODVERSIONS=y` to `# CONFIG_MODVERSIONS is not set`.
- Removed `CONFIG_MODULE_REL_CRCS=y` because it only applies when `MODVERSIONS` is enabled.
- Synced current generated kernel config:
  - `out/target/product/I001D/obj/KERNEL_OBJ/.config`
  - `include/config/auto.conf`
  - `include/generated/autoconf.h`
- Restored the GSI exports previously commented while chasing individual CRC failures.
- Removed stale kernel outputs that could carry old CRC sections:
  - GSI object/aggregate objects
  - top-level `built-in.o`
  - `vmlinux`
  - `vmlinux.o`
  - `Module.symvers`

Forward check:

- Verified active I001D source defconfig and generated `.config` both contain `# CONFIG_MODVERSIONS is not set`.
- Verified generated enabled config files no longer contain `CONFIG_MODVERSIONS` or `CONFIG_MODULE_REL_CRCS`.
- Scanned current kernel object files with `readelf -S` and found no remaining `___kcrctab` sections.
- Other defconfigs in the kernel tree still contain `CONFIG_MODVERSIONS=y`, but the active device uses `device/asus/I001D/BoardConfig.mk: TARGET_KERNEL_CONFIG := vendor/I001D_defconfig`, so those are not relevant to this build.

Next build handoff:

- User should rerun `m bootimage vendorimage`.

### Follow-up: remaining GSI channel-scratch write exports

User reran the limited image build and the same genksyms/LLD pattern moved to:

- `gsi_write_wdi3_channel_scratch2_reg`

This confirms the issue is the family of GSI channel scratch write helpers with packed-union arguments, not only `gsi_write_channel_scratch`.

Checked active kernel config:

- No IPA/GSI/RMNET/WDI entries are built as loadable modules (`=m`).
- The observed users are in-tree and built-in.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/drivers/platform/msm/gsi/gsi.c` to comment out the remaining channel-scratch write exports:
  - `EXPORT_SYMBOL(gsi_write_channel_scratch3_reg);`
  - `EXPORT_SYMBOL(gsi_write_channel_scratch2_reg);`
  - `EXPORT_SYMBOL(gsi_write_wdi3_channel_scratch2_reg);`
- Left other scratch exports intact:
  - `gsi_write_device_scratch`
  - `gsi_write_evt_ring_scratch`
  - `gsi_read_channel_scratch`
  - `gsi_read_wdi3_channel_scratch2_reg`
  - `gsi_update_mhi_channel_scratch`
- Removed stale kernel outputs again:
  - `drivers/platform/msm/gsi/gsi.o`
  - related aggregate `built-in.o` files
  - `vmlinux`
  - `Module.symvers`

Next build handoff:

- User should rerun `m bootimage vendorimage`.

### Follow-up: GSI channel-scratch read/update exports

User reran the limited image build and the genksyms/LLD pattern moved again, this time to:

- `gsi_read_channel_scratch`

Inspection showed the remaining channel-scratch read/update helpers use the same packed union/packed struct ABI pattern:

- `gsi_read_channel_scratch`
- `gsi_read_wdi3_channel_scratch2_reg`
- `gsi_update_mhi_channel_scratch`

The active kernel config still has no IPA/GSI/RMNET/WDI loadable modules (`=m`), and observed callers are built-in.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/drivers/platform/msm/gsi/gsi.c` to comment out:
  - `EXPORT_SYMBOL(gsi_read_channel_scratch);`
  - `EXPORT_SYMBOL(gsi_read_wdi3_channel_scratch2_reg);`
  - `EXPORT_SYMBOL(gsi_update_mhi_channel_scratch);`
- Left non-channel scratch exports active:
  - `gsi_write_device_scratch`
  - `gsi_write_evt_ring_scratch`
- Removed stale kernel outputs again:
  - `drivers/platform/msm/gsi/gsi.o`
  - related aggregate `built-in.o` files
  - `vmlinux`
  - `Module.symvers`

Next build handoff:

- User should rerun `m bootimage vendorimage`.

## 2026-07-11 Kernel long-build status check

User reported Soong warnings during `m bootimage vendorimage`:

- `ninja may be stuck`
- Current visible step: `Building Kernel Image (Image.gz-dtb)`

Checked VM state without starting/stopping the build:

- `out/soong.log` showed ninja still inside the kernel `Image.gz-dtb` target.
- Process tree showed active kernel sub-make under `techpack/audio/asoc/...`.
- Kernel output timestamps continued to advance after the warning:
  - `techpack/audio/asoc/codecs/wcd_cpe_services.o` at `12:42:30`
  - `techpack/audio/asoc/codecs/wcd-spi.o` at `12:42:56`
  - `techpack/audio/asoc/codecs/wcd-mbhc-v2.o` at `12:43:25`
  - `techpack/audio/asoc/codecs/wcd-mbhc-adc.o` at `12:43:55`

Conclusion:

- No failure found at this point.
- This looked like a long kernel sub-make step where `.ninja_log` was not updated, causing Soong's watchdog warning.
- User should wait for either completion or the next explicit compiler/linker error.

## 2026-07-11 Kernel LLD module CRC relocation fix

User reran the limited image build and reported a kernel link failure:

- `ld.lld: error: relocation R_AARCH64_ABS32 cannot be used against symbol '__crc_gsi_write_channel_scratch'`
- Failure was in `___kcrctab+gsi_write_channel_scratch` from `drivers/platform/msm/gsi/gsi.o`.

Root cause:

- The kernel has `CONFIG_MODVERSIONS=y`, which emits exported-symbol CRC entries.
- With the older absolute CRC encoding, `include/linux/export.h` emits `.long __crc_<symbol>`.
- LLD rejects that absolute 32-bit relocation in the linked arm64 kernel image.
- The kernel tree already supports `CONFIG_MODULE_REL_CRCS`, which changes CRC entries to relative `.long __crc_<symbol> - .`.

Handled VM modifications directly:

- Enabled `CONFIG_MODULE_REL_CRCS=y` in the active source defconfig:
  - `/home/premanandal1978/android/waterlily/kernel/asus/I001D/arch/arm64/configs/vendor/I001D_defconfig`
- Also updated the current generated kernel `.config` so the next incremental build uses the same setting immediately:
  - `/home/premanandal1978/android/waterlily/out/target/product/I001D/obj/KERNEL_OBJ/.config`
- Removed stale generated kernel config headers:
  - `include/generated/autoconf.h`
  - `include/config/auto.conf`
  - `include/config/module/rel/crcs.h`

Next build handoff:

- User should rerun `m bootimage vendorimage`.

### Follow-up: make `MODULE_REL_CRCS` effective

User reran the build and hit the same `R_AARCH64_ABS32` relocation error again.

Inspection showed:

- `CONFIG_MODULE_REL_CRCS=y` was present in source `.config` from the earlier edit.
- But it was missing from generated kernel config files:
  - `out/target/product/I001D/obj/KERNEL_OBJ/include/config/auto.conf`
  - `out/target/product/I001D/obj/KERNEL_OBJ/include/generated/autoconf.h`

Root cause:

- `MODULE_REL_CRCS` is an invisible Kconfig symbol.
- Setting it directly in a defconfig is not enough unless an architecture or another symbol selects it.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/arch/arm64/Kconfig`:
  - Added `select MODULE_REL_CRCS if MODVERSIONS`.
- Synced generated kernel config for the next incremental run:
  - `.config`
  - `include/config/auto.conf`
  - `include/generated/autoconf.h`
  - `include/config/module/rel/crcs.h`
- Removed stale kernel outputs that could retain old absolute CRC relocations:
  - `drivers/platform/msm/gsi/gsi.o`
  - related `built-in.o` aggregate objects up through `drivers/built-in.o`
  - top-level `built-in.o`
  - `vmlinux`

Verified generated config now contains:

- `CONFIG_MODVERSIONS=y`
- `CONFIG_MODULE_REL_CRCS=y`

Next build handoff:

- User should rerun `m bootimage vendorimage`.

## 2026-07-11 Android sched header follow-up

User reran the limited image build and reported:

- `hardware/qcom-caf/sm8150/audio/hal/audio_extn/audio_extn.c:1050`
- `error: variable has incomplete type 'struct sched_param'`

Root cause:

- The previous kernel UAPI patch correctly hid `struct sched_param` from Android userspace in `kernel/asus/I001D/include/uapi/linux/sched/types.h`.
- However, including that kernel header still defined the installed header guard before bionic's `<sched.h>` was parsed.
- Android 16 bionic then skipped its own `struct sched_param`, leaving only a forward declaration.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/kernel/asus/I001D/include/uapi/linux/sched/types.h` to undefine both possible guard names for Android userspace after the kernel-only portion:
  - `_UAPI_LINUX_SCHED_TYPES_H`
  - `_LINUX_SCHED_TYPES_H`
- Removed `out/soong/.intermediates/vendor/bliss/build/soong/generated_kernel_includes` again so Soong regenerates installed UAPI headers from the patched source.

Next build handoff:

- User should rerun `m bootimage vendorimage`.

## 2026-07-12 Official vs port ROM comparison for bootloop/AVB error

Compared:

- Official: `waterlily-i001d-reconstruction/artifacts/Bliss-v19.6-I001D-OFFICIAL-gapps-20260616.zip`
- Port: `waterlily-i001d-reconstruction/artifacts/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip`

Generated local comparison reports under:

- `waterlily-i001d-reconstruction/comparison/payload-critical/payload_summary.txt`
- `waterlily-i001d-reconstruction/comparison/payload-critical/avb_report.txt`
- `waterlily-i001d-reconstruction/comparison/payload-critical/boot_report.txt`
- `waterlily-i001d-reconstruction/comparison/payload-critical/ramdisk/ramdisk_diff.txt`

Key findings:

- Both ROMs are A/B payload OTAs with the same critical partition set: `boot`, `dtbo`, `system`, `vendor`, `vbmeta`.
- Port `dtbo.img` inside the OTA payload is byte-identical to official.
- Port payload `vbmeta.img` is internally consistent with the port payload `boot.img` and `dtbo.img`; the obvious payload-level boot/vbmeta hash mismatch is not present.
- Official and port `vbmeta.img` both use AVB flags `3` at the top-level vbmeta, so the port is not uniquely stricter than official there.
- The port boot image is missing the official kernel cmdline token:
  - official has `androidboot.boot_devices=soc/1d84000.ufshc`
  - port did not have it
- The port ramdisk is missing the official first-stage fstab duplicate:
  - official has `first_stage_ramdisk/system/etc/fstab.qcom`
  - port did not have it
- Port still has `system/etc/recovery.fstab`, and its contents match official, but that does not replace the missing first-stage path.

Assessment:

- The two high-signal early boot/AVB-adjacent differences are the missing `androidboot.boot_devices` kernel cmdline entry and the missing `first_stage_ramdisk/system/etc/fstab.qcom`.
- These directly affect first-stage mount and block-device discovery, and are a better immediate fix target than changing vbmeta flags or DTBO.
- Separate standalone images in `artifacts/` do not always match the images embedded in the OTA payload. If flashing separate `boot.img`, `dtbo.img`, or `vbmeta.img`, keep them as a matched set from the same build/export.

Handled VM modifications directly:

- Patched `/home/premanandal1978/android/waterlily/device/asus/sm8150-common/BoardConfigCommon.mk`:
  - added `androidboot.boot_devices=soc/1d84000.ufshc` to `BOARD_KERNEL_CMDLINE`
- Initial attempted fix added a `prebuilt_first_stage_ramdisk` module, but Soong rejected it because this tree's neverallow policy forbids defining `prebuilt_first_stage_ramdisk` from a device `Android.bp`.
- Corrected VM fix:
  - removed the forbidden `fstab.qcom.first_stage` Soong module
  - removed `fstab.qcom.first_stage` from `PRODUCT_PACKAGES`
  - added the same fstab through the existing recovery root overlay:
    - `/home/premanandal1978/android/waterlily/device/asus/I001D/recovery/recovery/root/first_stage_ramdisk/system/etc/fstab.qcom`
- Rationale:
  - `TARGET_RECOVERY_DEVICE_DIRS := $(DEVICE_PATH)/recovery`
  - Android build copies files from `device/asus/I001D/recovery/recovery/root` into the recovery-as-boot ramdisk, avoiding the Soong neverallow path.

Next build handoff:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D bp4a userdebug
m bootimage
```

## vbmeta Comparison

All three vbmetas (official OTA, new-fixed build, previous build) were analyzed with avbtool:

### Shared Properties
- **Algorithm**: SHA256_RSA4096 (all three)
- **Public Key SHA1**: `2597c218aae470a130f61162feaae70afd97f011` (AOSP test key, identical in all)
- **Public Key SHA256**: `7728e30f50bfa5cea165f473175a08803f6a8346642b5aa10913e9d9e6defef6` (identical)
- **Flags**: 3 (0x3 = VERIFICATION_DISABLED | HASHTREE_DISABLED, all three)
- **Rollback**: 0 (all three)

### Differences
- **Descriptors ordering**: Official has `boot.os_version → boot.fingerprint → dtbo.fingerprint → system.* → vendor.*`. Both builds have `boot.* → system.* → vendor.* → dtbo.fingerprint` (dtbo last).
- **Boot hash**: Different image sizes (official: 38985728, new: 38916096, previous: 38912000), different salts and digests
- **Dtbo hash**: Same size (6569984), different salt/digest
- **System hashtree root digest**: Different per build
- **Vendor hashtree root digest**: Different per build  
- **Signatures**: All different (expected — different content → different signature)
- **Aux block size**: Official 2944, builds 3008 (slightly larger)
- **File size**: Official 4096 (raw blob), builds 65536 (partition image)

### ret=5 Analysis
The `ret=5` (AVB_SLOT_VERIFY_RESULT_ERROR_INVALID_METADATA) from the original failed boot is likely **not** caused by AVB public key mismatch — all vbmetas share the same key. Since flags=3 disables verification, possible root causes:

1. **Invalid signature** — rebuilt vbmeta has different content → needs new signature. The build system may have signed with a corrupt or incompatible avbtool version.
2. **Bootloader-specific behavior** — ASUS I001D bootloader may enforce signature verification despite flags=3 (custom AVB implementation).
3. **Corrupt flash** — vbmeta partition may have been partially written or corrupted.
4. **Error misattribution** — ret=5 may originate from a later boot stage (e.g., kernel panic or init failure) misreported as AVB error.

**Recommendation**: Before next flash test, verify the build's vbmeta signature using `avbtool verify_image` with the test key.

## Deep Investigation Report (2026-07-12)

A comprehensive read-only investigation was completed: `deep-investigation-report.md`

### Key Finding
The `ret=5` / `result=2` error is definitively **AVB signature verification failure** — the vbmeta's RSA signature does not match its embedded public key. The recovery dmesg confirms `avb_slot_verify failed, result: 2` which is `AVB_SLOT_VERIFY_RESULT_ERROR_VERIFICATION`.

The missing `androidboot.boot_devices` cmdline and different kernel/ramdisk are **downstream** — they never execute because the bootloader rejects the vbmeta first.

### Root Cause
The embedded public key is the AOSP test key (`2597c218...`), but the signature was generated with a **different** private key. The build system's `BOARD_AVB_KEY_PATH` likely points to a different key file.

### Next Steps (if/when fixes are requested)
1. Verify that `BOARD_AVB_KEY_PATH` in the device tree points to `external/avb/test/data/testkey_rsa4096.pem`
2. Rebuild vbmeta with correct key
3. Verify signature with `avbtool verify_image --key <key>`
4. Fix the missing `androidboot.boot_devices` cmdline (already done)
5. Fix the missing `fstab.qcom` in first_stage_ramdisk (already done)
6. Flash and test

After build, inspect the new `out/target/product/I001D/boot.img` before flashing:

```bash
python3 - <<'PY'
from pathlib import Path
import struct, gzip
p = Path('out/target/product/I001D/boot.img')
d = p.read_bytes()
vals = struct.unpack('<10I', d[8:48])
kernel_size, ramdisk_size, page_size = vals[0], vals[2], vals[7]
align = lambda x,a: ((x+a-1)//a)*a
ramdisk_off = page_size + align(kernel_size, page_size)
ramdisk = gzip.decompress(d[ramdisk_off:ramdisk_off+ramdisk_size])
cmdline = (d[64:64+512] + d[608:608+1024]).split(b'\0', 1)[0].decode()
print(cmdline)
print('boot_devices:', 'androidboot.boot_devices=soc/1d84000.ufshc' in cmdline)
print('first_stage fstab:', b'first_stage_ramdisk/system/etc/fstab.qcom' in ramdisk or b'system/etc/fstab.qcom' in ramdisk)
PY
```

## 2026-07-12 Pstore Debug Boot Artifact

Created a temporary pstore-enabled boot image from the unofficial boot partition only:

- Source: `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260712.zip`
- Patched output: `D:\tmp\pstore-patch-20260712-220500\boot-pstore-debug.img`

Change applied:

- Added verbose logging and ramoops backend selection to the boot cmdline:
  - `loglevel=8`
  - `ignore_loglevel`
  - `initcall_debug`
  - `pstore.backend=ramoops`
  - `panic=10`
  - `printk.time=1`

Revert path:

- Original untouched boot image remains at `D:\tmp\pstore-patch-20260712-220500\rebuilt\boot.img`

## 2026-07-12 VM Boot Image Build Started

Started a live VM-side `m bootimage` run for the current pstore-enabled source state.

VM log:

- `/home/premanandal1978/android/waterlily/build-logs/bootimage-pstore.log`

Current output observed on the VM:

- `out/target/product/I001D/boot.img`
- `out/target/product/I001D/vbmeta.img`
- `out/target/product/I001D/system.img`
- `out/target/product/I001D/vendor.img`

This is the source-backed image intended for the next flash test.

## 2026-07-13 Official-Kernel Hybrid Test Set

Built a matched hybrid boot test set without running a new Android build.

Inputs:

- Official kernel extracted directly from the official Bliss 19.6 I001D OTA
  payload boot partition.
- Corrected reconstructed ramdisk and cmdline from local `artifacts/boot.img`.
- Current VM `dtbo.img`, `system.img`, and `vendor.img` descriptors.

Hybrid identity:

- Official kernel: `4.14.357-openela-perf+`
- Kernel SHA256: `1ab1aac4d5ad77c3f3abf2bcce54812b46be81d5d649c58e76ee6c26dd19818b`
- Port ramdisk SHA256: `a08533c0a0e43a51e72d11c5b6ac96b191d0196283e07a4205df225fc2a69c5e`
- Corrected `androidboot.boot_devices=soc/1d84000.ufshc` is present.
- `first_stage_ramdisk/system/etc/fstab.qcom` is present and matches the
  reconstructed source fstab.

Flashable local outputs:

- `artifacts/hybrid-official-kernel-20260713/boot-hybrid-official-kernel.img`
  - SHA256: `8f9aac00bb2d7155c09be67146fa71940887bc0a82a475ab5e1c22b931839eff`
- `artifacts/hybrid-official-kernel-20260713/vbmeta-hybrid-official-kernel.img`
  - SHA256: `bc20ceb8ef3738ae4c3cbe6f8cbdc7198bf45eb3bc983efd463259644383352f`
- `artifacts/hybrid-official-kernel-20260713/dtbo.img`
  - SHA256: `8a1acc291a6c894dab61696478f33186e2d044061a3bfdb58c9bacf7764bdd11`

AVB verification result:

- SHA256_RSA4096 vbmeta signature passed with the AOSP RSA4096 key.
- Hybrid boot hash passed.
- DTBO hash passed.
- System hashtree passed.
- Vendor hashtree passed.
- Vbmeta flags are `3`.

The phone was not visible in fastboot at the end of artifact preparation, so no
partition was flashed by the agent.

## 2026-07-13 Hybrid Runtime Breakthrough: BPF Loader Failure

The user flashed the matched official-kernel hybrid set to slot A.

Observed behavior:

- AVB verification no longer stopped the boot.
- The ROG splash remained visible for about 10 seconds instead of the previous
  approximately 0.3-second reset.
- The device then rebooted repeatedly.
- No ADB transport appeared during a 50-second USB poll.

TWRP was booted immediately afterward and all available persistent diagnostics
were collected read-only under:

- `bootloop-logs/hybrid-official-kernel-20260713-054554`

Collected material includes:

- `/metadata`
- `/data/recovery`
- TWRP `dmesg`, properties, cmdline, and recovery log
- Raw `abootlog`, `abootdebug`, `logfs`, and `logdump` partitions

TWRP findings:

- Active slot: `_a`
- `/data` is file-based encrypted and Android crash/dropbox directories were not
  available to TWRP.
- `/sys/fs/pstore` and `/proc/last_kmsg` are still unavailable.
- `abootlog` and `abootdebug` contained no readable retained text.
- The decisive persistent boot reason is:
  - `reboot,bpfloader-failed`
  - Source file: `/metadata/bootstat/persist.sys.boot.reason`

This proves the hybrid passed the bootloader, executed the official
`4.14.357-openela-perf+` kernel, mounted far enough to run Android init, and
reached Android's BPF loader. Android deliberately rebooted because the
`bpfloader` service exited unsuccessfully.

### Missing Official Device Property Identified

The exact platform revisions in the VM match the official OTA manifest:

- `packages/modules/Connectivity`: `c9f3e7795256bd4a3f99d02e604e24fd1552c2b3`
- `system/bpf`: `6c5f4f865220e27cda2a9f4210023a7a10f5bafc`
- `system/core`: `0d6db2bb104f0e63ae514d175d29a49584c251c3`
- `external/libbpf`: `ff800a483e65c6f736a1646d1164c6dd4995af92`

The vendor BPF object is also byte-identical to the official OTA inventory:

- `vendor/etc/bpf/filterPowerSupplyEvents.o`
- SHA256: `ddec446b97c5577043d19a75ae6cae593ace9294b6f68e9b568b66adada4ddca`

The official extracted Android 16 ramdisk contains a device-specific property
that is absent from the reconstruction:

- `ro.bpf.kver_override=5.10.239`
- Evidence: `D:\tmp\myBliss-analysis\android16\device\asus\I001D\ramdisk\prop.default`

The pinned `system/bpf` commit is titled `bpfloader: Allow overriding kernel
version`. Its commit message says devices with BPF backports should set
`ro.bpf.kver_override=5.10.239` so BPF programs requiring those backported
features are selected and loaded correctly.

Current assessment:

- The kernel substitution was successful and exposed the next real blocker.
- The reconstructed device configuration omitted the official BPF kernel-version
  override.
- Adding the exact official property is the next controlled boot test.
- This property should only be tested with the official OpenELA kernel (or a
  source kernel carrying equivalent BPF backports); claiming 5.10 capabilities
  on the old public 4.14.190 kernel is not yet justified.

### BPF Override Source Fix and Second Hybrid

The official ramdisk's property comments identify the original source file as:

- `device/asus/sm8150-common/product.prop`

The reconstructed common tree had no `product.prop` and no
`TARGET_PRODUCT_PROP` entry. Patched the VM source directly:

- Added to `device/asus/sm8150-common/BoardConfigCommon.mk`:
  - `TARGET_PRODUCT_PROP += $(DEVICE_PATH_COMMON)/product.prop`
- Added `device/asus/sm8150-common/product.prop`:
  - `# eBPF`
  - `ro.bpf.kver_override=5.10.239`

For an immediate controlled test without a long Android rebuild, patched the
same property into `prop.default` in the existing official-kernel hybrid
ramdisk, then regenerated its AVB footer and top-level vbmeta.

Second hybrid flashable set:

- Directory: `artifacts/hybrid-official-kernel-bpf-fix-20260713`
- `boot-hybrid-official-kernel-bpf-fix.img`
  - SHA256: `2d943da637b16b536b82d5a1e17d5e4966015d2ad651dad687379704334e5ce3`
- `vbmeta-hybrid-official-kernel-bpf-fix.img`
  - SHA256: `239e4d297ccdf08313c017fd32fd585d3f560530aab585e1d51a40aed0350f33`
- `dtbo.img`
  - SHA256: `8a1acc291a6c894dab61696478f33186e2d044061a3bfdb58c9bacf7764bdd11`

Second hybrid validation:

- Official kernel hash remains
  `1ab1aac4d5ad77c3f3abf2bcce54812b46be81d5d649c58e76ee6c26dd19818b`.
- `ro.bpf.kver_override=5.10.239` is present in ramdisk `prop.default`.
- Ramdisk CPIO parsed through all entries and the trailer.
- RSA4096 vbmeta signature passed.
- Boot hash, DTBO hash, system hashtree, and vendor hashtree all passed.
- Vbmeta flags remain `3`.

Next runtime test: flash the second matched set to slot A and observe whether the
device passes `bpfloader` and reaches the next boot stage.

### Second Hybrid Runtime Result

The user flashed the BPF-override hybrid set to slot A. The device still
bootlooped and TWRP was booted immediately afterward.

Persistent result:

- `/metadata/bootstat/persist.sys.boot.reason` still contains:
  - `reboot,bpfloader-failed`

Flashed slot-A identity was verified directly from TWRP:

- `/dev/block/by-name/boot_a`
  - SHA256: `2d943da637b16b536b82d5a1e17d5e4966015d2ad651dad687379704334e5ce3`
  - Exact match for `boot-hybrid-official-kernel-bpf-fix.img`.
- `/dev/block/by-name/vbmeta_a`
  - SHA256: `239e4d297ccdf08313c017fd32fd585d3f560530aab585e1d51a40aed0350f33`
  - Exact match for `vbmeta-hybrid-official-kernel-bpf-fix.img`.

Conclusion:

- The second result is not caused by flashing an old boot or vbmeta image.
- `ro.bpf.kver_override=5.10.239` is required official device configuration,
  but adding it alone did not make `bpfloader` succeed.
- The remaining BPF failure needs the actual loader/kernel-verifier error text.
- The next logging attempt is to boot TWRP temporarily with the official
  OpenELA kernel, which has pstore support, and check the retained ramoops log
  from the failed Android boot.

### Pstore Recovery Attempt and Early-ADB Debug Set

Built a temporary TWRP image containing:

- Official Bliss `4.14.357-openela-perf+` kernel.
- Original TWRP ramdisk.
- Original TWRP header-v2 embedded DTB, which contains
  `ramoops@bff00000`.

The temporary image was accepted by `fastboot boot`, but recovery ADB did not
appear. The likely limitation is incompatibility between the official kernel's
module ABI/signature policy and TWRP's modules. No partition was changed by this
temporary boot.

Prepared a third matched hybrid specifically to expose the live BPF loader log
through early ADB. It preserves the second hybrid and adds these temporary
debug properties to ramdisk `prop.default`:

- `ro.debuggable=1`
- `ro.force.debuggable=1`
- `ro.adb.secure=0`
- `persist.sys.usb.config=adb`
- `persist.vendor.usb.config=adb`
- `sys.usb.config=adb`

Debug flash set:

- Directory: `artifacts/hybrid-official-kernel-bpf-debug-adb-20260713`
- `boot-hybrid-bpf-debug-adb.img`
  - SHA256: `365b88f094735f99bdf91f5e801cdc4e9e1db2d44e0a1837287cc92616641d01`
- `vbmeta-hybrid-bpf-debug-adb.img`
  - SHA256: `670ed74c5a216355134bcbb0def2e7f7858d1560b1adba9fa0c31c83694c38d5`
- `dtbo.img`
  - SHA256: `8a1acc291a6c894dab61696478f33186e2d044061a3bfdb58c9bacf7764bdd11`

Full AVB verification passed again for vbmeta signature, boot, DTBO, system,
and vendor. Device is visible in fastboot on slot A and is ready for the debug
flash. The purpose of this image is logging only; the insecure ADB properties
must not be retained in a release build.

### Early-ADB Runtime Result and Property-Placement Correction

The user flashed the early-ADB debug set to slot A and rebooted while a
180-second USB monitor was active.

Monitor result:

- No ADB transport enumerated before or during the bootloop.
- The capture directory contains only the monitor state showing `adb_absent`:
  - `bootloop-logs/bpf-early-adb-20260713-062310`

Important correction:

- Patching `prop.default` inside the recovery-as-boot ramdisk does not prove the
  property is visible to normal Android second-stage init.
- The current generated normal-boot product property file is:
  - `out/target/product/I001D/system/product/etc/build.prop`
- It does not contain `ro.bpf.kver_override` because it predates the VM source
  fix.
- The official artifact identified the source as common-tree `product.prop`, so
  the VM source fix is still correct, but it must be rebuilt into or otherwise
  applied to the product directory inside the system partition.
- The unchanged `reboot,bpfloader-failed` result therefore does not yet refute
  the official BPF override; the runtime loader most likely never received it.

Next controlled test:

- Boot original TWRP.
- Mount slot-A system read-write.
- Back up `/system/product/etc/build.prop` (or its actual TWRP mount-relative
  equivalent).
- Add exactly `ro.bpf.kver_override=5.10.239` to that normal-boot property file.
- Verify the line on-device before rebooting.

This avoids a multi-gigabyte system rebuild/download for the diagnostic test.
The source-tree `product.prop` fix remains required for the eventual ROM build.

### Slot-A Normal-Boot Product Property Patched In Place

After the early-ADB test, original TWRP initially could not be reached while
slot A was active. Recovery path used:

- Entered fastboot with hardware keys.
- Verified slot B was bootable and successful.
- Temporarily switched active slot from A to B.
- Booted the untouched original TWRP image with `fastboot boot`.
- TWRP recovery ADB became available normally from slot B.

Mounted slot A explicitly, independent of TWRP's active slot:

- Block device: `/dev/block/by-name/system_a`
- Mount point: `/mnt/system_a_patch`
- Filesystem mounted read-write as ext4.
- Normal-boot property file:
  - `/mnt/system_a_patch/system/product/etc/build.prop`

Before patch:

- `ro.bpf.kver_override` was absent.
- SHA256: `8176cff734e1167938abd91a1d5e65f5f745c7ebfbef81b06706721002681c70`
- Mode/owner/context: `0600`, root:root, `u:object_r:system_file:s0`

Created an on-device backup:

- `/mnt/system_a_patch/system/product/etc/build.prop.pre-bpf-override`
- SHA256 matches the original:
  `8176cff734e1167938abd91a1d5e65f5f745c7ebfbef81b06706721002681c70`

Patched the real normal-boot product file with:

- `ro.bpf.kver_override=5.10.239`

After patch:

- Property verified at line 80.
- SHA256: `0dccec650aaf957defe210f91ccf580dfa50b32b68aecf1b6476e7cd06f89a43`
- Mode/owner/context restored to `0600`, root:root,
  `u:object_r:system_file:s0`.
- Filesystem sync completed.

Local before/after copies and command evidence are stored under:

- `bootloop-logs/system-a-bpf-property-patch-20260713-063735`

This is the first runtime test in which normal Android second-stage init will
actually be able to read the official `ro.bpf.kver_override` property.

### First Runtime Test With Product-Partition BPF Override

The user restored the clean matched hybrid set to active slot A after patching
the real normal-boot product property file:

- Boot SHA256:
  `2d943da637b16b536b82d5a1e17d5e4966015d2ad651dad687379704334e5ce3`
- Vbmeta SHA256:
  `239e4d297ccdf08313c017fd32fd585d3f560530aab585e1d51a40aed0350f33`
- DTBO SHA256:
  `8a1acc291a6c894dab61696478f33186e2d044061a3bfdb58c9bacf7764bdd11`

A 180-second host monitor ran from `06:41:20` through `06:44:20` while the
device booted. Runtime result:

- The ROG splash remained visible for the entire observation period.
- This was the longest sustained splash observed during the reconstruction.
- The device did not perform the previous rapid reboot during the monitor
  window.
- Neither an ADB transport nor fastboot transport enumerated.
- No logcat or dmesg could be captured because Android ADB never appeared.

Monitor evidence is stored under:

- `bootloop-logs/bpf-product-property-runtime-20260713-064120`

This is a confirmed behavioral change from the prior
`reboot,bpfloader-failed` loop, but it is not yet proof of a successful Android
boot. The next evidence step is to enter fastboot, use the known-good slot B
path to boot TWRP, and read `/metadata/bootstat/persist.sys.boot.reason` for
the latest attempt.

### Normal Android ADB and First Decisive Runtime Log

After the sustained splash test, the device was forced to fastboot and the
untouched TWRP image was booted temporarily through slot B. Recovery evidence:

- `/metadata/bootstat` was empty; the previous
  `reboot,bpfloader-failed` file was no longer present.
- `/data` remained encrypted and unavailable to TWRP beyond its recovery
  directory.
- `/sys/fs/pstore` was absent.
- A read-only slot-A mount confirmed the normal product property survived:
  - `ro.bpf.kver_override=5.10.239`
  - SHA256 of `system/product/etc/build.prop`:
    `0dccec650aaf957defe210f91ccf580dfa50b32b68aecf1b6476e7cd06f89a43`

To expose normal Android ADB, the slot-A product `build.prop` was patched with
temporary diagnostic properties while retaining the BPF override:

- `ro.debuggable=1`
- `ro.force.debuggable=1`
- `ro.adb.secure=0`
- `persist.sys.usb.config=adb`
- `persist.vendor.usb.config=adb`
- `sys.usb.config=adb`

The BPF-only version was backed up on-device as:

- `/system/product/etc/build.prop.bpf-only`
- SHA256:
  `0dccec650aaf957defe210f91ccf580dfa50b32b68aecf1b6476e7cd06f89a43`

The diagnostic version and device read-back both have SHA256:

- `89dbb0f255b7aa00c37ebc21cf3f8351d8ae363fea195ed513c0899efb4d8dc3`

Local before/after/read-back evidence is stored under:

- `bootloop-logs/system-a-runtime-adb-patch-20260713-065036`

On the next slot-A boot, normal Android ADB enumerated after approximately 27
seconds. A full initial logcat of about 2 MB was captured under:

- `bootloop-logs/product-adb-runtime-20260713-065308`

Runtime properties proved the intended test state:

- `ro.bpf.kver_override=[5.10.239]`
- `ro.debuggable=[1]`
- `ro.adb.secure=[0]`
- `persist.sys.usb.config=[adb]`
- `sys.usb.config=[adb]`
- `persist.sys.boot.reason=[]`

The old forced BPF reboot was bypassed. BPF compatibility is still incomplete:

- `NetBpfLoad` detected overridden kernel version `5.10.239` (`50a00ef`).
- Rust `BpfLoader` reported missing kernel BTF at
  `/sys/kernel/btf/vmlinux`.
- Some BPF objects failed, including `timeInState.bpf`, `fuseMedia.bpf`, and
  `gpuWork.bpf`.
- Despite those errors, the phone continued into zygote and native service
  startup instead of immediately rebooting with `bpfloader-failed`.

The present display blocker is now directly confirmed:

- `/vendor/bin/hw/android.hardware.graphics.composer@2.4-service` started.
- It logged `falling back to gralloc module`.
- It then logged `failed to open framebuffer device: No such file or
  directory`.
- The phone exposes DRM nodes `/dev/dri/card0` and `/dev/dri/renderD128`, but
  no legacy `/dev/fb0` or `/dev/graphics` node.
- `hwservicemanager` could not find
  `android.hardware.graphics.composer@2.1::IComposer/default`.
- `surfaceflinger` aborted with `failed to get hwcomposer service` and
  restarted about every five seconds.
- The user recalled deleting msmnile content during reconstruction, which is
  consistent with the missing Qualcomm display implementation observed at
  runtime. This recollection still requires source/artifact verification.

Other vendor/framework compatibility failures were also captured, including
audio HAL registration failures, missing linker symbols for the sensors HAL,
and missing legacy libraries for camera and DRM services. The hardware composer
failure is the direct reason the UI cannot leave the splash screen in this
test; the other failures remain subsequent reconstruction work.

### Missing Android 16 Qualcomm Hardware Composer Identified

The device was returned to fastboot and untouched TWRP was booted temporarily
through slot B. A read-only mount of `vendor_a` confirmed:

- No `hwcomposer.*.so` file exists under either `vendor/lib/hw` or
  `vendor/lib64/hw`.
- The generic `android.hardware.graphics.composer@2.4-service` binary and init
  RC are present.
- Qualcomm display support libraries such as `libsdmcore.so`,
  `libsdmutils.so`, `libdrmutils.so`, and `libgralloc.qti.so` are present.
- The composer VINTF fragment is absent from `vendor/etc/vintf/manifest`.

The VM source showed why:

- The Android 14 baseline product listed `hwcomposer.msmnile`.
- Reconstruction removed that stale module name to pass Android 16 package
  validation.
- The current Android 16 display source defines the replacement module as
  `hwcomposer.qcom` in
  `hardware/qcom-caf/sm8150/display/sdm/libs/hwc2/Android.bp`.
- That module has a required dependency on
  `android.hardware.graphics.composer-qti-display.xml`, so including the module
  installs both the HWC library and its VINTF declaration.

The previously generated official Android 16 vendor inventory independently
confirms the expected official files:

- `vendor/lib/hw/hwcomposer.qcom.so`
  - SHA256: `f84b49685abff45a2307de133fd4e57b9789ed02e5f719c55cec8702a42dec89`
- `vendor/lib64/hw/hwcomposer.qcom.so`
  - SHA256: `f4443a75d127cfde51ee4bee87680558ac8420d04ba073ff23f3abd4ed6c264a`
- `vendor/etc/vintf/manifest/android.hardware.graphics.composer-qti-display.xml`
  - SHA256: `e7a683cff14141d8b101150ac95c6738499601a6bf3bb13c3e33de87cae30f9b`

Corrective VM edit applied:

- Added `hwcomposer.qcom` to the display `PRODUCT_PACKAGES` list in
  `device/asus/sm8150-common/msmnile.mk`.
- Reproducible patch:
  `patches/add-hwcomposer-qcom.patch`.
- A targeted module build is pending user execution. No long Android build was
  started by the agent.

### Targeted Hardware Composer Build and Slot-A Test Install

The user ran the handed-off targeted VM build:

- `m hwcomposer.qcom -j"$(nproc)"`
- Build completed successfully in 7 minutes 29 seconds.

Primary outputs:

- `vendor/lib/hw/hwcomposer.qcom.so`
  - Size: 272424 bytes
  - SHA256: `45b787ce9001d4519833046ce7a2ffa03c769d14993f71bb1ad9dcbe99c10b95`
- `vendor/lib64/hw/hwcomposer.qcom.so`
  - Size: 374840 bytes
  - SHA256: `086b2d5a0d75e99aa6e03a9ebdd4283c7bcc6163102cffd1b26dc891a23208f3`
- `vendor/etc/vintf/manifest/android.hardware.graphics.composer-qti-display.xml`
  - Size: 2120 bytes
  - SHA256: `e7a683cff14141d8b101150ac95c6738499601a6bf3bb13c3e33de87cae30f9b`
  - This manifest hash is byte-for-byte identical to official Bliss.

Static `DT_NEEDED` checks found that the flashed vendor image also lacked two
non-core dependencies produced by the targeted build. Both architectures were
added:

- `vendor/lib/libgpu_tonemapper.so`
  - SHA256: `a8a00008bcae7729fba533be211efd7e5cd42c53ecfd3f7afda720b6a885277e`
- `vendor/lib64/libgpu_tonemapper.so`
  - SHA256: `771a395ec46ac8ff06f80fe444ed76c5dc1fbc2b41f2ff31fdf8a9ff10bf9ed3`
- `vendor/lib/libhidltransport.so`
  - SHA256: `4eeed60cbf06c0bbd2f7d65d355e430dcbce661b9ab83408965ee66d18fc69ff`
- `vendor/lib64/libhidltransport.so`
  - SHA256: `101a66b6b975e667d60d150ad69ae9620ccb0fdb410ae0d1a4c3d96aedc4a779`

The two `libhidltransport.so` hashes are also byte-for-byte identical to
official Bliss. Full 32-bit closure additionally required:

- `vendor/lib/android.hardware.graphics.composer@2.4.so`
  - SHA256: `129804e124425865c218711cec357ec0acab7d58f6723420cf2b6cafe9ec0479`
  - Byte-for-byte identical to official Bliss.

All eight files were installed into slot-A `vendor_a` from TWRP. Verification:

- Owner: root:root
- Mode: `0644`
- HWC modules: `u:object_r:same_process_hal_file:s0`
- `libhidltransport.so`: `u:object_r:same_process_hal_file:s0`
- `libgpu_tonemapper.so` and composer interface library:
  `u:object_r:vendor_file:s0`
- VINTF XML: `u:object_r:vendor_configs_file:s0`
- On-device hashes match the downloaded VM outputs.
- Both HWC architectures now have all vendor-side dependencies. Their only
  non-vendor dependencies are the normal system-provided `libsync`, `liblog`,
  `libc`, `libm`, and `libdl` LLNDK libraries.

Local test bundle:

- `artifacts/hwcomposer-qcom-test-20260713`

Rollback is deterministic because every destination was absent before this
test: remove these eight added files from `vendor_a`. No existing vendor file
was overwritten.

### Hardware Composer Fix Confirmed; Sensors HAL Is Next Blocker

The slot-A boot after the HWC test install reached the Bliss boot animation for
the first time. Runtime evidence:

- Normal Android ADB appeared after approximately 28 seconds.
- `android.hardware.graphics.composer@2.4-service` remained running.
- `surfaceflinger` remained running instead of aborting every five seconds.
- The user visually confirmed the Bliss animated splash.
- `system_server`, both zygotes, and boot animation started.
- `sys.boot_completed` remained unset.

The full runtime capture is under:

- `bootloop-logs/hwcomposer-qcom-runtime-20260713-073101`

A 15-minute filtered monitor then identified the next blocker. Every
`system_server` instance blocks while creating `SystemSensorManager` because
the default sensors HAL never registers. After 66 seconds Android's watchdog
kills the system process. Exact watchdog stack:

- `android.hardware.SystemSensorManager.nativeCreate`
- `android.hardware.SystemSensorManager.<init>`
- `ActivityTaskManagerService.installSystemProviders`
- `ContentProviderHelper.installSystemProviders`
- `SystemServer.startOtherServices`

The cycle repeats with a new `system_server` PID. After several system-server
deaths the device performs a complete reboot and starts the same cycle again.
HWC and SurfaceFlinger recover and remain functional after each full restart.

The sensors service failure immediately preceding the watchdog is:

- Binary: `/vendor/bin/hw/android.hardware.sensors@2.0-service.multihal`
- Linker error: missing C++ symbol
  `android::base::WriteStringToFd(..., android::base::borrowed_fd)`
- Therefore `android.hardware.sensors@2.0::ISensors/default` never registers.

Binary identity proved that the Android 14 prebuilt overwrote the correct
Android 16 source-built module:

- Flashed/reconstructed output binary SHA256:
  `d35f76900fe5bb0a9770b250a1f0a21f12e37a64ff5eb5b040dafb8eb5aa437f`
- Android 14 inventory has the same hash.
- Official Android 16 binary SHA256:
  `687a2021a43946455338b6e1103a1932c67209d3ccceb53b32d573d840f74873`
- The VM Soong intermediate built from
  `hardware/interfaces/sensors/2.0/multihal` has exactly the same Android 16
  hash as official Bliss.

Official vendor extraction also confirmed the current source versions of the
companion files:

- Init RC SHA256:
  `82338cab126ecf8dbef8d54a40ca70f17b99483897255fc9a53742edf25cf786`
- VINTF XML SHA256:
  `44ed7ec8fcc605c1bd6dcb2a455d0ce1d990fb2f7a8326b4d9fb4b3e7a9b3abc`
- Both VM `hardware/interfaces` source outputs match official Bliss.

Root cause is a product-copy collision:

- `msmnile.mk` correctly requests the source-built Android 16 multihal module.
- `vendor/asus/I001D/I001D-vendor.mk` then copies the stale Android 14 binary
  and RC to the same output paths, replacing the correct module outputs.

Corrective VM source edit applied:

- Removed the multihal binary and RC from
  `device/asus/I001D/proprietary-files.txt`.
- Removed only their two stale copy rules from
  `vendor/asus/I001D/I001D-vendor.mk`.
- All actual ASUS/QTI sensor blobs and configuration files remain unchanged.
- Reproducible patch:
  `patches/use-source-built-sensors-multihal.patch`.

The official Android 16 files extracted directly from the official OTA are
stored under:

- `artifacts/sensors-hal-official-test-20260713`

No follow-up build has been started by the agent. A reversible TWRP replacement
test of the binary and RC is pending.

### Slot-A Android 16 Sensors Multihal Test Install

With the device in TWRP, slot-A `vendor_a` was mounted read-only and the three
sensor service artifacts were revalidated:

- Binary was stale Android 14:
  `d35f76900fe5bb0a9770b250a1f0a21f12e37a64ff5eb5b040dafb8eb5aa437f`
- Init RC was stale Android 14:
  `78208c038ca55479f3835d4759d4845eec9b5bdf9c79f37b158a2b5c9f43de6c`
- VINTF XML was already the correct official Android 16 version:
  `44ed7ec8fcc605c1bd6dcb2a455d0ce1d990fb2f7a8326b4d9fb4b3e7a9b3abc`

Local copies of the two replaced Android 14 files were saved under:

- `artifacts/sensors-hal-official-test-20260713/device-before`

On-device rollback copies were created as filenames that init will not parse or
execute:

- `vendor/bin/hw/android.hardware.sensors@2.0-service.multihal.android14-backup`
- `vendor/etc/init/android.hardware.sensors@2.0-service-multihal.rc.android14-backup`

Installed the official/source-built Android 16 files:

- Sensors binary SHA256:
  `687a2021a43946455338b6e1103a1932c67209d3ccceb53b32d573d840f74873`
- Init RC SHA256:
  `82338cab126ecf8dbef8d54a40ca70f17b99483897255fc9a53742edf25cf786`

On-device metadata was restored exactly:

- Binary: root:shell, `0755`,
  `u:object_r:hal_sensors_default_exec:s0`
- Init RC: root:root, `0644`, `u:object_r:vendor_configs_file:s0`

Static dependency validation found all vendor-side dependencies present. Only
normal system-provided `liblog`, `libc`, `libm`, and `libdl` remain outside the
vendor partition. A monitored slot-A runtime test is next.

### Android 16 Sensors Runtime Result

The monitored slot-A boot started at host time `2026-07-13 07:56:49 +05:30`.
Normal Android ADB became available after approximately 25 seconds. At
`07:57:17`, all previously important processes were alive together:

- `system_server`: PID 1786
- Qualcomm HWC service: PID 1183
- Android 16 sensors multihal: PID 1167
- SurfaceFlinger: running
- Boot animation: running

By `07:57:46`, Android's service manager reported all of these framework
services as present:

- `sensorservice`
- `activity`
- `dropbox`
- `user`

The same four services were still present at `07:58:18`. This confirms that
the Android 16 sensors multihal replacement resolves the prior linker failure
and the resulting `SystemSensorManager.nativeCreate` startup block. The source
tree fix in `patches/use-source-built-sensors-multihal.patch` is therefore
runtime-validated.

`sys.boot_completed` was not observed during this capture. The device was then
returned to TWRP, where persistent tombstones from the tested boot were copied
to:

- `bootloop-logs/sensors-a16-runtime-20260713-075649/tombstones`

The newest tombstones alternate between two vendor services:

- Camera provider aborts during CamX initialization after opening
  `/dev/video0` returns `Operation already in progress`.
- `/vendor/bin/hw/android.hardware.audio.service` aborts with
  `Could not register Audio Core API`. Runtime messages show no 7.1 or 7.0
  devices factory and rejection of the 6.0 service because it is absent from
  the VINTF manifest.

These crashes are the next live compatibility findings. The capture proves
that they repeat, but does not yet prove which one prevents boot completion.

### VM Persistence Audit And Source Backup

The working VM source state was rechecked after the HWC and sensors runtime
breakthroughs. These proven fixes are present in the VM tree:

- `ro.bpf.kver_override=5.10.239` is in
  `device/asus/sm8150-common/product.prop`, and `BoardConfigCommon.mk` includes
  that file through `TARGET_PRODUCT_PROP`.
- `hwcomposer.qcom` is present in the common `msmnile.mk` product packages.
- The stale sensors multihal binary and RC copy rules are absent from both
  `device/asus/I001D/proprietary-files.txt` and
  `vendor/asus/I001D/I001D-vendor.mk`.

The audit found that the official-kernel breakthrough had not yet been
represented in the VM build configuration: `BoardConfig.mk` still selected
`kernel/asus/I001D`, which produces the nonworking `4.14.190-omni+` kernel.
This was corrected without starting a build:

- Extracted the exact kernel payload from the official Bliss Android 16 boot
  image.
- Stored it at `device/asus/I001D/prebuilt/kernel` on the VM.
- Size: `23315592` bytes.
- SHA256:
  `1ab1aac4d5ad77c3f3abf2bcce54812b46be81d5d649c58e76ee6c26dd19818b`.
- Added `TARGET_FORCE_PREBUILT_KERNEL := true` and
  `TARGET_PREBUILT_KERNEL := $(DEVICE_PATH)/prebuilt/kernel` to
  `device/asus/I001D/BoardConfig.mk`.
- Reproducible text patch: `patches/use-official-prebuilt-kernel.patch`.
- The verified local payload is under
  `artifacts/hybrid-official-kernel-20260713`.

The source kernel tree remains available for generated headers and currently
contains the earlier logging/pstore edits. The official kernel module set is
not yet integrated into the build and remains an explicit parity-audit item.

A full recovery bundle of the current VM worktree state was created and copied
locally:

- `artifacts/source-progress-backup-20260713/waterlily-source-progress-backup-20260713.tar.gz`
- Size: `20894695` bytes.
- SHA256:
  `21a80f98e968850d2664d42e753a9fb2cf9b597efe91e2778b608b40dc913f39`.

The bundle includes complete snapshots of both ASUS device trees, the
official prebuilt kernel, vendor packaging files, and binary git patches for
the I001D device, SM8150 common, vendor, and kernel repositories. No Android
build was started.

### Full Android 14 File-Use And Replacement Audit

A complete live slot-A versus official Android 16 versus old VM prebuilt audit
is stored under:

- `analysis/android14-live-audit-20260713`
- Main report: `analysis/android14-live-audit-20260713/report.md`

The phone's `vendor_a` and `system_a` partitions were mounted read-only in
TWRP. All 2,686 live vendor regular files, all 2,527 official vendor regular
files, and all vendor symlinks were compared. The seven old `system_ext`
prebuilts were also checked directly against the live and official system
images.

The old device inventory contains 939 files. Results:

- 728 are byte-for-byte identical to official Android 16 and must not be
  treated as stale merely because they originated in the Android 14 tree.
- 25 still use old bytes while official has different bytes at the same path.
- 169 still use old paths that official Android 16 omits, primarily the
  discarded 32-bit camera/audio side of the stack.
- 17 old files had already been changed during reconstruction testing.

Nineteen of the 25 stale same-path files have exact official Android 16
outputs already present in the VM source or Soong intermediates. Three more
(`audio.primary.msmnile` in both architectures and `liba2dpoffload`) have
current pinned SM8150 source. Their old `PRODUCT_COPY_FILES` rules overwrite
the source outputs and should be removed rather than replaced by more
prebuilts. Only the official `audio_effects.xml`, 64-bit
`com.qti.chi.override.so`, and 64-bit `libxditk_ditBSP.so` require official
device data from the OTA among this 25-file group.

The audit confirmed the two current runtime failures:

- Audio VINTF declares 7.1/7.0, but `msmnile.mk` selects audio 6.0 and
  soundtrigger 2.1 implementation files. Official is missing 30 audio 7.x and
  soundtrigger 2.2/2.3 files from live vendor and also supplies the required
  64-bit audio dependency set.
- The correct Android 16 Lineage camera provider is already exact official,
  but the old `android.hardware.camera.provider@2.4-service_64` and its RC are
  also packaged. Both start and compete for `/dev/video0`, matching the
  runtime `Operation already in progress` abort.

The kernel-module comparison found 14 common module paths and no byte matches.
Live modules are signed for/vermagic-bound to `4.14.190-omni+`; official
modules are signed for/vermagic-bound to `4.14.357-openela-perf+` with
`modversions`. The AVB test key cannot re-sign kernel modules. The exact
official 14-module set must accompany the now-forced official kernel, while
the three live-only modules require separate proof before retention.

Signing analysis:

- Native ELF files, RC/XML/config files, and scripts have no individual AVB
  signature and may be included in a newly built vendor image.
- Kernel modules retain their kernel-specific PKCS#7 signatures.
- Firmware retains OEM embedded signatures.
- APK and APEX signing is separate and cannot be replaced by the vbmeta key.
- No private key exists in the local reconstruction workspace. The VM defaults
  to AOSP's RSA-4096 AVB test key and AOSP application test certificate, not
  the official Bliss private keys.

The `SwapnilVicky/stable_releases` `waterlily` branch is an Android 16 source
manifest, not an I001D blob/device repository. The VM already matches its
SM8150 audio, display, and media revisions, so the open-source replacement
material is already available locally. The official OTA remains authoritative
for closed I001D/ASUS files.

### Planning-Only Agent Handoff

A complete handoff instruction was created at:

- `to-do-note.md`

It gives the next agent the local and VM locations, protected fixes, full audit
context, audio/camera/kernel-module workstreams, signing constraints, build
handoff rules, and acceptance criteria. The next agent is explicitly limited
to read-only inspection and must not edit Android source, build, flash, or
change the phone.

The required planning deliverable is:

- `analysis/android16-integration-implementation-plan.md`

That plan must contain exact proposed file/module edits, source-of-truth versus
generated-file ownership, prebuilt and removal manifests, validation and build
commands, rollback steps, risks, open questions, and an audit checklist. It is
to be reviewed before implementation begins.

## 2026-07-13: Checkpoint 02 Applied, Build Handoff Pending

The prior planning-only agent document was not used as implementation
authority. A separate narrow implementation record is at:

- `analysis/android16-integration-20260713/checkpoint-02-applied.md`

The VM was changed without starting a build. This checkpoint removes the old
camera provider that competed with the Android 16 provider, selects the Android
16 audio 7.1/effect 7.0/soundtrigger 2.3 stack, enables the 64-bit audio
service, replaces the top-level audio policy and effects XML with verified
official Android 16 files, and adds the official 64-bit QTI Audio HAL Extension
dependency set.

The inherited I001D proprietary manifest had 54 exact duplicate entries. It
was normalized, then `I001D-vendor.mk` was regenerated with the Android 16
Python tooling in legacy copy-only compatibility mode. Static validation passed:
no duplicate source or generated destinations, all 31 stale source-output
collision copies absent, obsolete camera/audio copies absent, and imported
official files matching their official image SHA-256 values.

The change is ready for the first test build. The build is intentionally handed
to the user; do not start it automatically. The next command sequence is:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m blissify
```

After a successful build, record the output ZIP name and test the new build
before attempting the deferred official kernel-module integration or broad
legacy-vendor cleanup.

## 2026-07-13: Checkpoint 03 TrustedUI Build Stop Fixed

The first user-run `m blissify` attempt reached Ninja after 7 minutes and
confirmed that the obsolete camera 2.4/device implementation files and audio
6.0/sound-trigger files were removed from the product output. Ninja then
stopped because the generated I001D copy list still referenced the absent
32-bit blob:

`vendor/asus/I001D/proprietary/vendor/lib/TrustedUI.so`

The audit classifies `/lib/libTrustedUI.so` as an old/live Android 14 prebuilt
that is absent from the official Android 16 vendor. The official Android 16
vendor retains only `/lib64/libTrustedUI.so`, whose audited SHA-256 is
`6b1a11fdfa6a63443deb55f546d9a6ec8d9d75e99f2fc49fd90e239e5ece73de`.

The VM was corrected without starting another build:

- Removed only `vendor/lib/TrustedUI.so` from
  `device/asus/I001D/proprietary-files.txt`.
- Preserved `vendor/lib64/libTrustedUI.so`.
- Regenerated `vendor/asus/I001D/I001D-vendor.mk` with the Android 16 Python
  extract-utils generator in the existing `check_elf=False` compatibility
  mode.
- Verified that all generated `vendor/asus/I001D/proprietary/...` copy sources
  exist and that the generated copy destinations have no duplicates.

Post-fix VM hashes:

- `device/asus/I001D/proprietary-files.txt`:
  `e27af89c48e44efcdd552bb473531fe735e7818ae4e2727e619e12813fb0e531`
- `vendor/asus/I001D/I001D-vendor.mk`:
  `c1d02c759036176f5daa8971c58f1c4618fe977846d019f7781033aadf0fd657`

Build handoff remains with the user. Resume the incremental build with:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m blissify
```

## 2026-07-13: Checkpoint 04 Post-Integration Build Successful

The user resumed `m blissify` after the TrustedUI manifest correction. The
incremental build completed successfully in 16 minutes 45 seconds.

Canonical dated output:

- `out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260713.zip`
- Size: `1,381,178,227` bytes
- SHA-256: `7844de0f25059c9ea8d3de112fa0e6df979fda339970f821920feac62dc84958`

The generated `bliss_I001D-ota.zip` alias has the same size, timestamp, and
SHA-256. Core image hashes from this successful build are:

- `boot.img`: `de10ddee08db30cbb2ec219ef0ed40dae5b03c07e54940fbd9e37f6613a417d3`
- `dtbo.img`: `28d5e62bc4afc92acf6368ffb9a556628b05c159335003d4dcdfa74e4e573a81`
- `vendor.img`: `fd999ee8e78a55df85c2a05b1437fc9a732e4fe937592c9de2d6d68068767a19`
- `vbmeta.img`: `92b89e7b3aa1eddaaafee43fd36cc488c45a820e035a854438089b7aecd4cda7`

No flash has been performed. The next boundary is device testing of this ZIP,
with particular attention to boot, camera-provider startup, audio playback and
capture, Bluetooth audio, and sound trigger. Defer official kernel-module
integration and broad legacy-vendor cleanup until this build's runtime results
are recorded.

The verified build ZIP was downloaded for testing to:

- `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260713.zip`

The local file is `1,381,178,227` bytes and its SHA-256 matches the VM output:
`7844de0f25059c9ea8d3de112fa0e6df979fda339970f821920feac62dc84958`.

## 2026-07-13: Checkpoint 05 Updated Runtime Log and Touch Watchdog Fix

The user flashed/tested the `20260713` OTA and reported a sustained Bliss
splash. TWRP initially exposed no pstore, last-kmsg, bootstat record, or
decrypted Android data. Recovery evidence was saved under:

- `bootloop-logs/post-checkpoint04-splash-20260713-111852`

Slot A `system/product/etc/build.prop` was backed up on-device as
`build.prop.checkpoint04-bpf-only`, then temporarily patched with the previously
proven ADB diagnostic properties while preserving
`ro.bpf.kver_override=5.10.239`. Normal Android ADB appeared after about 22
seconds and a full live log was captured as `logcat-live.txt`.

The immediate boot blocker is proven. Every `system_server` cycle blocks for
66 seconds and is watchdog-killed with this annotated main-thread stack:

- `ServiceManager.waitForDeclaredService`
- `LineageHardwareManager.getAIDLService`
- `LineageHardwareManager.isSupportedAIDL`
- `InputMethodManagerService.updateTouchSensitivity`
- `InputMethodManagerService.systemRunning`

The missing service is `vendor.lineage.touch.IGloveMode/default`. Static and
runtime inspection showed that the AIDL binary, RC, and VINTF fragment are all
packaged, but the executable is labeled generic `vendor_file`. The existing
SELinux path mapping covered only the obsolete HIDL executable name. The RC
also omitted AIDL interface declarations, so init could not map
`ctl.interface_start` requests.

VM source was corrected without starting a build:

- Added the new AIDL executable path to
  `device/asus/sm8150-common/sepolicy/vendor/file_contexts` with
  `hal_lineage_touch_default_exec`.
- Added `interface aidl vendor.lineage.touch.IGloveMode/default` and
  `interface aidl vendor.lineage.touch.ITouchscreenGesture/default` to the
  AIDL touch service RC.
- Post-edit hashes:
  - `file_contexts`: `d360577a3989ce26bb6d59b1ad2ab35d0a17ca4c0d1d2e31dae64e4b7aef6c47`
  - touch RC: `ae38b306b06f3dc482c9867a6489673cbd63a622b96b4df95e6a5459e626135f`

The log also records later work, not the current watchdog stack: camera provider
null dereferences in `CameraModule::notifyDeviceStateChange`, missing
`libprotobuf-cpp-lite-3.9.1.so` for Widevine and Qualcomm sensor helpers,
missing 32-bit `libpng.so` for Wi-Fi Display, and legacy configstore registration
failures. The Android 16 audio 7.1/effect 7.0 service now registers and opens
the SM8150 sound card, so the prior audio-core registration failure is fixed.

## 2026-07-13: Checkpoint 06 Touch Fix Validated, LMKD Cgroup-v2 Fix Applied

The Checkpoint 05 touch correction was tested reversibly on slot A in TWRP.
The touch executable was relabeled `hal_lineage_touch_default_exec` and its RC
was updated with both AIDL interface declarations. On the following Android
boot, these services registered:

- `vendor.lineage.touch.IGloveMode/default`
- `vendor.lineage.touch.ITouchscreenGesture/default`

The former 66-second `system_server` watchdog did not recur. Android reached
`boot_progress_enable_screen`, sent BOOT_COMPLETED broadcasts, stopped the boot
animation, reported `sys.boot_completed=1` and `dev.bootcomplete=1`, and user 0
reached `RUNNING_UNLOCKED`. Runtime evidence is saved in
`bootloop-logs/post-checkpoint04-splash-20260713-111852/logcat-touch-fix-runtime.txt`.

The next failure is UI/runtime instability rather than the original splash
watchdog. Launcher initially lacked its app-data subdirectories and ANRed in
`TouchInteractionService`; SetupWizard later became top-resumed. A broad
first-boot package/resource update then disconnected NetworkStack, causing the
framework to terminate `system_server` and restart the Android runtime. The
full merged log is `logcat-all-after-networkstack-crash.txt` in the same
evidence directory.

A separate deterministic platform mismatch was found during that restart:
`lmkd` exits continuously with `Old kill strategy can only be used with v1
cgroup hierarchy`. The running kernel is `4.14.357-openela-perf+`, mounts
cgroup v2, and has `CONFIG_PSI=y`, but the device tree forced the legacy
strategy with `ro.lmk.use_minfree_levels=true`. Android 16 `lmkd` source
defaults this property to false and explicitly rejects the legacy strategy on
non-v1 memcg.

VM source was corrected without starting a build:

- `device/asus/sm8150-common/system.prop` now sets
  `ro.lmk.use_minfree_levels=false`.
- Post-edit `system.prop` SHA-256:
  `d5eb990c8d0d030e132ab7ef0cdd497035953772aae560984ec2ac4f742de762`.

The next user-run build must include the Checkpoint 05 touch fixes and this
LMKD property change. Runtime acceptance requires `init.svc.lmkd=running`, no
repeat `system_server` restart, and a responsive SetupWizard/Launcher. Later
protobuf, configstore, camera, Wi-Fi Display, and kernel-module work remains
deferred until this UI-stability checkpoint is tested.

The user-run incremental build completed successfully. The regenerated OTA was
downloaded locally for flashing:

- `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-vanilla-20260713.zip`
- Size: `1,381,178,393` bytes
- SHA-256: `6cf4bf67acfc786f2ec795f83d3a7566c62931063d04b35a0bcc180678b58fff`

The local checksum exactly matches the VM output. Runtime testing of LMKD,
SetupWizard, Launcher, SystemUI, and system-server stability is pending.

## 2026-07-13: Checkpoint 07 First Successful Boot

The user flashed the verified Checkpoint 06 OTA and confirmed that it finally
boots into Android. This validates the combined touch AIDL service and LMKD
cgroup-v2/PSI corrections as the changes that cleared the splash/runtime
restart blockers.

Some applications remain broken and require separate runtime triage. Before
any further implementation, the current VM source state must be preserved and
the bundled Home Launcher provenance/version must be audited against
`https://github.com/Prem8791/homelauncher`. No launcher update has been
authorized or applied at this checkpoint.

The current modified VM source was preserved at:

- `/home/premanandal1978/android/waterlily/checkpoint-snapshots/checkpoint07-first-boot-20260713.tar.gz`
- Size: approximately `20 MiB`
- SHA-256: `3d81f4ab6b896d154c881f1f2b4105665a4c294b3fe1ce044a13212167bc7459`

The checkpoint contains binary Git patches, base commit IDs, status manifests,
and archives of untracked files for `device/asus/I001D`,
`device/asus/sm8150-common`, and `vendor/asus/I001D`. It deliberately excludes
reproducible build output and unchanged proprietary blobs.

### Home Launcher provenance and freshness audit

The successful ROM does not source `HomeLauncher` from the manifest-managed
AOSP `packages/apps/Launcher3` project. Soong module metadata traces both
`HomeLauncher` and its privileged-permission module to:

- `reference-material-full/android16/extras/HomeLauncher-full-backup`

That directory has no Git metadata and is a copied/hybrid backup. Its package
name, source layout, class names, resources, integration files, and product
module identify it as a snapshot of `Prem8791/homelauncher`. The built APK is
`/system/priv-app/HomeLauncher/HomeLauncher.apk`, package
`com.home.launcher`; because Soong supplies platform versioning, its APK
reports platform `versionCode=36`/`versionName=16` rather than the Gradle
project's `1`/`1.0.0`.

The integrated snapshot is not current. Its `MainActivity.kt` exactly matches
upstream commit `16c48bcf5b2767ccc9c3b5d716895f3c666f918c` from
2026-07-08 (`Handle nullable Soong view lookups`), although later AOSP
integration files were manually added to the backup. Upstream `main` is
`1f2a42cf7035f8c5c326aacf75540d6fded38753` from 2026-07-10
(`Fix recents card tilt, aspect ratio, app drawer nudge, and tile overlay
layout`), 31 commits beyond that matching source baseline.

Current upstream materially changes `MainActivity`, recents, app indexing,
system stats, app-drawer UI, layouts, themes, permissions, overlays, and the
AOSP build definition. It adds `app/src/aosp` AIDL/Kotlin inputs and replaces
the stale recent-tasks implementation. Therefore the current ROM launcher is
confirmed stale and should not be updated by merely replacing the APK or
copying a few Kotlin files; its current upstream AOSP integration set must be
reviewed as a unit before implementation.

Security note: the public upstream checkout currently contains a
`.gcloud-ssh` directory with apparent private/public VM key material. No key
contents were read or copied into the ROM. Any future source integration must
exclude that directory, and the exposed credential should be revoked and
removed from repository history independently of the ROM work.

## 2026-07-13: Checkpoint 08 Latest HomeLauncher Integrated For Build

At the user's request, upstream `Prem8791/homelauncher` was integrated into the
VM without starting a build. The active source is now a clean sparse Git
checkout at:

- `/home/premanandal1978/android/waterlily/packages/apps/HomeLauncher`
- Branch: `main`
- Commit: `1f2a42cf7035f8c5c326aacf75540d6fded38753`
- Commit date: `2026-07-10T20:19:19+05:30`
- Subject: `Fix recents card tilt, aspect ratio, app drawer nudge, and tile overlay layout`

Sparse-checkout rules explicitly omit `/.gcloud-ssh/`; that directory is not
present in the working tree. The checkout is clean. No GitHub commit or push
was performed.

To prevent duplicate Soong modules, the stale backup definitions were retained
but disabled by renaming only their `Android.bp` files:

- `HomeLauncher-full-backup/Android.bp.checkpoint07-stale-disabled`
- `HomeLauncher-full-backup/rom-integration/aosp/overlays/`
  `HomeLauncherConfigOverlay/Android.bp.checkpoint07-stale-disabled`

Static validation shows exactly one active definition each for `HomeLauncher`,
`privapp-permissions-com.home.launcher`, and `HomeLauncherConfigOverlay`, all
from `packages/apps/HomeLauncher`. The I001D product already includes the app
and permission modules. The optional recents overlay is defined but is not in
`PRODUCT_PACKAGES`.

The latest AOSP inputs are present, including `IOverviewProxy.aidl`,
`ISystemUiProxy.aidl`, `QuickStepService.kt`, `PlatformRecentTasksBackend.kt`,
and the AOSP-specific `RecentTasksRepository.kt`. The upstream-required scan
found no untyped Kotlin `findViewById` calls. A build was intentionally not
started; build ownership remains with the user.

### Pending application/runtime fixes after HomeLauncher source integration

1. Build `HomeLauncher` with Soong and confirm the new source compiles against
   this Android 16 tree.
2. Install/test the platform-signed output or include it in the next OTA;
   verify HOME launch, app drawer, recents cards/snapshots, minimize refresh,
   weather/glance UI, calendar permission behavior, and package-change refresh.
3. Keep `HomeLauncherConfigOverlay` disabled until the new QuickStep service is
   tested with SystemUI. Then test swipe-home, back, overview, task launch, and
   SystemUI stability before considering overlay activation.
4. Recollect HomeLauncher AVCs. Its active upstream policy proposal allows
   `platform_app` to read `proc_stat`, but that policy is not wired into the
   I001D device policy; CPU stats may remain unavailable. Thermal sysfs access
   remains a deferred, evidence-only policy decision.
5. Camera applications remain blocked by the Lineage camera-provider null
   dereference in `CameraModule::notifyDeviceStateChange`.
6. Widevine and Qualcomm sensor helpers remain blocked by missing
   `libprotobuf-cpp-lite-3.9.1.so` compatibility support.
7. Wi-Fi Display remains blocked by the missing 32-bit `libpng.so` dependency
   of `wifidisplayhalservice`.
8. Legacy configstore still fails registration and should be replaced with the
   Android 16 `disable_configstore` path.

Official 4.14.357 kernel-module integration remains a separate platform task,
not a HomeLauncher/application-source change.

The user-run `m HomeLauncher` build completed successfully. The resulting
platform APK was downloaded and checksum-verified locally:

- `artifacts/HomeLauncher-system-1f2a42c.apk`
- Size: `9,168,494` bytes
- SHA-256: `b4d4b641e0b1b1d2726882d5a52efe2a24d96dadfb0080326e0062cde1a48688`

The local checksum exactly matches the VM output. Runtime installation and
launcher/QuickStep validation are pending.

The rebuilt APK was installed successfully with `adb install -r`. Package
Manager now resolves `com.home.launcher` from the updated `/data/app` package
while retaining `/system/priv-app/HomeLauncher` as the system base. Reported
runtime metadata is `versionCode=36`, `versionName=16`, and
`lastUpdateTime=2026-07-13 12:47:31`. Launcher restart and behavioral validation
remain pending.

## 2026-07-13: Checkpoint 09 Stable Runtime And Full Source-State Freeze

A fresh runtime capture after installing the current HomeLauncher was saved at
`bootloop-logs/checkpoint09-current-runtime-20260713`. The device reports:

- `init.svc.lmkd=running`
- `sys.boot_completed=1`
- `dev.bootcomplete=1`
- live `system_server`, SystemUI, NetworkStack, and `com.home.launcher`

HomeLauncher is focused and repeatedly resumes without a recorded Java/native
crash. This closes the original touch-watchdog and LMKD restart blockers and
provides a smoke-test pass for the updated launcher. Full launcher feature and
QuickStep testing remains open.

The remaining log failures are deterministic crash loops:

- Missing `libprotobuf-cpp-lite-3.9.1.so` blocks Widevine,
  `sensors.qti`, sensor calibration, `libsnsapi`, and `camera.qcom.so`.
- The Lineage camera provider subsequently null-dereferences
  `CameraModule::notifyDeviceStateChange`.
- Legacy configstore cannot register `ISurfaceFlingerConfigs`.
- `wifidisplayhalservice` cannot link without 32-bit `libpng.so`.
- HomeLauncher/platform-app CPU-stat reads produced 516
  `proc_stat:file` SELinux denials in the captured buffer.

The ordered remaining rectification queue is maintained at the top of
`to-do-note.md`. Protobuf restoration is first because it is a shared upstream
dependency of DRM, sensors, and camera and may remove the camera failure's
initial trigger before any provider code change is considered.

The complete Checkpoint 09 source state was frozen on the VM and copied
locally:

- VM: `/home/premanandal1978/android/waterlily/checkpoint-snapshots/`
  `checkpoint09-stable-runtime-20260713.tar.gz`
- Local: `artifacts/checkpoint09-stable-runtime-20260713.tar.gz`
- Size: `20,320,685` bytes
- SHA-256: `58428ebf38492d4eb78706d07ff28edf0e4481b7c94211edffea06f95d41839f`

The archive contains base revisions, status manifests, binary patches, and
untracked-file archives for `device/asus/I001D`,
`device/asus/sm8150-common`, and `vendor/asus/I001D`; base/status records for
`vendor/asus/sm8150-common`; the clean pinned safe working tree and metadata
for `packages/apps/HomeLauncher`; the disabled stale HomeLauncher build files;
and the checkpoint progress/todo records. The exposed upstream `.gcloud-ssh`
directory is verified absent from the archive.

## 2026-07-13: Checkpoint 10 Camera Dependency Fix Applied

The user confirmed that HomeLauncher works; only its CPU statistics are
unavailable. That SELinux item is now explicitly low priority. The active port
priority is camera, then audio, then USB default configuration.

Current camera logs prove that `camera.qcom.so` cannot load because
`libprotobuf-cpp-lite-3.9.1.so` is absent. The same missing library also blocks
Widevine, `sensors.qti`, sensor calibration, and `libsnsapi`. Audit evidence
shows the Android 16 source tree already provides the correct compatibility
module:

- Soong module: `libprotobuf-cpp-lite-3.9.1-vendorcompat`
- 64-bit output SHA-256:
  `9230a89d07199372668ad3b16d2f33c93e5f79f33c96a6359207ac9232ec5698`
- 32-bit output SHA-256:
  `97159e0b02bfa06bd49853cb181a160baea641ee6201e0c186c044798b3c9c63`

Both hashes exactly match the official Android 16 I001D vendor image, so no
Android 14 proprietary protobuf blob is being restored. VM source was changed
without starting a build:

- Added `libprotobuf-cpp-lite-3.9.1-vendorcompat` to `PRODUCT_PACKAGES` in
  `device/asus/sm8150-common/msmnile.mk`.
- Post-edit `msmnile.mk` SHA-256:
  `8520dbe2f656460e6525642cb56e413b06110e21c7f90367f95346855a2772fd`.
- Targeted `git diff --check` passed.

This checkpoint must be built and runtime-tested before changing the camera
provider. Acceptance is that both `/vendor/lib` and `/vendor/lib64` contain the
3.9.1 library, dependent services link, and camera enumeration is retried. If
the provider null dereference remains after `camera.qcom.so` loads, it becomes
the next narrow camera fix.

## 2026-07-13: Checkpoint 11 Remaining Port Rectifications Applied

The user requested that all remaining port fixes be completed before the next
build, with HomeLauncher CPU statistics as the only intentional deferral. No
long Android build was started by the agent.

Live diagnostics established the following root causes:

- Audio services and the 7.1 HAL were alive, but AudioFlinger exposed no
  hardware threads and audio policy reported `AudioPolicyManager: 0x0`.
  `device/asus/I001D/audio/audio_policy_configuration.xml` still had the old
  invalid `AUDIO_CHANNEL_IN_MONO` mask on the output earpiece port and won the
  duplicate destination over the corrected common-tree copy.
- USB gadget requests for MTP repeatedly returned status 1 and fell back to
  ADB-only. The modern QTI gadget service was packaged while its required
  `vendor.usb.use_gadget_hal` and `vendor.usb.use_ffs_mtp` properties were
  absent, so init created neither the expected FunctionFS MTP endpoint nor the
  gadget-HAL configfs mode.
- `android.hardware.configstore@1.1-service` was still inherited from
  `base_vendor.mk`; removing a device-level package entry alone did not
  override it.
- The only unresolved linker dependencies in the live log were
  `libprotobuf-cpp-lite-3.9.1.so` and 32-bit `libpng.so`.
- The forced kernel is `4.14.357-openela-perf+`, while old module staging used
  `4.14.190-omni+` modules.

VM source changes:

- Replaced the I001D audio policy duplicate with the exact official Android 16
  common policy. SHA-256 is
  `06da3f64c4ff2a0f944918787e355939e87896a40a933981e51fb1ae90e40f6f`.
- Added a null guard to
  `hardware/interfaces/camera/provider/2.5/default/LegacyCameraProviderImpl_2_5.cpp`
  so a failed camera module cannot crash the provider during device-state
  notification.
- Added `disable_configstore`, `vendor.usb.use_gadget_hal=1`,
  `vendor.usb.use_ffs_mtp=1`, `sys.usb.mtp.batchcancel=1`, and
  `libpng.vendor:32` to the common product configuration.
- Imported all 14 official OTA kernel modules into
  `device/asus/I001D/prebuilt/modules` and added explicit vendor copy rules.
  Every SHA-256 matches the official Android 16 inventory. `modinfo` confirms
  `4.14.357-openela-perf+ SMP preempt mod_unload modversions aarch64`, a
  build-time kernel signer, and SHA-512 signatures. The matching official
  `modules.alias`, `modules.dep`, and `modules.softdep` files are packaged with
  the set so no dependency entry for the three obsolete modules survives.

Validation completed without a build:

- Targeted `git diff --check` passes for every changed file.
- Product configuration resolves `android.hardware.usb.gadget-service.qti`,
  `disable_configstore`, `libpng.vendor:32`, and
  `libprotobuf-cpp-lite-3.9.1-vendorcompat`.
- A current crash/linker audit finds no additional missing libraries beyond
  the two now addressed. The repeated native crashes are the known camera
  null dereference and obsolete configstore loop.

The next build must begin with `m installclean` to remove stale old camera,
configstore, and 4.14.190 module outputs before producing the OTA. After flash,
validate camera capture, audio playback/recording, USB default functions,
sensors, Widevine, Wi-Fi Display, and absence of crash loops. HomeLauncher CPU
statistics remain intentionally unresolved.

Checkpoint 11 was committed in every modified repository:

- `device/asus/I001D`: `2c4bd7c`
- `device/asus/sm8150-common`: `1b6288c`
- `vendor/asus/I001D`: `3e26a28`
- `hardware/interfaces`: `0b01d5a9dd`
- `packages/apps/HomeLauncher` remains clean at `1f2a42cf`.
- `vendor/asus/sm8150-common` remains clean at its existing pinned head.

A portable patch/head/hash snapshot was saved on both VM and workstation:

- VM: `checkpoint-snapshots/checkpoint11-port-rectifications-20260713.tar.gz`
- Local: `artifacts/checkpoint11-port-rectifications-20260713.tar.gz`
- Size: `21,980,376` bytes
- SHA-256: `d61ab6bfde0fc782bb1fa64ee50eeb6a1baa40e9da004468a4309f1b88a74aae`

## 2026-07-13: Checkpoint 12 USB Property Build Correction

The first clean GApps build reached vendor property post-processing and exposed
a duplicate assignment: the legacy common `vendor.prop` still set
`vendor.usb.use_gadget_hal=0`, while Checkpoint 11 added the required value `1`
through `PRODUCT_VENDOR_PROPERTIES`. The build correctly rejected both values.

The source now has one authoritative assignment:

- Changed the existing common `vendor.prop` value to
  `vendor.usb.use_gadget_hal=1`.
- Removed the duplicate product-make assignment while retaining
  `vendor.usb.use_ffs_mtp=1` and `sys.usb.mtp.batchcancel=1`.
- Targeted `git diff --check` passed.
- Committed `device/asus/sm8150-common` as `cb0326b`.

No compiled outputs were cleaned. Resume the same `blissify -g I001D` build.

## 2026-07-13: Checkpoint 13 Consolidated GApps OTA Built And Downloaded

The user-run clean GApps build completed successfully in 01:14:34. The agent
did not run the Android build. The resulting OTA is:

- `Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713.zip`
- Size: `1,538,599,977` bytes
- SHA-256: `704c22c1b0f4328cc098cbd9abc642b381691bdab61af68c98238c982f25470a`
- VM: `out/target/product/I001D/`
- Local: `artifacts/`

The VM and local hashes match exactly. Pre-download output validation passed:

- 32-bit and 64-bit `libprotobuf-cpp-lite-3.9.1.so` are installed.
- 32-bit vendor `libpng.so` is installed.
- The obsolete configstore service is absent and `disable_configstore` is
  installed.
- USB properties occur once with gadget HAL and FunctionFS MTP enabled.
- Both installed audio-policy copies match official SHA-256
  `06da3f64c4ff2a0f944918787e355939e87896a40a933981e51fb1ae90e40f6f`.
- Vendor contains exactly the 14 official signed modules plus matching module
  metadata; the three obsolete 4.14.190-only modules are absent.
- The packaged HomeLauncher APK matches the tested build exactly at SHA-256
  `b4d4b641e0b1b1d2726882d5a52efe2a24d96dadfb0080326e0062cde1a48688`.

Next action is flash/runtime acceptance testing. CPU statistics remain the only
intentional HomeLauncher deferral.

## 2026-07-13: Checkpoint 14 GApps Runtime Crash And HomeLauncher Audit

A complete bugreport was captured from the booted GApps build and saved as
`artifacts/bugreport-gapps-20260713-172813.zip`. Detailed findings are recorded
in `analysis/gapps-runtime-crash-audit-20260713.md`.

The recorded application crashes are AudioFX, Recorder, Bliss Neuron, and
Wallpaper/ThemePicker. AudioFX has service-initialization null dereferences;
Recorder receives invalid audio buffer size `-2`; Neuron explicitly aborts as
an unsupported device; and Wallpaper hits a native UBSan type mismatch.
Widevine also remains in a persistent linker restart loop because `CBS_init` is
missing from the loaded dependency set. Repeated `listAudioPorts` failures with
`-19` confirm the audio runtime path is still broken.

HomeLauncher CPU sampling was conclusively diagnosed. The `/proc/stat` delta
algorithm is supported, but the live build repeatedly denies
`platform_app -> proc_stat:file read`. The previously drafted policy patch was
not integrated into `system/sepolicy`. The framework hardware-properties API is
not a viable replacement because the device reports `ThermalService HAL Ready:
false`, and Android does not implement CPU-usage retrieval for its AIDL thermal
path.

OmniJaws 1.1 is installed, configured with MET Norway, and its provider returns
valid current weather for Imphal. HomeLauncher still uses Open-Meteo and lacks
`org.omnirom.omnijaws.READ_WEATHER`. Launcher source changes are waiting for the
numbered propagation-path selection required by its nested `AGENTS.md`.

## 2026-07-13: Checkpoint 15 Primary Audio HAL Root Cause

The ROM-wide loss of audio was traced to a deterministic mixer-file packaging
mismatch. Detailed evidence is recorded in
`analysis/audio-primary-hal-failure-20260713.md`.

The kernel exposes `sm8150-tavil-snd-card`, `/dev/snd` is populated, `tinymix`
enumerates 3,519 controls, and the vendor audio service registers. The primary
HAL nevertheless aborts because it requests `/vendor/etc/mixer_paths.xml`,
which is absent. The image contains only `mixer_paths_ZS660KL.xml`,
`mixer_paths_ZS660KL_EU.xml`, and `mixer_paths_pahu.xml`.

The active Qualcomm HAL derives lookup names from the ALSA card name and tries
`mixer_paths_tavil_snd.xml`, then `mixer_paths_tavil.xml`, then
`mixer_paths.xml`. It can never select the model-named ZS660KL files, and no
init rule renames them. Consequently `audio_route_init()` fails, the primary
module returns `-19`, AudioPolicy has no primary output or speaker/earpiece,
and recording also fails.

The recommended next source correction is to package the intended non-EU
I001D mixer source as `vendor/etc/mixer_paths.xml`, then rebuild and perform a
complete playback, recording, call-route, and external-route test. Secondary
`audio_platform_info.xml` device-name warnings should be audited after this
first blocker is removed. No source correction or Android build was performed
in this diagnostic checkpoint.

## 2026-07-13: Checkpoint 16 Audio, Crash, Widevine, And OmniJaws Fixes

The fixes identified by Checkpoints 14 and 15 are implemented and committed.
Detailed rationale and validation are recorded in
`analysis/gapps-runtime-fixes-20260713.md`.

The primary audio packaging correction preserves the existing ASUS mixer
contents and additionally installs `mixer_paths_ZS660KL.xml` as
`vendor/etc/mixer_paths.xml`, the fallback name selected by the Android 16
Qualcomm HAL. Commit: `device/asus/I001D` `3af8cd8`.

The captured crash paths now have targeted corrections:

- AudioFX null-safe initialization: `packages/apps/AudioFX` `a7f57d5`.
- Recorder audio-start failure handling: `packages/apps/Recorder` `c7748af`.
- Serialized DNG/XMP initialization for ThemePicker:
  `external/dng_sdk` `a514f19`.
- I001D-only exclusion of the unsupported Neuron Vulkan prebuilt:
  `vendor/extras` `6289416`.
- Legacy `CBS_init` export for ASUS Widevine:
  `external/boringssl` `efc8ddd7`.

The user clarified that the current HomeLauncher is the known-working baseline
through Bliss 14. Under propagation path 3 + 4, the weather card was migrated
from its private Open-Meteo implementation to the installed OmniJaws provider,
provider observation, and settings activity. The clean local Gradle build and
Kotlin/Soong compatibility scan passed. The local clone, current Waterlily VM,
and GitHub `origin/main` are aligned at `6b2bd1e`.

HomeLauncher CPU statistics remain the only intentional deferral. No Android
ROM build was run by the agent. The next user-run build is the normal
`blissify -g I001D`; `m installclean` is not required for this checkpoint.

A portable patch/documentation snapshot was saved on the VM and workstation:

- `checkpoint-snapshots/checkpoint16-runtime-fixes-20260713.tar.gz`
- `artifacts/checkpoint16-runtime-fixes-20260713.tar.gz`
- Size: `57,176` bytes
- SHA-256: `2640ff0d4816874d8728aa28618145d2b708169e081f0c6308069b95137659a1`

## 2026-07-13: Checkpoint 17 Runtime-Fix OTA Downloaded

The user-run GApps build containing Checkpoint 16 completed on the VM. The
agent did not run the Android build. Because the generated VM filename reused
the date of the prior OTA, the new artifact was downloaded with a distinct
local suffix so the earlier build remains preserved:

- VM: `out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713.zip`
- Local: `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713-checkpoint16.zip`
- Size: `1,482,820,637` bytes
- SHA-256: `824c91c1cc34a17ea33691967abcfb418cfc32cc4b3af98c4f3f20b8596223e2`

The VM and workstation size and SHA-256 match exactly. The OTA is ready for
flash and runtime acceptance testing of audio, application crash fixes,
Widevine, ThemePicker, Neuron exclusion, and HomeLauncher OmniJaws weather.

## 2026-07-13: Checkpoint 18 HomeLauncher CPU Runtime Diagnosis

Rooted ADB debugging on the flashed Checkpoint 16 image conclusively confirms
that HomeLauncher's CPU calculation is functional and that SELinux policy is
the sole runtime blocker. The installed launcher is the system-image build at
`/system/priv-app/HomeLauncher`, version code 36, but it executes in
`u:r:platform_app:s0:c512,c768`.

While SELinux is enforcing, every two-second `/proc/stat` sample produces:

`platform_app -> proc_stat:file { read }` denied for `com.home.launcher`.

After ten failed samples, the existing backoff behaves as designed and logs
`SystemStats: CPU read failed 10 times; throttling`. A controlled rooted test
briefly changed SELinux to permissive, restarted HomeLauncher, and verified the
same code displayed a live value of `CPU 19%`. SELinux was immediately restored
to `Enforcing`, which was verified after the test.

The complete pre-test device log was preserved locally at
`artifacts/homelauncher-cpu-20260713.log` (6,046,552 bytes). No source changes
and no Android build were performed in this diagnostic checkpoint. The proper
permanent correction is a narrowly scoped HomeLauncher SELinux domain and
`proc_stat` read permission; granting this permission to the shared
`platform_app` domain would unnecessarily expose CPU statistics to every app in
that domain.

## 2026-07-13: Checkpoint 19 HomeLauncher CPU SEPolicy Integrated

The HomeLauncher repository's documented Android policy patch was applied to
the active Android 16 VM source at
`system/sepolicy/private/platform_app_home_launcher.te`:

`allow platform_app proc_stat:file r_file_perms;`

Android 16's active `app_neverallows.te` and `platform_app.te` were inspected.
The rule is compatible with the current policy layout and is not prohibited by
the untrusted-app `/proc/stat` neverallow because `platform_app` is a trusted,
platform-signed application domain. The policy source passes `git diff
--check`. It was committed in the VM `system/sepolicy` repository as
`59d645659` (`Allow platform apps to read CPU statistics`).

This is a ROM policy correction, not an APK correction. Rebuilding or
installing HomeLauncher with `adb install -r` cannot change SELinux policy and
would continue to fail on the currently flashed image. The existing APK's CPU
reader was already proven functional by the Checkpoint 18 controlled test.
Activation therefore requires a user-run ROM build and flash. No Android build
was started by the agent.

## 2026-07-13: Checkpoint 20 Android 16 AI Integration Report

A comprehensive AI/OS integration design was created at `AI-integration.md`.
The report maps Android 16 capabilities across apps, framework services, Binder,
HALs, kernel-facing providers, security, hardware, communications, media,
storage, scheduling, personal data, accessibility, diagnostics, and custom ROM
extensions. It also defines risk/confirmation tiers, typed capability contracts,
prompt-injection defenses, SELinux separation, capability tokens, audit and undo
requirements, testing criteria, and a phased Waterlily/I001D implementation
plan.

The proposed architecture keeps language interpretation in a sandboxed model
process and delegates authority to a deterministic orchestrator and narrowly
scoped providers. Android 16 AppFunctions are identified as the preferred future
cross-app tool mechanism, with framework/Binder providers for OS control and
AIDL HALs for device-specific hardware. Accessibility is explicitly limited to
a user-enabled compatibility fallback. The report is based on current official
Android 16 and AOSP architecture/security documentation.

## 2026-07-13: Checkpoint 21 Low Speaker Volume Live Diagnosis

Live playback testing found that the currently playing source is
`org.lineageos.jelly` (the Jelly/Chromium browser), routed to the speaker at
48 kHz. Android's media stream is at 15/15, AudioFlinger master volume is 1.0,
and the speaker RX/mixer gains match the I001D device configuration. Both TFA
smart-amplifier instances report valid firmware, `Init complete` DSP state, and
active output paths.

The active Jelly audio track itself is reported by AudioFlinger with both track
gain and port volume at `-25 dB`, with no mute or underruns. This is a large
source-side attenuation before audio reaches the HAL and speakers, and is the
most probable immediate cause of the observed low playback volume. It points to
the browser/media element or an app-level output-volume setting rather than
missing ACDB files, a failed amplifier, or Android's media-volume control.

This remains to be compared against a ringtone or a local track in a separate
player before classifying the issue as ROM-wide. Speaker-protection feedback
routes also merit later calibration review, but they do not explain the measured
`-25 dB` Jelly track gain. No source changes or build were performed for this
diagnostic checkpoint.

### Test-state handoff

The user is performing a clean flash of another ROM build. Treat all existing
on-device observations as belonging to the pre-flash image until the new build
fingerprint and runtime audio behaviour have been collected. After first boot,
re-test speaker volume with both Jelly and an independent source (ringtone or a
local-media player) before applying any mixer, HAL, or calibration change.

## 2026-07-13: Release-Signing Objective

The project objective is to produce personally signed Bliss/I001D releases:
replace publicly known AOSP test keys with a private release-key set controlled
by the project owner, so APK identity and OTA acceptance persist securely across
future releases. This is distinct from ASUS OEM signing and will not remove the
expected unlocked-bootloader warning.

Do not generate or leave the private release key solely on the build VM. Before
implementation, choose a durable owner-controlled, encrypted offline backup
location and a controlled way to make the key available only for release
signing. Bootloader re-locking is explicitly out of scope until custom AVB-key
support for I001D has been proven.

## 2026-07-14: Checkpoint 22 ProdX Runtime Architectural Reverse-Engineering Started

A new independent, source-driven architecture investigation has started against
the active Android 16 Waterlily checkout. This is intentionally separate from
the earlier `AI-integration.md` design report: prior AI conclusions are not
being used as architectural premises.

Verified source scope at investigation start:

- VM: `instance-20260710-230647`, external IP `8.230.119.36`, zone
  `us-south1-a`.
- Source root: `/home/premanandal1978/android/waterlily`.
- The checkout is accessible through the documented `home` SSH account and
  passwordless `sudo -u premanandal1978` workflow.
- `repo list` reports 1,160 projects.
- Platform, Bliss, device, vendor, and reconstructed I001D sources will all be
  included; generated `out`, Git internals, and repo metadata are excluded from
  semantic source scans except where separately recorded as provenance.

The reproducible extractor is staged at
`analysis/prodx-runtime-architecture-20260714/extract_source_architecture.py`.
It records the resolved repo manifest, source/module scope, init declarations,
AIDL surfaces, Android components and permissions, Binder/runtime references,
SELinux types/rules/contexts, and VINTF HAL declarations as reviewable TSV/XML/
JSON evidence. No Android build has been started or requested.

## 2026-07-14: Checkpoint 23 ProdX Runtime Foundational Blueprint Completed

The independent Android 16/Bliss/I001D architecture investigation has been
correlated into a ProdX Runtime implementation blueprint. This checkpoint is
analysis and planning only; it does not implement ProdX and did not start an
Android build.

Primary deliverable:

- `analysis/prodx-runtime-architecture-20260714/ProdX-runtime-foundational-blueprint.md`

Supporting artifact index:

- `analysis/prodx-runtime-architecture-20260714/README.md`
- `analysis/prodx-runtime-architecture-20260714/catalog/component-catalog.tsv`
- `analysis/prodx-runtime-architecture-20260714/catalog/runtime-evidence-index.tsv`
- `analysis/prodx-runtime-architecture-20260714/catalog/extension-evidence.tsv`
- `analysis/prodx-runtime-architecture-20260714/catalog/role-bridge-matrix.tsv`
- `analysis/prodx-runtime-architecture-20260714/architecture-audit-20260714/`

Verified investigation/correlation scope:

- 1,160 repo projects.
- 47,667 Soong module declarations.
- 30,851 AIDL declarations.
- 11,066 manifest components and 12,173 manifest permission records.
- 126,135 SELinux evidence records.
- 39,733 runtime communication, policy, observation, and extension references.
- 1,282 included I001D product modules and 4,270 installed runtime artifacts.
- 241 installed init service stanzas.
- 628 installed VINTF records correctly separated into 119 manifest
  declarations and 509 compatibility requirements; requirements are not treated
  as proof of live HAL implementations.
- 15,151 correlated component catalog rows, all with explicit lifecycle,
  discovery, IPC, security, bridge, role, risk, availability, extensibility, and
  evidence fields or an explicit statement that the extracted evidence does not
  declare the field.

The normative architecture keeps a minimal authority/registry service in
SystemServer, places orchestration in a dedicated broker process/SELinux domain,
keeps models isolated and unprivileged, requires typed allowlisted providers,
and preserves Android permission, AppOps, SELinux, user/profile, device-policy,
authentication, framework, HAL, and kernel authority. It explicitly prohibits
generic Binder, intent, URI, path, property, shell, HAL, plugin-loading, init,
and SELinux control surfaces.

The report includes boot and service-registration graphs, Binder interaction and
capability invocation lifecycles, observation and extension pipelines, a P0
foundation dependency graph, P0/P1/P2/future backlogs, migration strategy, and
release acceptance gates. The P0 foundation is required before any real
capability provider implementation begins.

Evidence archives were downloaded and hash-verified:

- `architecture-audit-20260714.tar.gz` SHA-256
  `2f23cae31d3d0b9a921979cc42ac120499d7f996ddbf60b0ec14ef009d4f75d6`.
- `product-reachability-20260714.tar.gz` SHA-256
  `9aeeecef77f9ded17483a27a0b496ded39a8c514f80d85cd6c32e71a2bee5772`.

## 2026-07-14: Checkpoint 24 ProdX Investigation Consolidated Locally

The complete ProdX Runtime architecture investigation was consolidated into one
local folder without changing or deleting the original analysis artifacts:

`artifacts/prodx-runtime-architecture-20260714-complete`

The folder contains 54 files totaling 88,377,044 bytes, including the primary
blueprint, README, correlated catalogs, extraction/correlation scripts, raw
extracted evidence, both verified VM evidence archives, working evidence
indexes, and a snapshot of this progress file. The two archive SHA-256 values
remain identical to Checkpoint 23. No build or ProdX implementation was started.

## 2026-07-14: Checkpoint 25 Curated ProdX Architecture Archive Completed

A long-term architectural-review package was curated from the complete working
investigation. The originals and the Checkpoint 24 consolidation remain
unchanged.

Curated directory:

`artifacts/ProdX-Runtime-Architecture-Foundation-20260714`

Final archive:

`artifacts/ProdX-Runtime-Architecture-Foundation-20260714.zip`

Archive verification:

- Size: 5,969,255 bytes.
- SHA-256: `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec`.
- 49 files in the archive; 83,995,411 bytes uncompressed.
- ZIP CRC validation passed.
- `MANIFEST.json` hash/size verification passed for all 48 non-manifest files;
  the manifest contains an explicit non-hashable self-entry.
- `INDEX.md` coverage passed for all 49 files.
- Markdown cross-reference validation passed.

The archive is organized into architecture, implementation blueprint, catalogs,
inventories, runtime analysis, security analysis, diagrams, provenance, and raw
evidence. It excludes Python bytecode/cache, extraction and parser utilities,
duplicate tar archives, working lists, and unrelated reconstruction history.
The top-level README defines document authority and recommended reading order.
No Android build or ProdX implementation was started.

## 2026-07-14: Checkpoint 26 VM Ubuntu Edition Verified

The active build VM was inspected to guide a planned local Ubuntu installation.
It is an Ubuntu Server environment, not Ubuntu Desktop:

- Ubuntu `22.04.5 LTS (Jammy Jellyfish)`, x86_64.
- `ubuntu-server` meta-package installed (`1.481.5`).
- `ubuntu-minimal` and `ubuntu-standard` installed.
- No `ubuntu-desktop`, `ubuntu-desktop-minimal`, GNOME Shell, GDM, LightDM, or
  Xorg desktop meta-stack was installed.
- No display manager or GUI process was active.
- GCE cloud packages and the Ubuntu 22.04 LTS cloud license were present.
- Kernel: `6.8.0-1063-gcp`.
- Current VM capacity: 12 vCPUs, 62 GiB RAM, no swap, 485 GiB root filesystem.

The systemd default reported `graphical.target`, but this does not make it a
Desktop installation: the Server meta-package and absence of any desktop/display
manager stack are the definitive indicators. For the closest local build
parity, use Ubuntu Server 22.04 LTS amd64. Ubuntu Desktop 22.04 LTS is also
build-compatible if a local GUI is desired, but it is not what the VM uses.
No build or VM modification was performed.

## 2026-07-14: Checkpoint 27 Local Project Cleanup Completed

The local reconstruction workspace was consolidated and cleaned at the user's
request. This `progress.md` file is now the sole reconstruction-level Markdown
status/history document. The canonical state snapshot at the top was updated
before deletion to record the retained build, hashes, open work, ProdX archive,
VM environment, and planned local build host.

Retained artifacts:

- Latest ROM only:
  `artifacts/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713-cpu-sepolicy.zip`
  - Size: `1,482,821,089` bytes.
  - SHA-256:
    `967be99ea20d2114ab2e00e63c99fc4ecbc521b2efe59921186d2f00fe095844`.
  - Verified byte-identical to the current VM output.
- Curated architecture reference:
  `artifacts/ProdX-Runtime-Architecture-Foundation-20260714.zip`
  - Size: `5,969,255` bytes.
  - SHA-256:
    `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec`.

Preserved active work:

- `baseline/`
- `patches/`
- `tools/`
- `touch_aidl/`
- `vm-edit/`
- `work/` (including the active HomeLauncher source repository)

Removed as superseded/stale:

- All older Bliss ROM ZIPs, official reference package duplicates, incomplete
  and previous build packages.
- Old boot, vbmeta, dtbo, recovery/TWRP, pstore, BPF, hybrid-kernel, debug and
  stage images.
- Bugreports, bootloop/recovery logs, HomeLauncher APK copies, checkpoint/source
  backup tarballs, and temporary command/log artifacts.
- Duplicate/extracted ProdX working trees and evidence archives; the curated
  self-contained ZIP is retained.
- Standalone boot/AVB/parity/provenance/validation/integration reports, old TODO
  and handover documents, and one-off analysis/fix/parser scripts.
- Workspace IDE/cache and stray empty directories.

Cleanup verification:

- 57 entries removed.
- `8,211,999,124` bytes reclaimed (approximately `7.65 GiB`).
- Remaining reconstruction project: 3,202 files, `2,652,193,222` bytes
  (approximately `2.47 GiB`) before this checkpoint's small text addition.
- Exactly one Bliss build ZIP remains and its SHA-256 matches the retained
  canonical value.
- Exactly one reconstruction-level Markdown document remains: `progress.md`.
- Source/baseline/patch/tool repositories were not deleted.

Do not look for the retired standalone reports locally. Their durable status,
conclusions, and remaining work are represented by this canonical progress
history; the final ProdX investigation remains inside the curated archive.

## 2026-07-14: Checkpoint 28 ProdX Runtime Contract Specification Completed

The immutable ProdX Runtime contract layer was designed from the curated
Architecture Foundation as an architecture-only phase. The authoritative new
deliverable is:

- `artifacts/ProdX-Runtime-Contract-Specification-20260714.md`
- Version: `1.0.0-draft-freeze`
- Size: `67,420` bytes
- Lines: `1,361`
- SHA-256:
  `7c7743700c468847ef547bbbc22dcc9d1b027df0fee9e92895faee071706d886`

The specification freezes:

- the canonical abstract data model, deterministic CBOR encoding, diagnostic
  JSON projection, hashing/signature behavior, identifier grammar, namespaces,
  schema profile, SemVer rules, and compatibility guarantees;
- every required foundational runtime object and its ownership, lifecycle,
  authority, state, relationships, validation, extensibility, versioning, and
  failure behavior;
- descriptor/provider/registry admission and deterministic resolution;
- trusted execution context, request, policy decision, confirmation proof,
  constrained authorization, response, error, async operation, cancellation,
  and undo semantics;
- observation/event subscription leases, minimization, sequencing,
  deduplication, revocation, and the rule that observations never authorize
  actions;
- append-only audit-before-effect, recovery evidence, privacy-governed
  non-authoritative learning records, and signed extension admission;
- immutable versus platform-extensible versus independently evolving contracts;
- implementation dependency order, global invariants, conformance-test scope,
  governance, and explicit freeze criteria; and
- nine Mermaid diagrams covering UML classes, capability/provider lifecycle,
  registry/dependency flow, end-to-end invocation, transaction state,
  observation flow, audit/learning flow, extension admission, and whole-object
  relationships.

Validation completed:

- All 19 object families explicitly requested by the architecture phase are
  present, together with the strictly necessary envelope, schema/payload,
  identity, authorization, confirmation, registry snapshot/change, subscription,
  asynchronous operation, and undo objects.
- Markdown fencing is balanced (18 markers for nine Mermaid blocks).
- The file is valid UTF-8 without replacement characters.
- The document contains no Kotlin implementation, Binder/AIDL interface,
  provider implementation, Android component, or production code.
- No Android build, VM modification, or ProdX implementation was started.

## 2026-07-14: Checkpoint 29 ProdX Runtime Skeleton Specification Completed

The physical Android architecture for the ProdX Runtime was designed using the
Architecture Foundation and Runtime Contract Specification as immutable inputs.
The new authoritative deliverable is:

- `artifacts/ProdX-Runtime-Skeleton-Specification-20260714.md`
- Version: `1.0.0-draft-freeze`
- Size: `80,073` bytes
- Lines: `1,230`
- SHA-256:
  `f332bace6730a290dcaff5a7703d8e499eac08dcd309cee63a31248997bffbd6`

The skeleton freezes:

- a minimal Authority/Registry/Policy/authorization anchor inside
  `system_server` and exclusion of model, provider, extension parsing, event
  queues, learning and general workflow execution from that process;
- a proposed `packages/modules/ProdX` Mainline-style APEX ownership model for
  the Broker, Observation Hub/Event Pipeline, Audit Engine, Extension Manager,
  Learning Engine, Reasoning Host, Provider Framework/SDK and built-in provider
  families, with a platform-OTA staging fallback that preserves boundaries;
- exact proposed AOSP repository paths, source namespaces, architectural module
  names, process/UID placement, SELinux domain identities, service identities,
  storage ownership and signing/privileged-package rules;
- trusted SystemUI confirmation/authentication/indicator integration and
  Settings administration/history/revocation integration without moving policy
  or execution authority into either UI package;
- boot, initialization, service registration, per-user start/unlock/stop,
  shutdown, unclean-reboot recovery, Binder death, crash containment and
  restart behavior;
- IPC/trust boundaries and prohibited direct paths between models, Authority,
  providers, Android services, HALs, kernel, extensions and learning workers;
- built-in, ROM, OEM/vendor, AppFunction and exhaustive future provider-family
  placement according to permission, AppOps, data, risk and Treble boundaries;
- framework-manager relationships, module dependency direction, deployment,
  APEX/OTA/provider/package update and rollback compatibility, emergency disable,
  safe mode and resource/performance constraints; and
- nine Mermaid diagrams plus repository and namespace maps covering repository
  ownership, processes, boot/startup, dependencies, recovery, IPC, provider
  placement and deployment.

Validation completed:

- All requested runtime subsystems and Android placement categories are present.
- The document contains 20 balanced Markdown fence markers: nine Mermaid
  diagrams and one repository-tree block.
- No TODO/FIXME/placeholder or invalid UTF-8 replacement character exists.
- Immutable source hashes still match the Foundation and Contract artifacts.
- No Kotlin/Java production class, Binder/AIDL interface, Android manifest,
  SELinux rule, Soong/build file, provider implementation, VM change or Android
  build was created or started.

## 2026-07-14: Checkpoint 30 ProdX P0 Runtime Implementation Plan Completed

The master implementation-planning specification for the P0 Runtime was
completed from the immutable Foundation, Capability Investigation, Runtime
Contract and Runtime Skeleton references. The authoritative deliverable is:

- `artifacts/ProdX-P0-Runtime-Implementation-Specification-20260714.md`
- Version: `1.0.0-draft-freeze`
- Size: `83,860` bytes
- Lines: `1,480`
- SHA-256:
  `c3ef5e20208ebc13fdb8bd43e16dae770ecddd9ddcb8a19a8a22d57a00b33152`

The specification defines:

- the required P0 outcome, disabled/inventory/shadow/test-no-op modes and strict
  exclusion of all real Android capability providers;
- G0–G7 engineering gates, global definitions of ready/done, source provenance,
  review ownership and immutable requirement-to-test traceability;
- exact AOSP repository directories, source namespaces, planned Android target
  names, first-write milestones, compile-time graph and target visibility;
- logical interface ownership, consumers, contract objects, restrictions and
  complete pre-interface freeze requirements without producing AIDL;
- exact initialization and runtime dependency order;
- 16 independently gated milestones from P0-00 reference lock through P0-15
  end-to-end security/recovery hardening, each with entry criteria, work
  packages, dependencies, consumed/exposed interfaces, tests, completion
  criteria, integration points and rollback;
- parallel work lanes, critical path, relative implementation waves, engineering
  role decomposition and cross-subsystem validation matrix;
- named contract, Broker, Audit, Observation, Extension and provider-protocol
  fuzz targets plus framework, service, UI, Settings, security, integration and
  APEX/rollback test targets;
- the exact Android framework, kernel, native, HAL, vendor, content, ROM/OEM,
  provider, model and public API areas that must remain untouched during P0;
- incremental integration and global rollback playbooks; and
- final acceptance criteria proving that P0 supplies only a secure runtime
  substrate and cannot execute a real Android capability.

Validation completed:

- All 16 milestone headings and all requested P0 subsystems are present.
- The document contains five balanced Mermaid diagrams covering repository
  modification, compile dependencies, initialization, runtime dependencies and
  implementation waves.
- There are no TODO/FIXME/placeholder markers or invalid UTF-8 replacement
  characters.
- All three local immutable source artifact hashes were reverified and match the
  values embedded in the plan.
- No Kotlin, Java, C++, Rust, AIDL, SELinux policy, Android manifest, Soong file,
  build script, production code, provider implementation, VM change or Android
  build was created or started.

## 2026-07-14: Checkpoint 31 ProdX P0-00 Engineering Baseline Locked

P0-00 Reference Lock, Threat Ledger and Engineering Baseline was implemented as
a documentation-only change in the Android VM source tree. The sole permitted
new path is:

- `/home/premanandal1978/android/waterlily/packages/modules/ProdX/tests/reference/`

Immutable input lock:

- Runtime Foundation archive:
  `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec`.
- Runtime Contract Specification:
  `7c7743700c468847ef547bbbc22dcc9d1b027df0fee9e92895faee071706d886`.
- Runtime Skeleton Specification:
  `f332bace6730a290dcaff5a7703d8e499eac08dcd309cee63a31248997bffbd6`.
- P0 Runtime Implementation Specification:
  `c3ef5e20208ebc13fdb8bd43e16dae770ecddd9ddcb8a19a8a22d57a00b33152`.
- Capability Investigation 44-file deterministic content-set digest:
  `dd76ae42def1cf0d1618dfdeda610585adff361b5fd5021fece0dbd884c59a54`
  over 83,876,218 uncompressed bytes.

Engineering baseline captured:

- Bliss Waterlily manifest `origin/waterlily` at
  `e0b717c9a37b37fb35125e454df92887a419ebdf`.
- 1,160 repo-managed projects; resolved manifest SHA-256
  `ebdce4ba5ebff4d7b2269f13f94884f63572f70b3a86f1304107010a75da1da4`.
- Product `bliss_I001D-userdebug`, Android 16/API 36, build ID
  `BP4A.251205.006`.
- Ubuntu 22.04.5 LTS, 12 vCPU, 62 GiB RAM, OpenJDK 17.0.19, Python 3.10.12,
  repo 2.65, Git 2.34.1, Clang `r563880c`/21.0.0, Rust 1.88.0-dev, Go
  1.24.1, Ninja `1.9.0.git` and Make 4.3.
- Pre-existing `bionic`, `prebuilts/build-tools` and I001D kernel changes were
  recorded and preserved. Pre/post repo status is byte-identical with SHA-256
  `13d2b1e92e2bb4dbbfc9c77002937ff97834ca2b54e0452f811c3e54a197e31e`.

Governance/evidence created:

- Immutable reference lock and byte-identical local copies.
- Machine-readable baseline manifest, resolved Android manifest and repo status.
- Engineering governance, architecture-change process, append-only decision log,
  stable reviewer ownership/RACI, coding standards, rollback policy, security
  gates, testing strategy and release gates.
- Milestone/G0–G7 tracker and P0 dependency graph.
- 138 unique normative engineering requirements mapped to source section,
  owner, milestone, validation and gate.
- 79 unique validation procedures; every requirement has a valid mapping.
- 40 unique release-blocking threats; every threat references existing
  requirements and validations.
- A 21-entry content manifest plus the manifest file itself (22 files total).
  `BASELINE-FILES.sha256` SHA-256:
  `24731f2c4d85c5595bd108720b891c1b95c70c8c372d1a0ae5667ad3f6831ae9`.

Final verification:

- All immutable, Capability Investigation and baseline hashes pass.
- Baseline JSON parses successfully; TSV row/ID/reference checks pass.
- Final ProdX path contains 22 files, all documentation/status/evidence.
- Executable files: zero.
- Production source/interface/policy/build/binary extensions: zero.
- No framework, runtime service, provider, Binder interface, SELinux policy,
  Android manifest, Soong target, product configuration or executable was added.
- No Android build was run.
- Temporary VM upload/capture files were removed after verification.

G0 Reference Lock is complete. P0-01 Repository/Test Skeleton is the next
authorized milestone, subject to its entry criteria.

## 2026-07-14: Checkpoint 32 ProdX P0-01 Skeleton Installed, Graph Review Pending

P0-01 Repository Skeleton and Build Graph Declaration was installed at:

- `/home/premanandal1978/android/waterlily/packages/modules/ProdX/`

The P0-00 reference lock was verified before and after installation; all 21
entries in `tests/reference/BASELINE-FILES.sha256` still pass. Locked baseline
files were not edited. Live milestone tracking is maintained outside the locked
directory in `MILESTONES.md`.

Created boundaries:

- repository governance, ownership, target, provenance and metadata records;
- framework contract and API placeholders;
- broker, observation, audit and extension service placeholders;
- explicitly excluded post-P0 learning and reasoning placeholders;
- SDK and no-op test-provider placeholders;
- APEX and SELinux reservation directories containing documentation only;
- contract, unit, integration, security, fuzz, host, device and fixture test
  boundaries;
- architecture, engineering, tools and configuration documentation boundaries.

Build graph declaration:

- one root `Android.bp`;
- one empty, non-installable `phony` aggregate named `prodx-p0` with no
  dependency or output;
- no product, partition, classpath, APEX, application or init inclusion.

The first user-run `m prodx-p0` exposed an invalid initial declaration: Kati
cannot resolve a source-only Soong `filegroup` through a Make-exported
`phony.required` edge and reported `prodx-p0-skeleton-metadata` as a missing
TARGET module. The unnecessary file group and edge were removed instead of
using `BUILD_BROKEN_MISSING_REQUIRED_MODULES`. The corrected graph is a single
empty phony target and requires a user retry.

Static validation passed:

- 39 P0-01 files after the implementation report, plus 22 preserved P0-00
  files;
- no Kotlin, Java, AIDL, native, Rust, script, binary, Android manifest, APEX
  manifest, APK or APEX file;
- no installable or executable Soong declaration;
- no duplicate declared target name elsewhere under `packages/modules`;
- no reference to the target in `build/make`, `device` or `vendor` product
  configuration;
- `PROJECT-METADATA.json` parses and `bpfmt -d Android.bp` returns zero with no
  diff;
- the corrected zero-edge dependency graph is acyclic;
- `/tmp` rollback rehearsal removed all simulated P0-01 content while preserving
  the simulated P0-00 reference marker.

The authoritative report is
`packages/modules/ProdX/docs/engineering/P0-01-IMPLEMENTATION-REPORT.md` on the
VM. Per the build-handoff rule, the agent did not run an Android build. P0-01 is
in `REVIEW` pending user execution of `m prodx-p0` and the `m nothing` control
after `lunch bliss_I001D-userdebug`. P0-02 remains unauthorized until those
results are recorded and P0-01 moves to `COMPLETE`.

## 2026-07-14: Checkpoint 33 ProdX P0-01 Complete

The user completed both required VM-side graph validations after the corrected
empty phony declaration was installed:

- `m prodx-p0`: succeeded, validating that the selectable P0 aggregate exists
  and resolves without dependencies or artifacts.
- `m nothing`: succeeded with `Successfully read the makefiles` and
  `build completed successfully (19 seconds)`, validating the unselected
  control graph.

P0-01 is now `COMPLETE`. The checks built no ROM image and introduced no runtime
behavior. The live milestone record and implementation report were updated;
the immutable P0-00 reference set remains unchanged. P0-02 is still
`NOT_STARTED` and no P0-02 source, contract, target or behavior has been added.

## 2026-07-14: Checkpoint 34 Durable GitHub Takeover State

`D:\AndroidProjects\porting` was initialized as the `main` branch of:

- `https://github.com/Prem8791/porting.bliss16.withAI.git`

The durable checkpoint contains the operational local project, pinned upstream
repositories as Git submodules, the latest 1,482,821,089-byte ROM through Git
LFS, the complete 61-file ProdX mirror, and deterministic handoff documentation.

The VM was re-snapshotted from
`/home/premanandal1978/android/waterlily`. The Git handoff includes its pinned
1,160-project manifest, exact `repo status`, complete tracked `repo diff`, and a
61-entry ProdX checksum manifest. The local ProdX mirror verified 61/61 against
the VM. A separate physical-state manifest records path, size, and SHA-256 for
the workstation files, including ignored caches and nested Git databases.

The root `TAKEOVER.md` is the mandatory starting point for a replacement agent.
It records the VM connection, reading order, immutable boundaries, expected
local kernel-submodule status, successful P0-01 validation, and the fact that
P0-02 remains unauthorized. No Android build or new runtime implementation was
performed for this checkpoint.

## 2026-07-14: ProdX P0-02 Implementation Mapping Spec — v1.1.0

The P0-02 Implementation Mapping Specification was produced and then revised
through an engineering design review:

**v1.0.0** (`ProdX-P0-02-Implementation-Mapping-20260714.md`):
- 1,130 lines covering all 15 P0-02 specification sections.
- Mapped every source path, AOSP class reuse decision, AIDL interface, SELinux
  type, Soong target, and implementation dependency across all P0 milestones.
- No production code was written.

**Engineering design review** (`ProdX-P0-02-Engineering-Design-Review-20260714.md`):
- Identified 6 BLOCKING, 4 HIGH, 6 MEDIUM, and 3 LOW findings.
- Conditional GO recommendation: resolve all 6 BLK issues before P0-02 code.

**v1.1.0** (revised spec, all 6 BLK issues resolved):

| BLK | Issue | Fix |
|-----|-------|-----|
| BLK-01 | Deprecated `android.security.keystore.KeyStore` | Replaced with `java.security.KeyStore` + `AndroidKeyStore` |
| BLK-02 | Missing direct-boot `onUserUnlocking()` | Added DE/CE lifecycle partitioning |
| BLK-03 | Duplicate contract models | Canonical AIDL `parcelable` types in `android.app.prodx`; module Objects reduced to internal-only |
| BLK-04 | Confused-deputy in grant APIs | Moved to `IProdXGrantAdmin` with `PRODX_ADMIN` permission |
| BLK-05 | Unauthenticated `setAuthority(IBinder)` | Removed from all service interfaces; replaced with ServiceManager discovery + UID check |
| BLK-06 | Custom CBOR reimplementation | Replaced with `co.nstant.in.cbor` thin wrapper |

**Zero blocking issues remain.** P0-02 (Contract Runtime + Vectors + Canonical
Parcelable Types) is authorized for implementation start. No production code has
been written or generated by either specification version.

## 2026-07-15: ProdX Contract Runtime Manifest Fix and Build Handoff

The current `prodx-contract-runtime` Soong iteration reported that every
`android_app` and `android_test` module in the ProdX tree requires an
`AndroidManifest.xml`.

The user created the required manifests for all 10 affected modules. Per the
VM build workflow, the agent did not run the Android build. The next validation
is a clean Soong regeneration followed by the targeted contract-runtime build:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

P0-02 validation remains in progress pending the user's build result. Record
the next Soong/compiler error or successful completion before advancing the
milestone state.

## 2026-07-15: Duplicate `libOmxCore.so` Install Fixed

The user ran the clean `m prodx-contract-runtime` validation after adding all
10 required Android manifests. Manifest validation passed, but Ninja stopped
while defining phony modules because multiple rules generated:

- `out/target/product/I001D/vendor/lib64/libOmxCore.so`

VM inspection identified the two producers:

- the source-built `libOmxCore` module in
  `hardware/qcom-caf/sm8150/media/mm-core/Android.bp`; and
- proprietary 32-bit and 64-bit copies from
  `vendor/asus/sm8150-common/proprietary/vendor/{lib,lib64}/libOmxCore.so`.

The source implementation is already selected by
`device/asus/sm8150-common/msmnile.mk` and Qualcomm media product configuration.
The agent therefore removed the redundant proprietary copy entries for both
architectures from:

- `vendor/asus/sm8150-common/sm8150-common-vendor.mk`
- `device/asus/sm8150-common/proprietary-files.txt`

Both VM repository diffs pass `git diff --check`, and the source module remains
defined. No Android build was run by the agent. Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Remaining Confirmed OSS Media Install Collisions Removed

The next clean targeted build passed the earlier `libOmxCore.so` collision and
stopped at the next duplicate output:

- `out/target/product/I001D/vendor/lib64/libOmxVdec.so`

Inspection confirmed that `libOmxVdec` was simultaneously installed from its
Qualcomm source module and copied from the sm8150-common proprietary blobs for
both 32-bit and 64-bit targets. The two blob declarations were removed from the
generated vendor makefile and the durable proprietary extraction list.

To avoid repeating a roughly 3.5-minute graph regeneration for each adjacent
failure, the existing generated Ninja graph was then audited read-only for the
same exact condition: one output rule containing both an sm8150-common
proprietary blob input and a Qualcomm media Soong intermediate. That audit
confirmed four more source-versus-blob collisions, each for both architectures:

- `libOmxVenc.so`
- `libplatformconfig.so`
- `libc2dcolorconvert.so`
- `libstagefrighthw.so`

Those redundant proprietary entries were also removed from:

- `vendor/asus/sm8150-common/sm8150-common-vendor.mk`
- `device/asus/sm8150-common/proprietary-files.txt`

Together with the earlier `libOmxCore` fix, each file now has 12 precise
deletions: six source-built OSS media libraries across two architectures.
Proprietary-only codec libraries were preserved. Both repository diffs pass
`git diff --check`. The agent did not run an Android build.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Duplicate `libssrec.so` Install Fixed

The next clean targeted build passed the audited sm8150-common OSS media
collisions and advanced to a duplicate 32-bit audio output:

- `out/target/product/I001D/vendor/lib/libssrec.so`

The two producers were:

- the source-built Android 16 Qualcomm audio module at
  `hardware/qcom-caf/sm8150/audio/hal/audio_extn`; and
- the Android 14-era I001D proprietary blob at
  `vendor/asus/I001D/proprietary/vendor/lib/libssrec.so`.

The existing Ninja graph was checked for adjacent I001D proprietary-versus-
source Qualcomm audio collisions; only `libssrec.so` matched this condition.
The agent removed its single blob copy declaration and extraction-list entry
from:

- `vendor/asus/I001D/I001D-vendor.mk`
- `device/asus/I001D/proprietary-files.txt`

The physical blob was not deleted. Both repository diffs pass
`git diff --check`, and the source `libssrec` module remains defined. The agent
did not run an Android build.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Comprehensive ASUS Blob-versus-Soong Collision Cleanup

The next targeted build passed `libssrec.so` and stopped on the 64-bit
broadcast-radio implementation:

- `out/target/product/I001D/vendor/lib64/hw/android.hardware.broadcastradio@1.0-impl.so`

At the user's request, the agent performed a comprehensive current-product
audit instead of continuing one collision per build. The audit:

1. Derived all 1,763 active proprietary copy destinations from the current
   I001D and sm8150-common vendor makefiles.
2. Intersected those destinations with every install output in the current
   `build.bliss_I001D.incremental.ninja` Soong graph.
3. Confirmed exactly 14 remaining blob-versus-source collisions.
4. Removed each old blob copy reference and its matching extraction-list entry.
5. Repeated the full intersection against the same Soong graph and obtained
   zero remaining collisions across 1,749 active proprietary destinations.

The 14 comprehensively removed old copy references were:

- I001D: `vendor.qti.hardware.camera.device@1.0.so`, `libopus.so`,
  `libstagefright_amrnb_common.so`, `libstagefright_flacdec.so`,
  `libvorbisidec.so`, 32/64-bit `soundfx/libdynproc.so`, and
  `lib64/hw/fingerprint.default.so`.
- sm8150-common: `android.hardware.cas@1.2-service`, 32/64-bit
  `com.dsi.ant@1.0.so`, 32/64-bit
  `android.hardware.broadcastradio@1.0-impl.so`, and
  `vendor.qti.hardware.systemhelper@1.0.so`.

Files updated on the VM:

- `vendor/asus/I001D/I001D-vendor.mk`
- `device/asus/I001D/proprietary-files.txt`
- `vendor/asus/sm8150-common/sm8150-common-vendor.mk`
- `device/asus/sm8150-common/proprietary-files.txt`

The cleanup removed references only; physical old blobs remain available for
rollback. Non-colliding companion files were retained, including the CAS init
RC/VINTF material and the ANT implementation library. Across this full build
iteration, the I001D makefile/list each contain nine removals and the
sm8150-common makefile/list each contain 18 removals. All four repository diffs
pass `git diff --check`. No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Comprehensive Explicit-Copy Collision Audit Completed

The next targeted build passed the complete ASUS proprietary-reference cleanup
and stopped on a different collision class:

- `out/target/product/I001D/vendor/lib64/libhidlbase-v32.so`

The two producers were:

- an old explicit copy in `device/asus/sm8150-common/msmnile.mk` from
  `prebuilts/vndk/v32/.../libhidlbase.so`; and
- the maintained Android 16/Lineage compatibility module
  `hardware/lineage/compat:libhidlbase-v32`.

The audit was expanded beyond proprietary blobs. Every explicit
`TARGET_COPY_OUT_PRODUCT`, `TARGET_COPY_OUT_SYSTEM_EXT`, and
`TARGET_COPY_OUT_VENDOR` destination from every makefile under the reconstructed
I001D and sm8150-common device/vendor trees was intersected with the current
incremental Soong install graph.

Results before the fix:

- 1,854 active explicit copy destinations inspected.
- Exactly one copy-versus-Soong collision: `libhidlbase-v32.so`.

The obsolete three-line VNDK copy stanza was removed from
`device/asus/sm8150-common/msmnile.mk`. The maintained
`hardware/lineage/compat` module, its v32 prebuilts, and the extraction-time
blob-name rewrite remain intact.

Results after the fix:

- 1,853 active explicit copy destinations inspected.
- Zero explicit-copy-versus-Soong collisions remain.
- `git diff --check` passes for the updated device repository.
- No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: ProdX AIDL Imports Fixed and Capability Test Activity Added

The next targeted build passed product-graph definition and reached the first
intended ProdX framework-contract compiler error:

```text
frameworks/base/core/java/android/app/prodx/IProdXAuthority.aidl:
Failed to resolve 'ProdXMode'
```

Cause:

- The new framework AIDL interfaces referenced parcelables declared in separate
  AIDL files without importing them. Framework AIDL requires explicit imports,
  including for types in the same Java package.

VM changes:

- Added all nine required `android.app.prodx` imports to
  `IProdXAuthority.aidl`.
- Added `ProdXHealth` and `ProdXMode` imports to
  `IProdXSettingsMediator.aidl`.
- Added the `ProdXGrant` import to `IProdXGrantAdmin.aidl`.
- Audited the remaining ProdX SDK/runtime AIDL. Their signatures use only
  built-in types or fully-qualified external types, so no equivalent unresolved
  simple-name reference remains.

At the user's request, the existing safe test-provider APK boundary now has a
launcher Activity:

- Module: `ProdXNoOpTestProvider`.
- Activity: `com.android.prodx.provider.test.ProdXCapabilityActivity`.
- UI: capability spinner, Go button, and selectable result label, all created
  within the Activity without external layout resources.
- Capabilities: no-op echo, provider health, and synthetic observation.
- Behavior: deterministic PASS/FAIL text only; no real device action or data
  access is performed.
- The existing no-op provider service is now declared non-exported in the same
  manifest.

Static validation:

- The updated manifest is well-formed XML.
- All ProdX files are readable by the VM build user.
- Framework changes pass `git diff --check`.
- The Activity APK is intentionally not yet added to I001D `PRODUCT_PACKAGES`;
  it can be built and installed independently for testing without changing ROM
  product inclusion during this contract milestone.
- No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

After the contract target succeeds, build the test Activity APK separately:

```bash
m ProdXNoOpTestProvider
```

## 2026-07-15: ProdX Capability Test Activity APK Built Successfully

The user built the separately requested no-op capability Activity APK:

```text
[100% 461/461] ... install ProdXNoOpTestProvider.apk
#### build completed successfully (37 seconds) ####
```

The Java `system modules path not set in conjunction with -source 11` messages
were non-fatal build-tool warnings.

Verified VM output:

- `/home/premanandal1978/android/waterlily/out/target/product/I001D/system/app/ProdXNoOpTestProvider/ProdXNoOpTestProvider.apk`

The APK contains the launcher `ProdXCapabilityActivity` with its capability
spinner, Go button and result label. It remains independently installable and
is not yet added to I001D `PRODUCT_PACKAGES`.

## 2026-07-15: Test APK Release-SDK Install Compatibility Fix

Windows ADB successfully detected and authorized the device as ASUS I001D, but
the first Activity APK install failed with:

```text
INSTALL_FAILED_OLDER_SDK: Requires development platform Baklava but this is a
release platform.
```

Device properties confirmed Android 16, API 36, codename `REL`. The APK inherited
development-codename min/target SDK metadata from `sdk_version:
"system_current"`. The local ADB client's `--force-sdk` test override did not
bypass the Package Manager rejection.

VM fix in `packages/modules/ProdX/providers/test/Android.bp`:

```bp
min_sdk_version: "36",
target_sdk_version: "36",
```

The app still compiles against `system_current`, while its installation contract
now targets released Android 16 numerically. `bpfmt -d` reports no formatting
diff. Per the build-handoff rule, the agent did not rebuild the APK.

Next user command:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
m ProdXNoOpTestProvider
```

The first rebuild attempt used the misspelled target
`ProdXNoOpTestProvide` (missing the final `r`), so Ninja correctly reported an
unknown target and suggested `ProdXNoOpTestProvider`. No source change or clean
is required; rerun the correctly spelled module target.

## 2026-07-15: ProdX Capability Activity Installed and Launched on I001D

The user reran the correctly spelled module target after pinning the test APK to
released API 36:

```text
[100% 9/9] ... install ProdXNoOpTestProvider.apk
#### build completed successfully (18 seconds) ####
```

The rebuilt APK was copied from the VM to
`D:\tmp\ProdXNoOpTestProvider.apk`. ADB streaming install returned an empty
failure message, so the agent used the equivalent non-streamed Package Manager
path:

```bash
adb push D:\tmp\ProdXNoOpTestProvider.apk /data/local/tmp/ProdXNoOpTestProvider.apk
adb shell pm install -r /data/local/tmp/ProdXNoOpTestProvider.apk
```

Package Manager reported `Success`. The agent launched:

```text
com.android.prodx.provider.test/.ProdXCapabilityActivity
```

Activity Manager reported `Status: ok`, `LaunchState: COLD`, and 179 ms total
launch time. The spinner/Go/result UI is now ready for on-device interaction.

## 2026-07-15: ProdX AIDL Parcelable Direction Fixed

The next targeted build confirmed the import fix: `ProdXMode` resolved and AIDL
advanced to semantic parameter validation. It then reported that the parcelable
`mode` parameter in `IProdXAuthority.setMode` had no transfer direction.

Change on the VM:

```aidl
void setMode(in ProdXMode mode);
```

All ProdX framework, SDK and runtime-service AIDL signatures were audited for
custom parcelable parameters. Every such parameter now explicitly uses `in`;
no other missing `in`, `out`, or `inout` declaration was found. The framework
repository passes `git diff --check`. No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Contract Build Passed ProdX AIDL and Reached 87 Percent

The user reran the clean `prodx-contract-runtime` build after the parcelable
direction fix. It passed the earlier ProdX AIDL import and direction failures
and advanced to approximately 87% (`5358/6127`) in unrelated platform API-stub
generation under libcore and ICU.

The reported Metalava messages are existing non-fatal warnings about invalid
`@hide` Javadoc syntax and hidden ICU superclasses. No ProdX failure was present
in the supplied output, so no VM source change was made. The user should allow
the active build to finish and report its final success or first `FAILED:` /
`ERROR:` block.

## 2026-07-15: ProdX Internal Framework Package Hidden from Public SDK

The build passed ProdX AIDL compilation and later failed in the framework
Metalava API compatibility/lint gate. The generated report showed that
`android.app.prodx` was being interpreted as a new public SDK surface, producing
large families of `RawAidl`, `UnflaggedApi`, `MissingNullability`,
`RethrowRemoteException`, and released-API compatibility errors.

This exposure was incorrect for the current milestone. The locked P0 design
defines these Binder interfaces and parcelables as internal platform contracts;
public APIs, their flags, permissions and compatibility review remain explicitly
deferred.

Change on the VM:

- Added `frameworks/base/core/java/android/app/prodx/package-info.java` with a
  valid package-level `@hide` block tag, following existing frameworks/base
  practice.

Effect:

- ProdX framework classes, raw Binder interfaces and parcelables remain
  compiled and usable by internal platform code.
- The package is excluded from public/system SDK API stubs until a later API
  review milestone deliberately exposes an approved surface.
- No lint suppression, API-lint baseline copy, `current.txt` update, or released
  API baseline modification was made.
- Framework status confirms that `core/api` is unchanged.
- No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Remaining ProdX Context API Findings Fixed

The package-level `@hide` was effective: the refreshed Metalava report reduced
the ProdX API-lint findings from the earlier large generated-AIDL surface to
exactly two errors. Both were service-name constants in
`android.content.Context`, which is outside the hidden `android.app.prodx`
package:

- `PRODX_AUTHORITY_SERVICE`
- `PRODX_GRANT_ADMIN_SERVICE`

Inspection also found that these constants had been appended after the final
closing brace of `Context`, creating a latent Java source-structure error.

VM fix:

- Moved both constants inside the `Context` class.
- Added valid block-tag `@hide` documentation to each constant.
- Preserved their names and string values.

Validation:

- `frameworks/base` passes `git diff --check`.
- The final file tail confirms both fields are inside the class closing brace.
- The generated released-API baseline contains zero ProdX-specific findings.
- No API baseline, API signature file or lint suppression was changed.
- No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: Premature ProdXManager System API Exposure Removed

The next build passed the public API-stub gate but failed the system API-stub
variant. The refreshed system-lint report contained 10 ProdX findings, all
rooted in `ProdXManager` being explicitly annotated `@SystemApi` while its
return types were correctly hidden internal contracts.

Findings included `ReferencesHidden`, `UnavailableSymbol`, `ManagerConstructor`,
`UnflaggedApi`, `MissingNullability`, `UnhiddenSystemApi`, and
`HiddenTypeParameter`.

VM fix:

- Removed the premature `@SystemApi` annotation and unused import from
  `ProdXManager`.
- Added explicit block-tag `@hide` documentation to the manager class.
- Kept the manager implementation available to internal framework/platform
  callers.

Validation:

- The full `android.app.prodx` package contains no remaining `@SystemApi`,
  `@TestApi`, or `@FlaggedApi` exposure annotation.
- `frameworks/base` passes `git diff --check`.
- No system API baseline, signature file or lint suppression was changed.
- No Android build was run by the agent.

Next user validation:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
rm -rf out/soong/
m prodx-contract-runtime
```

## 2026-07-15: `prodx-contract-runtime` Build Passed

The user completed the clean targeted validation after the ProdX internal API
visibility fixes:

```text
[100% 6127/6127] ... check current API
#### build completed successfully (14:39 (mm:ss)) ####
```

Confirmed gates passed:

- complete Soong/Kati/Ninja product-graph definition;
- all reconstructed ASUS duplicate-install cleanup;
- ProdX framework AIDL resolution and parcelable direction checks;
- public API-stub and system API-stub generation;
- Metalava API lint and current-API checks; and
- Kotlin/JVM compilation for `prodx-contract-runtime` and its required graph.

The Metalava baseline-output message is informational; the build succeeded and
no generated baseline was copied into frameworks/base.

`prodx-contract-runtime` is now build-validated. P0-02 remains in validation
until its contract/unit/security checks and the separately requested capability
test Activity APK are built and exercised. The next user build handoff is:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-userdebug
m ProdXNoOpTestProvider
```

## 2026-07-15: Complete VM State Mirrored for Git Handoff

The current VM was captured without changing source files or starting a build.
The durable checkpoint contains:

- a pinned 1,160-project Repo manifest;
- human-readable status, porcelain status for eight dirty projects, and the
  complete tracked patch;
- all 201 files from standalone `packages/modules/ProdX`;
- all 72 untracked files within managed projects, including the 41-file
  `frameworks/base/core/java/android/app/prodx` contract tree;
- SHA-256 manifests verified after download with zero mismatches; and
- the successfully installed API-36 test APK.

Key hashes are recorded in `TAKEOVER.md` and
`handoff/vm-current/porting-handoff-summary.txt`. The APK SHA-256 is
`d4b699b9e052892904a3375fa3584fbb6bccb094404e5ed14f9f4b4f0020f85a`.
The workstation-only `.idea/` directory remains intentionally excluded, and
the known intentionally empty kernel submodule worktree remains untouched.

## 2026-07-15: Cross-PC Continuation Handoff Added

Root `handsoff.md` now provides a concise continuation path for another PC. It
records repository bootstrap commands, VM access and ownership details, exact
checkpoint locations and hashes, validated build/device outcomes, known dirty
state and stale-record caveats, the pending SystemServer integration issue, and
the safe next actions. The byte-exact VM checkpoint remains commit `a93d725`;
the handoff document is committed separately on top of it.
