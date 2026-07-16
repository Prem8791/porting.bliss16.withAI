package com.android.prodx.runtime.audit

import java.util.UUID

class AppendOnlyLedger(private val backend: LedgerBackend) {
    private val records = mutableListOf<LedgerRecord>()
    var health: AuditHealth = AuditHealth.HEALTHY
        private set

    init {
        val recovery = backend.recover()
        health = recovery.health
        var previousHash = LedgerHashChain.GENESIS_HASH
        recovery.frames.forEachIndexed { index, encoded ->
            try {
                val record = LedgerHashChain.decode(encoded)
                if (!LedgerHashChain.verify(record, index.toLong() + 1, previousHash)) {
                    health = AuditHealth.CORRUPT
                    return@forEachIndexed
                }
                records += record
                previousHash = record.recordHash
            } catch (_: RuntimeException) {
                health = AuditHealth.CORRUPT
            }
        }
    }

    @Synchronized
    fun append(
        reservationId: String,
        timestampMillis: Long,
        userId: Int,
        action: String,
        minimizedContent: ByteArray,
    ): LedgerRecord? {
        if (health != AuditHealth.HEALTHY) return null
        val previousHash = records.lastOrNull()?.recordHash ?: LedgerHashChain.GENESIS_HASH
        val record = LedgerHashChain.create(
            records.size.toLong() + 1,
            UUID.randomUUID().toString(),
            reservationId,
            timestampMillis,
            userId,
            action,
            minimizedContent,
            previousHash,
        )
        if (!backend.append(LedgerHashChain.encode(record))) {
            health = AuditHealth.READ_ONLY
            return null
        }
        records += record
        return record
    }

    @Synchronized
    fun snapshot(): List<LedgerRecord> = records.toList()
}
