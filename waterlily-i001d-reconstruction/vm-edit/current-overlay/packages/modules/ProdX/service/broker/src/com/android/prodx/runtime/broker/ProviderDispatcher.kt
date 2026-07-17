package com.android.prodx.runtime.broker

import android.app.prodx.ProdXCapabilityRequest
import android.app.prodx.ProdXExecutionAuthorization
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import android.os.Build
import android.os.IBinder
import android.os.DeadObjectException
import android.os.RemoteException
import android.util.Log
import com.android.prodx.sdk.IProdXProvider
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class ProviderEndpoint(
    val componentName: ComponentName,
    val packageName: String,
    val versionCode: Long,
    val signatureHash: String,
    val uid: Int,
    val bound: Boolean = false,
    val binder: IBinder? = null
)

data class DispatchResult(
    val success: Boolean,
    val resultData: ByteArray? = null,
    val errorMessage: String? = null,
    val providerUid: Int = -1,
    val dispatchDurationMs: Long = 0L
)

class ProviderDispatcher(private val context: Context) {

    private val providerEndpoints = ConcurrentHashMap<String, ProviderEndpoint>()
    private val activeDispatches = ConcurrentHashMap<String, Long>()
    private val connectionCounter = AtomicInteger(0)

    fun resolveProvider(
        providerComponent: String,
        expectedVersion: Long? = null,
        expectedSignature: String? = null
    ): ProviderEndpoint? {
        val componentName = ComponentName.unflattenFromString(providerComponent)
            ?: run {
                val parts = providerComponent.split("/")
                if (parts.size == 2) ComponentName(parts[0], parts[1])
                else return null
            }

        try {
            val info = context.packageManager.getPackageInfo(
                componentName.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.MATCH_SYSTEM_ONLY
            ) ?: return null

            if (expectedVersion != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (info.longVersionCode != expectedVersion) {
                    Log.w(TAG, "Version mismatch for $providerComponent: expected $expectedVersion, got ${info.longVersionCode}")
                    return null
                }
            }

            val signatureHash = computeSignatureHash(info.signingInfo)

            if (expectedSignature != null && signatureHash != expectedSignature) {
                Log.w(TAG, "Signature mismatch for $providerComponent")
                return null
            }

            val uid = info.applicationInfo?.uid ?: return null

            val endpoint = ProviderEndpoint(
                componentName = componentName,
                packageName = componentName.packageName,
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else info.versionCode.toLong(),
                signatureHash = signatureHash,
                uid = uid
            )

            providerEndpoints[providerComponent] = endpoint
            Log.i(TAG, "Resolved provider $providerComponent at UID $uid")
            return endpoint

        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Provider package not found: $providerComponent")
            return null
        }
    }

    fun dispatch(
        endpoint: ProviderEndpoint,
        request: ProdXCapabilityRequest,
        authorization: ProdXExecutionAuthorization,
        timeoutMs: Long = DEFAULT_DISPATCH_TIMEOUT_MS
    ): DispatchResult {
        val startTime = System.currentTimeMillis()
        val dispatchKey = "${endpoint.componentName.flattenToString()}:${request.capabilityId}"
        activeDispatches[dispatchKey] = startTime

        try {
            val binder = bindToProvider(endpoint)
            if (binder == null) {
                Log.e(TAG, "Failed to bind to provider ${endpoint.componentName}")
                return DispatchResult(false, errorMessage = "Provider binding failed")
            }

            val provider = IProdXProvider.Stub.asInterface(binder)

            val providerHealth = provider.health
            if (providerHealth <= 0) {
                Log.w(TAG, "Provider ${endpoint.componentName} not healthy")
                return DispatchResult(false, errorMessage = "Provider not healthy")
            }

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed >= timeoutMs) {
                return DispatchResult(false, errorMessage = "Pre-dispatch timeout")
            }

            Log.i(TAG, "Dispatching ${request.capabilityId} to ${endpoint.componentName}")
            val resultBytes = dispatchCapabilityRequest(provider, request, authorization, timeoutMs - elapsed)

            val totalDuration = System.currentTimeMillis() - startTime
            Log.i(TAG, "Dispatch completed for ${request.capabilityId} in ${totalDuration}ms")

            return DispatchResult(
                success = true,
                resultData = resultBytes,
                providerUid = endpoint.uid,
                dispatchDurationMs = totalDuration
            )

        } catch (e: DeadObjectException) {
            Log.e(TAG, "Provider ${endpoint.componentName} died during dispatch", e)
            return DispatchResult(false, errorMessage = "Provider process died")
        } catch (e: RemoteException) {
            Log.e(TAG, "Remote exception during dispatch to ${endpoint.componentName}", e)
            return DispatchResult(false, errorMessage = "Provider remote error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error dispatching to ${endpoint.componentName}", e)
            return DispatchResult(false, errorMessage = "Dispatch error: ${e.message}")
        } finally {
            activeDispatches.remove(dispatchKey)
        }
    }

    fun verifyProviderUid(providerComponent: String): Int? {
        return providerEndpoints[providerComponent]?.uid
    }

    fun getActiveDispatchCount(): Int = activeDispatches.size

    fun clearCache() {
        providerEndpoints.clear()
    }

    private fun bindToProvider(endpoint: ProviderEndpoint): IBinder? {
        if (endpoint.binder != null && endpoint.bound) {
            return endpoint.binder
        }

        try {
            val intent = Intent().apply {
                component = endpoint.componentName
            }

            val connId = connectionCounter.incrementAndGet()
            val latch = java.util.concurrent.CountDownLatch(1)
            val binderRef = arrayOf<IBinder?>(null)

            val conn = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    binderRef[0] = service
                    latch.countDown()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    val existing = providerEndpoints[endpoint.componentName.flattenToString()]
                    if (existing != null) {
                        providerEndpoints[endpoint.componentName.flattenToString()] =
                            existing.copy(bound = false, binder = null)
                    }
                }
            }

            val bound = context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            if (!bound) {
                Log.e(TAG, "Failed to bind service ${endpoint.componentName}")
                return null
            }

            val connected = latch.await(BIND_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
            if (!connected) {
                context.unbindService(conn)
                Log.e(TAG, "Timeout binding to ${endpoint.componentName}")
                return null
            }

            val updated = endpoint.copy(bound = true, binder = binderRef[0])
            providerEndpoints[endpoint.componentName.flattenToString()] = updated

            return binderRef[0]
        } catch (e: Exception) {
            Log.e(TAG, "Error binding to provider ${endpoint.componentName}", e)
            return null
        }
    }

    private fun dispatchCapabilityRequest(
        provider: IProdXProvider,
        request: ProdXCapabilityRequest,
        authorization: ProdXExecutionAuthorization,
        remainingTimeoutMs: Long
    ): ByteArray {
        return request.capabilityId?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
    }

    private fun computeSignatureHash(signingInfo: SigningInfo): String {
        return try {
            val sigs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                signingInfo.signingCertificateHistory ?: signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                signingInfo.apkContentsSigners
            } ?: return "unknown"

            val digest = MessageDigest.getInstance("SHA-256")
            for (sig in sigs) {
                digest.update(sig.toByteArray())
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compute signature hash", e)
            "unknown"
        }
    }

    companion object {
        private const val TAG = "ProviderDispatcher"
        private const val DEFAULT_DISPATCH_TIMEOUT_MS = 30_000L
        private const val BIND_TIMEOUT_MS = 5_000L
    }
}
