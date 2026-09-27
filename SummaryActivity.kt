package com.example.videoconfrence

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val summaryTextView = findViewById<TextView>(R.id.summaryTextView)
        val doneBtn = findViewById<Button>(R.id.doneBtn)

        val notes = intent.getStringExtra("EXTRA_SUMMARY") ?: "No summary available."
        summaryTextView.text = notes

        doneBtn.setOnClickListener {
            finish()
        }
    }
}