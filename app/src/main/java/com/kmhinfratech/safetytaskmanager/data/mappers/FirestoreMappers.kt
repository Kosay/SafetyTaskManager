package com.kmhinfratech.safetytaskmanager.data.mappers

// data/mappers/FirestoreMappers.kt
package com.kmhinfratech.safetytaskofficer.data.mappers

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.kmhinfratech.safetytaskofficer.data.model.Project
import com.kmhinfratech.safetytaskofficer.data.model.Task
import com.kmhinfratech.safetytaskofficer.data.model.TaskStatus

fun DocumentSnapshot.toTask(): Task {
    val id = id
    return Task(
        id = id,
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        location = getString("location") ?: "",
        riskCategory = getString("riskCategory") ?: "Low",
        date = getTimestamp("date") as Timestamp?,
        assignedTo = getString("assignedTo") ?: "",
        createdBy = getString("createdBy") ?: "",
        projectId = getString("projectId") ?: "",
        status = TaskStatus.from(getString("status")),
        closureNotes = getString("closureNotes"),
        closureImageUrls = (get("closureImageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )
}

fun DocumentSnapshot.toProject(): Project =
    Project(id = id, name = getString("name") ?: "")
