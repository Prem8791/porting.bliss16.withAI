package com.android.settings.prodx

import android.app.prodx.ProdXManager
import android.app.prodx.ProdXMode

class ProdXModePreferenceController(private val manager: ProdXManager? = null) {
    fun getCurrentModeLabel(): String =
        (manager?.settingsMode ?: ProdXMode.DISABLED).name.lowercase()
    fun setMode(mode: ProdXMode): Boolean = manager?.setModeAuthenticated(mode) ?: false
    fun emergencyDisable(): Boolean = manager?.emergencyDisableAuthenticated() ?: false
    fun isEmergencyDisabled(): Boolean = manager?.isEmergencyDisabled() ?: true
    fun hasAuthentication(): Boolean = manager?.hasSensitiveAdminAuthentication() ?: false
    fun requestAuthentication(challenge: ByteArray): Boolean =
        manager?.requestSensitiveAdminAuthentication(challenge) ?: false
}
