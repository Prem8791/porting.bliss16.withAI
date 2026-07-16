# ProdX Audit service

P0-07 provides the system-UID-only audit boundary for durable transaction
reservation, phase/outcome recording, cancellation, health, and minimized
history. The ledger is single-writer, append-only, framed and fsynced; every
record is sequence- and hash-chain-bound and caller payloads are reduced to a
bounded SHA-256 digest before persistence. Recovery preserves corrupt or
incomplete evidence and makes the partition fail closed instead of truncating
or resetting it. Raw ledger query, record editing, and deletion are not Binder
or shell surfaces.

`ProdXAuditService` and `ProdXAuditTests` are intentionally not added to a
production product package set until their targeted build and device tests
pass and the Authority binding is integrated.
