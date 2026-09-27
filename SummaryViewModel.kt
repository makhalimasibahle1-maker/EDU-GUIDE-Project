package com.example.videoconfrence

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoconfrence.database.AppDatabase
import com.example.videoconfrence.database.LectureSummaryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val summaryDao = AppDatabase.getDatabase(application).summaryDao()

    // Holds the current search query
    private val searchQuery = MutableStateFlow("")

    // Dynamically emits all summaries or filtered summaries whenever the query or DB changes
    val summaries: Flow<List<LectureSummaryEntity>> = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            summaryDao.getAllSummaries()
        } else {
            summaryDao.searchSummaries(query)
        }
    }

    // Function to handle search input
    fun searchSummaries(query: String) {
        searchQuery.value = query
    }

    // Function to insert a new summary into the Room database
    fun saveSummary(summary: LectureSummaryEntity) {
        viewModelScope.launch {
            summaryDao.insertSummary(summary)
        }
    }

    // Function to delete a summary
    fun deleteSummary(summary: LectureSummaryEntity) {
        viewModelScope.launch {
            summaryDao.deleteSummary(summary)
        }
    }
}