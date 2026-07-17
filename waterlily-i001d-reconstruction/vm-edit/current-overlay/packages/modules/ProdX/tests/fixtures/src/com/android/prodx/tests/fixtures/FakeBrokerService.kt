package com.android.prodx.tests.fixtures

import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXExecutionAuthorization
import com.android.prodx.runtime.broker.TransactionPhase
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class FakeTransaction(
    val transactionId: String,
    val request: ProdXCapabilityRequest,
    val callerUid: Int,
    val phase: TransactionPhase,
    val createdAt: Long,
    val resultData: ByteArray? = null,
    val errorDetail: String? = null,
    val idempotencyKey: String? = null
)

class FakeBrokerService {
    private val transactions = ConcurrentHashMap<String, FakeTransaction>()
    private val idempotencyMap = ConcurrentHashMap<String, String>()
    private var operational = true
    private var mode = "test_no_op"
    private var nextPhase: TransactionPhase = TransactionPhase.COMPLETION

    fun submitTransaction(request: ProdXCapabilityRequest, callerUid: Int = 1000): String {
        if (!operational) return ""

        val id = request.idempotencyKey
        if (id != null && idempotencyMap.containsKey(id)) {
            return idempotencyMap[id]!!
        }

        val txnId = "fake-txn-${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val txn = FakeTransaction(
            transactionId = txnId,
            request = request,
            callerUid = callerUid,
            phase = TransactionPhase.PROPOSAL,
            createdAt = now,
            idempotencyKey = id
        )
        transactions[txnId] = txn

        if (id != null) idempotencyMap[id] = txnId

        val simulatedPhase = nextPhase
        if (simulatedPhase != TransactionPhase.PROPOSAL) {
            transactions[txnId] = txn.copy(phase = simulatedPhase)
        }

        return txnId
    }

    fun cancelTransaction(transactionId: String): Boolean {
        val txn = transactions[transactionId] ?: return false
        if (txn.phase in setOf(
                TransactionPhase.COMPLETION, TransactionPhase.FAILED,
                TransactionPhase.CANCELLED, TransactionPhase.TIMEOUT
            )
        ) return false

        transactions[transactionId] = txn.copy(phase = TransactionPhase.CANCELLED)
        return true
    }

    fun getTransactionStatus(transactionId: String): Int {
        return transactions[transactionId]?.phase?.ordinal ?: -1
    }

    fun getTransactionPhase(transactionId: String): String {
        return transactions[transactionId]?.phase?.name ?: "UNKNOWN"
    }

    fun getTransactionTimestamp(transactionId: String): Long {
        return transactions[transactionId]?.createdAt ?: -1L
    }

    fun getTransactionResult(transactionId: String): ByteArray {
        return transactions[transactionId]?.resultData ?: ByteArray(0)
    }

    fun hasTransaction(transactionId: String): Boolean = transactions.containsKey(transactionId)

    fun queryTransactions(maxResults: Int): List<String> {
        return transactions.entries
            .sortedByDescending { it.value.createdAt }
            .take(maxResults.coerceIn(1, 1000))
            .map { it.key }
    }

    fun setResult(transactionId: String, result: ByteArray) {
        val txn = transactions[transactionId] ?: return
        transactions[transactionId] = txn.copy(resultData = result)
    }

    fun completeTransaction(transactionId: String) {
        val txn = transactions[transactionId] ?: return
        transactions[transactionId] = txn.copy(phase = TransactionPhase.COMPLETION)
    }

    fun failTransaction(transactionId: String, error: String) {
        val txn = transactions[transactionId] ?: return
        transactions[transactionId] = txn.copy(phase = TransactionPhase.FAILED, errorDetail = error)
    }

    fun setPhase(transactionId: String, phase: TransactionPhase) {
        val txn = transactions[transactionId] ?: return
        transactions[transactionId] = txn.copy(phase = phase)
    }

    fun setNextSimulatedPhase(phase: TransactionPhase) {
        nextPhase = phase
    }

    fun getTransaction(transactionId: String): FakeTransaction? = transactions[transactionId]

    fun getAllTransactions(): List<FakeTransaction> = transactions.values.toList()

    fun setOperational(operational: Boolean) { this.operational = operational }
    fun setMode(mode: String) { this.mode = mode }

    fun getActiveCount(): Int = transactions.count { it.value.phase in setOf(
        TransactionPhase.PROPOSAL, TransactionPhase.CONFIRMATION,
        TransactionPhase.AUTHORIZATION, TransactionPhase.DISPATCH
    ) }

    fun clear() {
        transactions.clear()
        idempotencyMap.clear()
    }
}
