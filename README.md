# Koratuwa — Multi-language Farming Guide Android App

Koratuwa ([translate:කොරටුව/கொரட்டுவா]) is a comprehensive Android mobile application for Sri Lankan farmers, designed to deliver market price tracking, multilingual guides, push notifications, and digital tools to boost productivity and access to local agricultural knowledge.

---

## 📱 Features

- Multi-language Support: Sinhala, Tamil, and English, with seamless language switching.
- Live Market Prices: Track crop prices, use the built-in calculator, and view price trends.
- Farming Guides: Download or generate crop guides as PDFs for offline reading.
- Sell & Post: Post crops for sale, manage sale requests, and receive real-time approval notifications.
- Push Notifications: Get notified when posts are approved/rejected or when new advisories are available.
- Location Directory: Quickly find and contact agriculture officers by region.
- Firebase Authentication: Secure login with Google or Email.
- Modern UI: Clean, touch-friendly visual design and responsive layouts.
- Offline Content: Key guides and price data available offline.

---

## 🛠️ Tech Stack

- Languages: Kotlin & Java
- IDE: Android Studio
- Backend: Firebase (Authentication, Firestore, Cloud Messaging, Storage)
- Third-party Libraries: Glide, Material Components, PDF generation packages

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version)
- JDK 11 or later
- Firebase project (with Authentication, Cloud Firestore, Cloud Messaging enabled)

### Installation

1. **Clone This Repository**
    ```
    git clone https://github.com/Iresh-Nimantha/koratuwa-mobile-app.git
    ```

2. **Open in Android Studio**
    - File > Open > select this folder

3. **Connect to Firebase**
    - Add the `google-services.json` file to your `app/` directory
    - Enable Authentication, Firestore, and Messaging in Firebase Console

4. **Build and Run**
    - On an emulator or any Android device

#### Project Structure

    Koratuwa/
    ├── app/
    │ ├── src/
    │ │ ├── main/
    │ │ │ ├── java/com/example/koratuwa/
    │ │ │ │ ├── activities/ # Activity classes
    │ │ │ │ │ ├── MainActivity.kt
    │ │ │ │ │ ├── LoginActivity.kt
    │ │ │ │ │ ├── MarketPricesActivity.kt
    │ │ │ │ │ └── ...
    │ │ │ │ ├── fragments/ # Fragment classes
    │ │ │ │ │ ├── HomeFragment.kt
    │ │ │ │ │ ├── GuidesFragment.kt
    │ │ │ │ │ └── ...
    │ │ │ │ ├── adapters/ # RecyclerView adapters
    │ │ │ │ ├── models/ # Data models
    │ │ │ │ ├── repositories/ # Data repositories
    │ │ │ │ ├── viewmodels/ # ViewModel classes
    │ │ │ │ ├── utils/ # Utility classes
    │ │ │ │ │ ├── LocaleHelper.kt # Language switching
    │ │ │ │ │ ├── NotificationHelper.kt
    │ │ │ │ │ └── ...
    │ │ │ │ └── services/ # Background services
    │ │ │ │ └── FirebaseMessagingService.kt
    │ │ │ ├── res/
    │ │ │ │ ├── layout/ # XML layouts
    │ │ │ │ ├── values/ # Default strings, colors, styles
    │ │ │ │ ├── values-si/ # Sinhala strings
    │ │ │ │ ├── values-ta/ # Tamil strings
    │ │ │ │ ├── drawable/ # Images and icons
    │ │ │ │ ├── mipmap/ # App launcher icons
    │ │ │ │ └── menu/ # Menu resources
    │ │ │ ├── assets/ # PDF guides, fonts
    │ │ │ └── AndroidManifest.xml
    │ │ └── test/ # Unit tests
    │ └── build.gradle # App-level Gradle
    ├── build.gradle # Project-level Gradle
    ├── google-services.json # Firebase config (DO NOT COMMIT)
    ├── gradle.properties
    ├── settings.gradle
    ├── LICENSE
    └── README.md
---

## ✨ Screenshots
![download](https://github.com/user-attachments/assets/409ce6b9-63f6-4004-b454-1ab56e34760c)

<img width="1058" height="596" alt="download" src="https://github.com/user-attachments/assets/ccb288ce-7d73-4026-a063-a9a0d60d560b" />

---

## 🤝 Contributors

- [Pasindu Inguruwaththa](https://www.linkedin.com/in/pasindu-inguruwaththa/) — UI UX Designer
- [Iresh Nimantha](https://www.linkedin.com/in/ireshnimantha/) — Developer

---

## 📄 License

Open-source under the MIT license.  
For details, see the [LICENSE](LICENSE) file.

---

## 📬 Feedback & Support

- email the maintainer directly

---

Koratuwa helps you farm smarter, connecting you to real-time knowledge, prices, and expert resources — in your language.

---

