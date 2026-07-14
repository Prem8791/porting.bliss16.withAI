# Workstation state checkpoint

`LOCAL-FILES.sha256.tsv` records the relative path, byte size, and SHA-256 of the physical files under `D:/AndroidProjects/porting` at checkpoint creation. It excludes only the parent repository's live `.git/` administrative directory and the two self-describing files `LOCAL-FILES.sha256.tsv` and `LOCAL-STATE.md`.

Manifest SHA-256:

```text
5e1e7e7b42494748083f12b4d0b5c069095806880df5192c7575f2f5e3aa3510
```

The manifest includes nested Git object databases, ignored build/cache files, baseline proprietary files, the complete mirrored ProdX tree, and the latest ROM, so the original physical workstation can be audited byte-for-byte.

The parent Git history intentionally represents nested upstream repositories as pinned submodules rather than duplicating their `.git` databases. The latest ROM is represented using Git LFS. Generated caches inside the HomeLauncher submodule remain documented by this physical manifest but are not part of the recoverable source checkpoint.
