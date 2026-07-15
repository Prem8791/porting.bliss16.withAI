package com.android.prodx.runtime.extension

class QuarantineStore {
    fun quarantine(extensionId: String): Boolean = true
    fun release(extensionId: String): Boolean = true
    fun isQuarantined(extensionId: String): Boolean = false
}
