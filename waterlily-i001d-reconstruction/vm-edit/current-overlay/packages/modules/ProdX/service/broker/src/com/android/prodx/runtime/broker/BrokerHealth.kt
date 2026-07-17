package com.android.prodx.runtime.broker

data class BrokerHealth(
    val operational: Boolean,
    val activeTransactions: Int,
    val mode: String = "unknown",
    val lastError: String? = null,
    val uptimeMs: Long = 0L,
    val authorityBound: Boolean = false,
    val checkpointCount: Int = 0,
    val totalTransactionsCompleted: Long = 0L,
    val totalTransactionsFailed: Long = 0L
) {
    fun toReport(): Map<String, Any?> = buildMap {
        put("operational", operational)
        put("active_transactions", activeTransactions)
        put("mode", mode)
        if (lastError != null) put("last_error", lastError)
        put("uptime_ms", uptimeMs)
        put("authority_bound", authorityBound)
        put("checkpoint_count", checkpointCount)
        put("total_completed", totalTransactionsCompleted)
        put("total_failed", totalTransactionsFailed)
    }

    fun toBriefString(): String = buildString {
        append("operational=$operational")
        append(" active=$activeTransactions")
        append(" mode=$mode")
        if (lastError != null) append(" error=$lastError")
    }
}
