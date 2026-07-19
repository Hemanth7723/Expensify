# 💰 Expensify - AI-Powered Expense Tracker

> *A secure, offline daily expense tracker with AI insights, multi-currency support, and smart analytics, Expensify is a privacy-first personal finance application that allows users to seamlessly capture, categorize, and analyze their spending patterns with complete data privacy.*

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)
![License](https://img.shields.io/badge/License-GPL%203.0-blue?style=for-the-badge)

[Features](#-features) • [Quick Start](#-quick-start) • [Tech Stack](#-tech-stack) • [Architecture](#-architecture) • [Contributing](#-contributing)

</div>

---

## 🎯 About

**Expensify** is a modern Android application that helps you track your daily expenses with confidence. Built with cutting-edge technology, it combines local-first privacy with AI-powered insights powered by Google's Gemini API.

### Why Expensify?

✅ **100% Offline** - Your data stays on your device  
✅ **AI-Powered Insights** - Get smart spending analysis  
✅ **Multi-Currency Support** - Track expenses in any currency  
✅ **Military-Grade Encryption** - Your financial data is secure  
✅ **Beautiful UI** - Modern Jetpack Compose interface  
✅ **CSV Export** - Analyze your budget in Excel  
✅ **Voice Input** - Add expenses hands-free  

---

## ✨ Features

*   **⚡ Dual Entry Modes:** Log expenses instantly using the standard manual input form or launch the dedicated, streamlined **Voice Logger** mode from the main interface.
*   **🧠 Offline NLP Engine:** The voice logging system utilizes ultra-fast local regex and keyword parsing patterns to completely resolve spoken text commands into organized data without sending audio to external services.
*   **💎 Fluid UI Overlays:** A beautifully integrated `VoiceSpeechParserPanel` dynamic modal handles real-time audio capture, streaming animations, and runtime recording permissions elegantly.
*   **🔒 Privacy-First Design:** Zero external tracker analytics, zero backend storage dependencies, and complete local execution by default.


### 💡 Core Features

| Feature | Description |
|---------|-------------|
| **Expense Tracking** | Log expenses with amount, category, date, and notes |
| **AI Insights** | Get personalized spending analysis powered by Gemini AI |
| **Multi-Currency** | Switch between currencies with real-time conversion |
| **Smart Analytics** | View spending breakdown by category and time period |
| **Voice Input** | Add expenses using voice commands |
| **Budget Export** | Export expenses as CSV for spreadsheet analysis |

### 🔒 Security & Privacy

| Feature | Description |
|---------|-------------|
| **End-to-End Encryption** | All data encrypted using AES-256-GCM |
| **Android Keystore** | Cryptographic keys stored securely |
| **Offline First** | No cloud dependency, your data is yours |
| **No Data Collection** | We never track or analyze your usage |
| **Open Source** | Full transparency with GPL 3.0 license |

### 👤 Profile Management

- Custom profile creation
- User name, email, and bio
- Multiple profile support
- Profile-specific settings
- Custom API key configuration

### 📊 Spending Analytics

- Category-wise breakdown
- Time-based analysis
- Spending trends
- Custom sorting (Newest, Oldest, Amount High/Low)
- Category filtering

---

## 🚀 Quick Start

### Prerequisites

- **Android Studio** (latest version recommended)
- **Android SDK 31+**
- **Gemini API Key** (free tier available at [Google AI Studio](https://ai.google.com))
- **Kotlin 1.9+**

### Installation Steps

#### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Hemanth7723/Expensify.git
cd Expensify
```

#### 2️⃣ Open in Android Studio
```bash
1. Launch Android Studio
2. Click "Open" and select the Expensify folder
3. Wait for Gradle sync to complete
4. Android Studio will automatically configure the project
```

#### 3️⃣ Configure API Key

Create a `.env` file in the project root:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

**How to get your Gemini API Key:**
1. Go to [Google AI Studio](https://ai.google.com)
2. Click "Get API Key"
3. Create a new API key
4. Copy and paste it in `.env`

See `.env.example` for reference.

#### 4️⃣ Fix Signing Configuration

Edit `app/build.gradle.kts` and remove this line:
```kotlin
signingConfig = signingConfigs.getByName("debugConfig")
```

#### 5️⃣ Run the App

```bash
1. Click "Run" or press Shift + F10
2. Select an emulator or connected Android device
3. Wait for the build to complete
4. Enjoy! 🎉
```

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
│  • Encryption (AES-GCM)            │
│  • Preferences (DataStore)          │
└─────────────────────────────────────┘
```

### Key Components

#### 🗄️ Database Layer
- **Room Database** for local persistence
- Expense table with full history
- Optimized queries for fast lookups
- Automatic migrations

#### 🔐 Security Layer
- **CryptoManager** handles encryption/decryption
- AES-256-GCM cipher for data protection
- Android Keystore integration
- Base64 encoding for storage

#### 🧠 AI Layer
- **Gemini API Integration** for smart insights
- Natural language processing
- Spending pattern analysis
- Personalized recommendations

#### 💾 State Management
- **StateFlow** for reactive data
- **MutableStateFlow** for state mutations
- Coroutines for async operations
- MVVM architecture pattern

---

## 📁 Project Structure

```
Expensify/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/expensify/
│   │   │   │   ├── data/
│   │   │   │   │   ├── CryptoManager.kt          # Encryption logic
│   │   │   │   │   ├── PreferencesManager.kt     # Settings storage
│   │   │   │   │   ├── VoiceRecognizerHelper.kt  # Voice input handler
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── GeminiService.kt      # Gemini API integration
│   │   │   │   │   │   └── RetrofitClient.kt     # Retrofit configuration
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── ExpenseDatabase.kt    # Room database setup
│   │   │   │   │   │   ├── ExpenseDao.kt         # Database access object
│   │   │   │   │   │   └── migrations/            # Database migrations
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Expense.kt            # Expense data model
│   │   │   │   │   │   ├── Profile.kt            # User profile model
│   │   │   │   │   │   └── Currency.kt           # Currency configuration
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── ExpenseRepository.kt  # Data repository
│   │   │   │   │       └── ProfileRepository.kt  # Profile repository
│   │   │   │   ├── ui/
│   │   │   │   │   ├── ExpenseViewModel.kt       # Main view model
│   │   │   │   │   ├── ProfileViewModel.kt       # Profile view model
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   ├── ExpenseListScreen.kt
│   │   │   │   │   │   ├── AnalyticsScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   ├── AddExpenseScreen.kt
│   │   │   │   │   │   └── VoiceLoggerScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── ExpenseCard.kt
│   │   │   │   │   │   ├── CategoryFilter.kt
│   │   │   │   │   │   ├── VoiceSpeechParserPanel.kt
│   │   │   │   │   │   └── AnalyticsChart.kt
│   │   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── dimens.xml
│   │   │   │   ├── drawable/
│   │   │   │   │   └── (app icons and images)
│   │   │   │   └── mipmap/
│   │   │   │       └── (app launcher icons)
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   │       └── (unit tests)
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .env.example
├── .gitignore
├── metadata.json
└── README.md
```

---

## 🔧 Configuration

### Environment Variables

Create a `.env` file:

```env
# Required: Your Gemini API Key
GEMINI_API_KEY=your_api_key_here
```

### Build Configuration

**build.gradle.kts** includes:
- Kotlin Compose plugin
- Google DevTools KSP
- Roborazzi for testing
- Google Secrets plugin

### Gradle Properties

Configured in `gradle.properties`:
- Kotlin version
- Compose version
- Target/Min SDK levels
- Build tools version

---

## 🎨 Features in Detail

### 💰 Add Expenses

```
Easy input interface with:
├─ Amount (numbers only)
├─ Category (preset or custom)
├─ Date & Time (auto-filled)
├─ Notes (optional)
└─ Receipt attachment (optional)
```

### 🗣️ Voice Input

- Speak expenses naturally
- "I spent $50 on groceries"
- AI processes and fills details
- Fallback for manual entry

### 📊 Analytics Dashboard

```
View insights:
├─ Total spending (current period)
├─ Category breakdown (pie/bar chart)
├─ Spending trends (line graph)
├─ Average daily spend
└─ Largest expenses
```

### 🌍 Multi-Currency

- 150+ currency support
- Real-time conversion
- Persistent currency selection
- Global spending summary

### 📤 CSV Export

```
Export data with:
├─ All expenses
├─ Date range filtering
├─ Category selection
└─ Formatted for Excel
```

---

## 🔐 Security Details

### Encryption Mechanism

**Algorithm:** AES-256-GCM (Advanced Encryption Standard)

```kotlin
// Every sensitive field is encrypted
val encryptedName = CryptoManager.encryptString(userName)
val decryptedName = CryptoManager.decryptString(encryptedName)
```

### Data Storage

- **Local-only:** No cloud uploads
- **Device-specific:** Tied to Android Keystore
- **Automatic backup:** Optional, encrypted
- **User control:** Delete anytime

### Permissions

Minimal required permissions:
- `INTERNET` - For Gemini API calls
- `RECORD_AUDIO` - For voice input (optional)
- `READ_EXTERNAL_STORAGE` - For receipt attachments (optional)

---

## 🚀 Building & Deployment

### Build APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing)
./gradlew assembleRelease
```

### Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedAndroidTest -P android.testInstrumentationRunnerArguments.class=...
```

### Continuous Integration

The project supports:
- GitHub Actions workflows
- Automated testing on pull requests
- Build verification
- Code quality checks

---

## 📚 API Reference

### ExpenseViewModel

```kotlin
// Add expense
fun addExpense(expense: Expense)

// Delete expense
fun deleteExpense(expense: Expense)

// Update expense
fun updateExpense(expense: Expense)

// Get AI insights
fun generateAIInsights()

// Change currency
fun setCurrency(currency: String)

// Sort expenses
fun setSortOrder(order: String) // "Newest", "Oldest", "Amount High", "Amount Low"

// Filter by category
fun filterByCategory(category: String)
```

### CryptoManager

```kotlin
// Encrypt string
fun encryptString(text: String): String

// Decrypt string
fun decryptString(encodedText: String): String

// Encrypt bytes
fun encrypt(bytes: ByteArray): ByteArray

// Decrypt bytes
fun decrypt(packed: ByteArray): ByteArray
```

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Gradle sync fails** | Update Android Studio to latest version |
| **API key not working** | Verify key in `.env` and ensure Gemini API is enabled |
| **App crashes on startup** | Check Android SDK version (min API 31) |
| **Encryption errors** | Ensure Android Keystore is accessible |
| **Voice input not working** | Grant microphone permissions in app settings |

### Debug Mode

Enable verbose logging:

```kotlin
// In ExpenseViewModel
private const val DEBUG = true

if (DEBUG) {
    Log.d("Expensify", "Detailed debug information")
}
```

---

## 📱 Supported Devices

- **Android:** 12+ (API 31+)
- **Screen Sizes:** Phone & Tablet
- **Architectures:** ARM64, x86_64

Recommended specs:
- RAM: 4GB minimum, 6GB+ recommended
- Storage: 50MB free space
- Network: WiFi or mobile for Gemini API calls

---

## 🔄 Version History

| Version | Features | Release Date |
|---------|----------|--------------|
| 1.0 | Core expense tracking, encryption, AI insights | 2025-07-12 |

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** changes (`git commit -m 'Add AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Development Guidelines

- Follow Kotlin naming conventions
- Add comments for complex logic
- Write unit tests for new features
- Update README if needed
- Keep commits atomic and descriptive

---

## 📋 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

**What this means:**
- ✅ Free to use, modify, and distribute
- ✅ Must provide source code
- ✅ Must include license notice
- ✅ Same license for derivatives

---

## 🙏 Acknowledgments

- **Gemini AI** for intelligent spending insights
- **Jetpack Compose** for modern UI
- **Room Database** for reliable local storage
- **Android Keystore** for security

---

## 📧 Contact & Support

- 🐛 **Report Bugs:** [GitHub Issues](https://github.com/Hemanth7723/Expensify/issues)
- 💬 **Discussions:** [GitHub Discussions](https://github.com/Hemanth7723/Expensify/discussions)
- 📝 **Feature Requests:** [GitHub Issues](https://github.com/Hemanth7723/Expensify/issues/new?labels=enhancement)

---

## 🎓 Learning Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose/documentation)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Android Security Best Practices](https://developer.android.com/topic/security)
- [Gemini API Documentation](https://ai.google.dev/)
- [Retrofit Guide](https://square.github.io/retrofit/)

---

## 🌟 Star History

If you found this project helpful, please consider starring it! ⭐

<div align="center">

**Made with ❤️ for better expense tracking**

*Track smarter. Spend wisely. Save more.* 💚

[↑ Back to Top](#-expensify---ai-powered-expense-tracker)

</div>
