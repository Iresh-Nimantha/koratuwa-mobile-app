# Firebase Setup Guide for Koratuwa App

## 🔑 SHA-1/SHA-256 Keys Configuration

### Current Debug Keys (Add these to Firebase Console):

**SHA-1:**
```
FC:F0:36:E4:2B:75:D9:C5:69:B1:CB:52:F2:42:04:C5:75:FB:AB:89
```

**SHA-256:**
```
85:BA:6C:3C:20:F3:32:DB:04:12:20:AD:CB:2B:0D:C9:D7:E1:6D:CC:2A:93:17:80:D4:89:7F:27:50:84:D9:F0
```

### Steps to Add Keys to Firebase Console:

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Select Project**: `koratuwa-c2c6a4c3`
3. **Project Settings**: Click gear icon → Project settings
4. **Select Android App**: `com.example.koratuwa`
5. **Add SHA Certificate Fingerprints**:
   - Click "Add fingerprint"
   - Paste SHA-1: `FC:F0:36:E4:2B:75:D9:C5:69:B1:CB:52:F2:42:04:C5:75:FB:AB:89`
   - Click "Add fingerprint" again
   - Paste SHA-256: `85:BA:6C:3C:20:F3:32:DB:04:12:20:AD:CB:2B:0D:C9:D7:E1:6D:CC:2A:93:17:80:D4:89:7F:27:50:84:D9:F0`
6. **Download Updated google-services.json** (if prompted)
7. **Replace** the current `app/google-services.json` file

## 🏗️ Release Keystore Setup (For Production)

### Generate Release Keystore:
```bash
# Run the provided script
./generate_release_keystore.bat

# Or manually:
keytool -genkey -v -keystore keystore/koratuwa-release-key.keystore -alias koratuwa-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

### Get Release Keystore SHA Fingerprints:
```bash
keytool -list -v -keystore keystore/koratuwa-release-key.keystore -alias koratuwa-key-alias
```

### Update build.gradle.kts for Release:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("keystore/koratuwa-release-key.keystore")
        storePassword = "your_store_password"
        keyAlias = "koratuwa-key-alias"
        keyPassword = "your_key_password"
    }
}

buildTypes {
    release {
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

## 🔧 Current Configuration Status:

- ✅ **Debug Keys**: Generated and ready to add to Firebase
- ✅ **Firebase Project**: `koratuwa-c2c6a4c3` configured
- ✅ **Package Name**: `com.example.koratuwa` matches
- ⚠️ **Release Keystore**: Not configured yet (use script when ready)
- ⚠️ **Firebase Console**: SHA keys need to be added manually

## 🚨 Important Notes:

1. **Keep Keystore Safe**: The release keystore is crucial for app updates
2. **Backup**: Always backup your keystore files
3. **Firebase Console**: Must add SHA keys for authentication to work
4. **Testing**: Test Firebase Auth after adding keys to console

## 📱 Testing Firebase Authentication:

After adding SHA keys to Firebase Console:
1. Build and run the app
2. Try the registration flow
3. Check Firebase Console → Authentication → Users
4. Verify user data is saved to Firestore

## 🆘 Troubleshooting:

- **Authentication fails**: Check if SHA keys are added to Firebase Console
- **Build errors**: Ensure google-services.json is updated
- **Release build issues**: Configure release keystore properly
