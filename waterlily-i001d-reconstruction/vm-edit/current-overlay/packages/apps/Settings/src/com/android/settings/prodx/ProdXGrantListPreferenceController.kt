package com.android.settings.prodx

import android.app.prodx.ProdXManager

class ProdXGrantListPreferenceController(
    private val manager: ProdXManager? = null,
    private val userId: Int = 0,
) {
    fun getGrantCount(): Int = manager?.getAdminGrants(userId)?.size ?: 0
    fun getProviderCount(): Int = manager?.adminProviders?.size ?: 0
    fun getQuarantinedProviderCount(): Int = manager?.quarantinedProviders?.size ?: 0
    fun revokeGrant(grantId: String): Boolean = manager?.revokeGrantAuthenticated(grantId) ?: false
    fun suspendGrant(grantId: String): Boolean = manager?.suspendGrantAuthenticated(grantId) ?: false
    fun setProviderQuarantined(providerId: String, quarantined: Boolean): Boolean =
        manager?.setProviderQuarantinedAuthenticated(providerId, quarantined) ?: false
}
