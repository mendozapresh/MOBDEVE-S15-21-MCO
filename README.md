
# SteadyMe — Native Android (Phase 3)

SteadyMe is a native Android application designed to help users track and reflect on their mood. This Phase 3 implementation follows the provided Figma design and includes user authentication, mood logging, mood history, insights, and camera-based check-ins.

## Features
1. Login and Sign Up — Users can create an account and securely log in using Firebase Authentication.
2. Home Screen — Provides access to the app's main mood-tracking features.
3. Mood Logging — Users can record their mood manually and view their previous entries.
4. Mood History — Displays the user's recorded mood logs from Firestore.
5. Insights — Shows mood trends based on the user's recorded entries.
6. Camera Check-In — Uses the device camera and ML Kit for face detection during check-ins.
7. Daily Reminders — Uses WorkManager to schedule daily mood-tracking notifications.

## Implementation Notes
- ViewBinding is used throughout the app instead of findViewById.
- Firebase Authentication handles user login and registration.
- Cloud Firestore stores user information and mood logs.
- CameraX provides camera functionality for the check-in feature.
- ML Kit is used for face detection to detect and frame a user's face.
- The camera check-in does not claim to identify or classify emotions. Users are still responsible for confirming and recording their mood.
- WorkManager handles scheduled daily notifications. Notification permission is requested on Android 13 and later.
- MPAndroidChart is used to display the user's mood scores and recent mood trends.

## Data and Privacy
Mood records are stored in Cloud Firestore under the authenticated user's account. Firestore security rules should be configured to prevent users from accessing another user's records.

# MOBDEVE-S15-21-MCO
>>>>>>> 09d959842802eb0f1ee3e1c4b7cac8a00f57270c
