# OmniCast 4K: Production-Grade Universal TV Casting & Remote

A high-performance Android casting, screen mirroring, and smart TV control system engineered around production realities: realistic protocol pipelines (WebRTC/Low-latency RTSP vs. standard Cast/DLNA), brand-specific pairing handshakes (LG webOS SSAP, Samsung Tizen WS, Google TV Remote v2 PIN pairing, and Roku ECP), a hybrid app launcher with local-to-mirror fallback for reading apps (Kindle), and thermal-safe 1080p60 mirroring with 4K UHD direct media offload.

---

## Technical Specifications & Production Realities

| Architecture Pillar | Real-World Limitation | Production Solution |
|---|---|---|
| **Mirroring Latency** | Google Cast (300-500ms), DLNA (1.5-4s), OEM-locked Miracast. | Dual Pipeline: **Low-Latency Engine** (WebRTC / Local RTSP HTML5 receiver for ~60–120ms gaming/desktop) and **Standard Cast Pipeline** (DLNA/Cast AVTransport for ~400–800ms video playback). |
| **Resolution & Thermals** | 4K 60fps real-time `MediaCodec` encodes trigger thermal throttling & battery drain. | **1080p 60fps default** for live screen mirroring; **4K UHD reserved for direct local media streaming** (photos & videos offloaded directly to TV hardware decoder). |
| **TV App Launching & Kindle** | Kindle has no native app on webOS, Tizen, or Roku OS. | **Hybrid App Launcher**: Direct remote invocation for TV apps (YouTube, Netflix, Prime); **Local-App-to-Mirror fallback for Kindle**, launching Kindle locally and mirroring to TV. |
| **Remote Control Pairing** | TV brands enforce strict, distinct authentication protocols. | **Per-Brand Protocol Adapters**: `WebOsClient` (SSAP WebSocket + client token), `TizenClient` (Port 8002 tokenized WS), `AndroidTvRemoteClient` (PIN verification + TLS certs), and `RokuEcpClient` (ECP HTTP). |
| **Android 14/15 OS Rules** | Strict `mediaProjection` foreground service limits and display lifecycle kills. | Fresh `MediaProjection` consent intents per session, resilient `MediaProjection.Callback` registration, and `FOREGROUND_SERVICE_MEDIA_PROJECTION` compliance. |

---

## 1. System Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                        OmniCast Jetpack Compose UI                     │
│  [ScreenMirrorHUD]   [RemotePad & PIN Modal]   [HybridAppLauncher]     │
│  [MediaCaster 4K]    [DeviceScannerRadar]      [SetupPermissionWizard] │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ (StateFlow / Actions)
┌───────────────────────────────────▼────────────────────────────────────┐
│                             CastViewModel                              │
├────────────────────────────────────────────────────────────────────────┤
│  • Engine Coordinator & Session State Machine                          │
│  • Latency & Thermal Guardrail Controller                              │
│  • Permission & MediaProjection Consent Flow                           │
└──────────────┬────────────────────┬────────────────────┬───────────────┘
               │                    │                    │
┌──────────────▼───────┐ ┌──────────▼──────────┐ ┌───────▼──────────────┐
│ Remote Control Layer │ │  Streaming Engine   │ │ Local Persistence    │
├──────────────────────┤ ├─────────────────────┤ ├──────────────────────┤
│ • WebOsClient (SSAP) │ │ • Low-Latency RTSP  │ │ Room Database (KSP)  │
│ • TizenClient (WS)   │ │ • Direct DLNA / Cast│ │ • DiscoveredDevice   │
│ • AndroidTvRemote    │ │ • MediaCodec 1080p60│ │ • BrandAuthToken     │
│   (TLS / PIN Dialog) │ │ • Direct 4K File    │ │ • CastHistory        │
│ • RokuEcpClient      │ │   Streamer (HTTP)   │ │ • TvAppShortcut      │
└──────────────────────┘ └─────────────────────┘ └──────────────────────┘
```

---

## 2. Remote Pairing Lifecycle

Every TV brand exhibits a stateful security lifecycle:
1. **Roku**: Zero-pairing needed. HTTP POST to `http://<ip>:8060/keypress/<key>` and `/launch/<appId>`.
2. **LG webOS**: Connects to `ws://<ip>:3000/`. Sends client handshake. If no stored `client-key` token exists in Room, TV displays a pairing prompt. When user clicks "Allow" on TV, webOS returns the token, which is stored in Room.
3. **Samsung Tizen**: Connects to `wss://<ip>:8002/api/v2/channels/samsung.remote.control`. Displays authorization popup on TV; returns token stored in database.
4. **Android TV / Google TV**: Requires pairing on port 6467. Generates 4-digit numeric code on TV screen. OmniCast displays an in-app PIN entry dialog, executes TLS handshake, and saves credentials.

---

## 3. Hybrid TV App Launcher & Kindle Flow

- **Direct TV Apps**: YouTube (`com.google.android.youtube.tv` / Tizen `org.tizen.youtube` / Roku `800`), Netflix, Prime Video, Disney+, Spotify. Sends launch payload directly to TV.
- **Kindle & Reading Apps**: Because Amazon does not distribute a standalone Kindle TV app for webOS, Tizen, or Roku, OmniCast initiates **Local-to-Mirror Mode**:
  1. Activates screen mirroring to TV with Reading & High-Contrast profile.
  2. Launches the local Kindle Android app or browser reader on the phone.
  3. Displays a persistent on-screen floating control pill on phone to turn pages or adjust brightness on the TV screen.

---

## 4. Streaming & Latency Modes

1. **Low-Latency Game & Interactive Mirroring (Default)**:
   - Target: 1080p @ 60fps, ~65–110ms latency.
   - Bitrate: 14–18 Mbps H.264/HEVC.
   - Low thermal envelope, zero jitter dropouts.
2. **Direct 4K UHD Video & Photo Casting**:
   - Target: 3840x2160 (4K), ~400–600ms latency.
   - Bypasses real-time screen re-encoding. Serves media directly via local embedded HTTP server / UPnP AVTransport to TV's native hardware decoder.
3. **Adaptive Profile**:
   - Dynamically scales between 1080p and 720p depending on Wi-Fi packet drop and round-trip ping.
