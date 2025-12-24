package com.kmhinfratech.safetytaskmanager.data.model


import com.google.firebase.Timestamp

enum class TaskStatus(val raw: String) {
    Pending("pending"),
    InProgress("in-progress"),
    Submitted("submitted"),
    Completed("completed");

    companion object {
        fun from(raw: String?): TaskStatus =
            values().firstOrNull { it.raw == raw } ?: Pending
    }
}

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val riskCategory: String = "Low",
    val date: Timestamp? = null, // due date in your schema
    val assignedTo: String = "",
    val createdBy: String = "",
    val projectId: String = "",
    val status: TaskStatus = TaskStatus.Pending,
    val closureNotes: String? = null,
    val closureImageUrls: List<String> = emptyList(),
)

data class Project(
    val id: String = "",
    val name: String = "",
)

data class TaskUi(
    val task: Task,
    val projectName: String = "",
)
