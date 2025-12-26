# Veyra TV 📺

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin)
![License](https://img.shields.io/badge/License-Open%20Source-blue?style=flat)
![Status](https://img.shields.io/badge/Status-Active-success)

**Veyra TV** is a modern, open-source IPTV media player application built for Android Mobile and Android TV. Designed with **Jetpack Compose** and **Material Design 3**, it offers a sleek, responsive, and user-friendly interface for streaming your personal M3U playlists.

---

## ⚠️ Important Disclaimer

**Veyra TV is strictly a media player.**

*   ❌ **No Content Provided:** This application **does not** contain any TV channels, movies, streams, or playlists.
*   ❌ **No Subscriptions:** We do not sell or provide IPTV subscriptions.
*   ✅ **User Content:** Users must provide their own content (M3U playlists) to use this application.
*   ✅ **Legal Use:** Veyra TV is designed to play legal, free-to-air content or content you have the rights to access. The developers do not endorse or support the streaming of copyright-protected material without permission.

---

## ✨ Features

### 📺 Core Playback
*   **Powerful Player:** Built on top of **Media3 (ExoPlayer)** for reliable and high-performance streaming.
*   **Format Support:** Supports HLS (m3u8), DASH, and other standard streaming formats.
*   **Quality Control:** Dynamic video quality selection (Auto, 1080p, 720p, etc.) and track selection.
*   **Live Buffering:** Optimized buffering strategy for live content.

### 🎨 User Interface & Experience
*   **Cross-Platform UI:** Fully responsive design that adapts seamlessly between **Android Mobile** (touch) and **Android TV** (D-pad/Remote).
*   **Material Design 3:** Modern aesthetics with dark mode support for cinematic viewing.
*   **TV-First Experience:** Dedicated focus handling, banner support, and remote control navigation.

### 🛠️ Functionality
*   **Playlist Management:** fast parsing of M3U playlists.
*   **Smart Search:** Real-time channel search with debounce for efficiency.
*   **Categorization:** Auto-grouping of channels by categories defined in the playlist.
*   **Favorites:** (Coming Soon) Mark your most-watched channels for quick access.

---

## 📱 Screenshots

| Mobile Home | Video Player | Settings |
|:---:|:---:|:---:|
| *(Add Screenshot)* | *(Add Screenshot)* | *(Add Screenshot)* |

---

## 🛠️ Tech Stack

Veyra TV is built using modern Android development practices and libraries:

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Video Player:** [AndroidX Media3 (ExoPlayer)](https://developer.android.com/media/media3)
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
*   **Database:** [Room](https://developer.android.com/training/data-storage/room)
*   **Asynchrony:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
*   **Dependency Injection:** Manual / ViewModel Factory (Scalable to Hilt/Koin)
*   **Networking:** [OkHttp](https://square.github.io/okhttp/)

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 11+
*   Android SDK API 36 (compileSdk) / API 24 (minSdk)

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Shyam-vadgama/veyra-tv.git
    ```
2.  **Open in Android Studio:**
    *   File -> Open -> Select the cloned directory.
3.  **Build the project:**
    *   Let Gradle sync.
    *   Run on an Emulator or Physical Device.

---

## 🤝 Contributing

We welcome contributions from the open-source community! Whether it's fixing bugs, improving the UI, or adding new features, your help is appreciated.

### How to Contribute
1.  **Fork** the repository.
2.  Create a new **Branch** for your feature/fix.
3.  **Commit** your changes.
4.  **Push** to your fork.
5.  Submit a **Pull Request**.

### Reporting Issues
*   Found a bug? [Report it here](https://github.com/Shyam-vadgama/veyra-tv/issues/new?template=bug_report.yml)
*   Have a feature idea? [Request it here](https://github.com/Shyam-vadgama/veyra-tv/issues/new?template=feature_request.yml)

---

## 📄 License

This project is open-source. Please check the [LICENSE](LICENSE) file for more details.

---

## 📬 Contact & Support

For support, questions, or just to say hi:

*   **GitHub Issues:** [Project Issues](https://github.com/Shyam-vadgama/veyra-tv/issues)
*   **Email:** shyam.veyra.tv@gmail.com

---

<p align="center">
  Made with ❤️ by the Veyra Open Source Team
</p>
