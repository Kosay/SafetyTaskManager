package com.kmhinfratech.safetytaskmanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()

    private var tasksListener: ListenerRegistration? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()
        setupRecyclerView()

        // --- Navigation Logic ---

        findViewById<Button>(R.id.btnMap).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // Filtering Logic for TaskListActivity
        findViewById<Button>(R.id.btnCompleted).setOnClickListener {
            navigateToTaskList("completed", "Completed Tasks")
        }

        findViewById<Button>(R.id.btnPending).setOnClickListener {
            navigateToTaskList("pending", "Pending Tasks")
        }

        findViewById<Button>(R.id.btnInProgress).setOnClickListener {
            navigateToTaskList("in-progress", "In Progress Tasks")
        }
        //Navigat
        findViewById<Button>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        // FIX: Toast inside the listener, not floating in onCreate
        findViewById<Button>(R.id.btnSendReport).setOnClickListener {
            Toast.makeText(this, "Submitted view will be developed later", Toast.LENGTH_SHORT).show()
        }

        loadUserTasks()
    }
    // Helper to keep code clean
    private fun navigateToTaskList(filter: String, title: String) {
        val intent = Intent(this, TaskListActivity::class.java).apply {
            putExtra("FILTER_TYPE", filter)
            putExtra("SCREEN_TITLE", title)
        }
        startActivity(intent)
    }
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvTasks)

        // FIX: Passed the mandatory onClick lambda to match TaskAdapter's new constructor
        taskAdapter = TaskAdapter(taskList) { task ->
            val intent = Intent(this, TaskDetailsActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            startActivity(intent)
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = taskAdapter
    }
    private fun loadUserTasks() {
        val currentUser = auth.currentUser ?: return

        // NOTE: Ensure your collection name is consistently "Tasks" or "tasks"
        tasksListener = db.collection("Tasks")
            .whereEqualTo("assignedTo", currentUser.uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading tasks", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    taskList.clear()
                    for (doc in snapshots) {
                        // Include the document ID manually if not stored in the fields
                        val task = doc.toObject(Task::class.java).copy(id = doc.id)

                        if (task.status != "completed") {
                            taskList.add(task)
                        }
                    }
                    taskAdapter.notifyDataSetChanged()
                }
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        tasksListener?.remove()
    }
}