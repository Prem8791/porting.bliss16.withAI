package com.android.prodx.runtime.observation

data class IncidentRecord(
    val id: String,
    val timestamp: Long,
    val severity: IncidentSeverity,
    val source: String,
    val description: String,
    val confidence: Double,
    val provenanceChain: List<String> = emptyList(),
    val recommendedAction: String = "",
    val acknowledged: Boolean = false
)

enum class IncidentSeverity {
    INFO,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
