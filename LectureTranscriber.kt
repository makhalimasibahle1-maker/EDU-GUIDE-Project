package com.example.videoconfrence

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class LectureTranscriber(
    private val context: Context,
    private val onTextTranscribed: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val fullTranscript = StringBuilder()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val TAG = "LectureTranscriber"
    private var isListening = false

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition is not available on this device/emulator.")
            return
        }

        isListening = true
        initAndStart()
    }

    private fun initAndStart() {
        if (!isListening) return

        // Destroy old instance to avoid memory leak and internal binding errors
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    fullTranscript.append("$text ")
                    Log.d(TAG, "Transcribed: $text")
                    onTextTranscribed(fullTranscript.toString().trim())
                }

                // Restart listening after a small pause
                restartListeningWithDelay()
            }

            override fun onError(error: Int) {
                Log.e(TAG, "SpeechRecognizer error code: $error")
                // Code 7 = NO_MATCH, Code 6 = SPEECH_TIMEOUT. Restart safely.
                restartListeningWithDelay()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun restartListeningWithDelay() {
        if (isListening) {
            mainHandler.postDelayed({
                initAndStart()
            }, 500) // 500ms delay gives native speech service time to reset
        }
    }

    fun stopAndGetTranscript(): String {
        isListening = false
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping SpeechRecognizer", e)
        }
        return fullTranscript.toString().trim()
    }
}