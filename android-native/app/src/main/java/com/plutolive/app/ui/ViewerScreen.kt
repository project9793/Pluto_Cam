package com.plutolive.app.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.plutolive.app.Signaling
import com.plutolive.app.WebRtcClient
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

private class ViewerState {
    var sig: Signaling? = null
    var offerApplied = false
    val seen = mutableSetOf<String>()
}

@Composable
fun ViewerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val main = remember { Handler(Looper.getMainLooper()) }
    val rtc = remember { WebRtcClient(ctx) }
    val state = remember { ViewerState() }
    val exo = remember { ExoPlayer.Builder(ctx).build() }

    var code by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Idle") }
    var mode by remember { mutableStateOf("none") } // none | webrtc | hls

    val renderer = remember { SurfaceViewRenderer(ctx).apply { setEnableHardwareScaler(true) } }

    DisposableEffect(Unit) {
        renderer.init(rtc.eglContext, null)
        onDispose {
            try { state.sig?.stop() } catch (_: Exception) {}
            try { rtc.dispose() } catch (_: Exception) {}
            exo.release()
        }
    }

    fun ui(block: () -> Unit) { main.post { block() } }

    fun connect() {
        val id = code.trim().uppercase()
        if (id.length < 4) { status = "Enter the room code"; return }
        mode = "webrtc"
        status = "Connecting..."
        state.offerApplied = false
        state.seen.clear()

        rtc.initPeer(receiveOnly = true)

        val sig = Signaling(
            sessionId = id,
            onSession = { sess ->
                if (sess != null) {
                    val offer = sess.optJSONObject("offer")
                    if (offer != null && !state.offerApplied) {
                        state.offerApplied = true
                        rtc.setRemote(
                            SessionDescription(SessionDescription.Type.OFFER, offer.optString("sdp"))
                        )
                        rtc.createAnswer()
                    }
                    val hc = sess.optJSONObject("hostCandidates")
                    if (hc != null) {
                        val keys = hc.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            if (state.seen.add(k)) {
                                val c = hc.optJSONObject(k) ?: continue
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
        state.sig = sig

        rtc.callback = object : WebRtcClient.Callback {
            override fun onLocalSdp(sdp: SessionDescription) {
                if (sdp.type == SessionDescription.Type.ANSWER) {
                    sig.put("answer", JSONObject().put("type", "answer").put("sdp", sdp.description))
                }
            }

            override fun onIceCandidate(candidate: IceCandidate) {
                sig.post(
                    "viewerCandidates",
                    JSONObject()
                        .put("candidate", candidate.sdp)
                        .put("sdpMid", candidate.sdpMid)
                        .put("sdpMLineIndex", candidate.sdpMLineIndex)
                )
            }

            override fun onRemoteVideo(track: VideoTrack) {
                rtc.attachRemote(track, renderer)
                ui { status = "Live" }
            }

            override fun onState(state2: PeerConnection.PeerConnectionState) {
                ui { status = state2.name }
            }
        }

        sig.start()
        sig.put("viewerConnected", "true")
        status = "Waiting for camera..."
    }

    fun playLink() {
        val url = link.trim()
        if (url.isEmpty()) { status = "Paste a link first"; return }
        try { state.sig?.stop() } catch (_: Exception) {}
        state.sig = null
        mode = "hls"
        exo.setMediaItem(MediaItem.fromUri(url))
        exo.prepare()
        exo.playWhenReady = true
        status = "Playing link"
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(DeepBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = {
            try { state.sig?.stop() } catch (_: Exception) {}
            exo.stop()
            onBack()
        }) { Text("< Back", color = Cyan) }

        Text("Viewer Mode", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Watch another device's camera", color = Muted, fontSize = 13.sp)

        Spacer(Modifier.height(12.dp))
        if (mode == "hls") {
            AndroidView(
                factory = { PlayerView(it).apply { player = exo } },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black, RoundedCornerShape(14.dp))
            )
        } else {
            AndroidView(
                factory = { renderer },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black, RoundedCornerShape(14.dp))
            )
        }

        Spacer(Modifier.height(10.dp))
        Text("Status: " + status, color = Cyan, fontSize = 14.sp)

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(8) },
            label = { Text("Room code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Connect") { connect() }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = link,
            onValueChange = { link = it },
            label = { Text("HLS (.m3u8) or video link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton("Play link") { playLink() }
    }
}
