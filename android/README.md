# Pluto Live - Android (Kotlin)

A native Android app (Kotlin) that runs the Pluto Live web app full-screen.

* **Pluto Cam - Broadcast**: phone camera + mic -> WebRTC -> Firebase
* **Pluto TV - Watch**: 6-digit code, HLS/MP4 link player, 20s backup fallback
* Camera/mic permissions, full-screen, immersive mode, app icon

The app loads the live page straight from the GitHub Pages repo:

    https://project9793.github.io/Pluto_Cam/

So any update you push to the repo appears in the app on the next open - no rebuild needed.

A copy of the page is also bundled at `app/src/main/assets/pluto-live.html` and is used
automatically if the phone has no internet. Assets are served over
`https://appassets.androidplatform.net/`, which keeps them a **secure context**
(required by `getUserMedia()` and WebRTC).

## Build

1. Open the `PlutoLive` folder in **Android Studio** (Giraffe / Hedgehog or newer).
2. Let Gradle sync (it downloads Gradle 8.7 + the Android Gradle Plugin).
3. `Run` on a phone, or `Build > Build Bundle(s)/APK(s) > Build APK(s)`.
4. Install the APK on the phone (camera device) and/or the Android TV.

If Android Studio asks for the Gradle wrapper JAR, choose
*"Use Gradle from: gradle-wrapper.properties"* or let it generate the wrapper.

## Notes

* `applicationId` is `com.plutolive.app` - change it in `app/build.gradle.kts` if you
  registered a different package in Firebase.
* The HTML already contains your Firebase config, so no extra setup is needed.
* Requires Android 7.0 (API 24) or newer.
* Internet is required (Firebase signalling + HLS links).
