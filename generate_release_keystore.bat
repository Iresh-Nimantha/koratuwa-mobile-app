@echo off
echo Generating Release Keystore for Koratuwa App
echo ============================================
echo.

REM Create keystore directory if it doesn't exist
if not exist "keystore" mkdir keystore

echo Generating release keystore...
keytool -genkey -v -keystore keystore/koratuwa-release-key.keystore -alias koratuwa-key-alias -keyalg RSA -keysize 2048 -validity 10000

echo.
echo ============================================
echo Release keystore generated successfully!
echo Location: keystore/koratuwa-release-key.keystore
echo.
echo Next steps:
echo 1. Keep this keystore file safe - you'll need it for all future releases
echo 2. Update app/build.gradle.kts to use this keystore for release builds
echo 3. Generate SHA-1/SHA-256 for this keystore and add to Firebase Console
echo.
echo To get SHA fingerprints for this keystore, run:
echo keytool -list -v -keystore keystore/koratuwa-release-key.keystore -alias koratuwa-key-alias
echo.
pause
