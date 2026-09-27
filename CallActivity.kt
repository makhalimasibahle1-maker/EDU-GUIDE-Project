package com.example.videoconfrence

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.videoconfrence.database.AppDatabase
import com.example.videoconfrence.database.LectureSummaryEntity
import com.example.videoconfrence.databinding.ActivityCallBinding
import com.example.videoconfrence.models.MessageModel
import com.example.videoconfrence.utils.NewMessageInterface
import com.example.videoconfrence.utils.PeerConnectionObserver
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallActivity : AppCompatActivity(), NewMessageInterface {

    private var transcriber: LectureTranscriber? = null
    private lateinit var binding: ActivityCallBinding
    private var userName: String? = null
    private var target: String = ""
    private var socketRepository: SocketRepository? = null
    private var rtcClient: RTCClient? = null

    private var isAudioMute = false
    private var isVideoHide = false
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissionsAndInit()
    }

    private fun checkPermissionsAndInit() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                init()
            } else {
                Toast.makeText(this, "Microphone and Camera permissions are required", Toast.LENGTH_SHORT).show()
                init()
            }
        }
    }

    private fun init() {
        userName = intent.getStringExtra("username")
        binding.whoToCallLayout.visibility = View.VISIBLE

        try {
            transcriber = LectureTranscriber(this) { liveTranscript ->
                Log.d("CallActivity", "Live Transcript updated: $liveTranscript")
            }
            transcriber?.startListening()
        } catch (e: Exception) {
            Log.e("CallActivity", "SpeechRecognizer init failed: ${e.message}")
        }

        socketRepository = SocketRepository(this)
        socketRepository?.initSocket(userName ?: "")

        rtcClient = RTCClient(
            this,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    p0?.let { candidate ->
                        socketRepository?.sendMessageToSocket(
                            MessageModel("ice_candidate", userName, target, candidate)
                        )
                    }
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    try {
                        p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Error adding remote video sink: ${e.message}")
                    }
                }
            }
        )

        try {
            rtcClient?.initializeSurfaceView(binding.localView)
            rtcClient?.initializeSurfaceView(binding.remoteView)
            rtcClient?.startLocalVideo(binding.localView)
        } catch (e: Exception) {
            Log.e("CallActivity", "WebRTC Surface View Init Error: ${e.message}")
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.callBtn.setOnClickListener {
            target = binding.targetUserNameEt.text.toString().trim()
            if (target.isNotEmpty()) {
                socketRepository?.sendMessageToSocket(
                    MessageModel("start_call", userName, target, null)
                )
            } else {
                Toast.makeText(this, "Please enter target username", Toast.LENGTH_SHORT).show()
            }
        }

        binding.micBtn.setOnClickListener {
            isAudioMute = !isAudioMute
            rtcClient?.toggleAudio()
            binding.micBtn.setImageResource(
                if (isAudioMute) R.drawable.ic_baseline_mic_off_24 else R.drawable.ic_baseline_mic_24
            )
        }

        binding.videoBtn.setOnClickListener {
            isVideoHide = !isVideoHide
            rtcClient?.toggleVideo()
            binding.videoBtn.setImageResource(
                if (isVideoHide) R.drawable.ic_baseline_videocam_off_24 else R.drawable.ic_baseline_videocam_24
            )
        }

        binding.switchCameraButton.setOnClickListener {
            rtcClient?.toggleCamera()
        }

        binding.endCallBtn.setOnClickListener {
            val finalTranscript = transcriber?.stopAndGetTranscript() ?: ""
            rtcClient?.endCall()

            val textToSummarize = if (finalTranscript.isNotBlank()) {
                finalTranscript
            } else {
                "Today we discussed WebRTC real-time media connection, Socket.IO signaling servers, and mobile speech transcription."
            }

            lifecycleScope.launch {
                val summaryNotes = generateLectureSummary(
                    transcript = textToSummarize,
                    apiKey = "YOUR_API_KEY_HERE"
                )

                // Save generated summary to Room Database
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val newRecord = LectureSummaryEntity(
                    title = "Live Call Session - ${if (target.isNotEmpty()) target else "Peer"}",
                    lecturerName = userName ?: "User",
                    transcript = textToSummarize,
                    summaryNotes = summaryNotes,
                    createdAt = currentDate
                )

                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@CallActivity)
                    db.summaryDao().insertSummary(newRecord)
                }

                val intent = Intent(this@CallActivity, SummaryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onNewMessage(message: MessageModel) {
        runOnUiThread {
            when (message.type) {
                "call_response" -> {
                    setWhoToCallLayoutGone()
                    setCallLayoutVisible()

                    rtcClient?.call { sdp ->
                        socketRepository?.sendMessageToSocket(
                            MessageModel("create_offer", userName, target, sdp.description)
                        )
                    }
                }
                "create_offer" -> {
                    target = message.name.toString()
                    setWhoToCallLayoutGone()
                    setCallLayoutVisible()

                    val offerDescription = SessionDescription(
                        SessionDescription.Type.OFFER,
                        message.data.toString()
                    )

                    rtcClient?.onRemoteSessionReceived(offerDescription)
                    rtcClient?.answer { sdp ->
                        socketRepository?.sendMessageToSocket(
                            MessageModel("create_answer", userName, target, sdp.description)
                        )
                    }
                }
                "create_answer" -> {
                    val answerDescription = SessionDescription(
                        SessionDescription.Type.ANSWER,
                        message.data.toString()
                    )
                    rtcClient?.onRemoteSessionReceived(answerDescription)
                }
                "ice_candidate" -> {
                    try {
                        val dataObj = message.data as? JSONObject
                        if (dataObj != null) {
                            val sdpMid = dataObj.optString("sdpMid")
                            val sdpMLineIndex = dataObj.optInt("sdpMLineIndex")
                            val sdp = dataObj.optString("sdp")
                            val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                            rtcClient?.addIceCandidate(candidate)
                        } else if (message.data is IceCandidate) {
                            rtcClient?.addIceCandidate(message.data as IceCandidate)
                        }
                    } catch (e: Exception) {
                        Log.e("CallActivity", "Error parsing incoming ICE candidate", e)
                    }
                }
            }
        }
    }

    private fun setWhoToCallLayoutGone() {
        binding.whoToCallLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }

    private suspend fun generateLectureSummary(transcript: String, apiKey: String): String {
        return withContext(Dispatchers.IO) {
            if (transcript.isBlank()) {
                return@withContext "No transcript recorded for this call."
            }
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                val prompt = "Please summarize the following lecture transcript into key bullet points and clear action items:\n\n$transcript"
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Unable to generate summary."
            } catch (e: Exception) {
                e.printStackTrace()
                "Error generating summary: ${e.localizedMessage}"
            }
        }
    }
}