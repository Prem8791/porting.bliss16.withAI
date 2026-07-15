package com.android.prodx.runtime.audit

data class TransactionReservation(val id: String, val transactionHash: ByteArray) {
    val isActive: Boolean get() = true
    fun cancel() {}
}
