package com.home.launcher.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.home.launcher.data.SystemStats
import com.home.launcher.data.SystemStatsProvider

class SystemStatsBar(
    private val context: Context,
    private val batteryView: TextView,
    private val ramView: TextView,
    private val cpuView: TextView,
    private val tempView: TextView,
    private val storageView: TextView
) {
    private val provider = SystemStatsProvider(context)
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            refresh()
            handler.postDelayed(this, 2000)
        }
    }
    private var running = false

    fun start() {
        if (running) return
        running = true
        refresh()
        handler.postDelayed(refreshRunnable, if (shouldThrottle()) 10000 else 2000)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(refreshRunnable)
    }

    fun refresh() {
        val stats = provider.getStats()
        batteryView.text = buildString {
            append(if (stats.isCharging) "🔌 " else "🔋 ")
            append(stats.batteryPercent)
            append("%")
        }
        ramView.text = "🧠 ${stats.ramUsedMb}/${stats.ramTotalMb}GB"
        val cpuText = if (stats.cpuPercent < 0) "⏸" else "⚙️ ${stats.cpuPercent}%"
        cpuView.text = cpuText
        tempView.text = "🌡 ${"%.1f".format(stats.temperatureCelsius)}°C"
        storageView.text = "💾 ${stats.storagePercent}%"
    }

    fun shouldThrottle(): Boolean = provider.shouldThrottleCpuPoll()
}
