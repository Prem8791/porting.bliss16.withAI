package com.android.prodx.runtime.audit

class RecoveryJournal {
    fun writeEntry(data: ByteArray): Boolean = true
    fun recover(): List<ByteArray> = emptyList()
}
