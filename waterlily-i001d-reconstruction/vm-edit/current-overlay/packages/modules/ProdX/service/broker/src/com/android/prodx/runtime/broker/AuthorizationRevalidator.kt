package com.android.prodx.runtime.broker

import android.app.prodx.ProdXExecutionAuthorization
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
        if (System.currentTimeMillis() > authorization.expiresAt) {
            Log.w(TAG, "Authorization expired")
            return RevalidationResult.expired()
        }

        if (authorization.authorizationId == null || authorization.authorizationId.isEmpty()) {
            return RevalidationResult.invalid("Missing authorization ID")
        }

        if (authorization.token == null || authorization.token.isEmpty()) {
            return RevalidationResult.invalid("Missing token")
        }

        return RevalidationResult.valid()
    }

    companion object {
        private const val TAG = "AuthorizationRevalidator"
    }
}
