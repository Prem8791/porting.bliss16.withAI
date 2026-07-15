package com.android.prodx.contract

class CanonicalJsonProjection {
    fun toJson(data: Map<String, Any?>): String = "{}"
    fun fromJson(json: String): Map<String, Any?> = emptyMap()
}
