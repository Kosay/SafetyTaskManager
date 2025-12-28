package com.kmhinfratech.safetytaskmanager
import com.google.firebase.firestore.PropertyName
data class Project(
    val id: String,
    val name: String,
    val kmlUrl: String,
    val defaultLat: Double = 24.2312,
    val defaultLng: Double = 55.7667
)
