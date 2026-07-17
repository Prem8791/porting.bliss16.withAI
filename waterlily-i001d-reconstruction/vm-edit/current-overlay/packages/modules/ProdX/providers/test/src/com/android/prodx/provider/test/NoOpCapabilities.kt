package com.android.prodx.provider.test

import com.android.prodx.sdk.RiskLevel

object NoOpCapabilities {
    val all: List<NoOpCapability> = listOf(
        NoOpCapability(
            capabilityId = "prodx.test.noop.echo",
            name = "Echo request",
            inputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "message" to mapOf("type" to "string"),
                    "repeat" to mapOf("type" to "integer", "default" to 1),
                ),
                "required" to listOf("message"),
            ),
            outputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "echo" to mapOf("type" to "string"),
                    "timestamp" to mapOf("type" to "integer"),
                    "repeated" to listOf(
                        mapOf("type" to "string"),
                    ),
                ),
            ),
            requiredPermission = "",
            riskLevel = RiskLevel.NONE,
            description = "Echoes back the input message for testing connectivity",
        ),
        NoOpCapability(
            capabilityId = "prodx.test.noop.health",
            name = "Provider health",
            inputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "includeHistory" to mapOf("type" to "boolean", "default" to false),
                ),
            ),
            outputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "status" to mapOf("type" to "string"),
                    "healthCode" to mapOf("type" to "integer"),
                    "uptimeMs" to mapOf("type" to "integer"),
                    "lastError" to mapOf("type" to "string"),
                ),
            ),
            requiredPermission = "",
            riskLevel = RiskLevel.NONE,
            description = "Returns the current health status of the no-op provider",
        ),
        NoOpCapability(
            capabilityId = "prodx.test.noop.observation",
            name = "Synthetic observation",
            inputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "eventType" to mapOf("type" to "string", "default" to "synthetic.ping"),
                    "count" to mapOf("type" to "integer", "default" to 1),
                    "includeMetadata" to mapOf("type" to "boolean", "default" to false),
                ),
            ),
            outputSchema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "observations" to listOf(
                        mapOf(
                            "type" to "object",
                            "properties" to mapOf(
                                "observationId" to mapOf("type" to "string"),
                                "observationType" to mapOf("type" to "string"),
                                "timestamp" to mapOf("type" to "integer"),
                                "source" to mapOf("type" to "string"),
                            ),
                        ),
                    ),
                    "count" to mapOf("type" to "integer"),
                ),
            ),
            requiredPermission = "android.permission.PRODX_OBSERVE",
            riskLevel = RiskLevel.LOW,
            description = "Generates synthetic observation events without accessing device data",
        ),
    )

    fun findById(capabilityId: String): NoOpCapability? = all.firstOrNull { it.capabilityId == capabilityId }

    fun execute(capability: NoOpCapability, input: ByteArray? = null): String = when (capability.capabilityId) {
        "prodx.test.noop.echo" ->
            "PASS — the no-op echo request was accepted without a device effect."
        "prodx.test.noop.health" ->
            "PASS — the no-op test provider reports healthy."
        "prodx.test.noop.observation" ->
            "PASS — a synthetic observation was produced without reading device data."
        else ->
            "FAIL — the selected capability is not registered by the no-op provider."
    }

    fun executeWithResult(capability: NoOpCapability, input: ByteArray? = null): Any = when (capability.capabilityId) {
        "prodx.test.noop.echo" -> {
            val message = input?.toString(Charsets.UTF_8) ?: "hello"
            mapOf(
                "echo" to message,
                "timestamp" to System.currentTimeMillis(),
                "repeated" to listOf(message),
            )
        }
        "prodx.test.noop.health" -> mapOf(
            "status" to "healthy",
            "healthCode" to 0,
            "uptimeMs" to System.currentTimeMillis(),
            "lastError" to "",
        )
        "prodx.test.noop.observation" -> {
            val count = 1
            val observations = (1..count).map {
                mapOf(
                    "observationId" to "obs-${System.currentTimeMillis()}-$it",
                    "observationType" to "synthetic.ping",
                    "timestamp" to System.currentTimeMillis(),
                    "source" to "test.synthetic",
                )
            }
            mapOf("observations" to observations, "count" to observations.size)
        }
        else -> mapOf("error" to "Unknown capability")
    }
}
