package com.android.prodx.runtime.audit

class RetentionManager(private val redactor: PrivacyRedactor = PrivacyRedactor()) {
    fun appendTombstone(
        ledger: AppendOnlyLedger,
        reservationId: String,
        userId: Int,
        timestampMillis: Long,
        reason: String,
    ): Boolean = ledger.append(
        reservationId,
        timestampMillis,
        userId,
        "TOMBSTONE",
        redactor.minimize(reason.toByteArray(Charsets.UTF_8)),
    ) != null
}
