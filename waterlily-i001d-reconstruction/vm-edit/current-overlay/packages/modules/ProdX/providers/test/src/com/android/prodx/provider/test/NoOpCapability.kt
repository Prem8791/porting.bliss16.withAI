package com.android.prodx.provider.test

import com.android.prodx.sdk.RiskLevel

data class NoOpCapability(
    val capabilityId: String,
    val name: String,
    val inputSchema: Map<String, Any?> = emptyMap(),
    val outputSchema: Map<String, Any?> = emptyMap(),
    val requiredPermission: String = "",
    val riskLevel: RiskLevel = RiskLevel.NONE,
    val description: String = "",
)
