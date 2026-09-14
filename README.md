# Strömr

Strömr is a modern Android podcast and audio streaming application built with Kotlin and Jetpack Compose.

## Features

- **Podcast & Audio Streaming**: Fetch and parse RSS podcast feeds with audio playback.
- **Background Playback**: Continuous background audio playback via AndroidX Media3.
- **Modern UI**: Intuitive, responsive user interface built using Jetpack Compose and Material 3 with dark theme support.
- **Local Persistence**: Caching and local episode storage powered by Room database.
- **Robust Error Handling**: Friendly error reporting for network drops, playback issues, and unavailable media.

## Tech Stack

- **Kotlin** with Coroutines & Flow
- **Jetpack Compose** & **Material 3** for declarative UI
- **AndroidX Media3** (ExoPlayer & MediaSession) for audio streaming and background playback service
- **Retrofit** & **OkHttp** for network requests
- **Room** (with KSP) for local database storage
- **kxml2** for RSS XML feed parsing
- **Navigation Compose** for in-app screen navigation

## Getting Started

### Prerequisites

- Android Studio (recent version recommended) or IntelliJ IDEA with required plugins
- JDK 17 (or JDK 11+)
- Android SDK (API Level 26+ required, target SDK 36)

### Installation

Clone the repository and build the project:

```bash
git clone https://github.com/Fenix-Spirit/Stromr.git
cd Stromr
./gradlew assembleDebug
```

### Running

To run unit tests:

```bash
./gradlew test
```

To install and run directly on a connected Android device or emulator:

```bash
./gradlew installDebug
```

## License

[MIT](LICENSE)
