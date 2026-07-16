package com.android.settings.prodx

import android.app.prodx.ProdXManager

class ProdXHealthPreferenceController(private val manager: ProdXManager? = null) {
    fun getHealthStatus(): String = manager?.settingsHealth?.status ?: "service_unavailable"
    fun isOperational(): Boolean = manager?.settingsHealth?.isOperational ?: false
    fun getRegistryGeneration(): Long = manager?.registryGeneration?.generationId ?: 0L
}
