# State handoff

`vm-current/` records the exact recoverable Android VM state captured on
2026-07-15. It includes the pinned Repo manifest, human-readable and porcelain
status, the complete tracked patch, inventories and SHA-256 manifests for new
files, and a compressed archive of every untracked file inside a managed Repo
project.

The corresponding source mirrors are:

- `../vm-edit/packages-modules-ProdX/`: all 201 files in the standalone ProdX
  module tree;
- `../vm-edit/managed-repo-untracked/`: all 72 untracked files beneath managed
  projects, preserving their Android-root-relative paths; and
- `../artifacts/ProdXNoOpTestProvider-api36-20260715.apk`: the rebuilt API-36
  capability test APK.

`local-current/` records the physical workstation inventory and nested
repository identities at the earlier Git checkpoint. The parent repository
stores operational project files, pins upstream repositories as submodules,
and stores the latest ROM using Git LFS.
