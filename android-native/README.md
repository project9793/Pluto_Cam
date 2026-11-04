# Pluto Live - Native Android (Kotlin)

A **fully native** Kotlin app (no WebView, no HTML) for Pluto Live.

* **Camera Mode** - phone camera + mic -> WebRTC -> Firebase signaling
* **Viewer Mode** - 6-digit room code (WebRTC) or an HLS/MP4 link (ExoPlayer)

## Stack

| Layer | Used |
|-------|------|
| UI | Jetpack Compose + Material 3 |
| Video (live) | `org.webrtc` (`io.github.webrtc-sdk:android`) |
| Signaling | Firebase Realtime Database **REST API** + anonymous auth |
| Link playback | AndroidX Media3 / ExoPlayer |
| Language | Kotlin 2.0 |

### Why REST for signaling?
It avoids `google-services.json` completely. The app signs in anonymously with your
web API key, then reads/writes `liveSessions/<code>.json`. Same database, same rules
as the web app - so both can talk to each other.

## Build

1. Open the `android-native` folder in **Android Studio**.
2. Let Gradle sync (AGP 8.5.2, Kotlin 2.0.20, Gradle 8.7).
3. Run on a phone, or `Build > Build APK(s)`.

`applicationId` is `com.plutolive.app` - change it in `app/build.gradle.kts` if needed.

## Firebase

Already configured in `FirebaseConfig.kt`:

```kotlin
const val API_KEY = "AIza..."
const val DB_URL  = "https://pluto-cam-default-rtdb.firebaseio.com"
```

Make sure in the Firebase console:

* **Authentication -> Anonymous** is enabled
* **Realtime Database** rules allow `auth != null` on `liveSessions/$sid`

## Notes

* Android 7.0 (API 24)+. Camera + microphone permissions are requested on launch.
* The web app (`index.html` in this repo) uses the same database, so an Android phone
  can broadcast and the web page on a TV can watch - and vice versa.
* TURN: add your TURN server in `WebRtcClient.initPeer()` for strict mobile networks.
