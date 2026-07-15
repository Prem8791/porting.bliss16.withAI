package com.android.prodx.contract.Objects

data class ObservationRecord(
    val sourceId: String,
    val eventType: String,
    val payload: ByteArray
)
