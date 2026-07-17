package com.android.prodx.runtime.observation

import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.android.prodx.runtime.IProdXSourceAdapter

data class SourceEntry(
    val sourceId: String,
    val sourceType: String,
    val adapterBinder: IBinder,
    val registeredAt: Long = System.currentTimeMillis(),
    var active: Boolean = true,
    var lastHealthCheckMs: Long = System.currentTimeMillis(),
    var consecutiveFailures: Int = 0
) {
    fun toSummary(): String = "$sourceId($sourceType) active=$active failures=$consecutiveFailures"
}

class SourceRegistry {
    private val sources = mutableMapOf<String, SourceEntry>()
    private val VALID_SOURCE_TYPES = setOf(
        "telemetry", "security", "health", "performance",
        "network", "accessibility", "overlay", "credential",
        "package", "permission", "device_admin", "system"
    )

    fun registerSource(adapterBinder: IBinder): Boolean {
        val adapter = IProdXSourceAdapter.Stub.asInterface(adapterBinder)
        val sourceId: String
        val sourceType: String
        try {
            sourceId = adapter.sourceId
            sourceType = adapter.sourceType
        } catch (e: RemoteException) {
            Log.e("SourceRegistry", "Failed to read source adapter", e)
            return false
        }

        if (sources.containsKey(sourceId)) return false
        if (!isValidSourceType(sourceType)) return false

        val entry = SourceEntry(
            sourceId = sourceId,
            sourceType = sourceType,
            adapterBinder = adapterBinder
        )
        sources[sourceId] = entry
        return true
    }

    fun registerSource(sourceId: String, sourceType: String, adapterBinder: IBinder? = null): Boolean {
        if (sources.containsKey(sourceId)) return false
        if (!isValidSourceType(sourceType)) return false
        val entry = SourceEntry(
            sourceId = sourceId,
            sourceType = sourceType,
            adapterBinder = adapterBinder ?: object : android.os.Binder() {}
        )
        sources[sourceId] = entry
        return true
    }

    fun unregisterSource(sourceId: String): Boolean {
        val entry = sources[sourceId] ?: return false
        entry.active = false
        sources.remove(sourceId)
        return true
    }

    fun getSource(sourceId: String): SourceEntry? = sources[sourceId]

    fun getAdapter(sourceId: String): IProdXSourceAdapter? {
        val entry = sources[sourceId] ?: return null
        return IProdXSourceAdapter.Stub.asInterface(entry.adapterBinder)
    }

    fun getAllSources(): List<SourceEntry> = sources.values.toList()

    fun getActiveSources(): List<SourceEntry> = sources.values.filter { it.active }

    fun getSourcesByType(sourceType: String): List<SourceEntry> =
        sources.values.filter { it.sourceType == sourceType }

    fun checkHealth(sourceId: String): Boolean {
        val entry = sources[sourceId] ?: return false
        return try {
            val adapter = IProdXSourceAdapter.Stub.asInterface(entry.adapterBinder)
            val active = adapter.isActive
            entry.active = active
            entry.lastHealthCheckMs = System.currentTimeMillis()
            if (active) entry.consecutiveFailures = 0
            else entry.consecutiveFailures++
            active
        } catch (e: RemoteException) {
            entry.consecutiveFailures++
            entry.active = false
            false
        }
    }

    fun checkAllHealth(): Map<String, Boolean> {
        return sources.keys.associateWith { checkHealth(it) }
    }

    fun sourceCount(): Int = sources.size

    fun activeSourceCount(): Int = sources.values.count { it.active }

    fun reset() {
        sources.clear()
    }

    fun isValidSourceType(type: String): Boolean = type in VALID_SOURCE_TYPES

    fun getRegisteredTypes(): Set<String> =
        sources.values.map { it.sourceType }.toSet()
}
