# Healynk

A Jetpack Compose Android app for personal health tracking with Firebase Auth, Firestore, and secure local PIN storage.

## Firebase Setup

1. Create a project in [Firebase Console](https://console.firebase.google.com/).
2. Add an Android app with package `com.example.healynk`.
3. Download the generated `google-services.json` and place it under `app/`.
4. Enable Email/Password authentication and Firestore database.
5. Sync Gradle in Android Studio.

## Assets

Add the Healynk logo image to `app/src/main/res/drawable/ic_healynk_logo.png` so Splash and Home screens can display it.

## Notes

- Firestore offline persistence is enabled by default.
- PINs are stored locally using `EncryptedSharedPreferences` and never leave the device.

