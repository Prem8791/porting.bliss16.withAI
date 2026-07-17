package com.android.prodx.runtime.observation

data class SlidingWindowRate(
    val windowStartMs: Long,
    var count: Int
)

class BackpressureController(
    private var globalMaxEventsPerSecond: Int = 500,
    private var defaultMaxPerSource: Int = 50,
    private var defaultMaxPerConsumer: Int = 100
) {
    private val slidingWindows = mutableMapOf<String, SlidingWindowRate>()
    private val sourceWindows = mutableMapOf<String, SlidingWindowRate>()
    private val consumerWindows = mutableMapOf<String, SlidingWindowRate>()
    private var droppedEvents = mutableMapOf<String, Long>()
    private var totalDropped = 0L

    fun isAllowed(sourceId: String): Boolean {
        val globalAllowed = checkWindow("__global__", slidingWindows, globalMaxEventsPerSecond)
        if (!globalAllowed) return false

        val perSourceLimits = perSourceLimits[sourceId] ?: defaultMaxPerSource
        val sourceAllowed = checkWindow("source_$sourceId", sourceWindows, perSourceLimits)
        if (!sourceAllowed) return false

        return true
    }

    fun isConsumerAllowed(consumerToken: String): Boolean {
        val limits = perConsumerLimits[consumerToken] ?: defaultMaxPerConsumer
        return checkWindow("consumer_$consumerToken", consumerWindows, limits)
    }

    fun recordEvent(sourceId: String) {
        recordInWindow("__global__", slidingWindows, globalMaxEventsPerSecond)
        val limits = perSourceLimits[sourceId] ?: defaultMaxPerSource
        recordInWindow("source_$sourceId", sourceWindows, limits)
    }

    fun recordConsumerEvent(consumerToken: String) {
        val limits = perConsumerLimits[consumerToken] ?: defaultMaxPerConsumer
        recordInWindow("consumer_$consumerToken", consumerWindows, limits)
    }

    fun recordDropped(sourceId: String): Boolean {
        droppedEvents[sourceId] = (droppedEvents[sourceId] ?: 0L) + 1
        totalDropped++
        return true
    }

    fun getDroppedCount(sourceId: String): Long = droppedEvents[sourceId] ?: 0L

    fun getTotalDropped(): Long = totalDropped

    fun setGlobalRate(maxPerSecond: Int) {
        globalMaxEventsPerSecond = maxPerSecond
    }

    fun setSourceRate(sourceId: String, maxPerSecond: Int) {
        perSourceLimits[sourceId] = maxPerSecond
    }

    fun setConsumerRate(consumerToken: String, maxPerSecond: Int) {
        perConsumerLimits[consumerToken] = maxPerSecond
    }

    fun getCurrentGlobalRate(): Double {
        val window = slidingWindows["__global__"] ?: return 0.0
        val elapsed = (System.currentTimeMillis() - window.windowStartMs) / 1000.0
        return if (elapsed > 0) window.count / elapsed else 0.0
    }

    fun getCurrentSourceRate(sourceId: String): Double {
        val window = sourceWindows["source_$sourceId"] ?: return 0.0
        val elapsed = (System.currentTimeMillis() - window.windowStartMs) / 1000.0
        return if (elapsed > 0) window.count / elapsed else 0.0
    }

    fun reset() {
        slidingWindows.clear()
        sourceWindows.clear()
        consumerWindows.clear()
        droppedEvents.clear()
        totalDropped = 0L
    }

    private val perSourceLimits = mutableMapOf<String, Int>()
    private val perConsumerLimits = mutableMapOf<String, Int>()

    private fun checkWindow(key: String, windows: MutableMap<String, SlidingWindowRate>, maxRate: Int): Boolean {
        val now = System.currentTimeMillis()
        val window = windows[key] ?: return true
        if (now - window.windowStartMs > 1000) return true
        return window.count < maxRate
    }

    private fun recordInWindow(key: String, windows: MutableMap<String, SlidingWindowRate>, maxRate: Int) {
        val now = System.currentTimeMillis()
        val window = windows.getOrPut(key) { SlidingWindowRate(now, 0) }
        if (now - window.windowStartMs > 1000) {
            window.windowStartMs = now
            window.count = 1
        } else {
            window.count++
        }
    }
}
