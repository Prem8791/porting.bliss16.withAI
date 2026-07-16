package com.android.prodx.runtime.broker

class BrokerCheckpointStore {
    fun save(checkpoint: ByteArray): Boolean = true
    fun load(): ByteArray? = null
}
