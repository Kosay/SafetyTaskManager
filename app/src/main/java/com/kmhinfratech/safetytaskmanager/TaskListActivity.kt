package com.kmhinfratech.safetytaskmanager

import android.content.Intent // Ensure this is imported
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TaskListActivity : AppCompatActivity() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var adapter: TaskAdapter
    private var taskListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        val filterType = intent.getStringExtra("FILTER_TYPE") ?: "all"
        val screenTitle = intent.getStringExtra("SCREEN_TITLE") ?: "Tasks"

        findViewById<TextView>(R.id.tvToolbarTitle).text = screenTitle
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        rvTasks = findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(this)

        // CORRECTED: Ensure TaskDetailsActivity is created (see Step 2)
// Inside TaskListActivity onCreate...
        adapter = TaskAdapter(emptyList()) { task: Task ->
            val intent = Intent(this@TaskListActivity, TaskDetailsActivity::class.java)
            intent.putExtra("TASK_ID", task.id) // This 'id' now refers to your Task.id
            startActivity(intent)
        }
        rvTasks.adapter = adapter

        loadTasks(filterType)
    }

    private fun loadTasks(filter: String) {
        val userId = auth.currentUser?.uid ?: return
        var query = db.collection("Tasks").whereEqualTo("assignedTo", userId)

        if (filter != "all") {
            query = if (filter == "completed") {
                query.whereIn("status", listOf("completed", "submitted"))
            } else {
                query.whereEqualTo("status", filter)
            }
        }

        taskListener = query.addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            if (snapshots != null) {
                // Ensure Task.kt data class exists in this package
                val taskList = snapshots.toObjects(Task::class.java)
                adapter.updateData(taskList)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        taskListener?.remove()
    }
}