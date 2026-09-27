package com.example.videoconfrence

import android.os.Build
import android.util.Log
import com.example.videoconfrence.models.MessageModel
import com.example.videoconfrence.utils.NewMessageInterface
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.IceCandidate

class SocketRepository(private val messageInterface: NewMessageInterface) {

    private var socket: Socket? = null
    private val TAG = "SocketRepository"
    private val gson = Gson()

    fun initSocket(username: String) {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
            }

            // Automatically pick 10.0.2.2 for Emulators and PC IP for Physical Devices
            val serverUrl = if (isEmulator()) {
                "http://10.0.2.2:3000"
            } else {
                "http://10.156.0.76:3000"
            }

            Log.d(TAG, "Connecting to socket server at: $serverUrl")
            socket = IO.socket(serverUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket.IO connected successfully")
                sendMessageToSocket(
                    MessageModel("store_user", username, null, null)
                )
            }

            val events = listOf("call_response", "create_offer", "create_answer", "ice_candidate")

            for (event in events) {
                socket?.on(event) { args ->
                    if (args != null && args.isNotEmpty()) {
                        try {
                            val dataObj = args[0] as? JSONObject
                            val message = MessageModel(
                                type = event,
                                name = dataObj?.optString("name"),
                                target = dataObj?.optString("target"),
                                data = dataObj?.opt("data")
                            )
                            messageInterface.onNewMessage(message)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing socket event: $event", e)
                        }
                    }
                }
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket.IO disconnected")
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization error", e)
        }
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            val eventName = message.type ?: return
            Log.d(TAG, "Emitting event: $eventName with data: $message")

            val jsonObject = JSONObject().apply {
                put("type", message.type)
                put("name", message.name)
                put("target", message.target)

                when (val data = message.data) {
                    is String -> put("data", data)
                    is IceCandidate -> {
                        val candidateJson = JSONObject().apply {
                            put("sdpMid", data.sdpMid)
                            put("sdpMLineIndex", data.sdpMLineIndex)
                            put("sdp", data.sdp)
                        }
                        put("data", candidateJson)
                    }
                    null -> {}
                    else -> put("data", JSONObject(gson.toJson(data)))
                }
            }

            socket?.emit(eventName, jsonObject)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending socket message", e)
        }
    }

    fun disconnect() {
        try {
            socket?.disconnect()
            socket?.off()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting socket", e)
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion"))
    }
}