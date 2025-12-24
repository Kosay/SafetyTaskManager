package com.kmhinfratech.safetytaskmanager.data.repo

// data/repo/TaskRepository.kt
package com.kmhinfratech.safetytaskofficer.data.repo

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.kmhinfratech.safetytaskofficer.data.mappers.toProject
import com.kmhinfratech.safetytaskofficer.data.mappers.toTask
import com.kmhinfratech.safetytaskofficer.data.model.Project
import com.kmhinfratech.safetytaskofficer.data.model.Task
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) {
    private val tasksCol = db.collection("Tasks")
    private val projectsCol = db.collection("projects")

    suspend fun getAssignedTasksOnce(officerUid: String): List<Task> {
        val snap = tasksCol.whereEqualTo("assignedTo", officerUid).get().await()
        return snap.documents.map { it.toTask() }
    }

    suspend fun getProjectsByIds(projectIds: Set<String>): Map<String, Project> {
        if (projectIds.isEmpty()) return emptyMap()
        // Batched reads: Firestore getAll supports up to 500 docs per call (practically you’ll be far below).
        val refs = projectIds.map { projectsCol.document(it) }
        val docs = db.getAll(*refs.toTypedArray()).await()
        return docs.associate { it.id to it.toProject() }
    }

    suspend fun setTaskStatus(taskId: String, status: String) {
        tasksCol.document(taskId).update(
            mapOf(
                "status" to status,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun uploadEvidence(taskId: String, localUri: Uri): String {
        // Requires network. If offline, caller should show an error.
        val fileName = "${UUID.randomUUID()}.jpg"
        val path = "task_evidence/$taskId/$fileName"
        val ref = storage.reference.child(path)

        ref.putFile(localUri).await()
        val downloadUrl = ref.downloadUrl.await()
        return downloadUrl.toString()
    }

    suspend fun submitTaskForApproval(
        taskId: String,
        closureNotes: String,
        imageUrls: List<String>,
    ) {
        tasksCol.document(taskId).update(
            mapOf(
                "status" to "submitted",
                "closureNotes" to closureNotes,
                "closureImageUrls" to imageUrls,
                "submittedAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun getTask(taskId: String): Task {
        val doc = tasksCol.document(taskId).get().await()
        return doc.toTask()
    }

    suspend fun getProjectName(projectId: String): String {
        val doc = projectsCol.document(projectId).get().await()
        return doc.getString("name") ?: ""
    }
}
