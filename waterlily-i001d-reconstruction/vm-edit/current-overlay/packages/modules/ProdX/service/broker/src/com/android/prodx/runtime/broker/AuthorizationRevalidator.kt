package com.android.prodx.runtime.broker

import android.app.prodx.ProdXExecutionAuthorization
import android.app.prodx.ProdXToken
import android.app.prodx.ProdXGrant
import android.app.prodx.ProdXPolicy
import android.app.prodx.ProdXRegistry
import android.util.Log

data class RevalidationResult(
    val valid: Boolean,
    val reason: String? = null,
    val expired: Boolean = false,
    val epochMismatch: Boolean = false,
    val audienceMismatch: Boolean = false,
    val callerMismatch: Boolean = false
) {
    companion object {
        fun valid(): RevalidationResult = RevalidationResult(true)
        fun invalid(reason: String): RevalidationResult = RevalidationResult(false, reason)
        fun expired(): RevalidationResult = RevalidationResult(false, "Authorization expired", expired = true)
        fun epochMismatch(): RevalidationResult = RevalidationResult(false, "Epoch mismatch", epochMismatch = true)
        fun audienceMismatch(): RevalidationResult = RevalidationResult(false, "Audience mismatch", audienceMismatch = true)
    }
}

class AuthorizationRevalidator {

    fun revalidate(
        authorization: ProdXExecutionAuthorization,
        callerUid: Int,
        callerPackage: String?,
        targetProviderId: String?,
        currentRegistryGeneration: Long,
        currentPolicyEpoch: Long,
        currentGrantEpoch: Long
    ): RevalidationResult {
        val errors = mutableListOf<String>()

        if (authorization.expiryMs != null && System.currentTimeMillis() > authorization.expiryMs) {
            Log.w(TAG, "Authorization expired")
            return RevalidationResult.expired()
        }

        if (authorization.audience != null && targetProviderId != null) {
            if (authorization.audience != targetProviderId) {
                Log.w(TAG, "Audience mismatch: expected ${authorization.audience}, got $targetProviderId")
                return RevalidationResult.audienceMismatch()
            }
        }

        if (authorization.callerUid != null && authorization.callerUid != callerUid) {
            Log.w(TAG, "Caller UID mismatch: expected ${authorization.callerUid}, got $callerUid")
            return RevalidationResult.invalid("Caller UID mismatch")
        }

        if (authorization.registryGeneration != null && authorization.registryGeneration != currentRegistryGeneration) {
            Log.w(TAG, "Registry generation mismatch: expected ${authorization.registryGeneration}, current $currentRegistryGeneration")
            return RevalidationResult.epochMismatch()
        }

        if (authorization.policyEpoch != null && authorization.policyEpoch != currentPolicyEpoch) {
            Log.w(TAG, "Policy epoch mismatch")
            return RevalidationResult.epochMismatch()
        }

        if (authorization.grantEpoch != null && authorization.grantEpoch != currentGrantEpoch) {
            Log.w(TAG, "Grant epoch mismatch")
            return RevalidationResult.epochMismatch()
        }

        if (authorization.nonce == null || authorization.nonce.isEmpty()) {
            errors.add("Missing nonce")
        }

        if (authorization.proof == null || authorization.proof.isEmpty()) {
            errors.add("Missing confirmation proof")
        }

        if (errors.isNotEmpty()) {
            return RevalidationResult.invalid(errors.joinToString("; "))
        }

        return RevalidationResult.valid()
    }

    fun revalidateToken(
        token: ProdXToken,
        expectedAudience: String?,
        currentPolicyEpoch: Long
    ): RevalidationResult {
        if (token.isExpired) {
            return RevalidationResult.expired()
        }

        if (expectedAudience != null && token.audience != expectedAudience) {
            return RevalidationResult.audienceMismatch()
        }

        if (token.policyEpoch != currentPolicyEpoch) {
            return RevalidationResult.epochMismatch()
        }

        return RevalidationResult.valid()
    }

    companion object {
        private const val TAG = "AuthorizationRevalidator"
    }
}
