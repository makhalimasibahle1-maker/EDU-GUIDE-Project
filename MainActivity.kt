package com.example.videoconfrence

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videoconfrence.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation to Saved Summaries Screen
        binding.btnSavedSummaries.setOnClickListener {
            val intent = Intent(this, SavedSummariesActivity::class.java)
            startActivity(intent)
        }

        binding.enterBtn.setOnClickListener {
            val username = binding.usernameEt.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Request Camera and Microphone permissions before moving to CallActivity
            PermissionX.init(this)
                .permissions(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO
                )
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        val intent = Intent(this, CallActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Camera and Microphone permissions are required to place calls",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}