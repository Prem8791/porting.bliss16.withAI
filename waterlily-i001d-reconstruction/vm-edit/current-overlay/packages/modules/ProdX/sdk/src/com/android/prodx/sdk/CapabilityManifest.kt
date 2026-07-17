package com.android.prodx.sdk

data class RiskLevel(val level: Int, val label: String) {
    companion object {
        val NONE = RiskLevel(0, "none")
        val LOW = RiskLevel(1, "low")
        val MEDIUM = RiskLevel(2, "medium")
        val HIGH = RiskLevel(3, "high")
        val CRITICAL = RiskLevel(4, "critical")
    }
}

data class CapabilityManifest(
    val capabilityId: String,
    val name: String,
    val version: String,
    val inputSchema: Map<String, Any?>,
    val outputSchema: Map<String, Any?>,
    val permissionRequirements: Set<String> = emptySet(),
    val riskLevel: RiskLevel = RiskLevel.NONE,
    val confirmationRequired: Boolean = false,
    val description: String = "",
    val tags: Set<String> = emptySet(),
) {
    fun toMap(): Map<String, Any?> = buildMap {
        put("capabilityId", capabilityId)
        put("name", name)
        put("version", version)
        put("inputSchema", inputSchema)
        put("outputSchema", outputSchema)
        if (permissionRequirements.isNotEmpty()) put("permissionRequirements", permissionRequirements.toList())
        put("riskLevel", riskLevel.label)
        put("confirmationRequired", confirmationRequired)
        put("description", description)
        if (tags.isNotEmpty()) put("tags", tags.toList())
    }
}

data class ProviderManifest(
    val providerId: String,
    val providerVersion: String,
    val capabilities: List<CapabilityManifest>,
    val metadata: Map<String, String> = emptyMap(),
) {
    fun findCapability(capabilityId: String): CapabilityManifest? =
        capabilities.firstOrNull { it.capabilityId == capabilityId }

    fun hasCapability(capabilityId: String): Boolean =
        capabilities.any { it.capabilityId == capabilityId }

    fun toMap(): Map<String, Any?> = buildMap {
        put("providerId", providerId)
        put("providerVersion", providerVersion)
        put("capabilities", capabilities.map { it.toMap() })
        if (metadata.isNotEmpty()) put("metadata", metadata)
    }
}
