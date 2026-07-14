# Deterministic Agent Takeover

## Current outcome

P0-00 and P0-01 of the ProdX Runtime are complete. P0-02 is `NOT_STARTED` and must not begin without explicit user authorization.

P0-01 created an inert repository skeleton only. It contains no runtime service, Binder interface, provider, SELinux rule, permission, feature flag, manifest, executable code, or installed artifact. The user successfully ran both `m prodx-p0` and the `m nothing` control on 2026-07-14.

## Read first

1. Root `AGENTS.md`.
2. `waterlily-i001d-reconstruction/progress.md`, especially checkpoints 31–33.
3. `waterlily-i001d-reconstruction/vm-edit/packages-modules-ProdX/README.md`.
4. That tree's `MILESTONES.md` and `docs/engineering/P0-01-IMPLEMENTATION-REPORT.md`.
5. Its immutable `tests/reference/README.md`, `REFERENCE-LOCK.md`, and P0 implementation specification.

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

Connect with:

```powershell
gcloud compute ssh instance-20260710-230647 --project=customrom-501702 --zone=us-south1-a
```

## Exact VM checkpoint

The recoverable VM checkpoint is in `waterlily-i001d-reconstruction/handoff/vm-current/`:

- `porting-handoff-manifest.xml`: pinned 1,160-project Android Repo manifest.
- `porting-handoff-repo-status.txt`: exact tracked dirty-project status.
- `porting-handoff-repo-diff.patch`: complete tracked uncommitted patch.
- `porting-handoff-prodx-files.sha256`: all 61 ProdX files and hashes.

Recorded SHA-256 values:

```text
manifest:     ebdce4ba5ebff4d7b2269f13f94884f63572f70b3a86f1304107010a75da1da4
repo status:  13d2b1e92e2bb4dbbfc9c77002937ff97834ca2b54e0452f811c3e54a197e31e
repo diff:    0e0eb40275a2e70fd007227b75e08353f4c1072789cc1488bce07be62aba5bef
ProdX list:   5bbd8c896b5c817075eb7dbb95c9a8bb12437d6ead20adc9efe97bfa16c75e07
```

The tracked VM patch currently contains only the `bionic/libc/include/sched.h` guard and six deleted build-tools `date`/`tar` symlinks. ProdX is not a Repo-manifest project yet, so its full 61-file mirror and checksum manifest are stored separately.

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

There is no authorized implementation action after this checkpoint. Wait for the user to authorize P0-02 or another clearly scoped task.
