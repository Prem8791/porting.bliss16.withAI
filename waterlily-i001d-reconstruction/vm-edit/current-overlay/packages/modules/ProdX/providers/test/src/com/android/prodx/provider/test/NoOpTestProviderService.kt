package com.android.prodx.provider.test

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import com.android.prodx.sdk.CapabilityInventory
import com.android.prodx.sdk.HealthReporter
import com.android.prodx.sdk.HealthSeverity
import com.android.prodx.sdk.IProdXProvider
import com.android.prodx.sdk.IProdXProviderHealthCallback

class NoOpTestProviderService : Service() {
    private val manifest = NoOpManifest()
    private val capabilityInventory = CapabilityInventory()
    private val healthReporter = HealthReporter()
    private val healthCallbacks = mutableListOf<IProdXProviderHealthCallback>()
    private val activeOperations = mutableSetOf<String>()
    private val syntheticSource = SyntheticObservationSource()

    private val binder = object : IProdXProvider.Stub() {
        override fun getProviderId(): String = manifest.providerId

        override fun isReady(): Boolean = true

        override fun getHealth(): Int = healthReporter.getHealthCode()

        override fun executeCapability(capabilityId: String, input: ByteArray?): ByteArray {
            val capability = NoOpCapabilities.findById(capabilityId)
                ?: return """{"error":"Capability '$capabilityId' not found"}""".toByteArray(Charsets.UTF_8)

            val operationId = "op-${System.currentTimeMillis()}"
            activeOperations.add(operationId)

            return try {
                val result = NoOpCapabilities.executeWithResult(capability, input)
                healthReporter.report(0, HealthSeverity.OK, "Executed $capabilityId")
                val resultJson = buildJsonResult(operationId, capabilityId, result)
                resultJson.toByteArray(Charsets.UTF_8)
            } catch (e: Exception) {
                healthReporter.report(500, HealthSeverity.ERROR, "Failed $capabilityId: ${e.message}")
                """{"error":"${e.message}","operationId":"$operationId"}""".toByteArray(Charsets.UTF_8)
            } finally {
                activeOperations.remove(operationId)
            }
        }

        override fun cancelOperation(operationId: String): Boolean {
            return activeOperations.remove(operationId)
        }

        override fun getCapabilityManifest(): ByteArray {
            return manifest.toProviderManifest().toMap().toString().toByteArray(Charsets.UTF_8)
        }
    }

    override fun onCreate() {
        super.onCreate()
        capabilityInventory.registerProvider(
            manifest.providerId,
            manifest.toProviderManifest().capabilities,
        )
        healthReporter.report(0, HealthSeverity.OK, "Provider initialized")
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        capabilityInventory.unregisterProvider(manifest.providerId)
        healthCallbacks.clear()
        super.onDestroy()
    }

    private fun buildJsonResult(operationId: String, capabilityId: String, result: Any): String = buildString {
        append("{\"operationId\":\"$operationId\"")
        append(",\"capabilityId\":\"$capabilityId\"")
        append(",\"success\":true")
        append(",\"result\":{")
        if (result is Map<*, *>) {
            append(result.entries.joinToString(",") { (k, v) ->
                when (v) {
                    is String -> "\"$k\":\"${v.replace("\"", "\\\"")}\""
                    is Number -> "\"$k\":$v"
                    is List<*> -> {
                        val items = v.joinToString(",") { item ->
                            when (item) {
                                is Map<*, *> -> {
                                    "{" + item.entries.joinToString(",") { (ik, iv) ->
                                        "\"$ik\":\"$iv\""
                                    } + "}"
                                }
                                else -> "\"$item\""
                            }
                        }
                        "\"$k\":[$items]"
                    }
                    else -> "\"$k\":\"$v\""
                }
            })
        }
        append("}}")
    }
}
