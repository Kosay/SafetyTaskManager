package com.kmhinfratech.safetytaskmanager

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = "",
    val description: String = "",
    val status: String = "",
    val riskCategory: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val assignedTo: String = ""
)