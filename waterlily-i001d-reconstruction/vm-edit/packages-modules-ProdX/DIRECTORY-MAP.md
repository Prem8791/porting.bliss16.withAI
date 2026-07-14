# Repository directory map

| Path | Status in P0-01 | Permanent responsibility |
|---|---|---|
| `framework/contract/` | placeholder | immutable wire-neutral runtime contracts |
| `framework/api/` | placeholder | platform-facing framework API surface |
| `service/broker/` | placeholder | request mediation and dispatch |
| `service/observation/` | placeholder | normalized observation ingestion |
| `service/audit/` | placeholder | append-only security audit boundary |
| `service/extension/` | placeholder | extension discovery and lifecycle |
| `service/learning/` | post-P0 placeholder | learning pipeline; excluded from P0 graph |
| `service/reasoning/` | post-P0 placeholder | model orchestration; excluded from P0 graph |
| `sdk/` | placeholder | provider/consumer SDK contracts |
| `providers/test/` | placeholder | future no-op conformance provider only |
| `apex/` | reserved placeholder | future APEX packaging and release metadata |
| `sepolicy/` | reserved placeholder | future ProdX SELinux policy only |
| `tests/` | hierarchy root | host, device, contract, unit, integration, security, fuzz, and fixtures |
| `tests/reference/` | immutable | P0-00 reference lock and engineering baseline |
| `docs/architecture/` | documentation | mappings and non-authoritative explanatory material |
| `docs/engineering/` | documentation | milestone reports and validation evidence |
| `tools/` | reserved placeholder | future non-production engineering tools |
| `config/` | reserved placeholder | future non-product test/configuration data |

No source-package directory exists in P0-01. Future provider families are reserved conceptually but their directories are not created before their approved milestones. Empty directories are represented by a scope-specific `README.md` so their purpose survives source control.
