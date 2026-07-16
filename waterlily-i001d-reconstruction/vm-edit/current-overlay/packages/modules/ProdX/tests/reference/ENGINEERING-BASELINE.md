# Engineering Baseline

Baseline ID: `BL-20260714-01`  
Captured UTC: `2026-07-14T12:05:53Z`  
VM instance: `instance-20260710-230647`  
Project/zone: `customrom-501702 / us-south1-a`  
Android root: `/home/premanandal1978/android/waterlily`

## Android manifest

| Field | Value |
|---|---|
| Manifest repository | `https://github.com/BlissRoms/stable_releases.git` |
| Manifest branch | `origin/waterlily` |
| Manifest checkout HEAD | `e0b717c9a37b37fb35125e454df92887a419ebdf` |
| Commit subject | `waterlily: Release manifest for BlissRoms v19.6 Waterlily` |
| Repo-managed projects | 1,160 |
| Resolved manifest | `resolved-manifest.xml` |
| Resolved manifest SHA-256 | `ebdce4ba5ebff4d7b2269f13f94884f63572f70b3a86f1304107010a75da1da4` |

`resolved-manifest.xml` is the exact `repo manifest -r` output and is the
authoritative revision list for repo-managed projects.

## Product/build configuration

| Field | Value |
|---|---|
| Product | `bliss_I001D` |
| Device | `I001D` |
| Variant | `userdebug` |
| Build flavor | `bliss_I001D-userdebug` |
| Android release/API | Android 16 / API 36 (`sdk_full 36.1`) |
| Build ID | `BP4A.251205.006` |
| Incremental | `1783952199` |
| Vendor brand/device/model | `asus / I001D / ASUS_I001D` |
| Board | `msmnile` |
| First API level | 28 |
| Last output directory timestamp | `2026-07-13T14:37:59.879320458Z` |
| Last output | `out/target/product/I001D/Bliss-v19.6-I001D-UNOFFICIAL-gapps-20260713.zip` |

The product values were recorded from the existing output property files. P0-00
did not run lunch, Soong, Ninja, or an Android build.

## Host/build environment

| Field | Value |
|---|---|
| OS | Ubuntu Server 22.04.5 LTS (Jammy), x86_64 |
| Kernel | `6.8.0-1063-gcp #69~22.04.1-Ubuntu` |
| CPU/RAM | 12 vCPU / 62 GiB RAM |
| Swap | None |
| Root filesystem at capture | 485 GiB total, 337 GiB used, 149 GiB available |
| Hostname | `instance-20260710-230647` |
| Locale/time zone | `C.UTF-8` / `Etc/UTC`; NTP synchronized |
| Shell | GNU bash 5.1.16 |
| Git | 2.34.1 |
| repo | 2.65, stable branch |
| Python | 3.10.12 |
| Java/Javac | OpenJDK 17.0.19 |
| Java path | `/usr/lib/jvm/java-17-openjdk-amd64/bin/java` |
| GNU Make | 4.3 |
| Ninja | `1.9.0.git` from AOSP prebuilts |
| ccache | 4.5.1 |
| Soong default Clang | `clang-r563880c` |
| Clang | Android 21.0.0, build 14054515, LLVM commit `5e96669f06077099aa41290cdb4c5e6fa0f59349` |
| Rust | 1.88.0-dev, Android toolchain 13951379 |
| Go | 1.24.1 |

## Pre-existing repository state

The checkout was not pristine before P0-00. These differences are explicitly
outside ProdX and must be preserved:

| Project | Pre-existing state |
|---|---|
| `bionic` | Modified `libc/include/sched.h` (2 inserted lines) |
| `prebuilts/build-tools` | Six deleted prebuilt `date`/`tar` files under Darwin, Linux ARM64 and Linux x86 paths |
| `kernel/asus/I001D` | Five modified files: `Makefile`, `arch/arm64/Kconfig`, `arch/arm64/configs/vendor/I001D_defconfig`, `include/uapi/linux/sched/types.h`, `scripts/gcc-wrapper.py` (18 insertions, 2 deletions) |

Repo-managed status before P0-00 is preserved verbatim in `repo-status.txt`
(SHA-256 `13d2b1e92e2bb4dbbfc9c77002937ff97834ca2b54e0452f811c3e54a197e31e`).
Supplemental ASUS repository heads at capture:

| Path | HEAD |
|---|---|
| `device/asus/I001D` | `3af8cd8935a776acfc8c40a6aefb51a7f8dc8fad` |
| `device/asus/sm8150-common` | `cb0326b670753d129571cf84b2630eac1c0cecaf` |
| `vendor/asus/I001D` | `3e26a28c19c50968074ef9e25f3f2a0eabfa87d8` |
| `vendor/asus/sm8150-common` | `b5f485169923f41bf3588df159e1351f5f4edb1b` |
| `kernel/asus/I001D` | `368dd4099045c66ae294f4a9d3717d615920c329` |

## P0-00 allowed delta

Before this milestone, `packages/modules/ProdX` was absent. The only permitted
Android-tree addition is:

`packages/modules/ProdX/tests/reference/`

It may contain Markdown, TSV, JSON, XML, plain-text status, checksum records and
the immutable reference copies. It may not contain source, interface, policy,
manifest, build, resource, binary executable, library, APK, APEX, object, archive
other than the locked evidence ZIP, or generated runtime artifact.

The post-install verification must compare repo status and targeted path
inventories against this baseline and record any deviation as a P0-00 failure.

