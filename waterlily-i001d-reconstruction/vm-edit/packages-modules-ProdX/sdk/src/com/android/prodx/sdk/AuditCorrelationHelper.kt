package com.android.prodx.sdk

class AuditCorrelationHelper {
    fun generateCorrelationId(): String = java.util.UUID.randomUUID().toString()
}
