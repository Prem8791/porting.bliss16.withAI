package com.android.prodx.runtime.broker

import android.util.Log
import java.io.PrintWriter

open class BrokerShellCommand(private val broker: BrokerService) {

    open fun execute(cmd: String): String {
        val args = cmd.trim().split("\\s+".toRegex())
        if (args.isEmpty()) return "Usage: ..."

        return try {
            when (args[0]) {
                "list" -> handleList(args)
                "health" -> handleHealth()
                "cancel" -> handleCancel(args)
                "dump" -> handleDump(args)
                "status" -> handleStatus(args)
                "help" -> handleHelp()
                else -> "Unknown command: ${args[0]}. Try 'help'."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Shell command error: ${args[0]}", e)
            "Error: ${e.message}"
        }
    }

    private fun handleList(args: List<String>): String {
        val maxResults = args.getOrNull(1)?.toIntOrNull()?.coerceIn(1, 1000) ?: 50
        val ids = broker.stateMachine.getTransactionIds(maxResults)

        if (ids.isEmpty()) return "No transactions"

        val sb = StringBuilder("Transactions ($maxResults shown):\n")
        for (id in ids) {
            val phase = broker.stateMachine.getPhase(id)
            sb.append("  $id -> $phase\n")
        }
        return sb.toString().trimEnd()
    }

    private fun handleHealth(): String {
        val health = broker.currentHealth
        return buildString {
            appendLine("Broker Health:")
            appendLine("  Operational: ${health.operational}")
            appendLine("  Mode: ${health.mode}")
            appendLine("  Active Transactions: ${health.activeTransactions}")
            appendLine("  Authority Bound: ${health.authorityBound}")
            appendLine("  Uptime: ${health.uptimeMs}ms")
            appendLine("  Completed: ${health.totalTransactionsCompleted}")
            appendLine("  Failed: ${health.totalTransactionsFailed}")
            health.lastError?.let { appendLine("  Last Error: $it") }
        }.trimEnd()
    }

    private fun handleCancel(args: List<String>): String {
        if (args.size < 2) return "Usage: cancel <transaction-id>"
        val txnId = args[1]
        val record = broker.stateMachine.getRecord(txnId)
        if (record == null) return "Transaction not found: $txnId"

        val currentPhase = record.currentPhase
        if (currentPhase in setOf(TransactionPhase.COMPLETION, TransactionPhase.FAILED, TransactionPhase.CANCELLED, TransactionPhase.TIMEOUT)) {
            return "Transaction $txnId already in terminal phase: $currentPhase"
        }

        val result = broker.stateMachine.transition(txnId, TransactionPhase.CANCELLED)
        return if (result.isSuccess) {
            broker.onTransactionCancelled(txnId)
            "Cancelled transaction: $txnId"
        } else {
            "Failed to cancel: ${result.exceptionOrNull()?.message}"
        }
    }

    private fun handleDump(args: List<String>): String {
        val sb = StringBuilder("Broker State Dump:\n")
        sb.appendLine("  Active transactions: ${broker.stateMachine.activeCount()}")
        sb.appendLine("  Provider dispatches active: ${broker.providerDispatcher?.getActiveDispatchCount() ?: 0}")
        sb.appendLine("  Checkpoints stored: ${broker.checkpointCount()}")

        if (args.contains("--all")) {
            sb.appendLine("\n  All transaction records:")
            for (id in broker.stateMachine.getTransactionIds(1000)) {
                val record = broker.stateMachine.getRecord(id)
                if (record != null) {
                    sb.appendLine("    $id: phase=${record.currentPhase} created=${record.createdAt} caller=${record.callerUid}")
                }
            }
        }

        return sb.toString().trimEnd()
    }

    private fun handleStatus(args: List<String>): String {
        if (args.size < 2) return "Usage: status <transaction-id>"
        val txnId = args[1]
        val record = broker.stateMachine.getRecord(txnId)
        if (record == null) return "Transaction not found: $txnId"
        return buildString {
            appendLine("Transaction: $txnId")
            appendLine("  Phase: ${record.currentPhase}")
            appendLine("  Created: ${record.createdAt}")
            appendLine("  Updated: ${record.updatedAt}")
            appendLine("  Caller UID: ${record.callerUid}")
            appendLine("  Request Hash: ${record.requestHash}")
            record.errorDetail?.let { appendLine("  Error: $it") }
        }.trimEnd()
    }

    private fun handleHelp(): String {
        return """
Commands:
  list [max]          List recent transactions (default 50)
  health              Show broker health
  cancel <txn-id>     Force cancel a transaction
  dump [--all]        Dump broker state
  status <txn-id>     Show transaction status
  help                Show this help
        """.trimIndent()
    }

    open fun onShellCommand(exec: Any?, pw: PrintWriter?) {
        pw?.println("ProdX Broker Shell")
    }

    companion object {
        private const val TAG = "BrokerShellCommand"
    }
}
