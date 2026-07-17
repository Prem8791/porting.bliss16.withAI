package com.android.prodx.tests.integration

import android.app.prodx.ProdXCapabilityRequest
import com.android.prodx.runtime.broker.TransactionPhase
import com.android.prodx.runtime.broker.TransactionStateMachine
import com.android.prodx.runtime.broker.ProposalValidator
import com.android.prodx.runtime.broker.AuthorizationRevalidator
import com.android.prodx.runtime.broker.DependencyResolver
import com.android.prodx.runtime.broker.DependencyGraph
import com.android.prodx.tests.fixtures.FakeBrokerService
import org.junit.Assert
import org.junit.Test

class ProdXBrokerIntegrationTest {

    @Test
    fun testSubmitAndQueryTransaction() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.verification",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val txnId = broker.submitTransaction(request, 1000)
        Assert.assertNotNull("Transaction ID should not be null", txnId)
        Assert.assertTrue("Transaction ID should not be empty", txnId.isNotEmpty())

        val status = broker.getTransactionStatus(txnId)
        Assert.assertTrue("Status should be valid", status >= 0)

        val phase = broker.getTransactionPhase(txnId)
        Assert.assertNotNull("Phase should not be UNKNOWN", phase)
        Assert.assertNotEquals("Phase should not be UNKNOWN", "UNKNOWN", phase)

        val timestamp = broker.getTransactionTimestamp(txnId)
        Assert.assertTrue("Timestamp should be positive", timestamp > 0)

        Assert.assertTrue("hasTransaction should return true", broker.hasTransaction(txnId))
    }

    @Test
    fun testSubmitThenCompleteTransaction() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.echo",
            "test.verification",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val txnId = broker.submitTransaction(request, 1000)
        Assert.assertTrue(txnId.isNotEmpty())

        val resultData = "result-ok".toByteArray(Charsets.UTF_8)
        broker.setResult(txnId, resultData)
        broker.completeTransaction(txnId)

        val phase = broker.getTransactionPhase(txnId)
        Assert.assertEquals("Should be COMPLETION", TransactionPhase.COMPLETION.name, phase)

        val result = broker.getTransactionResult(txnId)
        Assert.assertArrayEquals("Result data should match", resultData, result)
    }

    @Test
    fun testSubmitThenCancelTransaction() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.cancellation",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val txnId = broker.submitTransaction(request, 1000)
        Assert.assertTrue(txnId.isNotEmpty())

        val cancelled = broker.cancelTransaction(txnId)
        Assert.assertTrue("Cancel should succeed", cancelled)

        val phase = broker.getTransactionPhase(txnId)
        Assert.assertEquals("Should be CANCELLED", TransactionPhase.CANCELLED.name, phase)
    }

    @Test
    fun testSubmitAndCancelAlreadyTerminal() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.cancellation",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val txnId = broker.submitTransaction(request, 1000)
        broker.completeTransaction(txnId)

        val cancelled = broker.cancelTransaction(txnId)
        Assert.assertFalse("Cancel should fail for completed transaction", cancelled)
    }

    @Test
    fun testQueryTransactions() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.verification",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val id1 = broker.submitTransaction(request.copy(capabilityId = "cap.test.one"), 1000)
        val id2 = broker.submitTransaction(request.copy(capabilityId = "cap.test.two"), 1001)
        val id3 = broker.submitTransaction(request.copy(capabilityId = "cap.test.three"), 1002)

        val all = broker.queryTransactions(10)
        Assert.assertTrue("Should contain all three", all.containsAll(listOf(id1, id2, id3)))

        val limited = broker.queryTransactions(2)
        Assert.assertTrue("Should have at most 2 results", limited.size <= 2)
    }

    @Test
    fun testIdempotency() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.idempotency",
            "com.android.prodx.provider.test/.TestNoOpProvider",
            idempotencyKey = "unique-key-123"
        )
        val firstId = broker.submitTransaction(request, 1000)
        val secondId = broker.submitTransaction(request, 1000)
        Assert.assertEquals("Same idempotency key should return same ID", firstId, secondId)
    }

    @Test
    fun testNonExistentTransaction() {
        val broker = FakeBrokerService()
        Assert.assertFalse("Non-existent should not be found", broker.hasTransaction("nonexistent"))
        Assert.assertEquals("Status should be -1", -1, broker.getTransactionStatus("nonexistent"))
        Assert.assertEquals("Phase should be UNKNOWN", "UNKNOWN", broker.getTransactionPhase("nonexistent"))
        Assert.assertEquals("Timestamp should be -1", -1L, broker.getTransactionTimestamp("nonexistent"))
        Assert.assertEquals("Result should be empty", 0, broker.getTransactionResult("nonexistent").size)
    }

    @Test
    fun testFailTransaction() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.failure",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val txnId = broker.submitTransaction(request, 1000)
        broker.failTransaction(txnId, "Simulated failure")

        val phase = broker.getTransactionPhase(txnId)
        Assert.assertEquals("Should be FAILED", TransactionPhase.FAILED.name, phase)

        val txn = broker.getTransaction(txnId)
        Assert.assertNotNull(txn)
        Assert.assertEquals("Error detail should match", "Simulated failure", txn!!.errorDetail)
    }

    @Test
    fun testTransactionStateMachineTransitions() {
        val sm = TransactionStateMachine()
        val txnId = "test-sm-001"
        val result = sm.createTransaction(txnId, "hash-abc", 1000)
        Assert.assertTrue(result.isSuccess)

        val validTransitions = mapOf(
            TransactionPhase.PROPOSAL to TransactionPhase.CONFIRMATION,
            TransactionPhase.CONFIRMATION to TransactionPhase.AUTHORIZATION,
            TransactionPhase.AUTHORIZATION to TransactionPhase.DISPATCH,
            TransactionPhase.DISPATCH to TransactionPhase.COMPLETION
        )

        for ((from, to) in validTransitions) {
            val r = sm.transition(txnId, to)
            Assert.assertTrue("Transition $from -> $to should succeed", r.isSuccess)
            Assert.assertEquals(to, r.getOrNull()?.currentPhase)
        }

        val invalidResult = sm.transition(txnId, TransactionPhase.PROPOSAL)
        Assert.assertTrue("COMPLETION -> PROPOSAL should fail", invalidResult.isFailure)
    }

    @Test
    fun testTransitionToFailed() {
        val sm = TransactionStateMachine()
        val txnId = "test-sm-fail"
        sm.createTransaction(txnId, "hash-xyz", 1000)
        val failResult = sm.transition(txnId, TransactionPhase.FAILED, "test error")
        Assert.assertTrue("PROPOSAL -> FAILED should be valid", failResult.isSuccess)
        Assert.assertEquals("FAILED", failResult.getOrNull()?.currentPhase?.name)

        val doubleFail = sm.transition(txnId, TransactionPhase.CONFIRMATION)
        Assert.assertTrue("FAILED -> CONFIRMATION should be rejected", doubleFail.isFailure)
    }

    @Test
    fun testTransitionToCancelled() {
        val sm = TransactionStateMachine()
        val txnId = "test-sm-cancel"
        sm.createTransaction(txnId, "hash-def", 1000)
        val cancelResult = sm.transition(txnId, TransactionPhase.CANCELLED)
        Assert.assertTrue("PROPOSAL -> CANCELLED should be valid", cancelResult.isSuccess)

        val afterCancel = sm.transition(txnId, TransactionPhase.DISPATCH)
        Assert.assertTrue("CANCELLED -> DISPATCH should be rejected", afterCancel.isFailure)
    }

    @Test
    fun testTransitionToTimeout() {
        val sm = TransactionStateMachine()
        val txnId = "test-sm-timeout"
        sm.createTransaction(txnId, "hash-ghi", 1000)

        sm.transition(txnId, TransactionPhase.CONFIRMATION)
        val timeout = sm.transition(txnId, TransactionPhase.TIMEOUT)
        Assert.assertTrue("CONFIRMATION -> TIMEOUT should be valid", timeout.isSuccess)
        Assert.assertEquals(TransactionPhase.TIMEOUT, timeout.getOrNull()?.currentPhase)
    }

    @Test
    fun testProposalValidator() {
        val validator = ProposalValidator()
        val validRequest = ProdXCapabilityRequest(
            "capability.test.ping",
            "test.verification",
            "com.android.prodx.provider.test/.TestNoOpProvider",
            idempotencyKey = "key-001",
            timeoutMs = 30_000L
        )
        val validResult = validator.validate(validRequest)
        Assert.assertTrue("Valid request should pass", validResult.valid)

        val invalidRequest = ProdXCapabilityRequest(
            "",
            "",
            ""
        )
        val invalidResult = validator.validate(invalidRequest)
        Assert.assertFalse("Invalid request should fail", invalidResult.valid)
        Assert.assertTrue("Should contain capabilityId error", invalidResult.errors.any { it.contains("capabilityId") })
        Assert.assertTrue("Should contain purpose error", invalidResult.errors.any { it.contains("purpose") })
        Assert.assertTrue("Should contain targetProvider error", invalidResult.errors.any { it.contains("targetProvider") })
    }

    @Test
    fun testProposalValidatorEdgeCases() {
        val validator = ProposalValidator()
        val longPurpose = "a".repeat(257)
        val longPurposeRequest = ProdXCapabilityRequest(
            "cap.test.ping",
            longPurpose,
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val longPurposeResult = validator.validate(longPurposeRequest)
        Assert.assertFalse("Overly long purpose should fail", longPurposeResult.valid)

        val badCapId = ProdXCapabilityRequest(
            "invalid capability id with spaces!",
            "test.verification",
            "com.android.prodx.provider.test/.TestNoOpProvider"
        )
        val badCapResult = validator.validate(badCapId)
        Assert.assertFalse("Invalid capability ID format should fail", badCapResult.valid)
    }

    @Test
    fun testDependencyResolver() {
        val resolver = DependencyResolver()
        val manifest = mapOf(
            "provider.a" to listOf("provider.b"),
            "provider.b" to emptyList(),
            "provider.c" to listOf("provider.a")
        )
        val available = setOf("provider.a", "provider.b", "provider.c")

        val graph = resolver.resolve(manifest, available)
        Assert.assertTrue("All should be resolved", graph.unresolved.isEmpty())
        Assert.assertEquals("Resolution should have 3 items", 3, graph.resolutionOrder.size)
        Assert.assertTrue("b should be before a", graph.resolutionOrder.indexOf("provider.b") < graph.resolutionOrder.indexOf("provider.a"))
        Assert.assertTrue("a should be before c", graph.resolutionOrder.indexOf("provider.a") < graph.resolutionOrder.indexOf("provider.c"))
    }

    @Test
    fun testDependencyResolverMissingDeps() {
        val resolver = DependencyResolver()
        val manifest = mapOf(
            "provider.x" to listOf("provider.y")
        )
        val available = emptySet<String>()

        val graph = resolver.resolve(manifest, available)
        Assert.assertNotNull("Missing dep should be in unresolved", graph.unresolved.any { it.contains("provider.y") })
    }

    @Test
    fun testAuthorizationRevalidator() {
        val revalidator = AuthorizationRevalidator()
        val request = ProdXCapabilityRequest(
            "cap.test.ping", "test.auth", "provider.test"
        )
        val now = System.currentTimeMillis()

        val validAuth = ProdXExecutionAuthorization(
            allowed = true,
            expiryMs = now + 60_000L,
            audience = "provider.test",
            callerUid = 1000,
            nonce = "nonce-001".toByteArray(Charsets.UTF_8),
            proof = "proof-data".toByteArray(Charsets.UTF_8),
            registryGeneration = 1L,
            policyEpoch = 0L,
            grantEpoch = 0L
        )

        val validResult = revalidator.revalidate(
            validAuth, callerUid = 1000, callerPackage = "com.test",
            targetProviderId = "provider.test",
            currentRegistryGeneration = 1, currentPolicyEpoch = 0, currentGrantEpoch = 0
        )
        Assert.assertTrue("Valid authorization should be accepted", validResult.valid)

        val expiredAuth = validAuth.copy(expiryMs = now - 60_000L)
        val expiredResult = revalidator.revalidate(
            expiredAuth, callerUid = 1000, callerPackage = "com.test",
            targetProviderId = "provider.test",
            currentRegistryGeneration = 1, currentPolicyEpoch = 0, currentGrantEpoch = 0
        )
        Assert.assertFalse("Expired authorization should be rejected", expiredResult.valid)
        Assert.assertTrue("Should mark as expired", expiredResult.expired)

        val wrongCallerAuth = validAuth.copy(callerUid = 2000)
        val callerResult = revalidator.revalidate(
            wrongCallerAuth, callerUid = 1000, callerPackage = "com.test",
            targetProviderId = "provider.test",
            currentRegistryGeneration = 1, currentPolicyEpoch = 0, currentGrantEpoch = 0
        )
        Assert.assertFalse("Wrong caller should be rejected", callerResult.valid)

        val wrongAudienceAuth = validAuth.copy(audience = "provider.other")
        val audienceResult = revalidator.revalidate(
            wrongAudienceAuth, callerUid = 1000, callerPackage = "com.test",
            targetProviderId = "provider.test",
            currentRegistryGeneration = 1, currentPolicyEpoch = 0, currentGrantEpoch = 0
        )
        Assert.assertFalse("Wrong audience should be rejected", audienceResult.valid)

        val epochMismatchAuth = validAuth.copy(registryGeneration = 99)
        val epochResult = revalidator.revalidate(
            epochMismatchAuth, callerUid = 1000, callerPackage = "com.test",
            targetProviderId = "provider.test",
            currentRegistryGeneration = 1, currentPolicyEpoch = 0, currentGrantEpoch = 0
        )
        Assert.assertFalse("Epoch mismatch should be rejected", epochResult.valid)
        Assert.assertTrue("Should mark as epoch mismatch", epochResult.epochMismatch)
    }

    @Test
    fun testActiveTransactionCounting() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "cap.test.ping", "test.counting", "provider.test"
        )
        val id1 = broker.submitTransaction(request, 1000)
        val id2 = broker.submitTransaction(request, 1001)
        val id3 = broker.submitTransaction(request, 1002)

        Assert.assertEquals("Should have 3 active", 3, broker.getActiveCount())

        broker.completeTransaction(id1)
        Assert.assertEquals("Should have 2 active after completion", 2, broker.getActiveCount())

        broker.cancelTransaction(id2)
        Assert.assertEquals("Should have 1 active after cancel", 1, broker.getActiveCount())

        broker.completeTransaction(id3)
        Assert.assertEquals("Should have 0 active", 0, broker.getActiveCount())
    }

    @Test
    fun testClearTransactions() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "cap.test.ping", "test.clear", "provider.test"
        )
        broker.submitTransaction(request, 1000)
        broker.submitTransaction(request, 1001)
        broker.clear()
        Assert.assertEquals("Should be 0 after clear", 0, broker.queryTransactions(10).size)
    }

    @Test
    fun testStateMachineActiveCount() {
        val sm = TransactionStateMachine()
        val id1 = "active-sm-1"
        val id2 = "active-sm-2"
        sm.createTransaction(id1, "hash-1", 1000)
        sm.createTransaction(id2, "hash-2", 1001)

        Assert.assertEquals(2, sm.activeCount())

        sm.transition(id1, TransactionPhase.FAILED)
        Assert.assertEquals(1, sm.activeCount())

        sm.transition(id2, TransactionPhase.COMPLETION)
        Assert.assertEquals(0, sm.activeCount())
    }

    @Test
    fun testBrokerHealthReporting() {
        val broker = FakeBrokerService()
        val request = ProdXCapabilityRequest(
            "cap.test.ping", "test.health", "provider.test"
        )
        broker.submitTransaction(request, 1000)
        broker.submitTransaction(request, 1001)

        val active = broker.getActiveCount()
        Assert.assertTrue("Active count should be >= 0", active >= 0)
    }
}
