# Strömr

Strömr is a modern Android podcast and audio streaming application built with Kotlin and Jetpack Compose.

## Features

- **Podcast Subscriptions**: Subscribe to RSS podcast feeds by URL and manage them from the Feeds screen.
- **Episodes & Tracks**: Browse podcast episodes and audio tracks in separate lists.
- **Background Playback**: Continuous audio playback with a media notification, powered by AndroidX Media3 (ExoPlayer + MediaSession).
- **Player Controls**: Play/pause, skip back 10 seconds, skip forward 30 seconds, previous/next, and adjustable playback speed.
- **Up Next Queue**: Add episodes or tracks to a playback queue.
- **Automatic Feed Refresh**: Feeds refresh in the background with WorkManager, with a configurable interval and an optional Wi-Fi-only mode.
- **Offline Library**: Episode metadata is cached locally in a Room database.
- **Modern UI**: Jetpack Compose and Material 3 with dark theme support and cover art loading.
- **Localization**: Available in English and Portuguese.
- **Friendly Error Handling**: Clear messages for network failures, server errors, missing files, and unsupported audio formats.

## Tech Stack

- **Kotlin** with Coroutines & Flow
- **Jetpack Compose** & **Material 3** for declarative UI
- **Navigation Compose** for in-app navigation
- **AndroidX Media3** (ExoPlayer & MediaSession) for streaming and background playback
- **Retrofit** & **OkHttp** for network requests
- **Room** (with KSP) for local database storage
- **DataStore Preferences** for subscriptions and user settings
- **WorkManager** for periodic background feed refresh
- **Coil** for cover art image loading
- **XmlPullParser** for RSS parsing (**kxml2** is used in unit tests)

## Project Structure

```text
app/src/main/java/com/spiritfenix/stromr/
├── data/          # Models, repository, RSS parser, DataStore stores, Room database
├── navigation/    # Navigation graph and routes
├── network/       # Retrofit RSS API client and feed refresh worker
├── service/       # Media3 playback service
└── ui/            # Compose screens, ViewModels, UI state, and theme
```

## Getting Started

### Prerequisites

- Android Studio (recent version recommended) or IntelliJ IDEA with Android support
- JDK 17
- Android SDK 36 (minimum supported Android version: API 26 / Android 8.0)

### Installation

Clone the repository and build a debug APK:

```bash
git clone https://github.com/Fenix-Spirit/Stromr.git
cd Stromr
./gradlew assembleDebug
```

### Running

Install and run on a connected Android device or emulator:

```bash
./gradlew installDebug
```

Run the unit tests:

```bash
./gradlew test
```

Run the instrumented tests on a connected device or emulator:

```bash
./gradlew connectedAndroidTest
```

## Releases

Signed release APKs are built with the **release** GitHub Actions workflow (`.github/workflows/release.yml`). The workflow is started manually and:

1. Bumps the `major`, `minor`, or `patch` version in `app/build.gradle.kts`.
2. Runs the unit tests.
3. Builds a signed, minified release APK.
4. Commits the version bump, creates a `vX.Y.Z` tag, and publishes a GitHub Release with the APK attached.

The workflow requires these repository secrets:

| Secret              | Description                     |
|---------------------|---------------------------------|
| `KEYSTORE_BASE64`   | Base64-encoded release keystore |
| `KEYSTORE_PASSWORD` | Keystore password               |
| `KEY_ALIAS`         | Signing key alias               |
| `KEY_PASSWORD`      | Signing key password            |

To build a signed release locally, set the `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` environment variables, then run:

```bash
./gradlew assembleRelease
```

If `KEYSTORE_PATH` isn't set, the release APK is built unsigned.

## License

[MIT](LICENSE)
