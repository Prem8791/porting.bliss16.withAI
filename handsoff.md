# Waterlily / ProdX Cross-PC Handoff

This file is the short operational entry point for continuing the Android 16
Bliss Waterlily ASUS I001D reconstruction from another computer. The complete
state description is in `TAKEOVER.md`; the chronological engineering record is
in `waterlily-i001d-reconstruction/progress.md`.

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
  standalone 201-file ProdX tree; and
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
Tracked patch:  a8efcf83c6d2668f6afc01264766f0916ae344eda8496bd436d55ab5d0c7de24
Porcelain list: e869b8ec3639837538d288028b25047a21e1252b558718192662f126a192a4f7
ProdX hashes:   197cd334678b51ab9def3bfd818612f186d7dc0ab02bbb20ab79f63f107dddc6
Untracked tgz:  9e7851d6fbf1a3a115034c365329254a2e6b509b9d0314cc057ac772ed8ceeee
```

The source mirrors were verified after download: 201/201 ProdX files and 72/72
managed-project untracked files matched their VM SHA-256 values.

## Current validated state

- The duplicate vendor-install collisions encountered during incremental Soong
  graph generation were audited and removed; the expanded explicit-copy audit
  ended with zero known collisions.
- ProdX framework AIDL imports and parcelable directions were fixed.
- ProdX remains an internal hidden platform contract; no API lint baseline or
  public/system signature file was changed.
- `m prodx-contract-runtime` passed 6127/6127 in 14:39.
- `m ProdXNoOpTestProvider` passed 9/9 in 18 seconds after pinning the APK to
  released API 36.
- The API-36 APK installed on the connected ASUS I001D, and
  `com.android.prodx.provider.test/.ProdXCapabilityActivity` launched with
  Activity Manager status `ok`.

The Activity contains a capability spinner, Go button, and result label for
no-op echo, provider health, and synthetic observation checks.

## Known caveats

- The VM contains unrelated dirty changes. Preserve them. The exact list and
  patch are in `handoff/vm-current/`.
- `packages/modules/ProdX/MILESTONES.md` and its original P0-01 README still
  describe the inert skeleton and are stale relative to current P0-02 code.
  Use `TAKEOVER.md` and `progress.md` for the current state until those live
  records are explicitly reconciled.
- The captured tracked patch shows ProdX startup blocks appended after the
  closing brace of `frameworks/base/services/java/com/android/server/SystemServer.java`.
  The targeted contract build did not expose this latent full-framework compile
  problem. Inspect and integrate those blocks inside the correct SystemServer
  startup method before attempting a complete ROM build.
- Six build-tools `date`/`tar` symlinks are deleted in the VM checkpoint. Do not
  recreate or discard them without first understanding why the VM needed that
  state.
- `.idea/` is workstation-specific and intentionally excluded from Git.

## Immediate continuation

1. Confirm Git is at or beyond checkpoint `a93d725` and read the latest
   `progress.md` entry.
2. Connect to the existing VM and run `repo status`; compare it with the saved
   porcelain inventory before making changes.
3. Exercise every item in `ProdXCapabilityActivity` on the device and record
   the displayed result.
4. Correctly integrate the pending SystemServer service-start code, preserving
   unrelated VM modifications.
5. Hand the next targeted build command to the user and append the result to
   `progress.md`.

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
