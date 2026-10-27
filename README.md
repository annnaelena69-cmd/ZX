# NEXUS X6 Gaming Launcher (Android)

This repository is now a native Android application project for POCO X6 gaming sessions, not a website.

## Features in this build
- Native Android dashboard built with Kotlin and Jetpack Compose
- Quick-launch buttons for PUBG Mobile, Mobile Legends, and Free Fire variants when installed
- Balanced, Smooth Feel, and Focus presets with saved preferences
- Crosshair overlay service with selectable shape and color; requires Android's display-over-other-apps permission
- Shortcuts to Android display and sound settings
- GitHub Actions workflow that builds a debug APK and uploads it as an artifact

## Limitations
- POCO X6 display hardware remains limited to 120 Hz; the app cannot turn it into 144 Hz or force higher game FPS.
- Performance presets are guidance, not a system-level performance hack.
- Color and impact switches save preferences; they do not globally rewrite another game's graphics, sound, or haptics.
- Real-time anime voice changing across other games is not implemented yet and requires a dedicated audio-processing implementation and compatibility testing.
- Some games may disallow overlays. Disable the crosshair if it violates a game's rules or causes problems.

## Get the APK
Open the repository's Actions tab and select a successful “Build NEXUS X6 Android APK” run. Download the artifact named nexus-x6-debug-apk. This is a debug APK for testing, not a Play Store release.

## Build locally
Install JDK 17 and Gradle 8.9, then run gradle assembleDebug from the repository root.
