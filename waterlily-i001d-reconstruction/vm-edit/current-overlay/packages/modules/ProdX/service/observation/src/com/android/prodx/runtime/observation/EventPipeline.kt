package com.android.prodx.runtime.observation

import com.android.prodx.contract.objects.EventRecord
import com.android.prodx.contract.objects.ObservationRecord

enum class PipelineStage {
    RECEIVE, VALIDATE, REDACT, QUEUE, DELIVER
}

data class PipelineResult(
    val success: Boolean,
    val stage: PipelineStage,
    val event: EventRecord? = null,
    val error: String? = null,
    val gapDetected: Boolean = false,
    val droppedMarker: Boolean = false
)

class EventPipeline(
    private val redactionPipeline: RedactionPipeline,
    private val observationQueue: ObservationQueue,
    private val consumerDeliveryManager: ConsumerDeliveryManager,
    private val backpressureController: BackpressureController,
    private val securityMonitor: SecurityMonitor
) {
    private var lastSequenceNumber: Long? = null
    private var gapDetected = false

    fun process(event: EventRecord): PipelineResult {
        val receiveResult = stageReceive(event)
        if (!receiveResult.success) return receiveResult

        val validateResult = stageValidate(event)
        if (!validateResult.success) return validateResult

        val redactResult = stageRedact(event)
        if (!redactResult.success) return redactResult

        val queueResult = stageQueue(redactResult.event!!)
        if (!queueResult.success) return queueResult

        val deliverResult = stageDeliver(queueResult.event!!)
        return deliverResult
    }

    private fun stageReceive(event: EventRecord): PipelineResult {
        securityMonitor.ingestEvent(
            EventSummary(
                eventId = event.eventId,
                eventType = event.eventType,
                timestamp = event.timestamp,
                source = event.source
            )
        )
        return PipelineResult(success = true, stage = PipelineStage.RECEIVE, event = event)
    }

    private fun stageValidate(event: EventRecord): PipelineResult {
        if (event.eventId.isBlank()) {
            return PipelineResult(success = false, stage = PipelineStage.VALIDATE, error = "Blank event ID")
        }
        if (event.source.isBlank()) {
            return PipelineResult(success = false, stage = PipelineStage.VALIDATE, error = "Blank source")
        }
        if (event.timestamp <= 0 || event.timestamp > System.currentTimeMillis() + 5000) {
            return PipelineResult(success = false, stage = PipelineStage.VALIDATE, error = "Invalid timestamp")
        }

        val seq = event.sequenceNumber
        if (seq != null && lastSequenceNumber != null) {
            if (seq <= lastSequenceNumber!!) {
                return PipelineResult(success = false, stage = PipelineStage.VALIDATE, error = "Duplicate or out-of-order sequence")
            }
            if (seq > lastSequenceNumber!! + 1) {
                gapDetected = true
                return PipelineResult(success = true, stage = PipelineStage.VALIDATE, event = event, gapDetected = true)
            }
        }
        if (seq != null) lastSequenceNumber = seq

        return PipelineResult(success = true, stage = PipelineStage.VALIDATE, event = event)
    }

    private fun stageRedact(event: EventRecord): PipelineResult {
        val redacted = redactionPipeline.redact(event)
        return PipelineResult(success = true, stage = PipelineStage.REDACT, event = redacted)
    }

    private fun stageQueue(event: EventRecord): PipelineResult {
        val allowed = backpressureController.isAllowed(event.source)
        if (!allowed) {
            val dropped = backpressureController.recordDropped(event.source)
            return PipelineResult(
                success = false,
                stage = PipelineStage.QUEUE,
                event = event,
                error = "Backpressure limit exceeded",
                droppedMarker = true
            )
        }

        val serialized = serializeEvent(event)
        val enqueued = observationQueue.enqueue(serialized, event.source)
        if (!enqueued) {
            return PipelineResult(
                success = false,
                stage = PipelineStage.QUEUE,
                event = event,
                error = "Queue full; oldest dropped with gap marker",
                droppedMarker = true
            )
        }

        backpressureController.recordEvent(event.source)
        return PipelineResult(success = true, stage = PipelineStage.QUEUE, event = event)
    }

    private fun stageDeliver(event: EventRecord): PipelineResult {
        val delivered = consumerDeliveryManager.deliverToAll(event)
        return PipelineResult(
            success = delivered,
            stage = PipelineStage.DELIVER,
            event = event,
            error = if (delivered) null else "Delivery failed for some consumers"
        )
    }

    fun isGapDetected(): Boolean = gapDetected

    fun resetGapDetection() { gapDetected = false }

    fun reset() {
        lastSequenceNumber = null
        gapDetected = false
        observationQueue.clear()
        backpressureController.reset()
    }

    private fun serializeEvent(event: EventRecord): ByteArray {
        return event.toEnvelopeMap().toString().encodeToByteArray()
    }
}
