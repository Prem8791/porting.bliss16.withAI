package com.android.prodx.sdk

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

class CancellationToken {
    @Volatile private var cancelled = false
    @Volatile private var timeoutMs: Long = 0L
    @Volatile private var startTime: Long = 0L
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val children = CopyOnWriteArrayList<CancellationToken>()
    @Volatile private var parent: CancellationToken? = null
    @Volatile private var timerThread: Thread? = null

    fun cancel() {
        if (cancelled) return
        cancelled = true
        timerThread?.interrupt()
        timerThread = null
        listeners.forEach { it.invoke() }
        children.forEach { it.cancel() }
    }

    fun isCancelled(): Boolean {
        if (parent?.isCancelled() == true) {
            cancelled = true
        }
        return cancelled || (timeoutMs > 0 && startTime > 0 &&
            System.currentTimeMillis() - startTime >= timeoutMs)
    }

    fun setTimeout(ms: Long): CancellationToken {
        timeoutMs = ms
        startTime = System.currentTimeMillis()
        timerThread = thread(isDaemon = true, name = "token-timeout-$ms") {
            try {
                Thread.sleep(ms)
                cancel()
            } catch (_: InterruptedException) {
            }
        }
        return this
    }

    fun onCancelled(listener: () -> Unit): CancellationToken {
        listeners.add(listener)
        return this
    }

    fun removeListener(listener: () -> Unit): Boolean = listeners.remove(listener)

    fun composeChild(): CancellationToken {
        val child = CancellationToken()
        child.parent = this
        children.add(child)
        return child
    }

    fun removeChild(child: CancellationToken): Boolean = children.remove(child)
}
