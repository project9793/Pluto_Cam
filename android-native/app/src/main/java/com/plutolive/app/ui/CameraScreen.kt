package com.plutolive.app.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.plutolive.app.Signaling
import com.plutolive.app.WebRtcClient
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

private class HostState {
    var sig: Signaling? = null
    var answerApplied = false
    val seen = mutableSetOf<String>()
}

@Composable
fun CameraScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val main = remember { Handler(Looper.getMainLooper()) }
    val rtc = remember { WebRtcClient(ctx) }
    val host = remember { HostState() }

    var running by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("------") }
    var status by remember { mutableStateOf("Idle") }
    var micOn by remember { mutableStateOf(true) }
    var camOn by remember { mutableStateOf(true) }
    var front by remember { mutableStateOf(false) }

    val renderer = remember {
        SurfaceViewRenderer(ctx).apply {
            setEnableHardwareScaler(true)
            setMirror(false)
        }
    }

    DisposableEffect(Unit) {
        renderer.init(rtc.eglContext, null)
        onDispose {
            try { host.sig?.stop() } catch (_: Exception) {}
            try { rtc.dispose() } catch (_: Exception) {}
        }
    }

    fun ui(block: () -> Unit) { main.post { block() } }

    fun startSession() {
        val id = randomCode()
        code = id
        status = "Starting..."
        host.answerApplied = false
        host.seen.clear()

        rtc.initPeer()
        if (!rtc.startCapture(renderer, front, 1280, 720, 30)) {
            status = "Camera not available"
            return
        }
        rtc.addLocalTracks()

        val sig = Signaling(
            sessionId = id,
            onSession = { sess ->
                if (sess != null) {
                    val ans = sess.optJSONObject("answer")
                    if (ans != null && !host.answerApplied) {
                        host.answerApplied = true
                        rtc.setRemote(
                            SessionDescription(SessionDescription.Type.ANSWER, ans.optString("sdp"))
                        )
                        ui { status = "Viewer connected" }
                    }
                    val vc = sess.optJSONObject("viewerCandidates")
                    if (vc != null) {
                        val keys = vc.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            if (host.seen.add(k)) {
                                val c = vc.optJSONObject(k) ?: continue
                                rtc.addIce(
                                    IceCandidate(
                                        c.optString("sdpMid"),
                                        c.optInt("sdpMLineIndex"),
                                        c.optString("candidate")
                                    )
                                )
                            }
                        }
                    }
                }
            },
            onError = { e -> ui { status = e } }
        )

        rtc.callback = object : WebRtcClient.Callback {
            override fun onLocalSdp(sdp: SessionDescription) {
                if (sdp.type == SessionDescription.Type.OFFER) {
                    sig.put("offer", JSONObject().put("type", "offer").put("sdp", sdp.description))
                    ui { status = "Waiting for viewer" }
                }
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                sig.post(
                    "hostCandidates",
                    JSONObject()
                        .put("candidate", candidate.sdp)
                        .put("sdpMid", candidate.sdpMid)
                        .put("sdpMLineIndex", candidate.sdpMLineIndex)
                )
            }

            override fun onRemoteVideo(track: VideoTrack) {}

            override fun onState(state: PeerConnection.PeerConnectionState) {
                ui { status = state.name }
            }
        }

        sig.start()
        host.sig = sig
        rtc.createOffer()
        running = true
    }

    fun stopSession() {
        try { host.sig?.stop() } catch (_: Exception) {}
        host.sig = null
        host.answerApplied = false
        host.seen.clear()
        try { rtc.dispose() } catch (_: Exception) {}
        running = false
        code = "------"
        status = "Idle"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(DeepBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = { stopSession(); onBack() }) {
            Text("< Back", color = Cyan)
        }
        Text("Camera Mode", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Turn this device into a live camera", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(14.dp))
        AndroidView(
            factory = { renderer },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black, RoundedCornerShape(14.dp))
        )

        Spacer(Modifier.height(12.dp))
        Text("Status: " + status, color = Cyan, fontSize = 14.sp)
        Text("Room code: " + code, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(14.dp))
        if (!running) {
            PrimaryButton("Start Live") { startSession() }
        } else {
            DangerButton("Stop") { stopSession() }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    micOn = !micOn
                    rtc.setMicEnabled(micOn)
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (micOn) "Mic: On" else "Mic: Off") }

            OutlinedButton(
                onClick = {
                    camOn = !camOn
                    rtc.setCamEnabled(camOn)
                },
                modifier = Modifier.weight(1f)
            ) { Text(if (camOn) "Cam: On" else "Cam: Off") }
        }
    }
}
