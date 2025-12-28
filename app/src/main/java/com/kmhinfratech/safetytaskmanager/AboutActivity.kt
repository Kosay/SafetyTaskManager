package com.kmhinfratech.safetytaskmanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.datatransport.BuildConfig

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Set dynamic version name
        val versionTextView = findViewById<TextView>(R.id.tvVersion)
        versionTextView.text = "Version 1.0"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnWebsite).setOnClickListener {
            openUrl("https://www.kosayhatem.pw")
        }

        findViewById<Button>(R.id.btnPrivacyPolicy).setOnClickListener {
            openUrl("https://www.kosayhatem.pw/")
        }

        findViewById<Button>(R.id.btnSupport).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:kosay@kosayhatem.pw")
                putExtra(Intent.EXTRA_SUBJECT, "App Support: Safety Task Manager")
            }
            startActivity(intent)
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}