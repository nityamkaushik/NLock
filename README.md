# NLock - Premium App Locker

NLock is a high-performance, secure, and elegantly designed Android App Locker. It ensures your privacy by allowing you to lock individual applications with a PIN or Biometric Authentication (Fingerprint). Built with speed, security, and a premium user experience in mind, NLock is the ultimate tool to protect your sensitive data on your device.

## 🚀 Key Features

- **Robust App Locking:** Secure any installed application (user or system apps) with a highly responsive PIN or Biometric prompt.
- **Biometric Integration:** Seamless and fast fingerprint authentication for quick access to your locked apps.
- **Advanced Settings Management:**
  - **System App Filtering:** Easily toggle the visibility and locking of system-level applications within the app list.
  - **Custom Re-lock Intervals:** Configure a custom grace period (in seconds or minutes) before an unlocked app is locked again, preventing annoying repeated prompts while multitasking.
- **Optimized Performance:** Engineered for smooth UI scrolling, rapid authentication flows, and snappy tactile/visual feedback during PIN entry.
- **Cloud Integration:** Integrated with Firebase Firestore for secure configuration and remote state management.
- **Premium UI/UX:** Clean, modern aesthetics with a minimalist, high-contrast design focusing on usability.

## 🛠️ Tech Stack & Architecture

- **Platform:** Android (Kotlin)
- **Database / Backend:** Firebase Firestore
- **Authentication:** Android Biometric API for fingerprint/face unlock
- **Architecture:** Follows modern Android development standards for responsiveness and maintainability.

## 🎨 Design & Aesthetics

The application features a premium, ultra-minimalist black-and-white design. The official logo is a flawless geometric fusion of the standard Android robot head and a modern security key—representing both the platform and the app's core mission: uncompromising security.

## ⚙️ Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/NLock.git
   ```
2. **Open in Android Studio:**
   - Launch Android Studio.
   - Select `Open an Existing Project` and navigate to the cloned `NLock` directory.
3. **Firebase Setup:**
   - Connect the app to your Firebase project.
   - Ensure your `google-services.json` file is placed inside the `app/` directory.
   - Configure Firestore security rules appropriately.
4. **Build and Run:**
   - Sync the Gradle project.
   - Run the application on a physical device or emulator.

## 🛡️ Permissions Required

To function correctly, NLock requires the following special Android permissions:
- **Usage Access:** To monitor when a protected application is launched.
- **Display Over Other Apps:** To seamlessly draw the lock screen over the protected app instantly.
- **Biometrics / Fingerprint:** To verify the user's identity securely.

## 📜 License

This project is licensed under the MIT License.
