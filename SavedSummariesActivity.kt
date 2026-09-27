package com.example.videoconfrence

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoconfrence.database.AppDatabase
import com.example.videoconfrence.database.SummaryAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedSummariesActivity : AppCompatActivity() {

    private lateinit var adapter: SummaryAdapter
    private lateinit var etSearch: EditText
    private lateinit var rvSummaries: RecyclerView
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_summaries)

        etSearch = findViewById(R.id.etSearch)
        rvSummaries = findViewById(R.id.rvSummaries)

        rvSummaries.layoutManager = LinearLayoutManager(this)
        adapter = SummaryAdapter(emptyList()) { summary ->
            Toast.makeText(this, "Selected: ${summary.title}", Toast.LENGTH_SHORT).show()
        }
        rvSummaries.adapter = adapter

        // Initial load of all summaries
        observeSummaries("")

        // Search listener for real-time filtering
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                observeSummaries(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeSummaries(query: String) {
        // Cancel the previous flow collection if the user is typing rapidly
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).summaryDao()
            val flow = if (query.isEmpty()) {
                dao.getAllSummaries()
            } else {
                dao.searchSummaries(query)
            }

            // Collect Flow emission and update RecyclerView automatically
            flow.collectLatest { list ->
                adapter.updateData(list)
            }
        }
    }
}