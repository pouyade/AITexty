# AITexty

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

AITexty is an Android SMS client with on-device AI, privacy tools, and full Persian (Farsi) support — including offline text-to-speech for accessibility.

**Package:** `com.dpouya.aitexty`  
**Min SDK:** 24 · **Target SDK:** 36 · **Java:** 17

---

## Features

### Messaging
- Default SMS/MMS app with conversation list, search, and compose
- New message flow with contact picker and phone-number entry
- Clickable links in message bubbles
- Custom chat themes per conversation
- Spam filter tab and spam classification

### On-device AI
- Local LLM inference via **llama.cpp** (`jllama` native library)
- Download and manage GGUF models from AI settings
- **Suggest reply** based on recent chat history
- **Auto-reply** rules with configurable persona per contact
- Spam classification helper

### Privacy & security
- **Blocklist** with silent, reject, or delete modes
- **Hidden chats** vault (PIN or secret gesture unlock)
- **Decoy notifications** for hidden conversations
- **Local encryption** for conversations stored on device
- **End-to-end encryption** between AITexty users via QR key exchange

### Accessibility
- Speak messages aloud (TTS)
- Speech-to-text in compose
- Voice commands (e.g. send message, read message, find chat)
- Adjustable speech rate and voice selection
- **Built-in offline Persian TTS** (Sherpa ONNX / Piper voices):
  - **Gyro** — bundled with the app
  - **Amir, Ganji, Ganji Adabi, Reza Ibrahim** — downloaded on demand (~63 MB each)
- Falls back to Google TTS or Samsung SMT when appropriate for non-Persian text

### Localization
- English and Persian UI (`en.plang`, `fa.plang`)
- RTL layout support

---

## Requirements

- Android 7.0 (API 24) or newer
- **arm64-v8a** or **armeabi-v7a** device (native libraries)
- Must be set as the **default SMS app** for full functionality
- Permissions: SMS, contacts, notifications, internet (model/voice downloads), camera (QR), microphone (speech input)

---

## Download

Pre-built **universal APK** (arm64-v8a + armeabi-v7a in one file):

| Source | How |
|--------|-----|
| **GitHub Releases** | Open [Releases](../../releases) and download `AITexty-*-release-universal.apk` |
| **CI artifact** | Actions → **Build universal APK** → latest run → **aitexty-universal-apk** |
| **Build locally** | `./gradlew :app:fatApk` → output in [`dist/`](dist/) |

Install on device: enable **Install unknown apps** for your file manager or browser, then open the APK.

To publish a release from CI, push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

---

## Build

1. Clone the repository.
2. Open the project in **Android Studio** (Ladybug or newer recommended) or use the Gradle wrapper from the command line.
3. Sync Gradle — JitPack is required for the Persian TTS dependency.
4. Build and run:

```bash
./gradlew :app:assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/`.

### Universal (fat) release APK

One APK with all supported CPU architectures (no per-ABI splits):

```bash
./gradlew :app:fatApk
```

Output: `dist/AITexty-<version>-release-universal.apk`

Without `keystore.properties`, the release APK is signed with the **debug keystore** so you can sideload it. For Play Store builds, copy `keystore.properties.example` to `keystore.properties` and add your release keystore.

---

## First run

1. Complete onboarding and grant SMS, contacts, and notification permissions.
2. Set AITexty as the **default SMS app** when prompted.
3. Optional: open **Settings → AI settings** to download a GGUF model for reply suggestions and auto-reply.
4. Optional: open **Settings → Accessibility** to configure TTS, speech rate, and Persian voice. Downloaded voices need storage space and a network connection for the first download.

If a Persian voice fails to load after an interrupted download, clear app storage once and select the voice again — the app validates model integrity (size + SHA-256) before loading.

---

## Project structure

```
app/src/main/java/com/dpouya/aitexty/
├── activities/          # Screens (Main, Chat, Settings, AI, Accessibility, …)
├── accessibility/       # TTS, STT, voice commands, Persian ONNX engine
├── ai/                  # LlamaEngine, ModelManager, auto-reply, spam classifier
├── components/          # BaseActivity, shared UI widgets
├── data/                # Room database, entities, SmsRepository
├── helper/              # Theme, locale, permissions, links, contacts
├── privacy/             # Hidden vault, blocklist, unlock gestures
├── security/            # CryptoManager, key exchange
├── sms/                 # Receivers, SMS provider, compose intent
└── ui/                  # Adapters, cells, action bar, chat themes
```

Assets and strings live under `app/src/main/assets/langs/`.

---

## Tech stack

| Area | Libraries / tools |
|------|-------------------|
| UI | AndroidX AppCompat, Material, RecyclerView |
| Database | Room |
| Background work | WorkManager |
| Crypto | AndroidX Security Crypto, AES-GCM |
| QR codes | ZXing |
| Persian TTS | [PersianTTS](https://github.com/am3n/PersianTTS) (Sherpa ONNX), Apache Commons Compress |
| AI | llama.cpp via `jllama` native bindings |

---

## Architecture notes

- **UI** is built mostly in code (programmatic layouts), not XML fragments.
- **SmsRepository** syncs system SMS with a local Room database for fast UI and AI context.
- **PersianTtsEngine** loads Piper ONNX models through Sherpa ONNX; native load is probed in an isolated process (`:persian_tts_probe`) so a bad model file cannot crash the main app.
- **NotificationCenter** provides in-app event bus-style updates between components.

---

## Third-party models

Persian TTS voice archives are downloaded from the [sherpa-onnx TTS models release](https://github.com/k2-fsa/sherpa-onnx/releases/tag/tts-models). AI models are user-supplied GGUF files configured in the app.

---

## License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE).

Copyright (c) 2026 Pouya D

Third-party libraries and downloaded models (e.g. Sherpa ONNX, Piper voices, GGUF files) remain under their own licenses.

---

## Author

Developed by **Pouya** (`com.dpouya.aitexty`).
