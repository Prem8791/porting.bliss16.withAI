package com.android.prodx.sdk

class CancellationToken {
    @Volatile private var cancelled = false
    fun cancel() { cancelled = true }
    fun isCancelled(): Boolean = cancelled
}
