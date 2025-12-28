package com.kmhinfratech.safetytaskmanager

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TaskDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var taskId: String? = null
    private var currentStatus: String = "pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)
        val taskId = intent.getStringExtra("TASK_ID")


        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        if (taskId != null) {
            loadTaskDetails(taskId!!)
        }

        findViewById<Button>(R.id.btnMainAction).setOnClickListener {
            handleStatusTransition()
        }
    }

    private fun loadTaskDetails(id: String) {
        db.collection("Tasks").document(id).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val task = snapshot.toObject(Task::class.java)
                task?.let {
                    findViewById<TextView>(R.id.tvDetailTitle).text = it.title
                    findViewById<TextView>(R.id.tvDetailLocation).text = it.location
                    findViewById<TextView>(R.id.tvDetailDesc).text = it.description

                    val statusLabel = findViewById<TextView>(R.id.tvDetailStatus)
                    statusLabel.text = it.status.uppercase()

                    currentStatus = it.status
                    updateUIBasedOnStatus(it.status, statusLabel)
                }
            }
        }
    }

    private fun updateUIBasedOnStatus(status: String, label: TextView) {
        val btn = findViewById<Button>(R.id.btnMainAction)

        when (status) {
            "pending" -> {
                label.setBackgroundColor(getColor(R.color.primary_red))
                btn.text = "START PROGRESS"
                btn.visibility = View.VISIBLE
                btn.isEnabled = true
            }
            "in-progress" -> {
                label.setBackgroundColor(getColor(R.color.primary_blue))
                btn.text = "MARK AS SUBMITTED"
                btn.visibility = View.VISIBLE
                btn.isEnabled = true
            }
            "submitted", "completed" -> {
                label.setBackgroundColor(android.graphics.Color.GRAY)
                btn.text = "TASK COMPLETED"
                btn.isEnabled = false
            }
        }
    }

    private fun handleStatusTransition() {
        val nextStatus = when (currentStatus) {
            "pending" -> "in-progress"
            "in-progress" -> "submitted"
            else -> null
        }

        if (nextStatus != null && taskId != null) {
            AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setMessage("Are you sure you want to change status to ${nextStatus.uppercase()}?")
                .setPositiveButton("Yes") { _, _ ->
                    db.collection("Tasks").document(taskId!!).update("status", nextStatus)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Status updated to $nextStatus", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}