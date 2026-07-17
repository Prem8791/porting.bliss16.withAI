package com.android.prodx.runtime.observation

data class QueueEntry(
    val id: String,
    val data: ByteArray,
    val enqueuedAt: Long,
    val sourceId: String,
    val sequenceNumber: Long,
    val isGapMarker: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QueueEntry) return false
        return id == other.id && sequenceNumber == other.sequenceNumber
    }

    override fun hashCode(): Int = id.hashCode() * 31 + sequenceNumber.hashCode()
}

class ObservationQueue(
    private var maxSize: Int = 1000
) {
    private val queue = ArrayDeque<QueueEntry>()
    private var sequenceCounter = 0L
    private var droppedCount = 0L
    private var watermarkHigh = 0
    private var watermarkLow = 0
    private var backpressureThreshold = (maxSize * 0.8).toInt()

    fun enqueue(data: ByteArray, sourceId: String): Boolean {
        val entry = QueueEntry(
            id = "evt_${sourceId}_${System.currentTimeMillis()}_${sequenceCounter}",
            data = data,
            enqueuedAt = System.currentTimeMillis(),
            sourceId = sourceId,
            sequenceNumber = sequenceCounter++
        )

        if (size() >= maxSize) {
            droppedCount++
            val dropped = queue.removeFirstOrNull()
            if (dropped != null) {
                val gapMarker = QueueEntry(
                    id = "gap_${sourceId}_${System.currentTimeMillis()}",
                    data = ByteArray(0),
                    enqueuedAt = System.currentTimeMillis(),
                    sourceId = sourceId,
                    sequenceNumber = entry.sequenceNumber,
                    isGapMarker = true
                )
                queue.addFirst(gapMarker)
            }
            return false
        }

        queue.addLast(entry)
        updateWatermarks()
        return true
    }

    fun dequeue(): QueueEntry? {
        val entry = queue.removeFirstOrNull()
        updateWatermarks()
        return entry
    }

    fun peek(): QueueEntry? = queue.firstOrNull()

    fun size(): Int = queue.size

    fun isFull(): Boolean = queue.size >= maxSize

    fun isBackpressureActive(): Boolean = queue.size >= backpressureThreshold

    fun remaining(): Int = maxSize - queue.size

    fun clear() {
        queue.clear()
        sequenceCounter = 0L
    }

    fun setMaxSize(size: Int) {
        maxSize = size
        backpressureThreshold = (maxSize * 0.8).toInt()
        while (queue.size > maxSize) {
            droppedCount++
            queue.removeFirstOrNull()
        }
        updateWatermarks()
    }

    fun getMaxSize(): Int = maxSize

    fun getDroppedCount(): Long = droppedCount

    fun getWatermarkHigh(): Int = watermarkHigh

    fun getWatermarkLow(): Int = watermarkLow

    fun getSequenceCounter(): Long = sequenceCounter

    fun toList(): List<QueueEntry> = queue.toList()

    private fun updateWatermarks() {
        watermarks.add(size())
        if (watermarks.size > 100) watermarks.removeFirst()
        watermarkHigh = watermarks.maxOrNull() ?: 0
        watermarkLow = watermarks.minOrNull() ?: 0
    }

    private val watermarks = ArrayDeque<Int>()
}
