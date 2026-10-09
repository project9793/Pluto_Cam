# Pluto Live — Pluto Cam & Pluto TV

One self-contained HTML app that streams a phone's live camera to an Android TV over the internet.
Firebase Realtime Database handles the WebRTC signaling; the video/audio itself flows peer-to-peer
through WebRTC (never through the database).

- **Pluto Cam · Broadcast** — runs on the phone. Captures camera + mic and starts a live session.
- **Pluto TV · Watch** — runs on the TV / laptop. Connects with a 6-digit code and plays the stream full-screen.

## Files in this repo

| File | Purpose |
|------|---------|
| `pluto-live.html` | The complete app (both roles in one file). |
| `README.md` | This guide. |

> Tip: rename `pluto-live.html` to `index.html` and it will open at your site root
> (`https://<user>.github.io/<repo>/`) instead of `.../pluto-live.html`. Both work.

## Deploy on GitHub Pages

1. In this repo: **Add file → Upload files**, drag in `pluto-live.html`, then **Commit changes**.
   (Optional: name it `index.html` instead.)
2. Go to **Settings → Pages**.
3. Under **Build and deployment → Source**, choose **Deploy from a branch**.
4. Branch: **main**, folder: **/ (root)**, then **Save**.
5. Wait about a minute. Your site will be live at:
   `https://project9793.github.io/Pluto_Cam/`
   (or `https://project9793.github.io/Pluto_Cam/pluto-live.html`)
6. Open that URL on the phone **and** on the Android TV.

HTTPS is provided by GitHub Pages automatically — required for camera and WebRTC.

## Firebase setup (project: pluto-cam)

The Firebase web config is already embedded in `pluto-live.html`. Also do this:

1. **Authentication → Sign-in method → Anonymous → Enable.**
2. **Realtime Database → Rules** — paste the rules below and **Publish**:

```json
{
  "rules": {
    "liveSessions": {
      "$sid": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

3. Optional but recommended for strict mobile networks: add a TURN server to `ICE_SERVERS`
   near the top of the script in `pluto-live.html`:

```js
var ICE_SERVERS = [
  { urls: ["stun:stun.l.google.com:19302", "stun:stun1.l.google.com:19302"] },
  { urls: "turn:YOUR_TURN_HOST:3478", username: "USER", credential: "PASS" }
];
```

## How to use

1. **Phone** → open the site → **Pluto Cam · Broadcast** → **Start Live**.
   A 6-digit code appears (and a QR).
2. **TV** → open the same site → **Pluto TV · Watch** → type the code → **Connect**.
3. The live camera shows on the TV. Use **Full screen** / **Fill** as needed.

## Notes & safety

- The Firebase web **API key is public by design** — it is meant to sit in client code.
  Real protection comes from the Realtime Database rules above.
- **Never** commit service-account private keys or TURN secrets to this repo.
- The TV's browser has no camera, so always use the manual code (QR is a convenience only).
- Session data is cleaned up when a broadcast stops.

## Test locally (no setup)

Open `pluto-live.html` in two tabs of the same browser, use **Broadcast** in one and **Watch**
in the other, and enter the code — it connects instantly in "Local test mode".
