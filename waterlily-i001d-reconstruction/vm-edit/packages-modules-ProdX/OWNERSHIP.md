# Ownership boundaries

This file records accountable roles without inventing personal identities. A conventional AOSP `OWNERS` file will be added only after the project assigns valid reviewer accounts.

| Boundary | Accountable role | Required reviewers |
|---|---|---|
| Repository root, build graph, release metadata | Runtime Platform Owner | Build Owner, Release Owner |
| `framework/contract` | Contract Owner | Runtime Platform Owner, Security Owner |
| `framework/api` and `sdk/` | API/SDK Owner | Contract Owner, Compatibility Owner |
| `service/broker` | Broker Owner | Authority/Policy Owner, Security Owner |
| `service/observation` | Observation Owner | Privacy Owner, Security Owner |
| `service/audit` | Audit Owner | Security Owner, Privacy Owner |
| `service/extension` | Extension Owner | Security Owner, Compatibility Owner |
| `service/learning` and `service/reasoning` | Post-P0 Model Runtime Owner | Privacy Owner, Security Owner |
| `providers/` | Provider Framework Owner | Domain Provider Owner, Security Owner |
| `apex/` | Release/Packaging Owner | Build Owner, Compatibility Owner |
| `sepolicy/` | SELinux Owner | Security Owner, subsystem owner |
| `tests/` | Verification Owner | relevant subsystem owner |
| `docs/` | Architecture Steward | document owner |

No ownership boundary grants authority to bypass Android permissions, AppOps, SELinux, package identity, user consent, or hardware policy.
