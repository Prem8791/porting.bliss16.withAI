package com.android.prodx.contract

data class ContractEnvelope(
    val version: Int,
    val schemaId: String,
    val payload: ByteArray
)
