# 🦅 Koraki (κόρακι)

> A privacy-first personal note manager and virtual companion app with a signature dark-crimson aesthetic, stealth vault security, and interactive Shimeji companions.

---

## ✨ Key Features

- **🖤 Crimson Design System**: Bespoke dark UI palette with ambient glow shaders, semantic indicators, and full edge-to-edge Material 3 support.
- **🔒 Stealth Vault**: Encrypted space for confidential notes, stealthily accessible via a secret search trigger passcode (default: `#koraki`) or Biometric authentication. Locked vault notes are strictly hidden from standard search queries.
- **🐾 Interactive Virtual Companions (Shimeji)**: Screen companions with custom sprite animations, emotional state tracking, and overlay interactions.
- **📝 Journaling & Note Management**: Fast markdown-supported notes, tags, media attachments, and companion association.
- **🛡️ Local-First & Private**: Local persistence powered by Room Database and Jetpack DataStore Preferences.

---

## 🛠️ Architecture & Tech Stack

- **Platform:** Native Android (Min SDK 34, Target SDK 36)
- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Architecture:** Single Activity, MVVM + Clean Architecture with Unidirectional Data Flow (UDF)
- **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
- **Local Persistence:** Room Database + Jetpack DataStore Preferences
- **Asynchronous Execution:** Kotlin Coroutines & `StateFlow`
- **Security & Authentication:** AndroidX Biometric API

---

## 📁 Package Structure

```
com.aima.koraki
├── data/
│   ├── local/          # Room DAOs, Entities, Converters & Database
│   ├── model/          # Virtual Companion & Metadata Models
│   ├── preferences/    # Vault & App Preferences
│   └── repository/     # Repositories (Note, Companion, Shimeji, Backup)
├── di/                 # Dagger Hilt Dependency Injection Modules
├── navigation/         # Navigation Routes & Graphs
├── ui/                 # Composable Screens, Components & ViewModels
│   ├── companions/     # Virtual Companion Management
│   ├── components/     # Reusable UI Cards, Top/Bottom Bars, Overlays
│   ├── editor/         # Note & Journal Editor
│   ├── login/          # Biometric & Authentication Screens
│   ├── notes/          # Main Notes Hub
│   ├── profile/        # User Profile & App Settings
│   ├── theme/          # Color Palette, Typography & Custom Shaders
│   └── vault/          # Stealth Vault Interface
└── util/               # Biometric & Media Management Utilities
```

---

## 📱 Target Hardware

- **Google Pixel 10 Pro**
- **Samsung Galaxy S24 FE**

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer recommended)
- **JDK 17+**
- **Android SDK 34+**

### Building from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/AimaKoraki/Koraki.git
   cd Koraki
   ```

2. **Open in Android Studio** and sync Gradle dependencies.

3. **Build & Install:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Code Formatting & Verification:**
   ```bash
   ./gradlew spotlessApply
   ./gradlew test
   ```

---

## 📜 License

This project is licensed under the [Apache License 2.0](LICENSE).
