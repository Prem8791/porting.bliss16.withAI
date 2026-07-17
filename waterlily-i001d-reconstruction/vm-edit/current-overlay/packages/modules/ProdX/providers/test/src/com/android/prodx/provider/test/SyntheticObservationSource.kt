package com.android.prodx.provider.test

import com.android.prodx.contract.objects.EventRecord
import java.util.UUID

data class StructuredSyntheticRecord(
    val recordId: String,
    val sourceId: String,
    val eventType: String,
    val timestamp: Long,
    val sequenceNumber: Long,
    val payload: Map<String, Any?>,
    val metadata: Map<String, String> = emptyMap(),
) {
    fun toByteArray(): ByteArray = buildString {
        append("{\"recordId\":\"$recordId\"")
        append(",\"sourceId\":\"$sourceId\"")
        append(",\"eventType\":\"$eventType\"")
        append(",\"timestamp\":$timestamp")
        append(",\"sequenceNumber\":$sequenceNumber")
        append(",\"payload\":{")
        append(payload.entries.joinToString(",") { (k, v) ->
            "\"$k\":\"$v\""
        })
        append("}")
        if (metadata.isNotEmpty()) {
            append(",\"metadata\":{")
            append(metadata.entries.joinToString(",") { (k, v) ->
                "\"$k\":\"$v\""
            })
            append("}")
        }
        append("}")
    }.toByteArray(Charsets.UTF_8)

    fun toEventRecord(source: String): EventRecord = EventRecord(
        eventId = recordId,
        eventType = eventType,
        timestamp = timestamp,
        source = source,
        subject = sourceId,
        payload = payload,
        metadata = metadata,
    )
}

class SyntheticObservationSource(val sourceId: String = "test.synthetic") {
    private var sequenceCounter: Long = 0L

    fun generateEvent(eventType: String = "synthetic.ping", includeMetadata: Boolean = false): ByteArray {
        sequenceCounter++
        val record = StructuredSyntheticRecord(
            recordId = UUID.randomUUID().toString(),
            sourceId = sourceId,
            eventType = eventType,
            timestamp = System.currentTimeMillis(),
            sequenceNumber = sequenceCounter,
            payload = buildMap {
                put("message", "Synthetic observation #$sequenceCounter from $sourceId")
                put("randomValue", (1000..9999).random().toString())
            },
            metadata = if (includeMetadata) {
                mapOf(
                    "generator" to "SyntheticObservationSource",
                    "sourceVersion" to "1.0",
                )
            } else emptyMap(),
        )
        return record.toByteArray()
    }

    fun generateStructuredRecord(eventType: String = "synthetic.ping"): StructuredSyntheticRecord {
        sequenceCounter++
        return StructuredSyntheticRecord(
            recordId = UUID.randomUUID().toString(),
            sourceId = sourceId,
            eventType = eventType,
            timestamp = System.currentTimeMillis(),
            sequenceNumber = sequenceCounter,
            payload = mapOf(
                "message" to "Synthetic observation #$sequenceCounter from $sourceId",
                "randomValue" to (1000..9999).random().toString(),
            ),
        )
    }

    fun generateBatch(count: Int, eventType: String = "synthetic.ping"): List<ByteArray> =
        (1..count).map { generateEvent(eventType) }

    fun resetSequence() {
        sequenceCounter = 0L
    }

    fun getCurrentSequenceNumber(): Long = sequenceCounter
}
