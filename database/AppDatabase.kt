package com.example.videoconfrence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [LectureSummaryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun summaryDao(): SummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lecture_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed 15 initial real records upon database creation
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getDatabase(context).summaryDao()
                    if (dao.getRecordCount() == 0) {
                        seedDefaultData(dao)
                    }
                }
            }

            private suspend fun seedDefaultData(dao: SummaryDao) {
                val initialData = listOf(
                    LectureSummaryEntity(title = "App Development Architecture", lecturerName = "Mr. Maine", transcript = "WebRTC signaling and Socket.IO peer connection lifecycle.", summaryNotes = "1. WebRTC handles peer media streaming.\n2. Socket.IO routes signaling offers and answers.", createdAt = "2026-09-01"),
                    LectureSummaryEntity(title = "Database Normalization", lecturerName = "Mrs. Swart", transcript = "1NF, 2NF, 3NF database designs and foreign keys.", summaryNotes = "1. Eliminate duplicate columns.\n2. Ensure foreign key relational integrity.", createdAt = "2026-09-02"),
                    LectureSummaryEntity(title = "Software Testing Strategies", lecturerName = "Dr. Smith", transcript = "Unit testing vs Integration testing and UAT matrices.", summaryNotes = "1. Unit testing isolates individual components.\n2. UAT verifies business requirements.", createdAt = "2026-09-03"),
                    LectureSummaryEntity(title = "Kotlin Coroutines & Threads", lecturerName = "Mr. Maine", transcript = "Dispatchers.IO, suspended functions, and UI safety.", summaryNotes = "1. Always run heavy tasks off the Main thread.\n2. Use Dispatchers.IO for database calls.", createdAt = "2026-09-04"),
                    LectureSummaryEntity(title = "Real-Time Media Streaming", lecturerName = "Prof. Johnson", transcript = "SDP offer and answer exchange with ICE candidates.", summaryNotes = "1. SDP defines media codecs.\n2. ICE candidates establish network traversal.", createdAt = "2026-09-05"),
                    LectureSummaryEntity(title = "Generative AI Integration", lecturerName = "Dr. Smith", transcript = "Using Gemini Flash 1.5 SDK for automated summaries.", summaryNotes = "1. Initialize GenerativeModel with API key.\n2. Post-process AI text into bullet points.", createdAt = "2026-09-06"),
                    LectureSummaryEntity(title = "Android Runtime Permissions", lecturerName = "Mrs. Swart", transcript = "RECORD_AUDIO and CAMERA permission handling.", summaryNotes = "1. Request permissions dynamically before mic access.\n2. Handle permission denial scenarios.", createdAt = "2026-09-07"),
                    LectureSummaryEntity(title = "UI Layouts & Material Design", lecturerName = "Mr. Maine", transcript = "ViewBinding and responsive constraint layouts.", summaryNotes = "1. ViewBinding eliminates findViewById overhead.\n2. Use ConstraintLayout for flat view hierarchies.", createdAt = "2026-09-08"),
                    LectureSummaryEntity(title = "HTTP API Design & JSON", lecturerName = "Prof. Johnson", transcript = "RESTful endpoints, Socket events, and JSON parsing.", summaryNotes = "1. Serialize data using JSON objects.\n2. Emit event payloads across sockets.", createdAt = "2026-09-09"),
                    LectureSummaryEntity(title = "Memory Management & Leaks", lecturerName = "Dr. Smith", transcript = "Handler lifecycle safety and destroying objects.", summaryNotes = "1. Unregister listeners on Activity destroy.\n2. Avoid holding long-lived static contexts.", createdAt = "2026-09-10"),
                    LectureSummaryEntity(title = "Data Structures for Search", lecturerName = "Mrs. Swart", transcript = "List filtering algorithms and RecyclerView adapters.", summaryNotes = "1. Filter lists dynamically on text change.\n2. Notify adapter of dataset updates.", createdAt = "2026-09-11"),
                    LectureSummaryEntity(title = "Cloud Hosting & Sockets", lecturerName = "Mr. Maine", transcript = "Deploying Node.js WebSocket listeners on port 3000.", summaryNotes = "1. Bind Express server to 0.0.0.0.\n2. Verify local firewall settings.", createdAt = "2026-09-12"),
                    LectureSummaryEntity(title = "Version Control with Git", lecturerName = "Prof. Johnson", transcript = "Branch management and GitHub repository setup.", summaryNotes = "1. Keep commits clear and atomic.\n2. Document setup steps in README.md.", createdAt = "2026-09-13"),
                    LectureSummaryEntity(title = "Error Handling & Feedback", lecturerName = "Mrs. Swart", transcript = "Custom toasts, error dialogs, and validation.", summaryNotes = "1. Provide real-time UI feedback on failures.\n2. Validate inputs before database storage.", createdAt = "2026-09-14"),
                    LectureSummaryEntity(title = "Software Project Lifecycle", lecturerName = "Dr. Smith", transcript = "Requirement analysis and final project deliverables.", summaryNotes = "1. Align code with rubric marking criteria.\n2. Complete documentation and test matrices.", createdAt = "2026-09-15")
                )

                for (item in initialData) {
                    dao.insertSummary(item)
                }
            }
        }
    }
}