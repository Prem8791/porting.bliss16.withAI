package com.home.launcher.system.platform

import android.app.ActivityManager
import android.app.ActivityTaskManager
import android.app.TaskStackListener
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.HardwareBuffer
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.os.UserHandle
import android.util.Log
import com.home.launcher.task.RecentTask
import com.home.launcher.task.RecentTasksBackend
import com.home.launcher.task.TaskListenerRegistration

class PlatformRecentTasksBackend(private val context: Context) : RecentTasksBackend {
    private val activityTaskManager: ActivityTaskManager = ActivityTaskManager.getInstance()
    private val handler = Handler(Looper.getMainLooper())
    private val snapshotCache = mutableMapOf<Int, Bitmap>()
    private var lastForegroundTaskId: Int? = null
    private var lastForegroundPackageName: String? = null

    override fun getRecentTasks(maxNum: Int): List<RecentTask> {
        return runCatching {
            val tasks = activityTaskManager.getRecentTasks(
                maxNum,
                ActivityManager.RECENT_WITH_EXCLUDED,
                UserHandle.USER_CURRENT
            )
            tasks.mapNotNull { task: ActivityManager.RecentTaskInfo ->
                val component = resolveTaskComponent(task)
                val packageName = component?.packageName
                    ?: task.baseIntent?.`package`
                    ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                val label = resolveTaskLabel(task, component, packageName)
                RecentTask(task.taskId, packageName, label, task.userId)
            }
        }.onFailure {
            Log.e(TAG, "getRecentTasks failed", it)
        }.getOrDefault(emptyList())
    }

    private fun resolveTaskComponent(task: ActivityManager.RecentTaskInfo): ComponentName? {
        return task.realActivity
            ?: task.baseIntent?.component
            ?: task.origActivity
            ?: task.topActivity
            ?: task.baseActivity
    }

    private fun resolveTaskLabel(
        task: ActivityManager.RecentTaskInfo,
        component: ComponentName?,
        packageName: String
    ): String {
        val taskLabel = task.taskDescription?.label?.toString()
        if (!taskLabel.isNullOrBlank()) return taskLabel

        return runCatching {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        }.getOrElse {
            component?.shortClassName ?: packageName
        }
    }

    override fun removeTask(taskId: Int): Boolean {
        return runCatching {
            val removed = activityTaskManager.removeTask(taskId)
            Log.i(TAG, "removeTask($taskId) success=$removed")
            removed
        }.onFailure {
            Log.e(TAG, "removeTask($taskId) failed", it)
        }.getOrDefault(false)
    }

    override fun removeAllVisibleRecentTasks(): Boolean {
        val taskIds = getRecentTasks(MAX_RECENTS_TO_REMOVE).map { task: RecentTask -> task.taskId }
        if (taskIds.isEmpty()) return false
        val removedCount = taskIds.count { taskId: Int -> removeTask(taskId) }
        Log.i(TAG, "removeAllVisibleRecentTasks removed $removedCount/${taskIds.size} tasks")
        return removedCount > 0
    }

    override fun startTaskFromRecents(taskId: Int): Boolean {
        return runCatching {
            val result = ActivityTaskManager.getService()
                .startActivityFromRecents(taskId, Bundle())
            Log.i(TAG, "startActivityFromRecents($taskId) result=$result")
            true
        }.onFailure {
            Log.e(TAG, "startActivityFromRecents($taskId) failed", it)
        }.getOrDefault(false)
    }

    override fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean): Bitmap? {
        snapshotCache[taskId]?.let { return it }
        val bitmap = readTaskSnapshot(taskId, isLowResolution)
        if (bitmap != null && !isProbablyBlank(bitmap)) {
            snapshotCache[taskId] = bitmap
            return bitmap
        }
        return null
    }

    private fun readTaskSnapshot(taskId: Int, isLowResolution: Boolean): Bitmap? {
        return runCatching {
            val snapshot = ActivityTaskManager.getService()
                .getTaskSnapshot(taskId, isLowResolution) ?: return@runCatching null
            val hardwareBuffer: HardwareBuffer = snapshot.hardwareBuffer ?: return@runCatching null
            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
            hardwareBuffer.close()

            if (bitmap == null) {
                Log.w(TAG, "snapshot bitmap unavailable for task $taskId")
                return@runCatching null
            }

            bitmap
        }.onFailure {
            Log.w(TAG, "getTaskSnapshot failed for task $taskId", it)
        }.getOrNull()
    }

    private fun captureTaskAfterBackground(
        taskId: Int,
        onSnapshotChanged: (snapshotTaskId: Int?) -> Unit
    ) {
        handler.postDelayed({
            val bitmap = readTaskSnapshot(taskId, false)
            if (bitmap == null) {
                Log.d(TAG, "No background snapshot available for task $taskId")
                return@postDelayed
            }
            if (isProbablyBlank(bitmap)) {
                Log.d(TAG, "Ignoring probably blank background snapshot for task $taskId")
                return@postDelayed
            }
            snapshotCache[taskId] = bitmap
            Log.d(TAG, "Cached background snapshot for task $taskId")
            onSnapshotChanged(taskId)
        }, SNAPSHOT_CAPTURE_DELAY_MS)
    }

    private fun isProbablyBlank(bitmap: Bitmap): Boolean {
        val softwareBitmap = runCatching {
            if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
        }.getOrNull() ?: return false

        if (softwareBitmap.width <= 0 || softwareBitmap.height <= 0) return true

        var minLuma = 255
        var maxLuma = 0
        var samples = 0
        val stepX = (softwareBitmap.width / BLANK_SAMPLE_GRID).coerceAtLeast(1)
        val stepY = (softwareBitmap.height / BLANK_SAMPLE_GRID).coerceAtLeast(1)

        var y = stepY / 2
        while (y < softwareBitmap.height) {
            var x = stepX / 2
            while (x < softwareBitmap.width) {
                val color = softwareBitmap.getPixel(x, y)
                val alpha = color ushr 24
                if (alpha > 8) {
                    val red = color shr 16 and 0xff
                    val green = color shr 8 and 0xff
                    val blue = color and 0xff
                    val luma = (red * 30 + green * 59 + blue * 11) / 100
                    minLuma = minOf(minLuma, luma)
                    maxLuma = maxOf(maxLuma, luma)
                    samples++
                }
                x += stepX
            }
            y += stepY
        }

        if (softwareBitmap !== bitmap) {
            softwareBitmap.recycle()
        }

        return samples > 0 && (maxLuma - minLuma) < BLANK_LUMA_RANGE_THRESHOLD
    }

    override fun forceStopPackage(packageName: String): Boolean {
        return runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.forceStopPackage(packageName)
            Log.i(TAG, "forceStopPackage($packageName) invoked")
            true
        }.onFailure {
            Log.e(TAG, "forceStopPackage($packageName) failed", it)
        }.getOrDefault(false)
    }

    override fun registerTaskChangeListener(
        onChanged: (snapshotTaskId: Int?) -> Unit
    ): TaskListenerRegistration? {
        val listener = object : TaskStackListener() {
            override fun onTaskStackChanged() {
                onChanged(null)
            }

            override fun onTaskCreated(taskId: Int, componentName: android.content.ComponentName?) {
                onChanged(null)
            }

            override fun onTaskRemoved(taskId: Int) {
                snapshotCache.remove(taskId)
                onChanged(null)
            }

            override fun onTaskMovedToFront(taskInfo: ActivityManager.RunningTaskInfo?) {
                val taskId = taskInfo?.taskId
                val packageName = taskInfo?.topActivity?.packageName
                val previousTaskId = lastForegroundTaskId
                val previousPackageName = lastForegroundPackageName
                if (
                    previousTaskId != null &&
                    previousTaskId != taskId &&
                    previousPackageName != context.packageName
                ) {
                    captureTaskAfterBackground(previousTaskId, onChanged)
                }
                if (taskId != null) {
                    lastForegroundTaskId = taskId
                    lastForegroundPackageName = packageName
                }
                onChanged(null)
            }
        }

        return runCatching {
            ActivityTaskManager.getService().registerTaskStackListener(listener)
            Log.i(TAG, "TaskStackListener registered via ActivityTaskManager")
            TaskListenerRegistration {
                runCatching {
                    ActivityTaskManager.getService().unregisterTaskStackListener(listener)
                    Log.i(TAG, "TaskStackListener unregistered via ActivityTaskManager")
                }.onFailure {
                    Log.e(TAG, "unregisterTaskStackListener failed", it)
                }
            }
        }.onFailure {
            Log.e(TAG, "registerTaskStackListener failed", it)
        }.getOrNull()
    }

    private companion object {
        const val TAG = "PlatformRecentsBackend"
        const val MAX_RECENTS_TO_REMOVE = 100
        const val SNAPSHOT_CAPTURE_DELAY_MS = 350L
        const val BLANK_SAMPLE_GRID = 10
        const val BLANK_LUMA_RANGE_THRESHOLD = 6
    }
}
