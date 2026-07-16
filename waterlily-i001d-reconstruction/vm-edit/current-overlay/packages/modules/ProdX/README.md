# ProdX Runtime

This directory is the permanent AOSP module root for the ProdX capability runtime. P0-01 contains an inert repository skeleton only: governance metadata, documentation, reserved source boundaries, and non-installable Soong aggregation.

No file in this milestone implements runtime logic, starts a process, registers a service, defines Binder IPC, installs a package, changes SELinux policy, declares a permission, or alters a product configuration.

## Authoritative references

The frozen P0-00 reference set is under `tests/reference/`. Its `BASELINE-FILES.sha256` manifest must verify before later milestone work begins. The live milestone state is maintained in `MILESTONES.md`; locked P0-00 files are never edited to record later progress.

## Namespace reservations

The reserved Java/Kotlin namespace is `com.android.prodx`. Subsystem namespaces are `com.android.prodx.framework`, `.service`, `.sdk`, `.provider`, and `.tests`. No source package is created in P0-01.

The canonical Soong target prefix is `prodx-`; platform component names reserved by the specifications retain their approved `ProdX` form. See `TARGETS.md`.

## Build participation

`prodx-p0` is an empty phony aggregate. It has no required modules, emits no artifact, and is absent from every product package list. Repository documentation remains ordinary source-tree metadata and is deliberately not modeled as a build dependency.

Read `DIRECTORY-MAP.md`, `OWNERSHIP.md`, `CONTRIBUTING.md`, and `docs/engineering/P0-01-IMPLEMENTATION-REPORT.md` before changing this tree.
