package com.android.prodx.runtime.observation

class ObservationQueue {
    private val maxSize = 1000
    fun enqueue(data: ByteArray): Boolean = true
    fun dequeue(): ByteArray? = null
    fun size(): Int = 0
}
