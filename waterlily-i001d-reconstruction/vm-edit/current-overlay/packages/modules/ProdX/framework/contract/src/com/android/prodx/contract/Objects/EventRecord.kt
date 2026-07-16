package com.android.prodx.contract.objects

import com.android.prodx.contract.ContractVersion

data class EventRecord(
    val eventId: String,
    val eventType: String,
    val timestamp: Long,
    val source: String,
    val subject: String?,
    val payload: Map<String, Any?>,
    val metadata: Map<String, String> = emptyMap(),
    val contractVersion: ContractVersion = ContractVersion.LATEST,
    val sequenceNumber: Long? = null,
    val parentEventId: String? = null
) {
    fun toEnvelopeMap(): Map<String, Any?> = buildMap {
        put("event_id", eventId)
        put("event_type", eventType)
        put("timestamp", timestamp)
        put("source", source)
        subject?.let { put("subject", it) }
        put("payload", payload)
        put("contract_version", contractVersion.toString())
        if (metadata.isNotEmpty()) put("metadata", metadata)
        sequenceNumber?.let { put("sequence_number", it) }
        parentEventId?.let { put("parent_event_id", it) }
    }
}
