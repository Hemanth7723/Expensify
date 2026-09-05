# 💰 Expensify - AI-Assisted Expense Tracker

> *A local-first personal expense tracker with optional AI-powered insights. Data is stored and encrypted on-device; AI features (voice parsing, insights) require an internet connection and a Google Gemini API key.*

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=pack-compose&logoColor=white)
![License](https://img.shields.io/badge/License-GPL%203.0-blue?style=for-the-badge)

[Features](#-features) • [Quick Start](#-quick-start) • [Tech Stack](#-tech-stack) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 🎯 About

**Expensify** is an Android application for tracking daily expenses with a strong emphasis on local privacy and encryption. AI-assisted features (voice parsing and spending insights) are optional and rely on Google&#39;s Gemini API; the core data storage and encryption operate on-device by default.

### Key Principles

- Local-first: your data stays on the device unless you explicitly export it
- Optional AI: Gemini-powered features are opt-in and require an API key
- Encrypted storage for sensitive fields using Android Keystore-backed AES-GCM
- Clean Compose UI with CSV export for offline analysis

---

## ✨ Features

* **Dual Entry Modes:** Manual form and AI-assisted Voice Logger (voice parsing uses Gemini when enabled).
* **Local Encryption:** Preferences and sensitive fields are encrypted prior to persistent storage.
* **CSV Export:** Export expenses as CSV for spreadsheets.
* **Lightweight UI:** Built with Jetpack Compose (Material 3).

### 💡 Core Features

| Feature | Description |
|---------|-------------|
| **Expense Tracking** | Log amount, category, timestamp, and optional notes |
| **AI Insights (Optional)** | Natural-language spending insights (requires Gemini API key) |
| **Currency** | Preferred currency symbol is stored; live conversion is not provided in this release |
| **Analytics** | Category and time-based breakdowns and simple visualizations |
| **Voice Input (Optional)** | Add expenses using voice; Gemini is used if configured |
| **CSV Export** | Export expenses in Excel-compatible CSV |

### 🔒 Security & Privacy

| Feature | Description |
|---------|-------------|
| **Encryption** | Sensitive values encrypted using AES-GCM (Android Keystore-backed) |
| **Keystore** | Cryptographic keys are managed by Android Keystore |
| **Local-first** | No cloud storage by default; AI calls are external and opt-in |
| **Open Source** | Full transparency under GPLv3 license |

### 👤 Profile Management

- Single local profile supported (name, email, bio, optional custom API key)
- Profile fields stored encrypted in SharedPreferences via CryptoManager

### 📊 Spending Analytics

- Category-wise breakdown
- Time-based grouping (day/week/month/year)
- Sorting and filtering

---

## 🚀 Quick Start

### Prerequisites

- **Android Studio** (latest stable recommended)
- **Android SDK** with appropriate platforms installed (minSdk 24, compileSdk/targetSdk 36)
- **Gemini API Key** (optional — required only for AI features)
- Gradle will manage Kotlin and plugin versions (see `gradle/libs.versions.toml`)

### Installation Steps

#### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Hemanth7723/Expensify.git
cd Expensify
```

#### 2️⃣ Open in Android Studio
1. Launch Android Studio
2. Click "Open" and select the Expensify folder
3. Wait for Gradle sync to complete

#### 3️⃣ (Optional) Configure Gemini API Key
Create a `.env` file in the project root (the Secrets Gradle Plugin will load it locally):

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

#### 4️⃣ Run the App
- Use Run in Android Studio or `./gradlew assembleDebug` then install the APK on a device/emulator.

---

## 🏗️ Architecture

### Tech Stack

```
┌─────────────────────────────────────┐
│      UI Layer (Jetpack Compose)     │
├─────────────────────────────────────┤
│     ViewModel (MVVM Pattern)        │
├─────────────────────────────────────┤
│   Repository & Data Layer           │
├─────────────────────────────────────┤
│  • Database (Room)                  │
│  • API (Retrofit + Gemini)          │
│  • Encryption (AES-GCM via Keystore)|
│  • Preferences (SharedPreferences)  │
└─────────────────────────────────────┘
```

### Key Components

#### 🗄️ Database Layer
- Room database for local persistence
- Current configuration uses `fallbackToDestructiveMigration()` — add explicit migrations before production to avoid data loss

#### 🔐 Security Layer
- `CryptoManager` handles AES-GCM encryption/decryption using Android Keystore
- Preferences store encrypted values for secrets (e.g., custom API key)

#### 🧠 AI Layer (Optional)
- Gemini API integration via Retrofit for parsing transcripts and generating insights
- Network calls require an API key and internet connectivity

#### 💾 State Management
- `StateFlow` + Coroutines for reactive state
- MVVM: Compose UI -> ViewModel -> Repository -> Room

---

## 📁 Project Structure (high level)

```
Expensify/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── CryptoManager.kt
│   │   │   │   │   ├── PreferencesManager.kt
│   │   │   │   │   ├── VoiceRecognizerHelper.kt
│   │   │   │   │   └── api/
│   │   │   │   │       └── GeminiService.kt
│   │   │   │   ├── data/database/
│   │   │   │   │   ├── ExpenseDatabase.kt
│   │   │   │   │   └── ExpenseDao.kt
│   │   │   │   ├── data/model/
│   │   │   │   │   └── Expense.kt
│   │   │   │   ├── data/repository/
│   │   │   │   │   └── ExpenseRepository.kt
│   │   │   │   └── ui/
│   │   │   │       ├── ExpenseViewModel.kt
│   │   │   │       └── MainActivity.kt
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔧 Configuration

### Environment Variables

Create a `.env` file for optional AI features:

```env
GEMINI_API_KEY=your_api_key_here
```

### Build Configuration

- KSP code generation is used (Room, Moshi)
- Secrets Gradle Plugin loads `.env` into BuildConfig for local builds

---

## 🎨 Features in Detail

### 💰 Add Expenses

- Amount, Category, Date/Time, Notes
- Manual entry is fully functional; AI-assisted voice entry is optional

### 🗣️ Voice Input

- Natural-language voice entry; Gemini is used when configured, otherwise use manual entry

### 📊 Analytics Dashboard

- Category breakdown and time-based grouping
- CSV export for offline analysis

---

## 🔐 Security Details

### Encryption Mechanism

- Sensitive strings are encrypted before storage using AES-GCM via Android Keystore (managed in `CryptoManager`). Example:

```kotlin
val encrypted = CryptoManager.encryptString(userName)
val decrypted = CryptoManager.decryptString(encrypted)
```

### Data Storage

- Local-only by default; AI calls are external and opt-in
- Preferences stored in `SharedPreferences` (encrypted values for secrets)
- Room DB uses destructive fallback for migrations in this branch; add migrations for production

### Permissions

Minimal required permissions:
- `INTERNET` (optional, for Gemini API calls)
- `RECORD_AUDIO` (optional, for voice input)
- `READ_EXTERNAL_STORAGE` (optional)

---

## 🚀 Building & Deployment

See project Gradle tasks for building and testing.

---

## 📚 API Reference (selected)

See `app/src/main/java/com/example/ui/ExpenseViewModel.kt` for exact signatures. Examples:

```kotlin
fun addExpense(title: String, amount: Double, category: String, note: String, date: Long)
fun genAiInsights()
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Gradle sync fails** | Update Android Studio and SDK |
| **API key not working** | Verify key in `.env` and enable the Generative Language API |
| **App crashes on startup** | Ensure device/emulator meets minSdk (API 24) |
| **Encryption errors** | Android Keystore may be unavailable on some test environments |

---

## 📱 Supported Devices

- **Android:** 7.0+ (API 24+)
- **Target SDK:** 36
- **Architectures:** ARM64, x86_64

---

## 🤝 Contributing

Fork, branch, implement, test, and open a PR. Keep changes focused and document behavior changes.

---

## 📋 License

GPLv3 (see LICENSE)

---

## 📧 Contact & Support

- Issues & Discussions: repository GitHub pages
