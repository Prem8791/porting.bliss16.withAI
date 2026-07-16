# State handoff

`vm-current/` and `../vm-edit/current-overlay/` are the canonical recovery
checkpoint for the Android VM state captured on 2026-07-16.

The overlay contains all 326 changed or new source files at their
Android-root-relative paths, including the complete standalone
`packages/modules/ProdX` tree. `vm-current/current-overlay-deletions.txt`
records the six paths intentionally absent from the VM. The per-file SHA-256
manifest is `vm-current/current-overlay.sha256`; the pinned Repo manifest,
project status and capture metadata are stored beside it.

The checkpoint is intentionally source-only. It excludes build output,
temporary transfer archives and superseded partial mirrors/patch fragments.
The latest ROM and test APK remain under `../artifacts/`.

## Restore onto a replacement VM

First create or sync a compatible Bliss Android 16 checkout. Use
`vm-current/resolved-manifest.xml` to audit the exact project revisions used by
this checkpoint. From the workstation repository root, upload the overlay and
deletion list:

```powershell
gcloud compute scp --quiet --recurse waterlily-i001d-reconstruction/vm-edit/current-overlay premanandal1978@INSTANCE:/home/premanandal1978/current-overlay --project=PROJECT --zone=ZONE
gcloud compute scp --quiet waterlily-i001d-reconstruction/handoff/vm-current/current-overlay-deletions.txt premanandal1978@INSTANCE:/home/premanandal1978/current-overlay-deletions.txt --project=PROJECT --zone=ZONE
gcloud compute scp --quiet waterlily-i001d-reconstruction/handoff/vm-current/current-overlay.sha256 premanandal1978@INSTANCE:/home/premanandal1978/current-overlay.sha256 --project=PROJECT --zone=ZONE
```

Then apply them on the VM:

```bash
cd /home/premanandal1978/android/waterlily
cp -a /home/premanandal1978/current-overlay/. ./
while IFS= read -r path; do rm -f -- "$path"; done \
  < /home/premanandal1978/current-overlay-deletions.txt
```

Verify the restored files from the Android root:

```bash
cd /home/premanandal1978/android/waterlily
sha256sum -c /home/premanandal1978/current-overlay.sha256
```

`local-current/` is the earlier workstation/nested-repository inventory.
`../vm-edit/device-asus-sm8150-common/` and
`../vm-edit/hardware-interfaces/` are retained recovery sets because they have
not yet been proven reproducible from the pinned manifest.
