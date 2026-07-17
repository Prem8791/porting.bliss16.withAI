package com.android.prodx.runtime.observation

import android.os.DeadObjectException
import android.os.RemoteException
import android.util.Log
import com.android.prodx.contract.objects.EventRecord

data class ConsumerEndpoint(
    val leaseId: String,
    val consumerToken: String,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 100
) {
    var consecutiveFailures: Int = 0
    var isAlive: Boolean = true
}

class ConsumerDeliveryManager(
    private val leaseManager: LeaseManager
) {
    private val consumers = mutableMapOf<String, MutableList<ConsumerEndpoint>>()
    private val deliveryLog = mutableListOf<DeliveryRecord>()

    data class DeliveryRecord(
        val eventId: String,
        val leaseId: String,
        val timestamp: Long,
        val success: Boolean,
        val attempt: Int,
        val error: String? = null
    )

    fun registerConsumer(leaseId: String, consumerToken: String): Boolean {
        val lease = leaseManager.getLease(leaseId) ?: return false
        if (lease.isExpired) return false

        val endpoint = ConsumerEndpoint(
            leaseId = leaseId,
            consumerToken = consumerToken
        )
        consumers.getOrPut(leaseId) { mutableListOf() }.add(endpoint)
        return true
    }

    fun unregisterConsumer(leaseId: String, consumerToken: String): Boolean {
        val entries = consumers[leaseId] ?: return false
        val removed = entries.removeAll { it.consumerToken == consumerToken }
        if (entries.isEmpty()) consumers.remove(leaseId)
        return removed
    }

    fun deliverToAll(event: EventRecord): Boolean {
        var allDelivered = true
        val affectedLeases = leaseManager.getLeasesForSource(event.source)

        for (lease in affectedLeases) {
            val entries = consumers[lease.id] ?: continue
            if (lease.isExpired) {
                entries.forEach { it.isAlive = false }
                continue
            }

            val iterator = entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val delivered = deliverWithRetry(entry, event)
                if (!delivered) {
                    if (entry.consecutiveFailures >= entry.maxRetries) {
                        entry.isAlive = false
                        iterator.remove()
                    }
                    allDelivered = false
                } else {
                    entry.consecutiveFailures = 0
                }
            }

            if (entries.isEmpty()) consumers.remove(lease.id)
        }

        return allDelivered
    }

    fun deliverToConsumer(leaseId: String, consumerToken: String, event: EventRecord): Boolean {
        val entries = consumers[leaseId] ?: return false
        val entry = entries.find { it.consumerToken == consumerToken } ?: return false

        val lease = leaseManager.getLease(leaseId)
        if (lease == null || lease.isExpired) {
            entry.isAlive = false
            entries.remove(entry)
            if (entries.isEmpty()) consumers.remove(leaseId)
            return false
        }

        return deliverWithRetry(entry, event)
    }

    fun getAliveConsumerCount(): Int {
        return consumers.values.flatten().count { it.isAlive }
    }

    fun getDeadConsumerCount(): Int {
        return consumers.values.flatten().count { !it.isAlive }
    }

    fun getDeliveryLog(): List<DeliveryRecord> = deliveryLog.toList()

    fun getDeliveryLogForEvent(eventId: String): List<DeliveryRecord> =
        deliveryLog.filter { it.eventId == eventId }

    fun reset() {
        consumers.clear()
        deliveryLog.clear()
    }

    private fun deliverWithRetry(entry: ConsumerEndpoint, event: EventRecord): Boolean {
        var attempt = 0
        while (attempt < entry.maxRetries) {
            attempt++
            try {
                logDelivery(event.eventId, entry.leaseId, attempt, true)
                entry.consecutiveFailures = 0
                return true
            } catch (e: DeadObjectException) {
                Log.w("ConsumerDeliveryManager", "Consumer dead for lease ${entry.leaseId}", e)
                entry.consecutiveFailures++
                entry.isAlive = false
                logDelivery(event.eventId, entry.leaseId, attempt, false, e.message)
                return false
            } catch (e: RemoteException) {
                Log.w("ConsumerDeliveryManager", "Remote exception delivering to ${entry.leaseId}", e)
                entry.consecutiveFailures++
                logDelivery(event.eventId, entry.leaseId, attempt, false, e.message)
                if (attempt < entry.maxRetries) {
                    Thread.sleep(entry.retryDelayMs * attempt)
                }
            }
        }
        return false
    }

    private fun logDelivery(eventId: String, leaseId: String, attempt: Int, success: Boolean, error: String? = null) {
        deliveryLog.add(
            DeliveryRecord(
                eventId = eventId,
                leaseId = leaseId,
                timestamp = System.currentTimeMillis(),
                success = success,
                attempt = attempt,
                error = error
            )
        )
    }
}
