package com.home.launcher.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader

data class SystemStats(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val cpuPercent: Int,
    val temperatureCelsius: Float,
    val storagePercent: Int
)

class SystemStatsProvider(private val context: Context) {

    private var lastCpuTicks: LongArray? = null
    private var lastCpuTime: Long = 0
    private var consecutiveCpuDenials: Int = 0

    companion object {
        private const val TAG = "SystemStats"
        private const val CPU_FAILURE_BACKOFF = 10
    }

    fun shouldThrottleCpuPoll(): Boolean = consecutiveCpuDenials >= CPU_FAILURE_BACKOFF

    fun getStats(): SystemStats {
        val batteryPercent = getBatteryPercent()
        val isCharging = isCharging()
        val (ramUsed, ramTotal) = getRamInfo()
        val cpuPercent = getCpuUsage()
        val temp = getTemperature()
        val storagePercent = getStoragePercent()

        return SystemStats(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            ramUsedMb = ramUsed,
            ramTotalMb = ramTotal,
            cpuPercent = cpuPercent,
            temperatureCelsius = temp,
            storagePercent = storagePercent
        )
    }

    private fun getBatteryPercent(): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return 0
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        return (level * 100) / scale
    }

    private fun isCharging(): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun getRamInfo(): Pair<Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val usedMb = (memInfo.totalMem - memInfo.availMem) / (1024 * 1024)
        return Pair(usedMb, totalMb)
    }

    private fun getCpuUsage(): Int {
        if (consecutiveCpuDenials >= CPU_FAILURE_BACKOFF) {
            return -1
        }
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine() ?: return 0
            reader.close()

            val parts = line.split("\\s+".toRegex())
            if (parts.size < 5) return 0

            val user = parts[1].toLong()
            val nice = parts[2].toLong()
            val system = parts[3].toLong()
            val idle = parts[4].toLong()
            val total = user + nice + system + idle

            val currentTicks = longArrayOf(user, nice, system, idle)
            val currentTime = SystemClock.elapsedRealtime()

            val previousTicks = lastCpuTicks
            if (previousTicks != null && lastCpuTime > 0) {
                val deltaIdle = idle - previousTicks[3]
                val deltaTotal = total - previousTicks.sum()

                val percent = if (deltaTotal > 0) {
                    ((deltaTotal - deltaIdle) * 100) / deltaTotal
                } else 0

                lastCpuTicks = currentTicks
                lastCpuTime = currentTime
                consecutiveCpuDenials = 0
                return percent.toInt().coerceIn(0, 100)
            }

            lastCpuTicks = currentTicks
            lastCpuTime = currentTime
            consecutiveCpuDenials = 0
            0
        } catch (e: Exception) {
            consecutiveCpuDenials++
            if (consecutiveCpuDenials >= CPU_FAILURE_BACKOFF) {
                Log.w(TAG, "CPU read failed $consecutiveCpuDenials times; throttling")
            }
            0
        }
    }

    private fun getTemperature(): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return 0f
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        return temp / 10.0f
    }

    private fun getStoragePercent(): Int {
        return try {
            val stat = StatFs(Environment.getDataDirectory().absolutePath)
            val total = stat.totalBytes
            val free = stat.availableBytes
            if (total > 0) {
                ((total - free) * 100 / total).toInt()
            } else 0
        } catch (e: Exception) {
            0
        }
    }
}
