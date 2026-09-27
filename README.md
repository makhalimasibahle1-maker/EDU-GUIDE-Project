EduGuide: Smart Lecture Assistant & AI Summary Module

 Project Overview
 
This module serves as the automated lecture capture and AI summarization hub for the **EduGuide** academic collaboration platform[cite: 3, 5]. It facilitates live virtual classes/video conferences and automatically converts lecture audio, transcripts, and notes into structured summaries stored in a local Room database[cite: 3, 5].

Technical Architecture & Tech Stack

Programming Language:Kotlin (100% Native)[cite: 5]
UI Framework: Android XML Views & Material Design Components (RecyclerView, EditText, CardView)[cite: 5]
Local Persistence:Room Database Persistence Library (LectureSummaryEntity, SummaryDao, AppDatabase)[cite: 5]
Asynchronous Processing:Kotlin Coroutines & Flow (collectLatest for real-time UI updates)[cite: 3, 5]
Media & Networking:WebRTC / RTCClient for real-time video conferencing[cite: 5]
Architecture Pattern: Repository / DAO-driven Android Architecture[cite: 5]

---

## Key Features & Functionalities

1. AI Summary Generation & Storage:
   Captures lecture metadata (title, lecturerName, transcript, summaryNotes, createdAt)[cite: 5].
   Persists data locally using Room DB with auto-generated primary keys[cite: 5].

2. Real-time Live Search & Dynamic Filtering:
   Instant search filtering across saved summaries by lecture title or lecturer name[cite: 5].
   Utilizes Kotlin Flow reactive streams to dynamically update the RecyclerView without app reloads[cite: 3, 5].

3. Input Validation & Feedback:**
   Prevents empty or corrupted inputs from being persisted to the database[cite: 3].
   Displays clear user notifications and status feedback[cite: 3].

 Project Structure

com.example.videoconfrence
│
├── database/
│   ├── AppDatabase.kt           # Room Database Instance
│   ├── LectureSummaryEntity.kt  # Summary Table Definition
│   └── SummaryDao.kt            # CRUD & Flow Search Queries
│
├── adapters/
│   └── SummaryAdapter.kt        # RecyclerView Adapter for Summaries
│
├── CallActivity.kt              # Video Conference / Live Lecture Screen
├── SavedSummariesActivity.kt    # Saved Summaries & Search Screen
└── LectureTranscriber.kt        # Audio / Transcript Processing
