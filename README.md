# BankiyeMonitor 🏦🛡️

**Bank SMS Parsing, Aggregation & Security Monitoring System for Wolaita Sodo, Ethiopia**

BankiyeMonitor addresses the critical security and information-overload challenges facing bank customers in Wolaita Sodo. Ethiopian banks (such as CBE, Dashen, Awash, Bank of Abyssinia, Wegagen, and CoopBank) rely heavily on raw SMS alerts for account notifications. Hackers and scammers frequently impersonate bank senders, inject phishing links, and execute social engineering or credential attacks that get buried in a stream of hundreds of normal transactions.

BankiyeMonitor provides an intelligent, on-device SMS parsing engine that verifies senders, converts raw texts into clean structured data, detects security threats in real time, and synthesizes executive summaries.

---

## 🌟 Key Features

### 1. Spoof Detection & Sender Verification
* **Sender Whitelist Registry**: Maintains verified gateway sender IDs for all major Ethiopian banks active in Wolaita Sodo (`CBE`, `ComBankET`, `921`, `DashenBank`, `AwashBank`, `BOA`, `Wegagen`, `CoopBank`).
* **Impersonation Blocker**: Distinguishes authentic bank alphanumeric IDs from arbitrary mobile numbers (`+251...`) or deceptive alphanumeric senders pretending to be banks.

### 2. Lexical Tokenizer & Structured Parser Engine
* **Grammar Rule Analysis**: Extracts:
  * **Amount**: Robust parsing of ETB currency formats with thousand separators (`15,450.50 ETB`, `Birr 500`).
  * **Transaction Type**: `CREDIT`, `DEBIT`, `LOGIN`, `LOGIN_FAILED`, `PASSWORD_RESET`, `DEVICE_CHANGE`, `OTP`, `BALANCE_INQUIRY`, `ACCOUNT_LOCKED`.
  * **Counterparty**: Recipient or sender party names.
  * **Balance & Reference Numbers**: Available balance, trace/txn IDs, account masks (`****1234`).
  * **Device & Session Info**: Extracts device identifiers and session tokens from security notifications.
* **Adaptive Bank Format Handling**: Configurable rule engine (`BankFormatRegistry`) allowing dynamic pattern adjustments for different banks without requiring full app re-architecture.

### 3. Social Engineering & Phishing Detection
* **Urgency Scoring Engine (0–100)**: Detects psychological pressure language commonly used in scam SMS (`"urgent"`, `"suspended"`, `"verify now"`, `"blocked within 24 hours"`).
* **Phishing Link Detector**: Flags URLs, shortened links (`bit.ly`, `tinyurl`), and suspicious TLDs.

### 4. 100% On-Device Privacy & Encryption
* **Zero Third-Party Cloud Leaks**: Raw bank SMS messages and financial figures never leave the user's phone.
* **Encrypted Room Database (SQLCipher)**: All stored messages, security audit logs, and summaries are encrypted locally on disk using AES-256.

### 5. Alert Scheduler & Executive Summaries
* **Push Insights**: WorkManager-powered background scheduler producing periodic summaries:
  * **Daily**: High-volume Wolaita Sodo business owners tracking cash flow.
  * **Weekly**: Standard personal accounts.
  * **Monthly**: Low-activity savers.
  * **Yearly**: Annual tax and financial audit records.
* **Synthesized Metrics**: Total received, total sent, net flow, login attempt tallies, failed logins, device change alerts, and counterparty breakdowns.

---

## 🏗️ Architecture

BankiyeMonitor follows modern Android architectural standards:
* **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern + Clean Architecture
* **UI**: Jetpack Compose with Material Design 3 (Material You dynamic theming)
* **Local Storage**: Room Database + SQLCipher SQLite encryption
* **Dependency Injection**: Dagger Hilt
* **Asynchronous Streams**: Kotlin Coroutines & Flows
* **Background Tasks**: Android Jetpack WorkManager
* **Real-time SMS Ingestion**: Android `BroadcastReceiver` with `Telephony.Sms.Intents`

```
app/src/main/java/com/bankiyemonitor/app/
├── BankiyeMonitorApp.kt             # Application class with Notification Channels
├── di/
│   └── AppModule.kt                 # Dagger Hilt DI providers
├── parser/
│   ├── BankFormatRegistry.kt        # Adaptive bank rules & verified sender whitelists
│   ├── BankSmsParser.kt             # Main parsing orchestration engine
│   ├── SmsTokenizer.kt              # Lexical analyzer / token classification
│   ├── SpoofDetector.kt             # Spoofing, urgency & phishing scoring
│   └── model/
│       └── ParsedSmsData.kt         # Domain models, Enums (RiskLevel, TransactionType)
├── data/
│   ├── local/
│   │   ├── BankiyeDatabase.kt       # Room Database (SQLCipher encrypted)
│   │   ├── entity/                  # ParsedSmsEntity, SecurityEventEntity, SummaryEntity
│   │   └── dao/                     # ParsedSmsDao, SecurityEventDao, TransactionDao
│   ├── preferences/
│   │   └── UserPreferencesManager.kt# Encrypted DataStore preferences
│   └── repository/
│       ├── SmsRepository.kt         # SMS querying, parsing & caching
│       ├── SecurityRepository.kt    # Security incident tracking
│       └── TransactionRepository.kt # Summary data access
├── worker/
│   ├── SummaryWorker.kt             # Periodic summary computation & notifications
│   └── AlertScheduler.kt            # WorkManager scheduler (Daily/Weekly/Monthly/Yearly)
├── sms/
│   └── SmsBroadcastReceiver.kt      # Real-time incoming SMS interceptor & security alerts
└── ui/
    ├── MainActivity.kt              # App entry & runtime SMS permission handling
    ├── navigation/
    │   ├── BankiyeNavigation.kt     # Screen destinations & bottom nav items
    │   └── BankiyeNavHost.kt        # NavGraph routing
    ├── theme/
    │   ├── Color.kt                 # Material 3 & semantic risk palettes
    │   ├── Theme.kt                 # Dynamic Color / Light & Dark themes
    │   └── Type.kt                  # Typography
    ├── components/
    │   └── DashboardComponents.kt   # Cards, alerts, badges, transaction rows
    └── screens/
        ├── dashboard/               # DashboardScreen & DashboardViewModel
        ├── transactions/            # TransactionsScreen & TransactionsViewModel
        ├── security/                # SecurityScreen & SecurityViewModel
        ├── summary/                 # SummaryScreen & SummaryViewModel
        ├── detail/                  # SmsDetailScreen & SmsDetailViewModel
        └── settings/                # SettingsScreen & SettingsViewModel
```

---

## 🚀 Getting Started

### Prerequisites
1. **Android Studio** (Hedgehog | 2023.1.1 or newer)
2. **Android SDK**: Min SDK 26 (Android 8.0), Target SDK 34 (Android 14)
3. **JDK**: Java 17

### Build & Run
1. Open Android Studio.
2. Select **Open** and choose the `bankiye` project directory (`c:\Users\NIMO\Desktop\bankiye`).
3. Allow Gradle to sync dependencies.
4. Run on an Android Device or Emulator:
   ```bash
   ./gradlew assembleDebug
   ```
5. On first launch, grant the **READ_SMS** and **RECEIVE_SMS** permissions when prompted. The app will automatically scan existing bank SMS and set up real-time protection.

### Running Unit Tests
Execute the parser verification suite:
```bash
./gradlew testDebugUnitTest
```

---

## 🔒 Privacy & Permissions
* **android.permission.READ_SMS**: Scans existing banking messages stored on device.
* **android.permission.RECEIVE_SMS**: Intercepts real-time banking texts to trigger instant security warnings.
* **android.permission.POST_NOTIFICATIONS**: Dispatches critical security alerts and executive summaries.

**No INTERNET permission is requested for transmitting user data.** Financial messages never leave the device.
