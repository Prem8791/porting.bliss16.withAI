package com.android.prodx.sdk

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class InventoryEntry(
    val providerId: String,
    val manifests: List<CapabilityManifest>,
    val registeredAt: Long = System.currentTimeMillis(),
)

class CapabilityInventory {
    private val providers = ConcurrentHashMap<String, InventoryEntry>()
    private val generation = AtomicLong(0)
    private val activeCapabilities = ConcurrentHashMap<String, String>() // capId -> providerId

    fun registerProvider(providerId: String, manifests: List<CapabilityManifest>) {
        val entry = InventoryEntry(providerId, manifests)
        providers[providerId] = entry
        manifests.forEach { activeCapabilities[it.capabilityId] = providerId }
        generation.incrementAndGet()
    }

    fun unregisterProvider(providerId: String): Boolean {
        val entry = providers.remove(providerId) ?: return false
        entry.manifests.forEach { activeCapabilities.remove(it.capabilityId) }
        generation.incrementAndGet()
        return true
    }

    fun getProviderManifests(providerId: String): List<CapabilityManifest>? =
        providers[providerId]?.manifests

    fun findCapabilityProvider(capabilityId: String): String? =
        activeCapabilities[capabilityId]

    fun findCapability(capabilityId: String): CapabilityManifest? {
        val providerId = activeCapabilities[capabilityId] ?: return null
        return providers[providerId]?.manifests?.firstOrNull { it.capabilityId == capabilityId }
    }

    fun getAllProviders(): Map<String, List<CapabilityManifest>> =
        providers.mapValues { it.value.manifests }

    fun getAllCapabilities(): List<CapabilityManifest> =
        providers.values.flatMap { it.manifests }

    fun getProviderCount(): Int = providers.size

    fun getCapabilityCount(): Int = activeCapabilities.size

    fun getCurrentGeneration(): Long = generation.get()

    fun hasCapability(capabilityId: String): Boolean =
        activeCapabilities.containsKey(capabilityId)

    fun hasProvider(providerId: String): Boolean =
        providers.containsKey(providerId)

    fun isProviderRegistered(providerId: String): Boolean =
        providers.containsKey(providerId)

    fun clear() {
        providers.clear()
        activeCapabilities.clear()
        generation.incrementAndGet()
    }
}
