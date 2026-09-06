# SOTI MobiControl Project & Session Documentation

**Session / Conversation ID**: `97a780b9-2636-4365-8f48-ec344f84652b`  
**Date**: September 6, 2026  
**Repository**: [trulypriyanshu/soti-device-configuration](https://github.com/trulypriyanshu/soti-device-configuration)  
**Live Application**: [https://soti-mdm.vercel.app/](https://soti-mdm.vercel.app/)  
**Latest Signed APK Build**: [GitHub Actions Run #6](https://github.com/trulypriyanshu/soti-device-configuration/actions/runs/34001704634)

---

## 1. Project Overview & Evolution

This project is a high-fidelity replica of the enterprise **SOTI MobiControl** Android management agent, supporting both a Progressive Web App (PWA) on Vercel and a standalone Android APK equipped with a native WebView Bridge.

### Key Milestones & Features Implemented:
1. **Pixel-Perfect SOTI MobiControl UI**:
   - Device configuration dashboard with Device Information, Management Status, Agent Information, Network Settings, and Troubleshooting sections.
   - Side drawer navigation with SOTI icon, Device ID, Submodel, App Catalog, Message Center, Support, and Privacy.
   - White / Light Theme matching authentic OEM enterprise screenshots.
   - Profiles screen displaying installed MDM policies (*Feature Restrictions - Global*).

2. **Strict Device ID & Name Formatting Rules**:
   - Device ID format: `C295-FAL_<empId>_<First>_<Last>_<Serial>` (Hyphen is allowed **only** inside `C295-FAL`; all other separators must be underscores `_`).
   - Device Brand: All uppercase (e.g. `GOOGLE`, `SAMSUNG`).
   - First and Last Name: Capitalized first letter, remaining lowercase (e.g. `Jay_Parmar`).
   - Hardware Serial Number: All uppercase (e.g. `62061XEBF2Q0AE`).

3. **Native Android Bridge (`com.soti.mdm.AndroidBridge`)**:
   - Bridges hardware state from Android into the JavaScript client via `@JavascriptInterface` attached to `window.Android`.
   - **Hardware Serial Number Detection**:
     - Tiers: `Build.getSerial()` (Android 8+) $\rightarrow$ `SystemProperties` reflection (`ro.serialno`, `ro.boot.serialno`, `gsm.sn1`, `ril.serialnumber`, `sys.serialnumber`) $\rightarrow$ `Build.SERIAL` $\rightarrow$ `Settings.Secure.ANDROID_ID`.
   - **Cellular Carrier**:
     - Detects live SIM carrier via `TelephonyManager.getNetworkOperatorName()` (e.g. `JIO`, `AIRTEL`, `VI`).
   - **Wi-Fi ON / OFF State Detection**:
     - Evaluates `WifiManager.isWifiEnabled()` and `ConnectivityManager.getNetworkCapabilities()` for active `TRANSPORT_WIFI`.
   - **Live Network Callback**:
     - `MainActivity.java` registers a `ConnectivityManager.NetworkCallback` that dispatches immediate updates to the web app (`autoUpdateNetworkDetails()`) whenever Wi-Fi is toggled on or off in device settings.

4. **Wi-Fi MAC Address & Hardware Privacy Handling**:
   - **Replacement of MAC Address with IP Address**:
     - An editable **Wi-Fi IP Address** field is present in the "Edit Device Information" modal with persistent local storage (`mdm_wifi_ip`) and real-time live preview.
     - **When Wi-Fi is ON**: The Wi-Fi row shows the configured/live IP address (default `10.32.165.233`).
     - **When Wi-Fi is OFF**: The Wi-Fi row automatically shows `"Wi-Fi is turned off"`.
     - Tapping the Wi-Fi row directly opens the modal focused on the Wi-Fi IP input.

5. **CI/CD Build Automation**:
   - GitHub Actions workflow (`.github/workflows/build-apk.yml`) automatically compiles and signs the release APK upon every push to `main`.
   - Generates release artifacts containing:
     - `SOTI-MobiControl-Release-Signed.apk`
     - `SOTI-MobiControl-Debug.apk`

---

## 2. Key File Directory

| File Path | Description |
|---|---|
| `index.html` | Core web application, UI design, modals, network detection logic, and service worker registration. |
| `device_configuration_pwa.html` | Exact synchronization of `index.html` for standalone PWA reference. |
| `sw.js` | Service Worker script handling network caching (`soti-config-v19`). |
| `manifest.json` | Web App Manifest for PWA installation (icons, theme colors, display standalone). |
| `android/app/src/main/AndroidManifest.xml` | Android application manifest with permissions (`ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, `READ_PHONE_STATE`, `INTERNET`). |
| `android/app/src/main/java/com/soti/mdm/MainActivity.java` | Native WebView host, status bar styling, and live `ConnectivityManager.NetworkCallback`. |
| `android/app/src/main/java/com/soti/mdm/AndroidBridge.java` | Native JavaScript bridge exposing hardware serial, carrier, and Wi-Fi state to `window.Android`. |
| `.github/workflows/build-apk.yml` | GitHub Actions workflow automating Gradle build and signed APK generation. |

---

## 3. Local Conversation Transcript Locations

All full conversation transcripts and execution step logs are permanently saved locally:
- **Full Trajectory Transcript**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\.system_generated\logs\transcript_full.jsonl`
- **Compact Step Transcript**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\.system_generated\logs\transcript.jsonl`
- **Implementation & Walkthrough Artifacts**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\walkthrough.md`  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\implementation_plan.md`
