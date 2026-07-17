package com.android.prodx.runtime.broker

import android.app.prodx.IProdXAuthority
import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXPolicyDecision
import android.app.prodx.ProdXExecutionContext
import android.app.prodx.ProdXExecutionAuthorization
import android.os.IBinder
import android.os.DeadObjectException
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class ConfirmationState(
    val transactionId: String,
    val challengeId: String,
    val startedAt: Long,
    val deadlineMs: Long,
    var completed: Boolean = false,
    var approved: Boolean = false,
    var authorization: ProdXExecutionAuthorization? = null,
    var error: String? = null
)

class ConfirmationCoordinator {
    private val activeConfirmations = ConcurrentHashMap<String, ConfirmationState>()
    private val challengeCounter = AtomicInteger(0)

    fun requestConfirmation(
        authority: IProdXAuthority,
        transactionId: String,
        request: ProdXCapabilityRequest,
        context: ProdXExecutionContext,
        timeoutMs: Long = DEFAULT_CONFIRMATION_TIMEOUT_MS
    ): ConfirmationResult {
        val challengeId = "challenge-${transactionId}-${challengeCounter.incrementAndGet()}"
        val deadlineMs = System.currentTimeMillis() + timeoutMs

        val state = ConfirmationState(
            transactionId = transactionId,
            challengeId = challengeId,
            startedAt = System.currentTimeMillis(),
            deadlineMs = deadlineMs
        )
        activeConfirmations[challengeId] = state

        try {
            val decision = authority.evaluatePolicy(context, request)

            if (!decision.allowed) {
                Log.w(TAG, "Policy denied for $transactionId: ${decision.reason}")
                return ConfirmationResult.denied(decision.reason ?: "Policy denied")
            }

            if (decision.requiresConfirmation) {
                Log.i(TAG, "Confirmation required for $transactionId")
                return ConfirmationResult.needsConfirmation(challengeId)
            }

            val proof = decision.proofChallenge ?: ByteArray(0)
            val authorization = authority.mintAuthorization(decision, proof)

            if (authorization == null) {
                return ConfirmationResult.failed("Failed to mint authorization")
            }

            state.completed = true
            state.approved = true
            state.authorization = authorization

            return ConfirmationResult.approved(authorization)
        } catch (e: DeadObjectException) {
            Log.e(TAG, "Authority binder died during confirmation for $transactionId", e)
            return ConfirmationResult.failed("Authority unavailable")
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception during confirmation for $transactionId", e)
            return ConfirmationResult.failed("Remote error: ${e.message}")
        }
    }

    fun handleConfirmationResponse(
        challengeId: String,
        approved: Boolean,
        proof: ByteArray? = null
    ): ConfirmationResult {
        val state = activeConfirmations[challengeId]
            ?: return ConfirmationResult.failed("Unknown challenge: $challengeId")

        if (state.completed) {
            return ConfirmationResult.failed("Challenge already completed")
        }

        if (System.currentTimeMillis() > state.deadlineMs) {
            state.completed = true
            state.error = "Confirmation timeout"
            return ConfirmationResult.timeout()
        }

        if (!approved) {
            state.completed = true
            state.approved = false
            state.error = "User denied"
            return ConfirmationResult.denied("User denied confirmation")
        }

        return ConfirmationResult.pendingProof()
    }

    fun completeWithAuthorization(challengeId: String, authorization: ProdXExecutionAuthorization): Boolean {
        val state = activeConfirmations[challengeId] ?: return false
        state.completed = true
        state.approved = true
        state.authorization = authorization
        return true
    }

    fun cancelChallenge(challengeId: String): Boolean {
        val state = activeConfirmations[challengeId] ?: return false
        state.completed = true
        state.approved = false
        state.error = "Cancelled"
        return true
    }

    fun getAuthorization(challengeId: String): ProdXExecutionAuthorization? {
        return activeConfirmations[challengeId]?.authorization
    }

    fun getActiveChallenges(): List<String> = activeConfirmations.filter { !it.value.completed }.keys.toList()

    fun cleanup(transactionId: String) {
        val toRemove = activeConfirmations.filter { it.value.transactionId == transactionId }.keys
        for (key in toRemove) {
            activeConfirmations.remove(key)
        }
    }

    sealed class ConfirmationResult {
        data class Approved(val authorization: ProdXExecutionAuthorization) : ConfirmationResult()
        data class Denied(val reason: String) : ConfirmationResult()
        data class NeedsConfirmation(val challengeId: String) : ConfirmationResult()
        data class Failed(val error: String) : ConfirmationResult()
        object PendingProof : ConfirmationResult()
        object Timeout : ConfirmationResult()
    }

    companion object {
        private const val TAG = "ConfirmationCoordinator"
        private const val DEFAULT_CONFIRMATION_TIMEOUT_MS = 60_000L
    }
}
