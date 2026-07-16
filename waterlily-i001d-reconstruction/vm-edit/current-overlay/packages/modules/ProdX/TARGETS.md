# Target ownership and reservations

## Declared in P0-01

| Target | Type | Owner | Output/install effect |
|---|---|---|---|
| `prodx-p0` | empty `phony` aggregate | Runtime Platform Owner | none |

The target has no required modules and is not referenced by a product, partition, APEX, application, service, classpath, bootclasspath, init file, or compatibility matrix.

## Reserved, not declared

The approved names `prodx-contract-test-vectors`, `ProdXContractTests`, `prodx-contract-runtime`, `prodx-framework-api`, `prodx-authority-service`, `ProdXBrokerService`, `ProdXObservationService`, `ProdXAuditService`, `ProdXExtensionService`, `prodx-provider-sdk`, `ProdXNoOpTestProvider`, and `com.android.prodx` remain documentary reservations. Their declaring milestones must satisfy their own gates before adding build modules.

Provider-family targets, model/runtime targets, product packages, and release aggregates are post-P0-01 and must not become dependencies of `prodx-p0` in this milestone.

## Visibility rule

The root package defaults to subtree-only visibility. Any future exception or dependency requires a requirement-linked visibility review and a dependency-cycle check.
