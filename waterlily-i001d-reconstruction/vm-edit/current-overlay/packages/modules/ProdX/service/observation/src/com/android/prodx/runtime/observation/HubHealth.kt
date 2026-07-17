package com.android.prodx.runtime.observation

data class HubHealth(
    val operational: Boolean,
    val queueDepth: Int,
    val activeLeaseCount: Int = 0,
    val activeSourceCount: Int = 0,
    val lastError: String? = null,
    val eventThroughput: Double = 0.0,
    val totalDropped: Long = 0L,
    val uptimeMs: Long = 0L,
    val watermarkHigh: Int = 0,
    val unacknowledgedIncidents: Int = 0
)
