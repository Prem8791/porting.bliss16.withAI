package com.android.prodx.runtime.observation

import com.android.prodx.contract.objects.EventRecord

data class RedactionRule(
    val sourceType: String,
    val fieldsToStrip: Set<String>,
    val allowedFields: Set<String> = emptySet(),
    val stripPii: Boolean = true,
    val stripCoordinates: Boolean = true,
    val stripContent: Boolean = true
)

class RedactionPipeline {
    private val defaultRule = RedactionRule(
        sourceType = "*",
        fieldsToStrip = setOf("pii", "email", "phone", "ssn", "credit_card"),
        allowedFields = emptySet(),
        stripPii = true,
        stripCoordinates = true,
        stripContent = true
    )

    private val rules = mutableMapOf<String, RedactionRule>()
    private val consentTokens = mutableMapOf<String, Set<String>>()

    init {
        rules["*"] = defaultRule

        rules["telemetry"] = defaultRule.copy(
            allowedFields = setOf("device_model", "os_version", "app_version")
        )
        rules["health"] = defaultRule.copy(
            stripContent = false,
            allowedFields = setOf("metric_name", "metric_value", "unit")
        )
        rules["permission"] = defaultRule.copy(
            allowedFields = setOf("permission_name", "package_name", "grant_status")
        )
        rules["security"] = defaultRule.copy(
            stripContent = false,
            allowedFields = setOf("threat_type", "severity", "source_ip", "timestamp")
        )
        rules["performance"] = defaultRule.copy(
            allowedFields = setOf("metric", "value", "threshold", "component")
        )
        rules["network"] = defaultRule.copy(
            stripCoordinates = false,
            allowedFields = setOf("connection_type", "signal_strength", "network_id")
        )
        rules["accessibility"] = defaultRule.copy(
            stripContent = true,
            allowedFields = setOf("service_name", "event_type", "package_name")
        )
        rules["overlay"] = defaultRule.copy(
            stripContent = false,
            allowedFields = setOf("window_type", "package_name", "flags")
        )
        rules["credential"] = defaultRule.copy(
            stripPii = true,
            stripContent = true,
            allowedFields = setOf("access_type", "source_package", "auth_method")
        )
        rules["package"] = defaultRule.copy(
            allowedFields = setOf("package_name", "installer", "install_type", "version")
        )
        rules["device_admin"] = defaultRule.copy(
            allowedFields = setOf("admin_package", "policy_type", "activation_source")
        )
    }

    fun redact(event: EventRecord): EventRecord {
        val rule = rules[event.source] ?: rules["*"] ?: defaultRule

        if (hasConsent(event.source, "full_access")) return event

        val redactedPayload = event.payload.mapValues { (key, value) ->
            when {
                key in rule.fieldsToStrip -> "[REDACTED]"
                rule.stripPii && isPiiField(key) -> "[REDACTED]"
                rule.stripCoordinates && isCoordinateField(key) -> "[REDACTED]"
                rule.stripContent && isContentField(key) -> "[REDACTED]"
                key in rule.allowedFields -> value
                else -> value
            }
        }

        return event.copy(payload = redactedPayload)
    }

    fun registerRule(rule: RedactionRule) {
        rules[rule.sourceType] = rule
    }

    fun setDefaultRule(rule: RedactionRule) {
        rules["*"] = rule
    }

    fun grantConsent(sourceId: String, token: String) {
        val existing = consentTokens[sourceId] ?: emptySet()
        consentTokens[sourceId] = existing + token
    }

    fun revokeConsent(sourceId: String, token: String) {
        val existing = consentTokens[sourceId] ?: return
        consentTokens[sourceId] = existing - token
    }

    fun hasConsent(sourceId: String, token: String): Boolean {
        return consentTokens[sourceId]?.contains(token) == true
    }

    fun getRuleForSource(sourceType: String): RedactionRule? = rules[sourceType]

    fun reset() {
        rules.clear()
        rules["*"] = defaultRule
        consentTokens.clear()
    }

    private fun isPiiField(key: String): Boolean {
        val piiKeys = setOf("name", "email", "phone", "address", "ssn", "credit_card",
            "account_number", "password", "token", "auth_code", "biometric", "dob", "ip_address")
        return key.lowercase() in piiKeys
    }

    private fun isCoordinateField(key: String): Boolean {
        val coordKeys = setOf("latitude", "longitude", "location", "gps", "coordinates",
            "lat", "lon", "altitude", "geo", "position")
        return key.lowercase() in coordKeys
    }

    private fun isContentField(key: String): Boolean {
        val contentKeys = setOf("content", "message", "body", "text", "data", "payload",
            "description", "details", "note", "comment", "raw", "input", "output")
        return key.lowercase() in contentKeys
    }
}
