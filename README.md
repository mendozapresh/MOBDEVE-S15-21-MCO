<<<<<<< HEAD
# SteadyMe — Native Android (Phase 3)

Production-oriented Java/XML implementation based on the supplied Figma flow: login/signup, home, logging options, manual mood logging, history, insights, and camera check-in.

## Setup
1. In Firebase, create an Android app with package `com.steadyme.app`, enable Email/Password Authentication and Cloud Firestore, then place the generated `google-services.json` in `app/`.
2. Add Firestore rules that restrict `users/{uid}` and `users/{uid}/moodLogs` to `request.auth.uid == uid`.
3. Open this folder in Android Studio, sync Gradle, and run on Android 8.0+.

## What is wired
- ViewBinding is used by every Activity/Fragment; no `findViewById` calls.
- Firebase Auth validates login and registration; Firestore reads/writes each signed-in user's records and live mood logs.
- CameraX and ML Kit face detection are connected. Facial emotion classification is intentionally not claimed: face detection is used only for framing/presence, while user confirmation remains clinically appropriate.
- WorkManager schedules a daily notification; Android 13+ notification permission is requested.
- MPAndroidChart renders the last seven Firestore mood scores.
=======
# MOBDEVE-S15-21-MCO
>>>>>>> 09d959842802eb0f1ee3e1c4b7cac8a00f57270c
