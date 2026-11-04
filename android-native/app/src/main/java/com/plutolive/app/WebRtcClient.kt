package com.plutolive.app

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

/**
 * Thin wrapper around org.webrtc: camera capture, peer connection, SDP/ICE.
 */
class WebRtcClient(private val context: Context) {

    interface Callback {
        fun onLocalSdp(sdp: SessionDescription)
        fun onIceCandidate(candidate: IceCandidate)
        fun onRemoteVideo(track: VideoTrack)
        fun onState(state: PeerConnection.PeerConnectionState)
    }

    var callback: Callback? = null

    private val eglBase: EglBase = EglBase.create()
    val eglContext: EglBase.Context get() = eglBase.eglBaseContext

    private val factory: PeerConnectionFactory = run {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private var pc: PeerConnection? = null
    private var capturer: VideoCapturer? = null
    private var surfaceHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var videoTrack: VideoTrack? = null
    private var audioTrack: AudioTrack? = null

    fun initPeer(receiveOnly: Boolean = false) {
        closePeer()
        val ice = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        val cfg = PeerConnection.RTCConfiguration(listOf(ice)).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        pc = factory.createPeerConnection(cfg, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { callback?.onIceCandidate(it) }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) callback?.onRemoteVideo(track)
            }
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                newState?.let { callback?.onState(it) }
            }
        })
        if (receiveOnly) {
            pc?.addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
        }
    }

    /** Starts camera + mic. Returns false if no camera could be opened. */
    fun startCapture(
        renderer: SurfaceViewRenderer,
        front: Boolean,
        width: Int,
        height: Int,
        fps: Int
    ): Boolean {
        val enumerator = Camera2Enumerator(context)
        val name = enumerator.deviceNames.firstOrNull {
            if (front) enumerator.isFrontFacing(it) else enumerator.isBackFacing(it)
        } ?: enumerator.deviceNames.firstOrNull() ?: return false

        val c = enumerator.createCapturer(name, null) ?: return false
        capturer = c
        surfaceHelper = SurfaceTextureHelper.create("pluto-capture", eglBase.eglBaseContext)
        videoSource = factory.createVideoSource(false)
        c.initialize(surfaceHelper, context, videoSource!!.capturerObserver)
        c.startCapture(width, height, fps)

        videoTrack = factory.createVideoTrack("pluto-video", videoSource).apply { addSink(renderer) }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
        }
        audioSource = factory.createAudioSource(constraints)
        audioTrack = factory.createAudioTrack("pluto-audio", audioSource)
        return true
    }

    fun addLocalTracks() {
        val p = pc ?: return
        videoTrack?.let { p.addTrack(it, listOf("pluto-stream")) }
        audioTrack?.let { p.addTrack(it, listOf("pluto-stream")) }
    }

    fun createOffer() {
        val p = pc ?: return
        p.createOffer(object : SdpAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return
                p.setLocalDescription(SdpAdapter(), sdp)
                callback?.onLocalSdp(sdp)
            }
        }, MediaConstraints())
    }

    fun createAnswer() {
        val p = pc ?: return
        p.createAnswer(object : SdpAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return
                p.setLocalDescription(SdpAdapter(), sdp)
                callback?.onLocalSdp(sdp)
            }
        }, MediaConstraints())
    }

    fun setRemote(sdp: SessionDescription) {
        pc?.setRemoteDescription(SdpAdapter(), sdp)
    }

    fun addIce(c: IceCandidate) {
        pc?.addIceCandidate(c)
    }

    fun attachRemote(track: VideoTrack, renderer: SurfaceViewRenderer) {
        track.addSink(renderer)
    }

    fun setMicEnabled(on: Boolean) { audioTrack?.setEnabled(on) }
    fun setCamEnabled(on: Boolean) { videoTrack?.setEnabled(on) }

    private fun closePeer() {
        try { pc?.close() } catch (_: Exception) {}
        pc = null
    }

    fun dispose() {
        try { capturer?.stopCapture() } catch (_: Exception) {}
        capturer?.dispose(); capturer = null
        surfaceHelper?.dispose(); surfaceHelper = null
        videoTrack?.dispose(); videoTrack = null
        audioTrack?.dispose(); audioTrack = null
        videoSource?.dispose(); videoSource = null
        audioSource?.dispose(); audioSource = null
        closePeer()
        factory.dispose()
        eglBase.release()
    }

    private open class SdpAdapter : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {}
        override fun onSetFailure(error: String?) {}
    }
}
