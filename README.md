# DeX Input Method Switcher

An Android app to automatically or manually switch input methods in Samsung DeX mode using Shizuku.

## Features
- **Shizuku Integration:** Manage Shizuku permissions and execute ADB commands without root.
- **DeX Detection:** Automatically detect Samsung DeX mode.
- **IME Management:** List installed IMEs and switch current default IME.
- **Auto Switch:** Automatically switch to a target IME when entering DeX mode.

## Prerequisites
- Android 7.0+ (API 24+)
- [Shizuku](https://shizuku.rikka.app/) app installed and running.
- Wireless Debugging enabled (for Shizuku setup).

## How to Build
1. Open in Android Studio.
2. Build and install on your Samsung device.

## Usage
1. Open the app.
2. Grant Shizuku permission when prompted.
3. Verify "Shizuku: Authorized" status.
4. Select an input method from the list to switch immediately.
5. Go to Settings to enable "Auto Switch in DeX" and select your target keyboard.
6. Connect to DeX and verify automatic switching.
