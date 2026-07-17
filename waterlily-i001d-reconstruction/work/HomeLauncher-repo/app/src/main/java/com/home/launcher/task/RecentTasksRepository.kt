package com.home.launcher.task

import android.content.Context
import android.graphics.Bitmap
import android.util.Log

class RecentTasksRepository(
    context: Context,
    private val backend: RecentTasksBackend = createBestBackend(context)
) {
    fun getRecentTasks(maxNum: Int): List<RecentTask> = backend.getRecentTasks(maxNum)

    fun removeTask(taskId: Int): Boolean = backend.removeTask(taskId)

    fun removeAllVisibleRecentTasks(): Boolean = backend.removeAllVisibleRecentTasks()

    fun startTaskFromRecents(taskId: Int): Boolean = backend.startTaskFromRecents(taskId)

    fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean = false): Bitmap? =
        backend.getTaskSnapshot(taskId, isLowResolution)

    fun forceStopPackage(packageName: String): Boolean = backend.forceStopPackage(packageName)

    fun registerTaskChangeListener(onChanged: (snapshotTaskId: Int?) -> Unit): TaskListenerRegistration? =
        backend.registerTaskChangeListener(onChanged)

    private companion object {
        private const val TAG = "RecentTasksRepo"

        fun createBestBackend(context: Context): RecentTasksBackend {
            Log.w(TAG, "Platform recents backend is unavailable outside the AOSP source set")
            return UnavailableRecentTasksBackend
        }
    }
}

private object UnavailableRecentTasksBackend : RecentTasksBackend {
    override fun getRecentTasks(maxNum: Int): List<RecentTask> = emptyList()
    override fun removeTask(taskId: Int): Boolean = false
    override fun removeAllVisibleRecentTasks(): Boolean = false
    override fun startTaskFromRecents(taskId: Int): Boolean = false
    override fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean): Bitmap? = null
    override fun forceStopPackage(packageName: String): Boolean = false
    override fun registerTaskChangeListener(
        onChanged: (snapshotTaskId: Int?) -> Unit
    ): TaskListenerRegistration? = null
}
