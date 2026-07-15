package com.android.prodx.runtime.observation

data class Lease(val id: String, val sourceId: String, val expiresAt: Long)

class LeaseManager {
    private val leases = mutableListOf<Lease>()
    fun createLease(sourceId: String, ttlMs: Long): Lease? = null
    fun revokeLease(leaseId: String): Boolean = false
    fun getActiveLeases(): List<Lease> = leases.toList()
}
