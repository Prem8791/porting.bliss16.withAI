package com.android.prodx.sdk

data class StructuredError(val code: Int, val message: String, val details: Map<String, String> = emptyMap())
