package com.android.prodx.sdk

import android.app.prodx.ProdXCapabilityDescriptor
import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXExecutionAuthorization
import android.app.prodx.ProdXManager
import android.content.Context
import android.os.Binder
import android.util.Log

class AuthorizationVerifier(context: Context) {
    private val tag = "ProdXAuthZ"
    private val manager = ProdXManager(context)

    fun verify(
        capabilityDescriptor: ProdXCapabilityDescriptor,
        purpose: String = "sdk_authorization",
        callerUid: Int = Binder.getCallingUid(),
    ): AuthorizationResult {
        if (!manager.resolveCapability(capabilityDescriptor)) {
            return AuthorizationResult.DENIED("Capability not resolved")
        }

        val callerContext = manager.deriveCallerContext(purpose)
        val request = ProdXCapabilityRequest(capabilityDescriptor, callerUid.toString().toByteArray())
        val decision = manager.evaluatePolicy(callerContext, request)
            ?: return AuthorizationResult.DENIED("Policy evaluation unavailable")

        return when {
            decision.isAllowed() -> AuthorizationResult.GRANTED(1.0)
            else -> AuthorizationResult.DENIED(decision.reason ?: "denied")
        }
    }

    fun mintAuthorization(
        capabilityDescriptor: ProdXCapabilityDescriptor,
        purpose: String = "sdk_authorization",
        proof: ByteArray = ByteArray(0),
        callerUid: Int = Binder.getCallingUid(),
    ): ProdXExecutionAuthorization? {
        if (!manager.resolveCapability(capabilityDescriptor)) return null

        val callerContext = manager.deriveCallerContext(purpose)
        val request = ProdXCapabilityRequest(capabilityDescriptor, callerUid.toString().toByteArray())
        val decision = manager.evaluatePolicy(callerContext, request) ?: return null
        if (!decision.isAllowed()) return null

        return manager.mintAuthorization(decision, proof)
    }

    fun verifyOrThrow(
        capabilityDescriptor: ProdXCapabilityDescriptor,
        purpose: String = "sdk_authorization",
        callerUid: Int = Binder.getCallingUid(),
    ) {
        val result = verify(capabilityDescriptor, purpose, callerUid)
        if (result !is AuthorizationResult.Granted) {
            Log.w(tag, "Authorization denied: ${result.reason}")
            throw SecurityException("Authorization denied: ${result.reason}")
        }
    }
}

sealed class AuthorizationResult {
    abstract val granted: Boolean
    abstract val reason: String?

    data class Granted(val confidence: Double = 1.0) : AuthorizationResult() {
        override val granted get() = true
        override val reason get() = null
    }

    data class Denied(override val reason: String) : AuthorizationResult() {
        override val granted get() = false
    }

    companion object {
        fun GRANTED(confidence: Double = 1.0): AuthorizationResult = Granted(confidence)
        fun DENIED(reason: String): AuthorizationResult = Denied(reason)
    }
}
