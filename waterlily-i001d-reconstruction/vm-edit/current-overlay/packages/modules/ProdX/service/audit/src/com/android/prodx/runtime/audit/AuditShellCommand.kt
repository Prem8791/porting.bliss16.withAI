package com.android.prodx.runtime.audit

/** Deliberately exposes health only; raw ledger reads and mutation are not shell surfaces. */
class AuditShellCommand(private val health: () -> AuditHealth) {
    fun execute(command: String): String = when (command) {
        "health" -> health().name
        else -> "unsupported"
    }
}
