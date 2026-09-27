package com.example.videoconfrence

import android.content.Context
import android.util.Log
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack

@Suppress("SpellCheckingInspection")
class RTCClient(
    private val context: Context,
    private val observer: PeerConnection.Observer
) {

    private val rootEglBase: EglBase = EglBase.create()

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        buildPeerConnectionFactory()
    }

    private val peerConnection: PeerConnection? by lazy {
        buildPeerConnection()
    }

    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(): PeerConnection? {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        return peerConnectionFactory.createPeerConnection(iceServers, observer)
    }

    fun initializeSurfaceView(view: SurfaceViewRenderer) {
        try {
            view.init(rootEglBase.eglBaseContext, null)
            view.setEnableHardwareScaler(true)
            view.setMirror(true)
        } catch (e: Exception) {
            Log.e("RTCClient", "SurfaceView already initialized or failed: ${e.message}")
        }
    }

    fun startLocalVideo(surfaceView: SurfaceViewRenderer) {
        try {
            val videoCapturer = createVideoCapturer() ?: run {
                Log.e("RTCClient", "No valid camera found for local video.")
                return
            }

            val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)
            val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.eglBaseContext)

            videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            videoCapturer.startCapture(720, 1280, 30)

            val videoTrack: VideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource)
            videoTrack.addSink(surfaceView)

            val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
            val audioTrack: AudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource)

            val streamId = "ARDAMS"
            peerConnection?.addTrack(videoTrack, listOf(streamId))
            peerConnection?.addTrack(audioTrack, listOf(streamId))
        } catch (e: Exception) {
            Log.e("RTCClient", "Error starting local video: ${e.message}")
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames

        // Try front camera first
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }

        // Fallback to back camera if front camera is unavailable on emulator
        for (deviceName in deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }

        return null
    }

    fun call(onSuccess: (SessionDescription) -> Unit = {}) {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let { sdp ->
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            onSuccess(sdp)
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, mediaConstraints)
    }

    fun answer(onSuccess: (SessionDescription) -> Unit = {}) {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                desc?.let { sdp ->
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {
                            peerConnection?.setRemoteDescription(object : SdpObserver {
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onSetSuccess() {}
                                override fun onCreateFailure(p0: String?) {}
                                override fun onSetFailure(p0: String?) {}
                            }, desc)
                        }
                        override fun onSetSuccess() {
                            onSuccess(sdp)
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, mediaConstraints)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun toggleAudio() {
        // Toggle audio track state
    }

    fun toggleVideo() {
        // Toggle video track state
    }

    fun toggleCamera() {
        // Switch between front and back camera
    }

    fun endCall() {
        peerConnection?.close()
    }
}