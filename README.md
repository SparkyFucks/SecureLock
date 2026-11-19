# SecureLock — Modern Android App Lock

> **Completely Free · Local-First · Zero Ads · Zero Trackers**

SecureLock is a privacy-first, native Android application built with Kotlin, Jetpack Compose, and Material 3. It protects applications on the user's device using local hardware-backed cryptography and standard Android security APIs.

---

## 📱 Features

- **Three Authentication Methods**:
  - **Numeric PIN**: Custom keypad with 4 to 8 digit input.
  - **Alphanumeric Password**: Strong text password with criteria validation.
  - **Pattern Lock**: Interactive 3x3 touch-drag Canvas with real-time gesture lines.
- **Biometric Unlock**: Integrated with AndroidX `BiometricPrompt` for fingerprint and face unlock.
- **Cryptographic Security**:
  - Credentials hashed using **PBKDF2-HMAC-SHA256** with 16-byte cryptographically random salt (10,000 iterations). Credentials are never stored in plaintext.
  - Android Keystore AES-256-GCM architecture.
- **Live Foreground App Interception**:
  - Foreground Service with low-priority, ongoing notification.
  - 350ms battery-friendly monitoring loop using `UsageStatsManager.queryEvents`.
  - Blocks access to protected apps with a `FLAG_SECURE` lock screen overlay that prevents screen captures and recent app thumbnail leaks.
  - Back/cancel gestures return the user safely to the home launcher.
- **Customizable Re-Lock Timing**:
  - **Immediately**: Re-locks as soon as the user leaves the protected app.
  - **After 1 minute**: Grace period allowing rapid switching between apps before re-locking.
- **App Selection Management**:
  - Queries launchable apps via `<queries>` launcher intent filter.
  - Real-time search and filter chips ("All", "Protected", "Unprotected").
- **Security Settings & Verification**:
  - Requires current credential confirmation before changing lock methods.
  - Live permissions management screen.
  - Tailored OEM background guidance for Xiaomi, Samsung, Huawei, Oppo, and Vivo devices.
- **Auto-Restart on Reboot**:
  - Automatically restores protection on system boot via `BootReceiver`.

---

## 🛠️ Tech Stack & Compatibility

- **Language**: Kotlin 1.9.23
- **UI Toolkit**: Jetpack Compose (BOM 2024.04.01)
- **Design System**: Material 3 (with Dynamic Color support on Android 12+)
- **Architecture**: Clean Architecture / MVVM with Kotlin Coroutines & Flow
- **Persistence**: Jetpack DataStore Preferences
- **Security**: Android Keystore & PBKDF2-HMAC-SHA256
- **SDK Target**:
  - `minSdk`: 26 (Android 8.0 Oreo — >96% device compatibility)
  - `compileSdk`: 34 (Android 14)
  - `targetSdk`: 34 (Android 14)

---

## 🚀 How to Open in Android Studio

1. Launch **Android Studio**.
2. Select **File -> Open...**.
3. Choose the directory: `d:/GooglePlayStore/SecureLock`.
4. Allow Gradle to sync dependencies and build.
5. Select a connected device or emulator and click **Run (Shift + F10)**.

---

## 📦 Ready-to-Install APK

The compiled debug APK is located at:
- `SecureLock/app-debug.apk` (17.11 MB)

To install via ADB:
```powershell
adb install SecureLock/app-debug.apk
```
