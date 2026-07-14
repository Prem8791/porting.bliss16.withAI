# State handoff

`vm-current/` records the exact recoverable Android VM state: pinned Repo manifest, tracked status, complete tracked diff, and the canonical 61-file ProdX checksum list.

`local-current/` records the physical workstation inventory and nested repository identities at the Git checkpoint. The parent repository stores operational project files, pins upstream repositories as submodules, and stores the latest ROM using Git LFS.
