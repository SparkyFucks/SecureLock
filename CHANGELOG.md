# Changelog

All notable changes to the **SecureLock** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-11-19 (v1.1)

### Added
- **Powered by SparkyLabs**: Brand footer added across Splash, Home, and Lock screens.
- **Anti-Uninstall & Settings Shield**:
  - Direct protection for System Settings (`com.android.settings`) and Google Play Store (`com.android.vending`) to prevent unauthorized uninstallation or data wiping.
  - One-tap toggle in Security Settings.
- **Security Recovery Questions**:
  - Password recovery via 5 customizable security questions in setup and settings.
  - Secret answers salted and hashed with PBKDF2-HMAC-SHA256 (10,000 rounds).
  - "Forgot Password?" dialog on Lock Screen overlay with real-time answer verification.
- **Higher Android Version Permissions & Foreground Resilience**:
  - Native runtime notification dialog (`POST_NOTIFICATIONS`) for Android 13+ (API 33+).
  - Try/catch fallback for package-specific usage access and overlay settings intents.
  - Maximum-priority Full-Screen Intent notification fallback on Android 10+/14+ if OEM ROM background activity launch is throttled.

### Changed
- Incremented `versionCode` to `2` and `versionName` to `"1.1.0"`.
- Cleaned up deprecation warnings in `LockScreenActivity` using `OnBackPressedDispatcher`.
- Added unit test cases for security answer case-insensitivity and whitespace trimming.

---

## [1.0.0] - 2026-11-19 (v1.0)

### Added
- Initial project architecture and launch of SecureLock.
- Native Jetpack Compose UI with Material 3 Dark and Light theme adaptivity.
- Three authentication methods: Numeric PIN (4-8 digits), Alphanumeric Password, and Pattern Lock (3x3 interactive Canvas).
- Biometric authentication integration via AndroidX `BiometricPrompt`.
- Android Keystore AES-256-GCM and PBKDF2-HMAC-SHA256 cryptographic credential hashing.
- Reactive local persistence with Jetpack DataStore Preferences.
- Installed application querying via `<queries>` launcher intent filter (compliant with Android 11+ package visibility).
- Background `AppLockForegroundService` with 350ms monitoring loop using `UsageStatsManager.queryEvents`.
- `FLAG_SECURE` lock screen overlay activity.
- Automatic restart on reboot via `BootReceiver`.
- Manufacturer-tailored OEM background killer guidance (Xiaomi, Samsung, Huawei, Oppo, Vivo).
