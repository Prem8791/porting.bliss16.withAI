package com.android.prodx.runtime.observation

import android.app.prodx.ProdXSubscriptionLease

data class Lease(
    val id: String,
    val sourceId: String,
    val consumerToken: String,
    val createdAt: Long,
    val ttlMs: Long,
    val policySpec: String = ""
) {
    val expiresAt: Long get() = createdAt + ttlMs
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
    val remainingMs: Long get() = (expiresAt - System.currentTimeMillis()).coerceAtLeast(0)
}

class LeaseManager {
    private val leases = mutableMapOf<String, Lease>()
    private val authorityPolicy = AuthorityPolicy()

    fun createLease(sourceId: String, consumerToken: String, ttlMs: Long, policySpec: String = ""): Lease? {
        if (!authorityPolicy.isLeaseCreationAllowed(sourceId, ttlMs)) return null
        if (ttlMs <= 0 || ttlMs > authorityPolicy.maxTtlMs) return null

        val leaseId = "lease_${sourceId}_${System.currentTimeMillis()}"
        val lease = Lease(
            id = leaseId,
            sourceId = sourceId,
            consumerToken = consumerToken,
            createdAt = System.currentTimeMillis(),
            ttlMs = ttlMs.coerceAtMost(authorityPolicy.maxTtlMs),
            policySpec = policySpec
        )
        leases[leaseId] = lease
        return lease
    }

    fun revokeLease(leaseId: String): Boolean {
        return leases.remove(leaseId) != null
    }

    fun getLease(leaseId: String): Lease? = leases[leaseId]

    fun getActiveLeases(): List<Lease> {
        enforceExpiry()
        return leases.values.toList()
    }

    fun getLeasesForConsumer(consumerToken: String): List<Lease> {
        enforceExpiry()
        return leases.values.filter { it.consumerToken == consumerToken }
    }

    fun getLeasesForSource(sourceId: String): List<Lease> {
        enforceExpiry()
        return leases.values.filter { it.sourceId == sourceId }
    }

    fun renewLease(leaseId: String, additionalMs: Long): Boolean {
        val existing = leases[leaseId] ?: return false
        if (existing.isExpired) return false
        if (additionalMs <= 0 || existing.remainingMs + additionalMs > authorityPolicy.maxTtlMs) return false

        leases[leaseId] = existing.copy(
            ttlMs = existing.ttlMs + additionalMs
        )
        return true
    }

    fun activeLeaseCount(): Int {
        enforceExpiry()
        return leases.size
    }

    fun reset() {
        leases.clear()
    }

    fun setAuthorityPolicy(policy: AuthorityPolicy) {
        authorityPolicy.maxTtlMs = policy.maxTtlMs
        authorityPolicy.maxLeasesPerSource = policy.maxLeasesPerSource
        authorityPolicy.maxLeasesPerConsumer = policy.maxLeasesPerConsumer
    }

    private fun enforceExpiry() {
        val now = System.currentTimeMillis()
        leases.entries.removeAll { (_, lease) -> lease.isExpired }
    }

    class AuthorityPolicy(
        var maxTtlMs: Long = 86_400_000L,
        var maxLeasesPerSource: Int = 10,
        var maxLeasesPerConsumer: Int = 50
    ) {
        fun isLeaseCreationAllowed(sourceId: String, ttlMs: Long): Boolean {
            if (ttlMs > maxTtlMs) return false
            val sourceLeases = leases.values.count { it.sourceId == sourceId }
            if (sourceLeases >= maxLeasesPerSource) return false
            return true
        }
    }
}
