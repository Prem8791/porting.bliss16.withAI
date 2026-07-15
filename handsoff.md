# Waterlily / ProdX Cross-PC Handoff

This file is the short operational entry point for continuing the Android 16
Bliss Waterlily ASUS I001D reconstruction from another computer. The complete
state description is in `TAKEOVER.md`; the chronological engineering record is
in `waterlily-i001d-reconstruction/progress.md`.

## Current point of our endeavour

The project is introducing ProdX as an Android-native capability layer inside
the OS so the future on-device AI can discover approved capabilities, request
authorization, execute through controlled providers, observe results, and leave
an auditable record. ProdX is intended to be the governed bridge between the AI
and system/device functions, not merely a standalone application.

Work has advanced beyond the inert repository skeleton:

- internal framework contracts and Binder AIDL types exist under
  `android.app.prodx`;
- authority, grants, policy, registry, observation, broker, extension, audit,
  provider SDK, permission, and SELinux boundaries have initial code/skeletons;
- Soong modules, manifests, contract code, service code, test fixtures, and
  multiple validation targets are present in `packages/modules/ProdX`;
- a no-op provider Activity exposes capabilities in a spinner and executes the
  selected test through a Go button, displaying the result in the same screen;
- the contract-runtime build passes, and the API-36 test Activity APK has been
  installed and launched on the ASUS I001D.

This is still the integration/validation stage, not a finished OS feature. The
test Activity currently demonstrates safe no-op/synthetic capability paths.
ProdX is not yet proven end-to-end as a boot-started service in a complete
flashed ROM, and real AI/device actions are not yet enabled. The malformed
SystemServer startup block has now been replaced with lifecycle-managed startup
of `ProdXAuthorityService`, and the resulting `m services` compile passed.
Required modules and permissions must next be wired into the product, SELinux
and lifecycle behavior must pass validation, a complete ROM must build and
boot, and the on-device AI-to-ProdX-to-provider flow must be tested.

## Git repository

```text
Remote: https://github.com/Prem8791/porting.bliss16.withAI.git
Branch: main
VM checkpoint commit: a93d725
```

On the new PC:

```powershell
git clone https://github.com/Prem8791/porting.bliss16.withAI.git
cd porting.bliss16.withAI
git lfs install
git lfs pull
git submodule update --init --recursive
git status --short --branch
```

Read these files before editing:

1. `AGENTS.md`
2. `TAKEOVER.md`
3. this file
4. `waterlily-i001d-reconstruction/progress.md`
5. `waterlily-i001d-reconstruction/handoff/README.md`

The original workstation intentionally had an empty working tree inside the
`baseline/kernel_asus_I001D` submodule while retaining its Git metadata. That
made the parent report the submodule as modified. A fresh recursive clone should
populate it normally at the parent-pinned commit; do not reproduce the empty
worktree.

## Live Android VM

```text
Google Cloud project: customrom-501702
Zone: us-south1-a
Instance: instance-20260710-230647
SSH login: home
Checkout owner: premanandal1978
Android root: /home/premanandal1978/android/waterlily
Product: bliss_I001D-userdebug
```

Connect from PowerShell:

```powershell
gcloud compute ssh home@instance-20260710-230647 --project customrom-501702 --zone us-south1-a
```

The `home` login may not be able to traverse the checkout owner's home
directory directly. Run source operations as `premanandal1978`, for example:

```bash
sudo -u premanandal1978 bash -lc 'cd /home/premanandal1978/android/waterlily && repo status'
```

Do not start a long Android build unless the user explicitly asks the agent to
run it. Ordinarily, make VM edits, provide the exact build command, and let the
user run it.

## Saved VM checkpoint

The Git checkpoint contains three complementary forms of VM state:

- `waterlily-i001d-reconstruction/handoff/vm-current/`: pinned manifest,
  status, porcelain inventory, tracked patch, hash manifests, and the archive
  of all managed-project untracked files;
- `waterlily-i001d-reconstruction/vm-edit/packages-modules-ProdX/`: the exact
  standalone 203-file ProdX tree; and
- `waterlily-i001d-reconstruction/vm-edit/managed-repo-untracked/`: all 72
  untracked files beneath Repo-managed projects, preserving Android-root paths.

The standalone test APK is saved at:

```text
waterlily-i001d-reconstruction/artifacts/ProdXNoOpTestProvider-api36-20260715.apk
SHA-256: d4b699b9e052892904a3375fa3584fbb6bccb094404e5ed14f9f4b4f0020f85a
```

Important checkpoint hashes:

```text
Repo manifest:  ebdce4ba5ebff4d7b2269f13f94884f63572f70b3a86f1304107010a75da1da4
Repo status:    04e287ae15ba629de513244186253c606ba63eac6794ab6c7e71ab9da71e37e5
Tracked patch:  2e830fdca62824e1c7d609f15b75e727ce3e6998f1eb48284f0c309fe525981f
Porcelain list: 535c166c92ce4875a4b9aaad394bf2a3c23536053b269939048d9c3e05d6322a
ProdX hashes:   5c5bf55242b003e2aac05aa40ecae74d4dbbc52e54796995525b982312ce97fb
Untracked tgz:  ecc70ed6492f61b7a8965d2e76d76909ba58e58bb21291c83a04aaa354bbacf6
```

The source mirrors were verified after download: 203/203 ProdX files and 72/72
managed-project untracked files matched their VM SHA-256 values.

## Current validated state

- The duplicate vendor-install collisions encountered during incremental Soong
  graph generation were audited and removed; the expanded explicit-copy audit
  ended with zero known collisions.
- ProdX framework AIDL imports and parcelable directions were fixed.
- ProdX remains an internal hidden platform contract; no API lint baseline or
  public/system signature file was changed.
- `m prodx-contract-runtime` passed 6127/6127 in 14:39.
- `m services` passed 1089/1089 in 1:50 with the corrected ProdX SystemServer
  lifecycle registration.
- `m prodx-system-feature.xml FrameworksResOverlay` passed 17/17 in 4:56. The
  feature XML landed in `/system/etc/permissions` and the overlay APK in
  `/vendor/overlay`.
- `m ProdXNoOpTestProvider` passed 9/9 in 18 seconds after pinning the APK to
  released API 36.
- The API-36 APK installed on the connected ASUS I001D, and
  `com.android.prodx.provider.test/.ProdXCapabilityActivity` launched with
  Activity Manager status `ok`.

The Activity contains a capability spinner, Go button, and result label for
no-op echo, provider health, and synthetic observation checks. All three checks
passed on the connected ASUS I001D.

## Known caveats

- The VM contains unrelated dirty changes. Preserve them. The exact list and
  patch are in `handoff/vm-current/`.
- `packages/modules/ProdX/MILESTONES.md` and its original P0-01 README still
  describe the inert skeleton and are stale relative to current P0-02 code.
  Use `TAKEOVER.md` and `progress.md` for the current state until those live
  records are explicitly reconciled.
- ProdX SystemServer startup is structurally corrected, passes
  `git diff --check`, and is compile-validated by `m services`. It has not yet
  been boot-validated in a newly built ROM.
- The framework default for ProdX is now disabled. I001D explicitly enables it
  through `FrameworksResOverlay` and includes the `android.software.prodx`
  feature XML. This product wiring is statically validated but awaits its
  targeted Soong/product build.
- The draft signature-permission and privapp XML files are not packaged. Five
  reviewed hidden signature-only permissions now exist in the platform
  manifest, and authority/grant Binder calls enforce least-privilege mappings.
  This security slice awaits `services` and `selinux_policy` build validation.
- Six build-tools `date`/`tar` symlinks are deleted in the VM checkpoint. Do not
  recreate or discard them without first understanding why the VM needed that
  state.
- `.idea/` is workstation-specific and intentionally excluded from Git.

## Immediate continuation

1. Confirm Git is at or beyond checkpoint `a93d725` and read the latest
   `progress.md` entry.
2. Connect to the existing VM and run `repo status`; compare it with the saved
   porcelain inventory before making changes.
3. Preserve the successful three-capability Activity result and `m services`
   checkpoint while moving into product wiring.
4. Run the handed-off `services` and `selinux_policy` build for the permission
   enforcement and canonical Binder service labels.
5. Remove or replace the still-unpackaged draft permission placeholders after
   the real platform declarations and policy pass their build gates.
6. Hand each build command to the user and append its result to `progress.md`.
7. After targeted gates pass, build/flash the ROM and validate the real
   AI-to-ProdX capability path on-device.

To reinstall and launch the saved APK from Windows PowerShell:

```powershell
adb push ".\waterlily-i001d-reconstruction\artifacts\ProdXNoOpTestProvider-api36-20260715.apk" /data/local/tmp/ProdXNoOpTestProvider.apk
adb shell pm install -r /data/local/tmp/ProdXNoOpTestProvider.apk
adb shell am start -W -n "com.android.prodx.provider.test/.ProdXCapabilityActivity"
```

If the VM is lost, do not treat `porting-handoff-repo-diff.patch` as a single
ordinary root patch: Repo's output is grouped by project. Restore the pinned
manifest first, apply each tracked section in its owning project, then copy the
two source mirrors into their Android-root-relative destinations and verify the
provided SHA-256 manifests.
