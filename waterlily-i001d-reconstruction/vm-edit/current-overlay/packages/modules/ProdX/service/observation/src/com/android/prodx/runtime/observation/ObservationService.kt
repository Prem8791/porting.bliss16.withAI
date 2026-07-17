package com.android.prodx.runtime.observation

import android.app.Service
import android.app.prodx.ProdXSubscriptionLease
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.android.prodx.runtime.IProdXObservation

class ObservationService : Service(), HubHealthProvider {
    private lateinit var leaseManager: LeaseManager
    private lateinit var sourceRegistry: SourceRegistry
    private lateinit var observationQueue: ObservationQueue
    private lateinit var redactionPipeline: RedactionPipeline
    private lateinit var backpressureController: BackpressureController
    private lateinit var securityMonitor: SecurityMonitor
    private lateinit var ruleEngine: RuleEngine
    private lateinit var consumerDeliveryManager: ConsumerDeliveryManager
    private lateinit var eventPipeline: EventPipeline
    private lateinit var hubShellCommand: HubShellCommand
    private var lastError: String? = null
    private var startTime: Long = 0L
    private var eventCount = 0L

    override fun onCreate() {
        super.onCreate()
        startTime = System.currentTimeMillis()
        leaseManager = LeaseManager()
        sourceRegistry = SourceRegistry()
        observationQueue = ObservationQueue()
        redactionPipeline = RedactionPipeline()
        backpressureController = BackpressureController()
        ruleEngine = RuleEngine()
        securityMonitor = SecurityMonitor(ruleEngine)
        consumerDeliveryManager = ConsumerDeliveryManager(leaseManager)
        eventPipeline = EventPipeline(
            redactionPipeline = redactionPipeline,
            observationQueue = observationQueue,
            consumerDeliveryManager = consumerDeliveryManager,
            backpressureController = backpressureController,
            securityMonitor = securityMonitor
        )
        hubShellCommand = HubShellCommand(
            leaseManager = leaseManager,
            sourceRegistry = sourceRegistry,
            observationQueue = observationQueue,
            backpressureController = backpressureController,
            eventPipeline = eventPipeline,
            securityMonitor = securityMonitor,
            hubHealth = this
        )
        Log.i("ObservationService", "Observation Service initialized")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("ObservationService", "onStartCommand called")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("ObservationService", "Observation Service destroyed")
    }

    private val binder = object : IProdXObservation.Stub() {
        override fun createLease(spec: String): ProdXSubscriptionLease? {
            val parts = spec.split(":")
            if (parts.size < 2) return null
            val sourceId = parts[0]
            val consumerToken = parts[1]
            val ttlMs = if (parts.size > 2) parts[2].toLongOrNull() ?: 300_000L else 300_000L

            val lease = leaseManager.createLease(sourceId, consumerToken, ttlMs, spec)
            if (lease == null) {
                lastError = "Lease creation denied for spec=$spec"
                return null
            }
            consumerDeliveryManager.registerConsumer(lease.id, consumerToken)
            return ProdXSubscriptionLease(lease.id, lease.sourceId, lease.expiresAt)
        }

        override fun revokeLease(leaseId: String): Boolean {
            val lease = leaseManager.getLease(leaseId) ?: return false
            consumerDeliveryManager.unregisterConsumer(leaseId, lease.consumerToken)
            return leaseManager.revokeLease(leaseId)
        }

        override fun registerSource(source: IBinder): Boolean {
            return sourceRegistry.registerSource(source)
        }

        override fun unregisterSource(sourceId: String): Boolean {
            return sourceRegistry.unregisterSource(sourceId)
        }

        override fun getHealth(): Int {
            return if (sourceRegistry.activeSourceCount() > 0 || leaseManager.activeLeaseCount() > 0) 1 else 0
        }

        override fun consumeObservation(leaseId: String, observationData: ByteArray): Boolean {
            val lease = leaseManager.getLease(leaseId) ?: return false
            if (lease.isExpired) return false
            eventCount++
            return true
        }

        override fun reportIncident(incidentJson: String): Boolean {
            Log.w("ObservationService", "Incident reported: $incidentJson")
            return true
        }

        override fun getIncidentTimeline(sinceMs: Long): Array<String> {
            val incidents = securityMonitor.getIncidentsSince(sinceMs)
            return incidents.map { it.toString() }.toTypedArray()
        }

        override fun acknowledgeIncident(incidentId: String): Boolean {
            return securityMonitor.acknowledgeIncident(incidentId)
        }
    }

    override fun getHealth(): HubHealth {
        val uptime = System.currentTimeMillis() - startTime
        val throughput = if (uptime > 0) eventCount.toDouble() / (uptime / 1000.0) else 0.0

        return HubHealth(
            operational = true,
            queueDepth = observationQueue.size(),
            activeLeaseCount = leaseManager.activeLeaseCount(),
            activeSourceCount = sourceRegistry.activeSourceCount(),
            lastError = lastError,
            eventThroughput = throughput,
            totalDropped = backpressureController.getTotalDropped(),
            uptimeMs = uptime,
            watermarkHigh = observationQueue.getWatermarkHigh(),
            unacknowledgedIncidents = securityMonitor.getUnacknowledgedIncidents().size
        )
    }

    fun executeShellCommand(cmd: String): String = hubShellCommand.execute(cmd)
}
