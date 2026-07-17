package com.android.prodx.runtime.observation

open class HubShellCommand(
    private val leaseManager: LeaseManager,
    private val sourceRegistry: SourceRegistry,
    private val observationQueue: ObservationQueue,
    private val backpressureController: BackpressureController,
    private val eventPipeline: EventPipeline,
    private val securityMonitor: SecurityMonitor,
    private val hubHealth: HubHealthProvider
) {
    open fun execute(cmd: String): String {
        val parts = cmd.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return "Usage: list_sources | list_leases | health | inject_event <json> | force_drain | reset"

        return when (parts[0]) {
            "list_sources" -> listSources()
            "list_leases" -> listLeases()
            "health" -> reportHealth()
            "inject_event" -> injectEvent(parts.drop(1).joinToString(" "))
            "force_drain" -> forceDrain()
            "reset" -> doReset()
            else -> "Unknown command: ${parts[0]}"
        }
    }

    private fun listSources(): String {
        val sources = sourceRegistry.getAllSources()
        if (sources.isEmpty()) return "No sources registered."
        return sources.joinToString("\n") { it.toSummary() }
    }

    private fun listLeases(): String {
        val leases = leaseManager.getActiveLeases()
        if (leases.isEmpty()) return "No active leases."
        return leases.joinToString("\n") { lease ->
            "${lease.id}: source=${lease.sourceId} consumer=${lease.consumerToken} expires_in=${lease.remainingMs}ms"
        }
    }

    private fun reportHealth(): String {
        val health = hubHealth.getHealth()
        return buildString {
            appendLine("Operational: ${health.operational}")
            appendLine("Queue Depth: ${health.queueDepth}")
            appendLine("Active Leases: ${health.activeLeaseCount}")
            appendLine("Active Sources: ${health.activeSourceCount}")
            appendLine("Event Throughput: ${"%.1f".format(health.eventThroughput)}/s")
            appendLine("Total Dropped: ${health.totalDropped}")
            appendLine("Watermark High: ${health.watermarkHigh}")
            appendLine("Unacknowledged Incidents: ${health.unacknowledgedIncidents}")
            health.lastError?.let { appendLine("Last Error: $it") }
            append("Uptime: ${health.uptimeMs}ms")
        }
    }

    private fun injectEvent(jsonSpec: String): String {
        if (jsonSpec.isBlank()) return "Usage: inject_event <json>"
        return "Event injected (simulated). Pipeline processing: ${eventPipeline.hashCode()}"
    }

    private fun forceDrain(): String {
        var drained = 0
        while (true) {
            val entry = observationQueue.dequeue() ?: break
            drained++
        }
        return "Drained $drained entries from queue."
    }

    private fun doReset(): String {
        leaseManager.reset()
        sourceRegistry.reset()
        observationQueue.clear()
        backpressureController.reset()
        eventPipeline.reset()
        securityMonitor.reset()
        return "Observation hub reset complete."
    }
}

fun interface HubHealthProvider {
    fun getHealth(): HubHealth
}
