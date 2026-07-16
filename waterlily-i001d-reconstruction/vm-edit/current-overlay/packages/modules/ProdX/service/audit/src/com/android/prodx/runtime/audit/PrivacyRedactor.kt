package com.android.prodx.runtime.audit

import java.security.MessageDigest

/** Persists only a bounded content digest; caller payload bytes never enter the ledger. */
class PrivacyRedactor(private val maximumPayloadBytes: Int = 64 * 1024) {
    fun minimize(data: ByteArray): ByteArray {
        require(data.size <= maximumPayloadBytes) { "Audit payload exceeds bounded size" }
        return MessageDigest.getInstance("SHA-256").digest(data)
    }
}
