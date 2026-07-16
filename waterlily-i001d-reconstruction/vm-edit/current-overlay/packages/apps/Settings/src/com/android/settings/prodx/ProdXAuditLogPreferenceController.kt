package com.android.settings.prodx

import android.app.prodx.ProdXManager

class ProdXAuditLogPreferenceController(
    private val manager: ProdXManager? = null,
    private val userId: Int = 0,
) {
    fun getAuditLogEntries(): List<String> =
        manager?.getMinimizedAuditHistory(userId, 0L, 20)?.map { it.action } ?: emptyList()

    fun getUnavailableReason(): String = "authority_history_mediator_unavailable"

    fun getSummary(): String {
        val entries = getAuditLogEntries()
        return if (entries.isEmpty()) "No Authority-mediated administrative events" else
            entries.takeLast(3).joinToString(separator = " • ")
    }
}
