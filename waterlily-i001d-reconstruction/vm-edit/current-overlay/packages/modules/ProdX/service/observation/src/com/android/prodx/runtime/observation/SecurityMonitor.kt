package com.android.prodx.runtime.observation

class SecurityMonitor(
    private val ruleEngine: RuleEngine,
    private val incidentHistory: MutableList<IncidentRecord> = mutableListOf()
) {
    private val eventBuffer = mutableListOf<EventSummary>()
    private val counters = mutableMapOf<String, Int>()
    private val provenanceChain = mutableListOf<String>()
    private var sequence = 0L

    init {
        registerDefaultRules()
    }

    private fun registerDefaultRules() {
        ruleEngine.registerRule(
            RuleDefinition(
                id = "rapid_permission_grant",
                name = "Rapid Permission Grant Detection",
                description = "Multiple permission grants detected in short window",
                condition = RuleCondition.ThresholdExceeded(
                    metric = "permission_grant",
                    maxCount = 5,
                    windowMs = 60_000
                ),
                severity = IncidentSeverity.MEDIUM,
                recommendedAction = "Review recent permission grant requests"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "overlay_accessibility_combo",
                name = "Overlay + Accessibility Combo",
                description = "Overlay and accessibility service enabled together",
                condition = RuleCondition.SequenceDetected(
                    eventTypes = listOf("overlay_enabled", "accessibility_enabled"),
                    withinWindowMs = 30_000
                ),
                severity = IncidentSeverity.HIGH,
                recommendedAction = "Investigate potential tapjacking attack"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "rapid_app_install",
                name = "Rapid Application Install Detection",
                description = "Multiple app installs in short window",
                condition = RuleCondition.ThresholdExceeded(
                    metric = "app_install",
                    maxCount = 3,
                    windowMs = 120_000
                ),
                severity = IncidentSeverity.LOW,
                recommendedAction = "Verify apps were user-initiated"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "suspicious_package_name",
                name = "Suspicious Package Name Pattern",
                description = "Package name matches known impersonation pattern",
                condition = RuleCondition.PatternMatch(
                    field = "package_name",
                    pattern = ".*[0-9]{4,}.*"
                ),
                severity = IncidentSeverity.INFO,
                recommendedAction = "Validate package authenticity"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "device_admin_escalation",
                name = "Device Admin Escalation",
                description = "Device admin activation with suspicious pattern",
                condition = RuleCondition.SequenceDetected(
                    eventTypes = listOf("device_admin_activated", "permission_grant"),
                    withinWindowMs = 10_000
                ),
                severity = IncidentSeverity.HIGH,
                recommendedAction = "Review device admin activation context"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "high_event_rate",
                name = "Abnormal Event Rate",
                description = "Source producing events above expected threshold",
                condition = RuleCondition.ThresholdExceeded(
                    metric = "event_received",
                    maxCount = 100,
                    windowMs = 60_000
                ),
                severity = IncidentSeverity.MEDIUM,
                recommendedAction = "Investigate source adapter behavior"
            )
        )

        ruleEngine.registerRule(
            RuleDefinition(
                id = "credential_access_pattern",
                name = "Credential Access Pattern",
                description = "Multiple credential-related events detected",
                condition = RuleCondition.SequenceDetected(
                    eventTypes = listOf("credential_access", "credential_access"),
                    withinWindowMs = 5_000
                ),
                severity = IncidentSeverity.CRITICAL,
                recommendedAction = "Immediately investigate credential access source"
            )
        )
    }

    fun ingestEvent(event: EventSummary) {
        eventBuffer.add(event)
        counters["event_received"] = (counters["event_received"] ?: 0) + 1
        when (event.eventType) {
            "permission_grant" -> counters["permission_grant"] = (counters["permission_grant"] ?: 0) + 1
            "app_install" -> counters["app_install"] = (counters["app_install"] ?: 0) + 1
            "overlay_enabled" -> counters["overlay_enabled"] = (counters["overlay_enabled"] ?: 0) + 1
            "accessibility_enabled" -> counters["accessibility_enabled"] = (counters["accessibility_enabled"] ?: 0) + 1
            "device_admin_activated" -> counters["device_admin_activated"] = (counters["device_admin_activated"] ?: 0) + 1
            "credential_access" -> counters["credential_access"] = (counters["credential_access"] ?: 0) + 1
        }

        sequence++
        trimEventBuffer()
    }

    fun evaluate(sourceId: String, fields: Map<String, Any?>): List<IncidentRecord> {
        provenanceChain.add("SecurityMonitor.evaluate")
        val context = RuleEvaluationContext(
            sourceId = sourceId,
            sequence = sequence,
            recentEvents = eventBuffer.toList(),
            counters = counters.toMap(),
            fields = fields,
            provenanceChain = provenanceChain.toList()
        )
        val incidents = ruleEngine.evaluate(context)
        incidentHistory.addAll(incidents)
        return incidents
    }

    fun getIncidentsSince(sinceMs: Long): List<IncidentRecord> {
        return incidentHistory.filter { it.timestamp >= sinceMs }
    }

    fun acknowledgeIncident(incidentId: String): Boolean {
        val index = incidentHistory.indexOfFirst { it.id == incidentId }
        if (index == -1) return false
        incidentHistory[index] = incidentHistory[index].copy(acknowledged = true)
        return true
    }

    fun getUnacknowledgedIncidents(): List<IncidentRecord> {
        return incidentHistory.filter { !it.acknowledged }
    }

    fun reset() {
        eventBuffer.clear()
        counters.clear()
        provenanceChain.clear()
        incidentHistory.clear()
        sequence = 0L
    }

    private fun trimEventBuffer() {
        val cutoff = System.currentTimeMillis() - 120_000
        eventBuffer.removeAll { it.timestamp < cutoff }
    }
}
