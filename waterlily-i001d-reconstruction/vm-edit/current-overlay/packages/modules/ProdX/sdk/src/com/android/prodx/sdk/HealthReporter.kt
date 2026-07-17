package com.android.prodx.sdk

import java.util.concurrent.CopyOnWriteArrayList

enum class HealthSeverity {
    OK, DEGRADED, WARNING, ERROR, CRITICAL
}

data class HealthRecord(
    val code: Int,
    val severity: HealthSeverity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)

class HealthReporter(private val historySize: Int = 100) {
    private var currentCode: Int = 0
    private var currentSeverity: HealthSeverity = HealthSeverity.OK
    private val listeners = CopyOnWriteArrayList<(Int, HealthSeverity) -> Unit>()
    private val history = ArrayDeque<HealthRecord>(historySize)

    fun getHealthCode(): Int = currentCode
    fun getSeverity(): HealthSeverity = currentSeverity

    fun report(code: Int, severity: HealthSeverity = severityForCode(code), message: String = ""): Boolean {
        currentCode = code
        currentSeverity = severity
        val record = HealthRecord(code, severity, message)
        if (history.size >= historySize) history.removeFirst()
        history.addLast(record)
        listeners.forEach { it.invoke(code, severity) }
        return severity != HealthSeverity.CRITICAL && severity != HealthSeverity.ERROR
    }

    fun report(healthCode: Int): Boolean = report(healthCode, severityForCode(healthCode))

    fun onHealthReported(listener: (Int, HealthSeverity) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Int, HealthSeverity) -> Unit): Boolean =
        listeners.remove(listener)

    fun getHistory(): List<HealthRecord> = history.toList()

    fun getHistorySince(timestamp: Long): List<HealthRecord> =
        history.filter { it.timestamp >= timestamp }

    fun getLastError(): HealthRecord? =
        history.lastOrNull { it.severity == HealthSeverity.ERROR || it.severity == HealthSeverity.CRITICAL }

    fun reset() {
        currentCode = 0
        currentSeverity = HealthSeverity.OK
        history.clear()
    }

    private fun severityForCode(code: Int): HealthSeverity = when {
        code <= 0 -> HealthSeverity.OK
        code in 1..99 -> HealthSeverity.DEGRADED
        code in 100..199 -> HealthSeverity.WARNING
        code in 200..499 -> HealthSeverity.ERROR
        else -> HealthSeverity.CRITICAL
    }
}
