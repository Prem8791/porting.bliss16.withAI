package com.android.prodx.tests.integration

import com.android.prodx.contract.objects.EventRecord
import com.android.prodx.runtime.observation.EventSummary
import com.android.prodx.tests.fixtures.FakeObservationHub
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProdXObservationIntegrationTest {
    private lateinit var hub: FakeObservationHub

    @Before
    fun setUp() {
        hub = FakeObservationHub()
    }

    @After
    fun tearDown() {
        hub.reset()
    }

    @Test
    fun testCreateLeaseRegisterSourceProduceEventConsume() {
        val lease = hub.leaseManager.createLease(
            sourceId = "test_source_1",
            consumerToken = "consumer_1",
            ttlMs = 60_000
        )
        assertNotNull("Lease should be created", lease)
        assertEquals("test_source_1", lease.sourceId)

        hub.sourceRegistry.registerSource("test_source_1", "telemetry")

        val event = EventRecord(
            eventId = "evt_001",
            eventType = "telemetry_metric",
            timestamp = System.currentTimeMillis(),
            source = "test_source_1",
            subject = "test",
            payload = mapOf("metric" to "cpu_usage", "value" to 42.0)
        )

        val result = hub.eventPipeline.process(event)
        assertTrue("Pipeline should process event successfully", result.success)
        assertEquals(1, hub.observationQueue.size())
    }

    @Test
    fun testInvalidLeaseRejection() {
        val lease = hub.leaseManager.createLease(
            sourceId = "source_a",
            consumerToken = "consumer_a",
            ttlMs = -1
        )
        assertNull("Lease with negative TTL should be rejected", lease)

        val lease2 = hub.leaseManager.createLease(
            sourceId = "source_a",
            consumerToken = "consumer_a",
            ttlMs = 86_400_001
        )
        assertNull("Lease exceeding max TTL should be rejected", lease2)
    }

    @Test
    fun testBackpressureOverflow() {
        hub.observationQueue.setMaxSize(5)
        hub.backpressureController.setGlobalRate(1000)

        for (i in 0 until 10) {
            val event = EventRecord(
                eventId = "evt_overflow_$i",
                eventType = "test_event",
                timestamp = System.currentTimeMillis(),
                source = "overflow_source",
                payload = mapOf("index" to i)
            )
            hub.eventPipeline.process(event)
        }

        assertTrue("Queue should be at capacity", hub.observationQueue.size() <= 5)
        assertTrue("Dropped count should be > 0", hub.backpressureController.getTotalDropped() >= 0)
    }

    @Test
    fun testIncidentDetection() {
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_1", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_2", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_3", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_4", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_5", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )
        hub.securityMonitor.ingestEvent(
            EventSummary("evt_perm_6", "permission_grant", System.currentTimeMillis(), "security_monitor")
        )

        val incidents = hub.securityMonitor.evaluate("security_monitor", mapOf("source" to "test"))
        assertTrue("Rapid permission grant should trigger incident", incidents.isNotEmpty())

        val permIncidents = incidents.filter { it.id.contains("rapid_permission_grant") }
        assertTrue("Threshold rule should fire", permIncidents.isNotEmpty())
    }

    @Test
    fun testLeaseRevocation() {
        val lease = hub.leaseManager.createLease(
            sourceId = "source_revoke",
            consumerToken = "consumer_revoke",
            ttlMs = 60_000
        )
        assertNotNull(lease)

        val revoked = hub.leaseManager.revokeLease(lease!!.id)
        assertTrue("Lease should be revoked", revoked)

        assertNull("Revoked lease should not be retrievable", hub.leaseManager.getLease(lease.id))
    }

    @Test
    fun testSourceRegistrationAndHealthCheck() {
        hub.sourceRegistry.registerSource("health_source", "health")

        assertEquals(1, hub.sourceRegistry.sourceCount())
        assertTrue(hub.sourceRegistry.getRegisteredTypes().contains("health"))
    }

    @Test
    fun testFullPipelineWithRedaction() {
        val lease = hub.leaseManager.createLease("redact_source", "redact_consumer", 60_000)
        assertNotNull(lease)

        val event = EventRecord(
            eventId = "evt_redact_1",
            eventType = "security_alert",
            timestamp = System.currentTimeMillis(),
            source = "redact_source",
            subject = "user@example.com",
            payload = mapOf(
                "pii" to "secret-data",
                "email" to "user@test.com",
                "metric_name" to "login_attempts",
                "threat_type" to "brute_force",
                "latitude" to 37.7749,
                "content" to "sensitive message"
            )
        )

        val result = hub.eventPipeline.process(event)
        assertTrue("Pipeline should succeed with redaction", result.success)

        val redacted = hub.redactionPipeline.redact(event)
        assertEquals("[REDACTED]", redacted.payload["pii"])
        assertEquals("login_attempts", redacted.payload["metric_name"])
    }

    @Test
    fun testIncidentAcknowledgment() {
        val incidents = hub.securityMonitor.evaluate("test_source", mapOf("package_name" to "com.evil9999"))
        assertTrue(incidents.isNotEmpty())

        val first = incidents.first()
        assertFalse(first.acknowledged)

        val ackResult = hub.securityMonitor.acknowledgeIncident(first.id)
        assertTrue("Incident should be acknowledged", ackResult)

        val unacked = hub.securityMonitor.getUnacknowledgedIncidents()
        assertFalse("Acknowledged incident should not appear in unacknowledged", unacked.any { it.id == first.id })
    }
}
