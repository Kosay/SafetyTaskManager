package com.kmhinfratech.safetytaskmanager

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        findViewById<ImageView>(R.id.btnBackProfile).setOnClickListener { finish() }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    findViewById<TextView>(R.id.tvProfileName).text = doc.getString("name")
                    findViewById<TextView>(R.id.tvProfileEmail).text = doc.getString("email")
                    findViewById<TextView>(R.id.tvProfileRole).text = doc.getString("role")

                }
            }
    }
}