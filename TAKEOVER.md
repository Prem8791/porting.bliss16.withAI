# Deterministic Agent Takeover

## Current outcome

P0-00 and P0-01 are complete. P0-02 implementation and validation are in
progress on the Android 16 Bliss Waterlily VM. Do not treat the older
`packages/modules/ProdX/MILESTONES.md` text as current; the VM source tree and
this handoff are the authoritative checkpoint until that live milestone record
is deliberately reconciled.

The user successfully built `prodx-contract-runtime` (6127/6127, 14:39) after
the duplicate-install, AIDL, and hidden-API fixes. The test provider target also
built successfully (9/9, 18 seconds). Its API-36 APK installed on the connected
I001D and `ProdXCapabilityActivity` launched successfully. P0-02 is not marked
complete because the wider contract, security, and device validation remains.

## Read first

1. Root `AGENTS.md`.
2. `waterlily-i001d-reconstruction/progress.md`, including the latest
   2026-07-15 entries.
3. `waterlily-i001d-reconstruction/handoff/README.md` and
   `handoff/vm-current/porting-handoff-summary.txt`.
4. `waterlily-i001d-reconstruction/vm-edit/packages-modules-ProdX/`.
5. The immutable ProdX `tests/reference/README.md`, `REFERENCE-LOCK.md`, and P0
   implementation specification.

Never edit a file covered by `tests/reference/BASELINE-FILES.sha256`. Live records belong outside that locked directory.

## VM connection and source

```text
Google Cloud project: customrom-501702
Zone: us-south1-a
Instance: instance-20260710-230647
User: premanandal1978
Android source: /home/premanandal1978/android/waterlily
Product: bliss_I001D-userdebug
ProdX module: /home/premanandal1978/android/waterlily/packages/modules/ProdX
```

Connect with the gcloud login account, then run source commands as the checkout
owner when necessary:

```powershell
gcloud compute ssh home@instance-20260710-230647 --project customrom-501702 --zone us-south1-a
```

## Exact VM checkpoint

The recoverable 2026-07-15 VM checkpoint is in
`waterlily-i001d-reconstruction/handoff/vm-current/`:

- `porting-handoff-manifest.xml`: pinned 1,160-project Android Repo manifest.
- `porting-handoff-repo-status.txt`: Repo's human-readable dirty status.
- `porting-handoff-repo-porcelain.txt`: exact tracked and untracked paths in
  eight dirty managed projects.
- `porting-handoff-repo-diff.patch`: complete tracked uncommitted patch.
- `porting-handoff-untracked-files.tar.gz`: contents of all 72 untracked files
  inside managed projects, with path list and SHA-256 manifest alongside it.
- `porting-handoff-prodx-files.sha256`: all 201 standalone ProdX files.
- `porting-handoff-framework-prodx-files.sha256`: the 41 framework contract
  files under `android/app/prodx`.

Recorded SHA-256 values:

```text
manifest:     ebdce4ba5ebff4d7b2269f13f94884f63572f70b3a86f1304107010a75da1da4
repo status:  04e287ae15ba629de513244186253c606ba63eac6794ab6c7e71ab9da71e37e5
repo diff:    51e8d407f64804406219aac6dbf1e5c0e3922e8da410bfffb88c98aa2ad6bd46
porcelain:    e869b8ec3639837538d288028b25047a21e1252b558718192662f126a192a4f7
ProdX list:   197cd334678b51ab9def3bfd818612f186d7dc0ab02bbb20ab79f63f107dddc6
framework:    d43f6879773ca9818aa8a5a972773552a79d233c47396b1cd311130672c40f9a
untracked:    7b79edcd1223b48a6c3aa21e79670b0f27a813b9a5588898dca67c61f4a09c82
untracked tgz:5f9452e943af8687f11211b0b9066a8c46aca9612a43aeabe0bba2e44cb2066f
test APK:     d4b699b9e052892904a3375fa3584fbb6bccb094404e5ed14f9f4b4f0020f85a
```

The tracked patch currently spans bionic, three external projects,
frameworks/base, and six deleted build-tools symlinks. New ProdX framework,
SystemUI, Settings, permission, service, and sepolicy files are preserved in the
managed-untracked mirror. ProdX itself is not a Repo-manifest project, so its
full 201-file mirror is stored separately.

## Local checkpoint

Upstream source trees under `baseline/` and `work/HomeLauncher-repo/` are pinned Git submodules. Generated Gradle caches and nested Git object databases are not copied into the parent history. The latest ROM ZIP is stored through Git LFS.

Latest ROM:

```text
waterlily-i001d-reconstruction/artifacts/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713-cpu-sepolicy.zip
size: 1,482,821,089 bytes
SHA-256: 967be99ea20d2114ab2e00e63c99fc4ecbc521b2efe59921186d2f00fe095844
```

The original physical-file inventory and nested-repository identities are under `waterlily-i001d-reconstruction/handoff/local-current/`.

On the original workstation, parent `git status` reports the kernel submodule as modified (`m`). This is expected: that baseline clone retains its Git object database and pinned HEAD, but its index/worktree was intentionally emptied during earlier cleanup. The parent commit still pins `368dd4099045c66ae294f4a9d3717d615920c329`. A fresh recursive clone will populate a clean kernel submodule at that commit.

## Mandatory operating rules

- Agents modify the VM directly when a source change is authorized.
- Agents do not start a long Android build unless the user explicitly requests it.
- When validation needs a build, give the exact commands to the user and wait for the result.
- Preserve unrelated dirty VM files.
- Update `progress.md`, the active milestone report, and the live milestone state after each material change.
- Do not begin a later ProdX milestone merely because the previous milestone is complete.

## Current next action

The corrected ProdX SystemServer lifecycle integration passed `m services`
(1089/1089 in 1:50). Continue interactive capability testing in
`ProdXCapabilityActivity` and record the result of every spinner item. Do not
flash a new OS merely to run the standalone test APK; the API-36 rebuild is
already installed on the current Android 16 device.
