package com.example.videoconfrence.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecture_summaries")
data class LectureSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var lecturerName: String,
    var transcript: String,
    var summaryNotes: String,
    val createdAt: String
)