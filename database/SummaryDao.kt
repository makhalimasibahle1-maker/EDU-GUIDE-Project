package com.example.videoconfrence.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO: Data Access Object for AI Lecture Summary Operations
@Dao
interface SummaryDao {

    // CREATE: Insert a new summary and return generated ID
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: LectureSummaryEntity): Long

    // READ: Get all saved summaries reactively
    @Query("SELECT * FROM lecture_summaries ORDER BY id DESC")
    fun getAllSummaries(): Flow<List<LectureSummaryEntity>>

    // READ: Search summaries by title or lecturer reactively
    @Query("SELECT * FROM lecture_summaries WHERE title LIKE '%' || :query || '%' OR lecturerName LIKE '%' || :query || '%'")
    fun searchSummaries(query: String): Flow<List<LectureSummaryEntity>>

    // UPDATE: Update an existing summary record
    @Update
    suspend fun updateSummary(summary: LectureSummaryEntity)

    // DELETE: Delete a specific summary record
    @Delete
    suspend fun deleteSummary(summary: LectureSummaryEntity)

    // Helper: Check count of records
    @Query("SELECT COUNT(*) FROM lecture_summaries")
    suspend fun getRecordCount(): Int
}