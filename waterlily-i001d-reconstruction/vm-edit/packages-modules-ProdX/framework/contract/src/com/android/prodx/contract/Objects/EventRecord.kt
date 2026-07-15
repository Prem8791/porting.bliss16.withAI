package com.android.prodx.contract.Objects

data class EventRecord(
    val eventId: String,
    val sourceId: String,
    val timestamp: Long,
    val data: ByteArray
)
