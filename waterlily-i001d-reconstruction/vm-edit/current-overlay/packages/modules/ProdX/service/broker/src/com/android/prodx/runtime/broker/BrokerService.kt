package com.android.prodx.runtime.broker

import android.app.Service
import android.app.prodx.IProdXAuthority
import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXExecutionAuthorization
import android.app.prodx.ProdXExecutionContext
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.DeadObjectException
import android.os.IBinder
import android.os.RemoteException
import android.os.Process
import android.os.UserHandle
import android.util.Log
import com.android.prodx.runtime.IProdXBroker
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class BrokerService : Service() {

    internal val stateMachine = TransactionStateMachine()
    internal var providerDispatcher: ProviderDispatcher? = null
    private val checkpointStore: BrokerCheckpointStore by lazy { BrokerCheckpointStore(this) }
    private val proposalValidator = ProposalValidator()
    private val confirmationCoordinator = ConfirmationCoordinator()
    private val authRevalidator = AuthorizationRevalidator()
    private val dependencyResolver = DependencyResolver()

    private var authorityBinder: IProdXAuthority? = null
    private var authorityBound = AtomicBoolean(false)
    private var currentMode = "disabled"
    private var currentRegistryGeneration = 0L
    private var currentPolicyEpoch = 0L
    private var currentGrantEpoch = 0L

    private val startTime = System.currentTimeMillis()
    private val completedCounter = AtomicLong(0)
    private val failedCounter = AtomicLong(0)
    private var lastError: String? = null
    private val executor = Executors.newCachedThreadPool()

    private val authorityConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            authorityBinder = IProdXAuthority.Stub.asInterface(service)
            authorityBound.set(true)
            Log.i(TAG, "Authority bound: $name")

            try {
                authorityBinder?.let { auth ->
                    currentMode = auth.mode?.name?.lowercase() ?: "disabled"
                    currentRegistryGeneration = auth.registryGeneration?.generationId ?: 0L
                }
            } catch (e: RemoteException) {
                Log.e(TAG, "Error reading authority state", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            authorityBinder = null
            authorityBound.set(false)
            Log.w(TAG, "Authority disconnected: $name")
        }

        override fun onBindingDied(name: ComponentName?) {
            authorityBinder = null
            authorityBound.set(false)
            Log.e(TAG, "Authority binding died: $name")
        }
    }

    private val binder = object : IProdXBroker.Stub() {
        override fun submitTransaction(request: ProdXCapabilityRequest): String {
            val callerUid = Binder.getCallingUid()
            return handleSubmitTransaction(request, callerUid)
        }

        override fun cancelTransaction(transactionId: String) {
            val callerUid = Binder.getCallingUid()
            handleCancelTransaction(transactionId, callerUid)
        }

        override fun getTransactionStatus(transactionId: String): Int {
            val phase = stateMachine.getPhase(transactionId)
            return phase?.ordinal ?: -1
        }

        override fun getTransactionResult(transactionId: String): ByteArray {
            val record = stateMachine.getRecord(transactionId) ?: return ByteArray(0)
            return record.resultData ?: ByteArray(0)
        }

        override fun queryTransactions(maxResults: Int): Array<String> {
            return stateMachine.getTransactionIds(maxResults).toTypedArray()
        }

        override fun getTransactionPhase(transactionId: String): String {
            return stateMachine.getPhase(transactionId)?.name ?: "UNKNOWN"
        }

        override fun getTransactionTimestamp(transactionId: String): Long {
            return stateMachine.getRecord(transactionId)?.createdAt ?: -1L
        }

        override fun hasTransaction(transactionId: String): Boolean {
            return stateMachine.hasTransaction(transactionId)
        }
    }

    override fun onCreate() {
        super.onCreate()
        providerDispatcher = ProviderDispatcher(this)
        checkpointStore.restoreFromFile()
        bindToAuthority()
        Log.i(TAG, "BrokerService created")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand: $intent")
        return START_STICKY
    }

    override fun onDestroy() {
        checkpointStore.flush()
        unbindAuthority()
        executor.shutdown()
        Log.i(TAG, "BrokerService destroyed")
        super.onDestroy()
    }

    private fun bindToAuthority() {
        val authorityIntent = Intent().apply {
            component = ComponentName("android", "com.android.server.prodx.ProdXAuthorityService")
        }
        val bound = bindService(authorityIntent, authorityConnection, Context.BIND_AUTO_CREATE)
        if (!bound) {
            Log.w(TAG, "Authority service not available, will retry")
            executor.schedule({ bindToAuthority() }, 5, TimeUnit.SECONDS)
        }
    }

    private fun unbindAuthority() {
        if (authorityBound.get()) {
            try { unbindService(authorityConnection) } catch (e: Exception) { /* ignore */ }
        }
    }

    private fun handleSubmitTransaction(request: ProdXCapabilityRequest, callerUid: Int): String {
        if (!isModeAllowed()) {
            lastError = "Submit rejected: mode $currentMode does not allow transactions"
            Log.w(TAG, lastError)
            return ""
        }

        val validation = proposalValidator.validate(request)
        if (!validation.valid) {
            lastError = "Validation failed: ${validation.errors.joinToString("; ")}"
            Log.w(TAG, lastError)
            return ""
        }

        val transactionId = generateTransactionId(request)
        val requestHash = computeRequestHash(request)

        val txnResult = stateMachine.createTransaction(
            transactionId = transactionId,
            requestHash = requestHash,
            callerUid = callerUid,
            idempotencyKey = request.idempotencyKey
        )

        if (txnResult.isFailure) {
            lastError = "Failed to create transaction: ${txnResult.exceptionOrNull()?.message}"
            Log.e(TAG, lastError)
            return ""
        }

        checkpointStore.saveCheckpoint(
            CheckpointEntry(
                transactionId = transactionId,
                requestHash = requestHash,
                callerUid = callerUid,
                createdAt = System.currentTimeMillis(),
                phaseName = TransactionPhase.PROPOSAL.name,
                updatedAt = System.currentTimeMillis(),
                errorDetail = null,
                resultData = null,
                idempotencyKey = request.idempotencyKey
            )
        )

        executor.submit { processTransaction(transactionId, request, callerUid) }
        return transactionId
    }

    private fun processTransaction(transactionId: String, request: ProdXCapabilityRequest, callerUid: Int) {
        try {
            val phase1 = stateMachine.transition(transactionId, TransactionPhase.CONFIRMATION)
            if (phase1.isFailure) {
                failTransaction(transactionId, "CONFIRMATION transition failed: ${phase1.exceptionOrNull()?.message}")
                return
            }
            checkpointStore.saveCheckpoint(
                checkpointStore.loadCheckpoint(transactionId)!!.copy(phaseName = TransactionPhase.CONFIRMATION.name)
            )

            val authority = authorityBinder
            if (authority == null) {
                failTransaction(transactionId, "Authority not available")
                return
            }

            val context = try {
                authority.deriveCallerContext(request.purpose)
            } catch (e: RemoteException) {
                failTransaction(transactionId, "Failed to derive context: ${e.message}")
                return
            }

            val confirmResult = confirmationCoordinator.requestConfirmation(
                authority = authority,
                transactionId = transactionId,
                request = request,
                context = context
            )

            when (confirmResult) {
                is ConfirmationCoordinator.ConfirmationResult.Approved -> {
                    continueToAuthorization(transactionId, request, callerUid, confirmResult.authorization)
                }
                is ConfirmationCoordinator.ConfirmationResult.Denied -> {
                    failTransaction(transactionId, "Confirmation denied: ${confirmResult.reason}")
                }
                is ConfirmationCoordinator.ConfirmationResult.NeedsConfirmation -> {
                    failTransaction(transactionId, "Confirmation required but no UI available")
                }
                is ConfirmationCoordinator.ConfirmationResult.Failed -> {
                    failTransaction(transactionId, "Confirmation failed: ${confirmResult.error}")
                }
                is ConfirmationCoordinator.ConfirmationResult.Timeout -> {
                    failTransaction(transactionId, "Confirmation timed out")
                }
                is ConfirmationCoordinator.ConfirmationResult.PendingProof -> {
                    failTransaction(transactionId, "Confirmation proof pending")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing transaction $transactionId", e)
            failTransaction(transactionId, "Processing error: ${e.message}")
        }
    }

    private fun continueToAuthorization(
        transactionId: String,
        request: ProdXCapabilityRequest,
        callerUid: Int,
        authorization: ProdXExecutionAuthorization
    ) {
        val phaseResult = stateMachine.transition(transactionId, TransactionPhase.AUTHORIZATION)
        if (phaseResult.isFailure) {
            failTransaction(transactionId, "AUTHORIZATION transition failed")
            return
        }
        checkpointStore.saveCheckpoint(
            checkpointStore.loadCheckpoint(transactionId)!!.copy(phaseName = TransactionPhase.AUTHORIZATION.name)
        )

        val revalidation = authRevalidator.revalidate(
            authorization = authorization,
            callerUid = callerUid,
            callerPackage = null,
            targetProviderId = request.targetProvider,
            currentRegistryGeneration = currentRegistryGeneration,
            currentPolicyEpoch = currentPolicyEpoch,
            currentGrantEpoch = currentGrantEpoch
        )

        if (!revalidation.valid) {
            failTransaction(transactionId, "Authorization revalidation failed: ${revalidation.reason}")
            return
        }

        continueToDispatch(transactionId, request, authorization)
    }

    private fun continueToDispatch(
        transactionId: String,
        request: ProdXCapabilityRequest,
        authorization: ProdXExecutionAuthorization
    ) {
        val phaseResult = stateMachine.transition(transactionId, TransactionPhase.DISPATCH)
        if (phaseResult.isFailure) {
            failTransaction(transactionId, "DISPATCH transition failed")
            return
        }
        checkpointStore.saveCheckpoint(
            checkpointStore.loadCheckpoint(transactionId)!!.copy(phaseName = TransactionPhase.DISPATCH.name)
        )

        val dispatcher = providerDispatcher
        if (dispatcher == null) {
            failTransaction(transactionId, "Provider dispatcher not available")
            return
        }

        val providerEndpoint = dispatcher.resolveProvider(
            providerComponent = request.targetProvider ?: ""
        )

        if (providerEndpoint == null) {
            failTransaction(transactionId, "Provider not found: ${request.targetProvider}")
            return
        }

        val dispatchResult = dispatcher.dispatch(
            endpoint = providerEndpoint,
            request = request,
            authorization = authorization,
            timeoutMs = request.timeoutMs ?: 30_000L
        )

        if (dispatchResult.success) {
            stateMachine.setResult(transactionId, dispatchResult.resultData ?: ByteArray(0))
            val completeResult = stateMachine.transition(transactionId, TransactionPhase.COMPLETION)
            if (completeResult.isSuccess) {
                completedCounter.incrementAndGet()
                checkpointStore.removeCheckpoint(transactionId)
                Log.i(TAG, "Transaction $transactionId completed successfully")
            }
        } else {
            failTransaction(transactionId, dispatchResult.errorMessage ?: "Dispatch failed")
        }
    }

    private fun failTransaction(transactionId: String, error: String) {
        stateMachine.transition(transactionId, TransactionPhase.FAILED, error)
        failedCounter.incrementAndGet()
        lastError = error
        checkpointStore.saveCheckpoint(
            checkpointStore.loadCheckpoint(transactionId)?.copy(
                phaseName = TransactionPhase.FAILED.name,
                errorDetail = error
            ) ?: return
        )
        Log.w(TAG, "Transaction $transactionId failed: $error")
    }

    internal fun onTransactionCancelled(transactionId: String) {
        checkpointStore.removeCheckpoint(transactionId)
        lastError = "Cancelled by user"
    }

    internal val currentHealth: BrokerHealth
        get() = BrokerHealth(
            operational = authorityBound.get(),
            activeTransactions = stateMachine.activeCount(),
            mode = currentMode,
            lastError = lastError,
            uptimeMs = System.currentTimeMillis() - startTime,
            authorityBound = authorityBound.get(),
            checkpointCount = checkpointStore.checkpointCount(),
            totalTransactionsCompleted = completedCounter.get(),
            totalTransactionsFailed = failedCounter.get()
        )

    internal val checkpointCount: Int get() = checkpointStore.checkpointCount()

    private fun isModeAllowed(): Boolean {
        return currentMode in setOf("shadow", "test_no_op") || isDevelopmentBuild()
    }

    private fun isDevelopmentBuild(): Boolean {
        return Build.TYPE in setOf("eng", "userdebug")
    }

    private fun generateTransactionId(request: ProdXCapabilityRequest): String {
        val idempotency = request.idempotencyKey
        if (idempotency != null) {
            return "txn-${idempotency}"
        }
        return "txn-${UUID.randomUUID().toString().take(16)}-${System.currentTimeMillis()}"
    }

    private fun computeRequestHash(request: ProdXCapabilityRequest): String {
        val raw = buildString {
            append(request.capabilityId ?: "")
            append(request.purpose ?: "")
            append(request.targetProvider ?: "")
            append(request.idempotencyKey ?: "")
        }
        return try {
            val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }.take(32)
        } catch (e: Exception) {
            "hash-${raw.length}"
        }
    }

    companion object {
        private const val TAG = "BrokerService"
    }
}
