package com.android.prodx.provider.test

import com.android.prodx.sdk.CapabilityManifest
import com.android.prodx.sdk.ProviderManifest

data class NoOpManifest(
    val providerId: String = "test.noop",
    val version: String = "1.0.0",
) {
    fun toProviderManifest(): ProviderManifest = ProviderManifest(
        providerId = providerId,
        providerVersion = version,
        capabilities = NoOpCapabilities.all.map { it.toCapabilityManifest() },
        metadata = mapOf(
            "description" to "No-op test provider for ProdX capability verification",
            "vendor" to "ProdX SDK Test",
        ),
    )

    private fun NoOpCapability.toCapabilityManifest(): CapabilityManifest = CapabilityManifest(
        capabilityId = capabilityId,
        name = name,
        version = version,
        inputSchema = inputSchema,
        outputSchema = outputSchema,
        permissionRequirements = if (requiredPermission.isNotEmpty())
            setOf(requiredPermission) else emptySet(),
        riskLevel = riskLevel,
        confirmationRequired = riskLevel.level >= RiskLevel.HIGH.level,
        description = description,
    )
}
