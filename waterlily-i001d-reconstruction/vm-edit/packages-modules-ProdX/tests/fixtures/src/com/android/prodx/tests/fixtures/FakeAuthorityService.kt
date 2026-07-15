package com.android.prodx.tests.fixtures

import android.app.prodx.IProdXAuthority
import android.app.prodx.ProdXMode
import android.app.prodx.ProdXHealth
import android.app.prodx.ProdXExecutionContext
import android.app.prodx.ProdXRegistryGeneration
import android.app.prodx.ProdXRegistrySnapshot
import android.app.prodx.ProdXCapabilityDescriptor
import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXPolicyDecision
import android.app.prodx.ProdXExecutionAuthorization

class FakeAuthorityService : IProdXAuthority.Stub() {
    override fun getMode(): ProdXMode = ProdXMode.TEST_NO_OP
    override fun getHealth(): ProdXHealth = ProdXHealth(true, "fake")
    override fun deriveCallerContext(purpose: String?): ProdXExecutionContext = ProdXExecutionContext(0, 0, "fake", purpose ?: "")
    override fun getRegistryGeneration(): ProdXRegistryGeneration = ProdXRegistryGeneration(0, 0)
    override fun getRegistrySnapshot(generationId: Long): ProdXRegistrySnapshot? = null
    override fun resolveCapability(descriptor: ProdXCapabilityDescriptor?): Boolean = false
    override fun evaluatePolicy(context: ProdXExecutionContext?, request: ProdXCapabilityRequest?): ProdXPolicyDecision = ProdXPolicyDecision(false, "fake")
    override fun mintAuthorization(decision: ProdXPolicyDecision?, proof: ByteArray?): ProdXExecutionAuthorization? = null
    override fun emergencyDisable() {}
    override fun isEmergencyDisabled(): Boolean = false
    override fun setMode(mode: ProdXMode?) {}
}
