# Provenance

P0-01 was derived exclusively from the five immutable inputs locked by P0-00: the Runtime Foundation, Runtime Contract Specification, Runtime Skeleton Specification, P0 Runtime Implementation Specification, and Capability Investigation. Their identities and SHA-256 values are recorded in `tests/reference/REFERENCE-LOCK.md` and `BASELINE-MANIFEST.json`.

The physical source-tree placement is `packages/modules/ProdX`. The declarations follow the local Android 16 Soong conventions observed in `packages/modules/common/Android.bp` and the platform `phony` module syntax. No external source or generated content is incorporated.

P0-00 evidence is copied into `tests/reference` and remains immutable. This document records only P0-01 derivation; it does not supersede the reference lock.
