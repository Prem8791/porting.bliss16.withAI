package com.android.prodx.contract.objects

import com.android.prodx.contract.ContentHash
import com.android.prodx.contract.ContractVersion

data class ObservationRecord(
    val observationId: String,
    val observationType: String,
    val timestamp: Long,
    val observer: String,
    val subject: String,
    val payload: Map<String, Any?>,
    val schemaHash: ContentHash?,
    val contractVersion: ContractVersion = ContractVersion.LATEST,
    val confidence: Double? = null,
    val ttlMs: Long? = null
) {
    companion object {
        const val DEFAULT_TTL_MS = 300_000L
    }

    val isExpired: Boolean
        get() = ttlMs?.let { System.currentTimeMillis() - timestamp > it } ?: false

    fun toEnvelopeMap(): Map<String, Any?> = buildMap {
        put("observation_id", observationId)
        put("observation_type", observationType)
        put("timestamp", timestamp)
        put("observer", observer)
        put("subject", subject)
        put("payload", payload)
        put("contract_version", contractVersion.toString())
        schemaHash?.let { put("schema_hash", it.value) }
        confidence?.let { put("confidence", it) }
    }
}
