# State handoff

`vm-current/` records the exact recoverable Android VM state captured on
2026-07-15. It includes the pinned Repo manifest, human-readable and porcelain
status, the complete tracked patch, inventories and SHA-256 manifests for new
files, and a compressed archive of every untracked file inside a managed Repo
project.

The corresponding source mirrors are:

- `../vm-edit/packages-modules-ProdX/`: all 203 files in the standalone ProdX
  module tree;
- `../vm-edit/managed-repo-untracked/`: all 74 untracked files beneath managed
  projects, preserving their Android-root-relative paths; and
- `../artifacts/ProdXNoOpTestProvider-api36-20260715.apk`: the rebuilt API-36
  capability test APK.

The I001D device tree is outside the managed Repo status captured on this VM.
Its current `device.mk` and `FrameworksResOverlay` config are mirrored as
`../vm-edit/device-asus-I001D-device.mk` and
`../vm-edit/device-asus-I001D-FrameworksResOverlay-config.xml`.

`local-current/` records the physical workstation inventory and nested
repository identities at the earlier Git checkpoint. The parent repository
stores operational project files, pins upstream repositories as submodules,
and stores the latest ROM using Git LFS.
