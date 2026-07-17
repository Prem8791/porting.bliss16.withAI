package com.home.launcher.task

import android.graphics.Bitmap

interface RecentTasksBackend {
    fun getRecentTasks(maxNum: Int): List<RecentTask>
    fun removeTask(taskId: Int): Boolean
    fun removeAllVisibleRecentTasks(): Boolean
    fun startTaskFromRecents(taskId: Int): Boolean
    fun getTaskSnapshot(taskId: Int, isLowResolution: Boolean = false): Bitmap?
    fun forceStopPackage(packageName: String): Boolean
    fun registerTaskChangeListener(onChanged: (snapshotTaskId: Int?) -> Unit): TaskListenerRegistration?
}
