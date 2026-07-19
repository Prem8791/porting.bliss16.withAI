# How to Build a VM Workspace to Build My Current ROM

This guide rebuilds the source workspace for the current Waterlily ASUS I001D
Bliss Android 16 ROM from a fresh Linux VM.

## What This Restores

This restores the source state used for the successful build:

- ROM: Bliss Android 16 / BP4A
- Device: ASUS I001D / Waterlily
- Product: `bliss_I001D-bp4a-userdebug`
- Build command: `blissify -g I001D`
- Backup owner: `Prem8791`

Known boundary: this is not a fully open-source device port. It preserves the
current reconstructed source plus the practical hard boundaries: proprietary
vendor blobs and prebuilt kernel/modules.

## VM Requirements

Use a Linux VM suitable for Android builds:

- Ubuntu 22.04 or similar
- 16 CPU cores recommended
- 64 GB RAM recommended
- 500 GB or more free disk space
- Git, curl, Python 3, Java/JDK, repo tool, Android build packages

Install the usual Android build tools:

```bash
sudo apt update
sudo apt install -y git-core gnupg flex bison build-essential zip curl zlib1g-dev \
  libc6-dev-i386 x11proto-core-dev libx11-dev lib32z1-dev libgl1-mesa-dev \
  libxml2-utils xsltproc unzip fontconfig python3
```

Install `repo` if missing:

```bash
mkdir -p ~/bin
curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
chmod a+x ~/bin/repo
export PATH=~/bin:$PATH
```

## Sync Bliss Android 16 Base

Create the workspace:

```bash
mkdir -p ~/android/waterlily
cd ~/android/waterlily
```

Initialize the same Bliss Android 16/BP4A branch used by the current build.
Use the Bliss manifest/branch appropriate for Bliss 19.6 Android 16 BP4A.

Example shape:

```bash
repo init -u https://github.com/BlissRoms/platform_manifest.git -b bp4a
```

If Bliss uses a more specific Android 16 branch name, use that exact branch
instead of `bp4a`.

## Add My Waterlily Restore Manifest

This is the important part. It pulls the device/vendor/kernel/reconstructed
pieces from my GitHub account and pins them to the known-good commits.

```bash
mkdir -p .repo/local_manifests
curl -L https://raw.githubusercontent.com/Prem8791/waterlily-i001d-a16-manifest/main/local_manifests/waterlily-i001d-a16.xml \
  -o .repo/local_manifests/waterlily-i001d-a16.xml
repo sync -c --force-sync --no-clone-bundle --no-tags
```

The manifest restores:

- `device/asus/I001D`
- `device/asus/sm8150-common`
- `vendor/asus/I001D`
- `vendor/asus/sm8150-common`
- `kernel/asus/I001D`
- `hardware/asus`
- `packages/modules/UprobeStats`
- `packages/modules/common`
- `packages/apps/HomeLauncher`

## Build

After sync completes:

```bash
cd ~/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
blissify -g I001D
```

Expected output shape:

```text
out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-gapps-*.zip
```

Known successful build from the original VM:

```text
Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260719.zip
SHA256: 1da25ee8c3e0d69ce00b9220f8bf06d3e9bf9de20720701aa823f618d1e5a1e7
Size: 1.4G
```

## If Soong Complains About APEX Allowed Deps

Run:

```bash
cd ~/android/waterlily
source build/envsetup.sh
lunch bliss_I001D-bp4a-userdebug
packages/modules/common/build/update-apex-allowed-deps.sh
```

Then rebuild:

```bash
blissify -g I001D
```

## If `out/.lock` Blocks the Build

That usually means another Soong/build process is still using the same output
directory.

Check first:

```bash
ps aux | grep -E 'soong|ninja|m --|blissify' | grep -v grep
```

If no real build is running, remove the stale lock:

```bash
rm -f out/.lock
```

Then retry the build.

## Important GitHub Entry Point

Restore manifest:

```text
https://github.com/Prem8791/waterlily-i001d-a16-manifest
```

That one repo is the map back to the current ROM source workspace.
