# Waterlily Android 16 Device Source Reconstruction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the current booting mixed Waterlily I001D Android 16 VM tree into a cleaner reconstructed Android 16 device source tree, with only unavoidable proprietary binaries left as documented prebuilts.

**Architecture:** Treat `device/asus/*` and generated vendor manifests as the source-owned adaptation layer. Keep closed firmware, closed HAL libraries, signed kernel modules, and APK/APEX signing artifacts as explicit proprietary boundaries. Split kernel source recovery into a separate decision track because the current boot path depends on an official/community prebuilt `4.14.357-openela-perf+` kernel and matching modules.

**Tech Stack:** Android 16 Bliss/AOSP build system, Kati/Soong, Lineage extract-utils style vendor manifests, VINTF, SELinux policy, Qualcomm SM8150 HAL sources, ASUS I001D proprietary blobs, Google Cloud VM `instance-20260710-230647`.

## Global Constraints

- VM source root: `/home/premanandal1978/android/waterlily`.
- VM instance: `instance-20260710-230647`, project `customrom-501702`, zone `us-south1-a`.
- Do not start long Android builds unless the user explicitly asks.
- The user will run build commands after migration; agents hand off exact commands and do not start targeted or full Android builds.
- Build validation commands must be handed to the user when they are long-running.
- Keep `waterlily-i001d-reconstruction/progress.md` updated after material source changes.
- Preserve unrelated dirty VM files.
- Do not replace closed firmware, camera blobs, DRM blobs, or signed modules with guessed source.
- Current known boot dependency: `device/asus/I001D/BoardConfig.mk` forces `TARGET_PREBUILT_KERNEL := $(DEVICE_PATH)/prebuilt/kernel`.
- Current missing project: `hardware/asus`.

---

## File Structure

- `device/asus/I001D/BoardConfig.mk`: I001D board flags, kernel selection, partition sizes, security patch, vendor include.
- `device/asus/I001D/device.mk`: I001D product packages, I001D-specific copy rules, module copy rules, product inheritance.
- `device/asus/I001D/proprietary-files.txt`: curated I001D proprietary blob manifest.
- `device/asus/I001D/init/`: I001D fstab/init source-owned files.
- `device/asus/sm8150-common/BoardConfigCommon.mk`: common board flags, common sepolicy, common kernel flags.
- `device/asus/sm8150-common/msmnile.mk`: common HAL/package selections and source-owned copy rules.
- `device/asus/sm8150-common/sepolicy/`: reconstructed Android 16 SELinux deltas.
- `device/asus/sm8150-common/vintf/`: reconstructed VINTF fragments.
- `vendor/asus/I001D/I001D-vendor.mk`: generated I001D vendor copy rules.
- `vendor/asus/I001D/proprietary/`: unavoidable I001D proprietary blobs.
- `vendor/asus/sm8150-common/sm8150-common-vendor.mk`: generated common vendor copy rules.
- `vendor/asus/sm8150-common/proprietary/`: unavoidable common proprietary blobs.
- `hardware/asus/`: recreate minimal ASUS interface definitions, starting with `vendor.asus.motor@1.0`, only where build/init/VINTF validation needs source metadata.
- `reference-material-full/android16/`: read-only Android 16 release artifact extraction used as behavioral evidence.
- `analysis/android16-source-reconstruction/`: new audit outputs, migration manifests, and verification logs.
- `patches/`: reproducible text patches for each checkpoint.

---

### Task 1: Current VM Prebuilt Inventory

**Files:**
- Create: `analysis/android16-source-reconstruction/prebuilt-inventory.md`
- Create: `analysis/android16-source-reconstruction/prebuilt-inventory.tsv`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: VM tree at `/home/premanandal1978/android/waterlily`.
- Produces: A categorized inventory used by every later task.

- [ ] **Step 1: Create the analysis directory on the VM**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
mkdir -p analysis/android16-source-reconstruction
```

Expected: directory exists.

- [ ] **Step 2: Capture direct prebuilt references**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
grep -RIn --exclude-dir=.git -E \
  'prebuilt|BUILD_PREBUILT|PRODUCT_COPY_FILES|TARGET_FORCE_PREBUILT_KERNEL|TARGET_PREBUILT_KERNEL|cc_prebuilt|prebuilt_etc' \
  device/asus/I001D device/asus/sm8150-common vendor/asus/I001D vendor/asus/sm8150-common \
  > analysis/android16-source-reconstruction/prebuilt-reference-grep.txt
```

Expected: file lists kernel, module, vendor blob, `prebuilt_etc`, and generated vendor copy surfaces.

- [ ] **Step 3: Capture proprietary blob paths**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
find device/asus/I001D/prebuilt vendor/asus/I001D/proprietary vendor/asus/sm8150-common/proprietary \
  -type f 2>/dev/null | sort \
  > analysis/android16-source-reconstruction/proprietary-and-prebuilt-files.txt
```

Expected: file contains current kernel, `.ko` modules, firmware, HALs, configs, APKs, and libraries.

- [ ] **Step 4: Classify each item**

Create `analysis/android16-source-reconstruction/prebuilt-inventory.tsv` with columns:

```text
path	category	current_owner	target_owner	action	reason
```

Use these category values only:

```text
source_config
source_built_hal_selection
generated_vendor_manifest
closed_firmware
closed_hal_blob
signed_kernel_module
prebuilt_kernel
signed_app_or_apex
unknown
```

Expected target owners:

```text
device_source
common_device_source
hardware_asus_source
vendor_manifest
proprietary_boundary
kernel_track
remove
investigate
```

- [ ] **Step 5: Summarize inventory**

Create `analysis/android16-source-reconstruction/prebuilt-inventory.md` with sections:

```markdown
# Android 16 Prebuilt Inventory

## Migratable Now

## Migratable After Source Metadata Exists

## Proprietary Boundary

## Kernel Track

## Remove Candidates

## Unknowns
```

Expected: every row in the TSV appears in exactly one section.

- [ ] **Step 6: Record progress**

Append to `waterlily-i001d-reconstruction/progress.md`:

```markdown
## 2026-07-19 Android 16 Source Reconstruction Inventory

Created VM inventory under `analysis/android16-source-reconstruction/`.
This inventory classifies current prebuilts into source-config migrations,
generated vendor manifest work, proprietary boundaries, kernel-track items,
remove candidates, and unknowns. No build was started.
```

Expected: progress file records the checkpoint.

---

### Task 2: Source-Owned Device/Common Configuration Cleanup

**Files:**
- Modify: `device/asus/I001D/BoardConfig.mk`
- Modify: `device/asus/I001D/device.mk`
- Modify: `device/asus/sm8150-common/BoardConfigCommon.mk`
- Modify: `device/asus/sm8150-common/msmnile.mk`
- Create: `analysis/android16-source-reconstruction/source-owned-config.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: Task 1 inventory.
- Produces: A source-owned set of Android 16 product and common config rules.

- [ ] **Step 1: Identify stale Android 14 package/copy rules**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
grep -RIn -E 'android.hardware.camera.provider@2.4|android.hardware.audio@6|soundtrigger.*2.1|vendor/lib/TrustedUI.so|prebuilts/vndk/v29|android.hardware.vibrator@1.2-service.I001D' \
  device/asus/I001D device/asus/sm8150-common vendor/asus/I001D vendor/asus/sm8150-common \
  > analysis/android16-source-reconstruction/stale-a14-rules.txt || true
```

Expected: file is empty or contains only items already documented as removed.

- [ ] **Step 2: Verify Android 16 source-built selections**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
grep -RIn -E 'android.hardware.audio.service|android.hardware.soundtrigger|android.hardware.sensors@2.0-service.multihal|hwcomposer.qcom|camera.provider' \
  device/asus/I001D device/asus/sm8150-common \
  > analysis/android16-source-reconstruction/android16-source-selections.txt
```

Expected: sensors multihal, HWC, audio 7.x/effect 7.0/soundtrigger 2.3, and current camera provider selections are visible where applicable.

- [ ] **Step 3: Remove source-output collision copy rules**

Edit only rules where a prebuilt copied from `vendor/asus/*/proprietary` overwrites a module built from Android 16 source.

Known examples to preserve as removed:

```text
vendor/bin/hw/android.hardware.sensors@2.0-service.multihal
vendor/etc/init/android.hardware.sensors@2.0-service-multihal.rc
vendor/lib/hw/audio.primary.msmnile.so
vendor/lib64/hw/audio.primary.msmnile.so
vendor/lib64/liba2dpoffload.so
android.hardware.camera.provider@2.4-service_64
```

Expected: source-built modules own their destinations.

- [ ] **Step 4: Document source-owned decisions**

Create `analysis/android16-source-reconstruction/source-owned-config.md`:

```markdown
# Source-Owned Android 16 Device Config

## Device Tree

## Common Tree

## Source-Built HAL Selections

## Removed Source-Output Collisions

## Remaining Proprietary Boundaries
```

Expected: each edited rule has a one-line reason.

- [ ] **Step 5: Static validation**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
```

Expected: lunch succeeds and reports `PLATFORM_VERSION=16`, `BLISS_DEVICE=I001D`, `BUILD_ID=BP4A.251205.006`.

- [ ] **Step 6: Hand off targeted build if needed**

If static validation passes and the edits affect package ownership, ask the user to run:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m nothing
```

Expected: Kati/Soong parsing reaches completion without duplicate destination or missing module errors.

- [ ] **Step 7: Record progress**

Append a checkpoint to `progress.md` listing edited files, removed copy rules, and whether `lunch` passed.

---

### Task 3: VINTF, Init, Fstab, Property, And SELinux Reconstruction

**Files:**
- Modify: `device/asus/I001D/init/`
- Modify: `device/asus/I001D/vendor.prop`
- Modify: `device/asus/I001D/system.prop`
- Modify: `device/asus/sm8150-common/product.prop`
- Modify: `device/asus/sm8150-common/vintf/`
- Modify: `device/asus/sm8150-common/sepolicy/`
- Create: `analysis/android16-source-reconstruction/runtime-config-reconstruction.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: Android 16 extracted reference files under `reference-material-full/android16/device/asus`.
- Produces: source-owned runtime configuration matching the Android 16 behavior.

- [ ] **Step 1: Diff current source against extracted Android 16 reference**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
diff -ruN device/asus/I001D/init reference-material-full/android16/device/asus/I001D/init \
  > analysis/android16-source-reconstruction/i001d-init-reference.diff || true
diff -ruN device/asus/sm8150-common/init reference-material-full/android16/device/asus/sm8150-common/init \
  > analysis/android16-source-reconstruction/common-init-reference.diff || true
```

Expected: diffs exist for review; do not blindly apply them.

- [ ] **Step 2: Verify fstab source ownership**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
sha256sum device/asus/I001D/init/etc/fstab.qcom reference-material-full/android16/device/asus/I001D/fstab.qcom \
  > analysis/android16-source-reconstruction/fstab-sha256.txt
```

Expected: if hashes differ, inspect and explain each intentional difference before editing.

- [ ] **Step 3: Audit VINTF declarations**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
find device/asus device/qcom vendor/asus -path '*vintf*' -o -name '*manifest*.xml' -o -name '*compatibility_matrix*.xml' \
  > analysis/android16-source-reconstruction/vintf-file-list.txt
grep -RIn -E 'android.hardware.audio|android.hardware.soundtrigger|android.hardware.camera|android.hardware.sensors|vendor.asus.motor|android.hardware.power|android.hardware.boot' \
  device/asus vendor/asus \
  > analysis/android16-source-reconstruction/vintf-hal-grep.txt || true
```

Expected: no duplicate stale boot/power fragments; audio/soundtrigger declarations match selected Android 16 services.

- [ ] **Step 4: Audit SELinux ownership**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
grep -RIn -E 'hal_camera|hal_audio|hal_sensors|vendor_asus|vendor_nfc|bpf|lmkd|touch|goodix' \
  device/asus/sm8150-common/sepolicy device/asus/I001D/sepolicy 2>/dev/null \
  > analysis/android16-source-reconstruction/sepolicy-relevant-rules.txt
```

Expected: rules are device/common source, not one-off runtime hacks.

- [ ] **Step 5: Apply only reviewed config deltas**

Edit init, fstab, props, VINTF, and sepolicy only when the Android 16 reference and runtime logs both support the change.

Expected: each change has a source-of-truth note in `runtime-config-reconstruction.md`.

- [ ] **Step 6: Run static config validation**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m selinux_policy
```

Expected: if the user authorizes this targeted build, `selinux_policy` completes. If not authorized, stop after `lunch` and hand off the command.

- [ ] **Step 7: Record progress**

Append edited files, validation status, and unresolved runtime-config gaps to `progress.md`.

---

### Task 4: Recreate Minimal `hardware/asus`

**Files:**
- Create: `hardware/asus/interfaces/motor/1.0/Android.bp`
- Create: `hardware/asus/interfaces/motor/1.0/IRotateCameraInterface.hal`
- Create: `hardware/asus/interfaces/current.txt`
- Modify: `device/asus/sm8150-common/BoardConfigCommon.mk` or relevant namespace config if needed
- Create: `analysis/android16-source-reconstruction/hardware-asus.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: prebuilt RC/VINTF references to `vendor.asus.motor@1.0::IRotateCameraInterface`.
- Produces: build-visible ASUS HIDL metadata so init/VINTF validation no longer depends on deleting interface declarations.

- [ ] **Step 1: Confirm all missing ASUS interface references**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
grep -RIn -E 'vendor\.asus\.[a-zA-Z0-9_.-]+@[0-9]+\.[0-9]+' \
  device/asus vendor/asus reference-material-full/android16 \
  > analysis/android16-source-reconstruction/vendor-asus-interface-references.txt || true
```

Expected: `vendor.asus.motor@1.0` appears if still referenced by blobs, RC files, or reference metadata.

- [ ] **Step 2: Create minimal motor interface package**

Create `hardware/asus/interfaces/motor/1.0/IRotateCameraInterface.hal`:

```hal
package vendor.asus.motor@1.0;

interface IRotateCameraInterface {
};
```

Expected: minimal metadata exists. Do not implement a fake service.

- [ ] **Step 3: Create HIDL build file**

Create `hardware/asus/interfaces/motor/1.0/Android.bp`:

```bp
hidl_interface {
    name: "vendor.asus.motor@1.0",
    root: "vendor.asus",
    srcs: [
        "IRotateCameraInterface.hal",
    ],
    interfaces: [
        "android.hidl.base@1.0",
    ],
    gen_java: false,
}
```

Expected: Soong can discover the interface metadata.

- [ ] **Step 4: Generate or update current.txt**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
hidl-gen -L hash -r vendor.asus:hardware/asus/interfaces vendor.asus.motor@1.0 \
  > hardware/asus/interfaces/current.txt
```

Expected: `current.txt` contains the hash for `vendor.asus.motor@1.0::IRotateCameraInterface`.

- [ ] **Step 5: Restore RC interface declaration only if validation supports it**

If the Android 16 reference RC declares:

```text
interface vendor.asus.motor@1.0::IRotateCameraInterface default
```

then restore it in the source-owned RC after Task 4 validation. If runtime does not require this service registration, document why it stays absent.

- [ ] **Step 6: Validate metadata**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m vendor.asus.motor@1.0
```

Expected: targeted module builds if HIDL tooling accepts the package.

- [ ] **Step 7: Record progress**

Append the `hardware/asus` decision and validation result to `progress.md`.

---

### Task 5: Normalize Proprietary Manifests And Generated Vendor Makefiles

**Files:**
- Modify: `device/asus/I001D/proprietary-files.txt`
- Modify: `device/asus/sm8150-common/proprietary-files.txt`
- Regenerate: `vendor/asus/I001D/I001D-vendor.mk`
- Regenerate: `vendor/asus/sm8150-common/sm8150-common-vendor.mk`
- Create: `analysis/android16-source-reconstruction/vendor-manifest-normalization.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: Task 1 inventory and Android 16 vendor path/hash inventories.
- Produces: vendor manifests that copy only required proprietary boundaries and do not overwrite source-built Android 16 outputs.

- [ ] **Step 1: Compare manifest paths against Android 16 vendor inventory**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
comm -23 \
  <(sed 's/#.*//' device/asus/I001D/proprietary-files.txt | sed '/^[[:space:]]*$/d' | sed 's/^-//' | cut -d: -f1 | sort) \
  <(sed 's#^/##' reference-material-full/android16/vendor/asus/android16-vendor-paths.txt | sort) \
  > analysis/android16-source-reconstruction/i001d-manifest-not-in-a16-vendor.txt
```

Expected: old Android 14-only paths appear for removal review.

- [ ] **Step 2: Mark source-built collisions for removal**

Ensure these destinations are not produced by vendor copy rules:

```text
vendor/bin/hw/android.hardware.sensors@2.0-service.multihal
vendor/etc/init/android.hardware.sensors@2.0-service-multihal.rc
vendor/lib/hw/audio.primary.msmnile.so
vendor/lib64/hw/audio.primary.msmnile.so
vendor/lib64/liba2dpoffload.so
vendor/bin/hw/android.hardware.camera.provider@2.4-service_64
vendor/etc/init/android.hardware.camera.provider@2.4-service_64.rc
vendor/lib/TrustedUI.so
```

Expected: copy rules for these are absent unless a later verified runtime result proves otherwise.

- [ ] **Step 3: Preserve proprietary boundaries**

Keep entries for:

```text
vendor/firmware/**
vendor/lib64/camera.device@*-impl.so
vendor/lib64/libarcsoft*.so
vendor/lib64/libAsus*.so
vendor/lib64/vendor.asus.*.so
vendor/lib64/vendor.qti.hardware.audiohalext@1.0.so
vendor/lib64/libxditk*.so
```

Expected: closed HALs and firmware are documented as proprietary, not removed just to look pure.

- [ ] **Step 4: Regenerate vendor makefiles**

Run the tree's existing extract-utils generator command used in prior checkpoints. If the exact wrapper is unknown, first inspect:

```bash
cd /home/premanandal1978/android/waterlily
find device/asus -maxdepth 3 -type f \( -name 'setup-makefiles.sh' -o -name 'extract-files.sh' \) -print
```

Then run the I001D and common setup scripts in legacy copy-only compatibility mode, matching prior checkpoint behavior.

Expected: generated makefiles have no duplicate destinations and no missing source files.

- [ ] **Step 5: Validate generated copy sources**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
awk -F: '/vendor\/asus\/.*proprietary/ {print $1}' vendor/asus/I001D/I001D-vendor.mk vendor/asus/sm8150-common/sm8150-common-vendor.mk \
  | sed 's/^[[:space:]]*//' | sort -u \
  > analysis/android16-source-reconstruction/generated-copy-sources.txt
while read -r src; do [ -e "$src" ] || echo "$src"; done \
  < analysis/android16-source-reconstruction/generated-copy-sources.txt \
  > analysis/android16-source-reconstruction/missing-generated-copy-sources.txt
```

Expected: `missing-generated-copy-sources.txt` is empty.

- [ ] **Step 6: Record progress**

Append manifest changes, regenerated files, and validation output paths to `progress.md`.

---

### Task 6: Kernel And Module Decision Track

**Files:**
- Create: `analysis/android16-source-reconstruction/kernel-decision.md`
- Option A keeps: `device/asus/I001D/prebuilt/kernel`
- Option A keeps: `device/asus/I001D/prebuilt/modules/*`
- Option B modifies: `kernel/asus/I001D`
- Option B modifies: `device/asus/I001D/BoardConfig.mk`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: current boot dependency on official/community `4.14.357-openela-perf+` kernel.
- Produces: an explicit decision: keep kernel prebuilts as a documented boundary, or start a separate kernel reconstruction project.

- [ ] **Step 1: Record current kernel facts**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
sha256sum device/asus/I001D/prebuilt/kernel device/asus/I001D/prebuilt/modules/*.ko \
  > analysis/android16-source-reconstruction/kernel-prebuilt-sha256.txt
grep -RIn -E 'TARGET_FORCE_PREBUILT_KERNEL|TARGET_PREBUILT_KERNEL|TARGET_KERNEL_SOURCE|TARGET_KERNEL_CONFIG' \
  device/asus/I001D device/asus/sm8150-common \
  > analysis/android16-source-reconstruction/kernel-selection.txt
```

Expected: hashes and selection rules are captured.

- [ ] **Step 2: Check source kernel identity**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
git -C kernel/asus/I001D rev-parse HEAD > analysis/android16-source-reconstruction/kernel-source-head.txt
grep -RIn -E '4\.14\.190|4\.14\.357|openela|I001D_defconfig|CONFIG_MODVERSIONS|CONFIG_MODULE_SIG' \
  kernel/asus/I001D/arch/arm64/configs kernel/asus/I001D 2>/dev/null \
  > analysis/android16-source-reconstruction/kernel-source-signal.txt || true
```

Expected: source kernel gap is documented.

- [ ] **Step 3: Choose Option A or B**

Option A: document kernel and `.ko` modules as proprietary/prebuilt boundary.

Option B: create a separate kernel reconstruction plan with its own branch, tests, and boot validation.

Expected recommendation for current ROM stabilization: Option A now, Option B later.

- [ ] **Step 4: Write decision document**

Create `analysis/android16-source-reconstruction/kernel-decision.md`:

```markdown
# Kernel Decision

## Current Boot Dependency

## Why The Prebuilt Kernel Is Still Required

## Required Conditions To Replace It With Source

## Module Signing And Vermagic Constraints

## Current Decision
```

Expected: no ambiguity about why a pure source tree still contains kernel prebuilts.

- [ ] **Step 5: Record progress**

Append the kernel decision and any deferred work to `progress.md`.

---

### Task 7: Validation Matrix And Build Handoff

**Files:**
- Create: `analysis/android16-source-reconstruction/validation-matrix.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: tasks 1-6.
- Produces: exact validation commands and acceptance criteria.

- [ ] **Step 1: Create validation matrix**

Create `analysis/android16-source-reconstruction/validation-matrix.md`:

```markdown
# Android 16 Source Reconstruction Validation Matrix

## Static Checks

## User-Run Targeted Builds

## Full Build Handoff

## Flash/Boot Checks

## Runtime Checks

## Rollback
```

Expected: each section has exact commands and pass/fail criteria.

- [ ] **Step 2: Run static checks**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
git diff --check
```

Expected: lunch succeeds; `git diff --check` prints no errors.

- [ ] **Step 3: Hand off targeted builds**

Ask user to run:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
m selinux_policy
m vendor.asus.motor@1.0
m android.hardware.sensors@2.0-service.multihal
m android.hardware.audio.service
```

Expected: the user reports whether each targeted build completes or produces a specific blocker to address.

- [ ] **Step 4: Hand off full build**

Only after targeted builds pass, ask user to run the GApps build:

```bash
cd /home/premanandal1978/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
blissify -g I001D
```

Expected: GApps output ZIP under `out/target/product/I001D/`, with a filename containing `gapps`.

- [ ] **Step 5: Runtime validation checklist**

After flashing a successful build, verify:

```text
boot completes to launcher
adb shell getprop ro.build.version.release returns 16
adb shell getprop ro.build.id returns BP4A.251205.006
sensors service registers
audio service registers and playback works
camera provider starts without competing provider abort
touch and station modules behave as before
no new SELinux denial flood blocks boot
```

Expected: runtime behavior is at least as good as the current booting mixed build.

- [ ] **Step 6: Record progress**

Append validation result, output artifact name, hashes, and unresolved gaps to `progress.md`.

---

### Task 8: Final Source Boundary Report

**Files:**
- Create: `analysis/android16-source-reconstruction/final-source-boundary-report.md`
- Modify: `waterlily-i001d-reconstruction/progress.md`

**Interfaces:**
- Consumes: all previous task outputs.
- Produces: final statement of what was migrated to source and what remains prebuilt by necessity.

- [ ] **Step 1: Create final report**

Create `analysis/android16-source-reconstruction/final-source-boundary-report.md`:

```markdown
# Final Source Boundary Report

## Migrated To Reconstructed Android 16 Source

## Kept As Proprietary Boundary

## Kept As Kernel Boundary

## Removed Android 14 Legacy Material

## Validation Evidence

## Deferred Work
```

Expected: the report can answer "how pure is this tree now?" without rereading the full history.

- [ ] **Step 2: Create patch bundle**

Run on VM:

```bash
cd /home/premanandal1978/android/waterlily
mkdir -p patches
git diff -- device/asus/I001D device/asus/sm8150-common vendor/asus/I001D vendor/asus/sm8150-common hardware/asus \
  > patches/android16-device-source-reconstruction.patch
```

Expected: patch captures source reconstruction changes and excludes build output.

- [ ] **Step 3: Record final checkpoint**

Append to `progress.md`:

```markdown
## 2026-07-19 Android 16 Device Source Reconstruction Boundary

The reconstructed source-owned portions, remaining proprietary boundaries,
kernel boundary, validation evidence, and deferred work are recorded in
`analysis/android16-source-reconstruction/final-source-boundary-report.md`.
No closed firmware/HAL/signature material was represented as source.
```

Expected: project handoff is current.

---

## Self-Review

- Spec coverage: The plan covers source config, HAL selection, VINTF/init/sepolicy, generated vendor manifests, missing `hardware/asus`, kernel/module boundaries, validation, and final reporting.
- Placeholder scan: The plan avoids open-ended placeholders and provides exact commands, paths, and expected outcomes.
- Boundary consistency: Closed blobs and signed kernel modules are consistently treated as proprietary/kernel boundaries, not source reconstruction targets.
