package com.android.prodx.sdk

import android.app.prodx.ProdXGrant
import android.app.prodx.ProdXPolicy
import android.app.prodx.ProdXRegistry
import android.app.prodx.ProdXToken
import android.os.Binder
import android.util.Log

class AuthorizationVerifier(
    private val policy: ProdXPolicy = ProdXPolicy(),
    private val grantStore: ProdXGrant = ProdXGrant(),
    private val registry: ProdXRegistry = ProdXRegistry()
) {
    private val tag = "ProdXAuthZ"

    fun verify(
        token: ByteArray,
        capabilityId: String,
        callerUid: Int = Binder.getCallingUid(),
    ): AuthorizationResult {
        val decoded = ProdXToken.decode(token) ?: return AuthorizationResult.DENIED("Malformed token")
        if (decoded.isExpired()) return AuthorizationResult.DENIED("Token expired")
        if (decoded.epoch != registry.currentEpoch()) return AuthorizationResult.DENIED("Epoch mismatch")

        val callerIdentity = registry.resolveCallerIdentity(callerUid) ?: return AuthorizationResult.DENIED("Unknown caller")
        if (!decoded.isBoundTo(callerIdentity)) return AuthorizationResult.DENIED("Identity binding mismatch")

        val grant = grantStore.getGrant(decoded.grantId) ?: return AuthorizationResult.DENIED("Grant not found")
        if (grant.isRevoked()) return AuthorizationResult.DENIED("Grant revoked")
        if (grant.isExpired()) return AuthorizationResult.DENIED("Grant expired")

        val policyResult = policy.evaluate(capabilityId, callerIdentity, grant)
        return when {
            policyResult.isAllowed() -> AuthorizationResult.GRANTED(policyResult.confidence)
            else -> AuthorizationResult.DENIED(policyResult.denyReason())
        }
    }

    fun verifyOrThrow(
        token: ByteArray,
        capabilityId: String,
        callerUid: Int = Binder.getCallingUid(),
    ) {
        val result = verify(token, capabilityId, callerUid)
        if (result !is AuthorizationResult.Granted) {
            Log.w(tag, "Authorization denied: ${result.reason}")
            throw SecurityException("Authorization denied: ${result.reason}")
        }
    }
}

sealed class AuthorizationResult(val granted: Boolean, val reason: String?) {
    data class Granted(val confidence: Double = 1.0) : AuthorizationResult(true, null)
    data class Denied(override val reason: String) : AuthorizationResult(false, reason)

    companion object {
        fun GRANTED(confidence: Double = 1.0): AuthorizationResult = Granted(confidence)
        fun DENIED(reason: String): AuthorizationResult = Denied(reason)
    }
}
