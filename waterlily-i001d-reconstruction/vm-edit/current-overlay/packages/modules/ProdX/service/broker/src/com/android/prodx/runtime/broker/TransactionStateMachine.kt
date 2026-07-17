package com.android.prodx.runtime.broker

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class TransactionRecord(
    val transactionId: String,
    val requestHash: String,
    val callerUid: Int,
    val createdAt: Long,
    val currentPhase: TransactionPhase,
    val updatedAt: Long,
    val errorDetail: String? = null,
    val resultData: ByteArray? = null
)

class TransactionStateMachine {

    private val transactions = ConcurrentHashMap<String, TransactionRecord>()
    private val idempotencyKeys = ConcurrentHashMap<String, String>()

    private val allowedTransitions: Map<TransactionPhase, Set<TransactionPhase>> = mapOf(
        TransactionPhase.PROPOSAL to setOf(
            TransactionPhase.CONFIRMATION, TransactionPhase.FAILED,
            TransactionPhase.CANCELLED, TransactionPhase.TIMEOUT
        ),
        TransactionPhase.CONFIRMATION to setOf(
            TransactionPhase.AUTHORIZATION, TransactionPhase.FAILED,
            TransactionPhase.CANCELLED, TransactionPhase.TIMEOUT
        ),
        TransactionPhase.AUTHORIZATION to setOf(
            TransactionPhase.DISPATCH, TransactionPhase.FAILED,
            TransactionPhase.CANCELLED, TransactionPhase.TIMEOUT
        ),
        TransactionPhase.DISPATCH to setOf(
            TransactionPhase.COMPLETION, TransactionPhase.FAILED,
            TransactionPhase.TIMEOUT
        ),
        TransactionPhase.COMPLETION to emptySet(),
        TransactionPhase.FAILED to emptySet(),
        TransactionPhase.CANCELLED to emptySet(),
        TransactionPhase.TIMEOUT to emptySet()
    )

    fun createTransaction(
        transactionId: String,
        requestHash: String,
        callerUid: Int,
        idempotencyKey: String? = null
    ): Result<TransactionRecord> {
        if (idempotencyKey != null) {
            val existing = idempotencyKeys[idempotencyKey]
            if (existing != null) {
                val record = transactions[existing]
                if (record != null) {
                    return Result.success(record)
                }
            }
        }

        val now = System.currentTimeMillis()
        val record = TransactionRecord(
            transactionId = transactionId,
            requestHash = requestHash,
            callerUid = callerUid,
            createdAt = now,
            currentPhase = TransactionPhase.PROPOSAL,
            updatedAt = now
        )
        transactions[transactionId] = record

        if (idempotencyKey != null) {
            idempotencyKeys[idempotencyKey] = transactionId
        }

        return Result.success(record)
    }

    fun transition(transactionId: String, newPhase: TransactionPhase, error: String? = null): Result<TransactionRecord> {
        val record = transactions[transactionId]
            ?: return Result.failure(IllegalArgumentException("Unknown transaction: $transactionId"))

        val current = record.currentPhase
        val allowed = allowedTransitions[current] ?: emptySet()

        if (newPhase !in allowed) {
            return Result.failure(
                IllegalStateException("Invalid transition from $current to $newPhase for transaction $transactionId")
            )
        }

        val now = System.currentTimeMillis()
        val updated = record.copy(
            currentPhase = newPhase,
            updatedAt = now,
            errorDetail = error ?: record.errorDetail
        )
        transactions[transactionId] = updated
        return Result.success(updated)
    }

    fun getRecord(transactionId: String): TransactionRecord? = transactions[transactionId]

    fun getPhase(transactionId: String): TransactionPhase? = transactions[transactionId]?.currentPhase

    fun getTransactionIds(maxResults: Int): List<String> {
        return transactions.entries
            .sortedByDescending { it.value.updatedAt }
            .take(maxResults.coerceIn(1, 1000))
            .map { it.key }
    }

    fun hasTransaction(transactionId: String): Boolean = transactions.containsKey(transactionId)

    fun activeCount(): Int = transactions.count { (_, record) ->
        record.currentPhase in setOf(
            TransactionPhase.PROPOSAL, TransactionPhase.CONFIRMATION,
            TransactionPhase.AUTHORIZATION, TransactionPhase.DISPATCH
        )
    }

    fun getActiveTransactionIds(): List<String> = transactions.filter { (_, record) ->
        record.currentPhase in setOf(
            TransactionPhase.PROPOSAL, TransactionPhase.CONFIRMATION,
            TransactionPhase.AUTHORIZATION, TransactionPhase.DISPATCH
        )
    }.keys.toList()

    fun setResult(transactionId: String, result: ByteArray): Result<Unit> {
        val record = transactions[transactionId]
            ?: return Result.failure(IllegalArgumentException("Unknown transaction: $transactionId"))
        transactions[transactionId] = record.copy(resultData = result)
        return Result.success(Unit)
    }

    fun snapshot(): Map<String, TransactionRecord> = transactions.toMap()

    fun restore(snapshot: Map<String, TransactionRecord>) {
        transactions.clear()
        transactions.putAll(snapshot)
    }
}
