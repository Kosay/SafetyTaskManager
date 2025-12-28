package com.kmhinfratech.safetytaskmanager

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationActivity : AppCompatActivity() {

    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var notificationListener: ListenerRegistration? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // 1. Back button logic
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // 2. RecyclerView Setup
        rvNotifications = findViewById(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        // 3. Initialize Adapter with the click listener
        adapter = NotificationAdapter(mutableListOf()) { notification ->
            onNotificationClicked(notification)
        }
        rvNotifications.adapter = adapter

        listenForNotifications()
    }

    private fun listenForNotifications() {
        val currentUid = auth.currentUser?.uid ?: return

        val query = db.collection("notifications")
            .whereEqualTo("userId", currentUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        notificationListener = query.addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            if (snapshots != null) {
                val list = snapshots.toObjects(Notification::class.java)
                adapter.updateData(list)
            }
        }
    }

    private fun onNotificationClicked(notification: Notification) {
        // Mark as read in Firestore
        db.collection("notifications").document(notification.id)
            .update("read", true)
            .addOnSuccessListener {
                // Navigate to Task Details
                val intent = Intent(this, TaskDetailsActivity::class.java)
                intent.putExtra("TASK_ID", notification.taskId)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Check connection", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationListener?.remove()
    }
}