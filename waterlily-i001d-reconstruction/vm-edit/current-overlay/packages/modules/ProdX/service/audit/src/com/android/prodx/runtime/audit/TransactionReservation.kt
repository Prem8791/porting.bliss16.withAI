package com.android.prodx.runtime.audit

data class TransactionReservation(
    val id: String,
    val transactionHash: ByteArray,
    val userId: Int,
    val riskLevel: Int,
    val createdAtMillis: Long,
    val expiresAtMillis: Long,
    var state: State = State.ACTIVE,
) {
    enum class State { ACTIVE, COMPLETED, CANCELLED, EXPIRED }

    fun isActive(nowMillis: Long): Boolean {
        if (state == State.ACTIVE && nowMillis >= expiresAtMillis) state = State.EXPIRED
        return state == State.ACTIVE
    }
}
