package com.plutolive.app

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Firebase Realtime Database signaling over the REST API.
 * No google-services.json / Firebase SDK required: we sign in anonymously,
 * then read + write liveSessions/<id>.json with the returned token.
 */
class Signaling(
    private val sessionId: String,
    private val onSession: (JSONObject?) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseConfig.DB_URL.trimEnd('/')
    private val running = AtomicBoolean(false)
    @Volatile private var token: String? = null
    private var lastRaw = ""

    fun start() {
        if (running.getAndSet(true)) return
        thread(name = "pluto-signaling") {
            try {
                token = signIn()
            } catch (e: Exception) {
                onError("Sign-in failed: " + (e.message ?: ""))
                return@thread
            }
            while (running.get()) {
                try {
                    val raw = get("liveSessions/" + sessionId + ".json")
                    if (raw != lastRaw) {
                        lastRaw = raw
                        onSession(if (raw.isBlank() || raw == "null") null else JSONObject(raw))
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "network error")
                }
                Thread.sleep(500)
            }
        }
    }

    fun stop() { running.set(false) }

    fun put(path: String, value: Any) = write("PUT", path, value)
    fun patch(path: String, value: Any) = write("PATCH", path, value)
    fun post(path: String, value: Any) = write("POST", path, value)

    private fun write(method: String, path: String, value: Any) {
        thread {
            try {
                val t = token ?: signIn().also { token = it }
                val body = value.toString()
                request(method, db + "/liveSessions/" + sessionId + "/" + path + ".json?auth=" + t, body)
            } catch (e: Exception) {
                onError(e.message ?: "write failed")
            }
        }
    }

    private fun get(path: String): String {
        val t = token ?: signIn().also { token = it }
        return request("GET", db + "/" + path + "?auth=" + t, null)
    }

    private fun signIn(): String {
        val body = "{\"returnSecureToken\":true}"
        val url = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + FirebaseConfig.API_KEY
        val text = request("POST", url, body)
        return JSONObject(text).getString("idToken")
    }

    private fun request(method: String, urlStr: String, body: String?): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.setRequestProperty("Content-Type", "application/json")
        if (body != null) {
            conn.doOutput = true
            conn.outputStream.use { it.write(body.toByteArray()) }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw RuntimeException("HTTP " + code + ": " + text.take(140))
        return text
    }
}
