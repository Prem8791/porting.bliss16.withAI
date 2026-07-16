# Immutable Reference Lock

Baseline generation: `BL-20260714-01`  
Lock state: `LOCKED`  
Hash algorithm: SHA-256

## Authoritative artifacts

| ID | Artifact | SHA-256 | Size | Authority |
|---|---|---|---:|---|
| REF-FOUNDATION | `ProdX-Runtime-Architecture-Foundation-20260714.zip` | `ee03e61c7ffa77c7d0dedf8d5b1e69a972925fd67a5919a83f6e6a8a92c3a2ec` | 5,969,255 | Security invariants, capability evidence, lifecycle and roadmap |
| REF-CONTRACT | `ProdX-Runtime-Contract-Specification-20260714.md` | `7c7743700c468847ef547bbbc22dcc9d1b027df0fee9e92895faee071706d886` | 67,420 | Runtime objects, serialization, versioning and compatibility |
| REF-SKELETON | `ProdX-Runtime-Skeleton-Specification-20260714.md` | `f332bace6730a290dcaff5a7703d8e499eac08dcd309cee63a31248997bffbd6` | 80,073 | Android repositories, processes, domains, boot and update placement |
| REF-P0 | `ProdX-P0-Runtime-Implementation-Specification-20260714.md` | `c3ef5e20208ebc13fdb8bd43e16dae770ecddd9ddcb8a19a8a22d57a00b33152` | 83,860 | P0 targets, milestones, gates, dependencies, tests and rollback |

The copies under `immutable/` must be byte-identical to these hashes. A hash
mismatch blocks all implementation.

## Capability Investigation content-set lock

The Capability Investigation is the curated evidence set inside
`REF-FOUNDATION`, consisting of all manifest entries under:

- `03-catalogs/`
- `04-inventories/`
- `05-runtime-analysis/`
- `06-security-analysis/`
- `07-diagrams/`
- `08-provenance/`
- `09-raw-evidence/`

Its deterministic content-set digest is:

`sha256:dd76ae42def1cf0d1618dfdeda610585adff361b5fd5021fece0dbd884c59a54`

Digest procedure:

1. Read `MANIFEST.json` from `REF-FOUNDATION`.
2. Select the 44 entries whose relative paths begin with one of the roots above.
3. Sort paths by ordinal byte order.
4. Emit one UTF-8/no-BOM line per entry as `<sha256><two spaces><path>\n`.
5. SHA-256 hash the resulting 44-line, LF-terminated byte stream.

Locked set size: `83,876,218` bytes uncompressed across 44 evidence files.
The Foundation archive hash and its internal manifest hashes are both required;
the aggregate digest does not replace either.

## Verification policy

- Verify references before contract generation, interface review, build graph
  changes, integration, release and rollback.
- Never overwrite a locked artifact. Add a new version and architecture change
  record.
- A reference change invalidates impacted decisions, requirements, test vectors,
  interfaces, catalogs and approvals until re-baselined.
- Local paths, timestamps and filenames are not authority; content hashes are.

