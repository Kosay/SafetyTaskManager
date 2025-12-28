package com.kmhinfratech.safetytaskmanager

data class Notification(
    val id: String = "",
    val taskId: String = "",
    val taskTitle: String = "",
    val message: String = "",
    val read: Boolean = false,
    val userId: String = ""
)