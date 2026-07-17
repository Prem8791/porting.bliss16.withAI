package com.home.launcher.task

data class RecentTask(
    val taskId: Int,
    val packageName: String,
    val label: String?,
    val userId: Int
)
