package com.android.prodx.tests.fixtures

import com.android.prodx.runtime.observation.BackpressureController
import com.android.prodx.runtime.observation.ConsumerDeliveryManager
import com.android.prodx.runtime.observation.EventPipeline
import com.android.prodx.runtime.observation.HubHealth
import com.android.prodx.runtime.observation.HubShellCommand
import com.android.prodx.runtime.observation.IncidentRecord
import com.android.prodx.runtime.observation.LeaseManager
import com.android.prodx.runtime.observation.ObservationQueue
import com.android.prodx.runtime.observation.RedactionPipeline
import com.android.prodx.runtime.observation.RuleEngine
import com.android.prodx.runtime.observation.SecurityMonitor
import com.android.prodx.runtime.observation.SourceRegistry

class FakeObservationHub {
    val leaseManager = LeaseManager()
    val sourceRegistry = SourceRegistry()
    val observationQueue = ObservationQueue(maxSize = 100)
    val redactionPipeline = RedactionPipeline()
    val backpressureController = BackpressureController(
        globalMaxEventsPerSecond = 1000,
        defaultMaxPerSource = 100,
        defaultMaxPerConsumer = 200
    )
    val ruleEngine = RuleEngine()
    val securityMonitor = SecurityMonitor(ruleEngine)
    val consumerDeliveryManager = ConsumerDeliveryManager(leaseManager)
    val eventPipeline = EventPipeline(
        redactionPipeline = redactionPipeline,
        observationQueue = observationQueue,
        consumerDeliveryManager = consumerDeliveryManager,
        backpressureController = backpressureController,
        securityMonitor = securityMonitor
    )
    val hubShellCommand = HubShellCommand(
        leaseManager = leaseManager,
        sourceRegistry = sourceRegistry,
        observationQueue = observationQueue,
        backpressureController = backpressureController,
        eventPipeline = eventPipeline,
        securityMonitor = securityMonitor,
        hubHealth = { getHealth() }
    )

    private var running = true
    private val incidents = mutableListOf<IncidentRecord>()

    fun isRunning(): Boolean = running

    fun setRunning(state: Boolean) { running = state }

    fun reset() {
        leaseManager.reset()
        sourceRegistry.reset()
        observationQueue.clear()
        redactionPipeline.reset()
        backpressureController.reset()
        securityMonitor.reset()
        incidents.clear()
        running = true
    }

    fun addIncident(incident: IncidentRecord) {
        incidents.add(incident)
    }

    fun getIncidents(): List<IncidentRecord> = incidents.toList()

    fun getHealth(): HubHealth = HubHealth(
        operational = running,
        queueDepth = observationQueue.size(),
        activeLeaseCount = leaseManager.activeLeaseCount(),
        activeSourceCount = sourceRegistry.activeSourceCount(),
        unacknowledgedIncidents = incidents.count { !it.acknowledged }
    )
}
