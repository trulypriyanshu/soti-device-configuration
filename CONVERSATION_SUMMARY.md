# SOTI MobiControl Project & Session Documentation

**Session / Conversation ID**: `97a780b9-2636-4365-8f48-ec344f84652b`  
**Date**: September 6, 2026  
**Repository**: [trulypriyanshu/soti-device-configuration](https://github.com/trulypriyanshu/soti-device-configuration)  
**Live Application**: [https://soti-mdm.vercel.app/](https://soti-mdm.vercel.app/)  
**Latest Signed APK Build**: [GitHub Actions Run #14](https://github.com/trulypriyanshu/soti-device-configuration/actions/runs/34037310247)

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
     - **Form Trigger**: Clicking on the Wi-Fi row or Device Information section does **not** open the form. The form opens **only** when tapping on the **User** section (`Agent Mode: User`) with **3 fingers 5 times**.

5. **Persistent SOTI MobiControl Notification**:
   - Matches official OEM notification shade layout (`IMG-20260906-WA0011.jpg`):
     - **Title**: `SOTI MobiControl`
     - **Text**: `SOTI MobiControl is running`
     - **Channel**: `soti_mobicontrol_persistent` with `IMPORTANCE_LOW` (silent, categorized under "Silent" notifications without sound/vibration).
     - **Icons**: SOTI vector logo small icon + high-resolution round SOTI logo large icon on the right.
     - **Ongoing**: Non-dismissible / persistent (`setOngoing(true)`).
     - **Trigger**: App automatically checks `POST_NOTIFICATIONS` permission and triggers the persistent notification immediately on launch and resume.

6. **Full SOTI Loading Page (Blue Background, Logo, & Loading Bar)**:
   - Replaced default Android splash screen with a branded `#0099DB` full loading screen:
     - **Background**: SOTI Corporate Blue (`#0099DB`).
     - **Logo**: White squircle badge container with centered SOTI logo.
     - **Loading Bar**: Horizontal white indeterminate progress bar centered beneath the logo.
   - Configured across both Android native launch (`values-v31/styles.xml`, `activity_main.xml`, `MainActivity.java`) and Web/PWA (`index.html`, `device_configuration_pwa.html`, `sw.js` cache `soti-config-v21`).

7. **CI/CD Build Automation**:
   - GitHub Actions workflow (`.github/workflows/build-apk.yml`) automatically compiles and signs the release APK upon every push to `main`.
   - Generates release artifacts containing:
     - `SOTI-MobiControl-Release-Signed.apk`
     - `SOTI-MobiControl-Debug.apk`

---

## 2. Key File Directory

| File Path | Description |
|---|---|
| `index.html` | Core web application, UI design, modals, network detection logic, full SOTI blue loading page, and service worker registration. |
| `device_configuration_pwa.html` | Exact synchronization of `index.html` for standalone PWA reference. |
| `sw.js` | Service Worker script handling network caching (`soti-config-v21`). |
| `manifest.json` | Web App Manifest for PWA installation (icons, theme colors, display standalone). |
| `android/app/src/main/AndroidManifest.xml` | Android application manifest with permissions (`ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, `READ_PHONE_STATE`, `POST_NOTIFICATIONS`, `INTERNET`). |
| `android/app/src/main/res/values-v31/styles.xml` | Android 12+ splash screen styling setting `#0099DB` background and animated icon. |
| `android/app/src/main/res/layout/activity_main.xml` | Android activity layout with full-screen `#0099DB` loading page, centered logo badge, and horizontal progress bar. |
| `android/app/src/main/java/com/soti/mdm/MainActivity.java` | Native WebView host, persistent notification, dynamic status bar coloring, and live network callback. |
| `android/app/src/main/java/com/soti/mdm/AndroidBridge.java` | Native JavaScript bridge exposing hardware serial, carrier, and Wi-Fi state to `window.Android`. |
| `.github/workflows/build-apk.yml` | GitHub Actions workflow automating Gradle build and signed APK generation. |

---

## 3. Chronological Conversation & Request Log

1. **Serial Number & Wi-Fi MAC Address Dynamic Detection**:
   - *User Issue*: Serial number not detected, Wi-Fi MAC address not detected, Wi-Fi not detecting ON/OFF condition, showing static MAC address.
   - *Resolution*: Created `AndroidBridge.java` to fetch actual device hardware serial and Wi-Fi status via `WifiManager` and `ConnectivityManager`.
2. **MAC Field in Form & Wi-Fi ON/OFF Condition**:
   - *User Request*: Since Android prevents apps from reading real hardware MAC directly due to privacy restrictions, detect only ON/OFF condition. Put MAC field in form. Show MAC address when Wi-Fi is ON, and show "Wi-Fi is turned off" when OFF.
   - *Resolution*: Added editable field with local storage caching and connected to live Wi-Fi callback.
3. **Save Conversation**:
   - *User Request*: Save this conversation.
   - *Resolution*: Established `CONVERSATION_SUMMARY.md` tracking all design rules, bridge implementations, and local transcript paths.
4. **Replace MAC Address with IP Address**:
   - *User Request*: Replace MAC address with IP address.
   - *Resolution*: Changed label and storage to Wi-Fi IP Address with default `10.32.165.233`.
5. **Reinstallation Question**:
   - *User Question*: Do I need to reinstall the app now?
   - *Resolution*: Clarified that web UI and cache updates take effect automatically via Service Worker, while Android-level changes require downloading the latest APK.
6. **Fetch IP Address from Device**:
   - *User Request*: Can't you make that IP address fetch from device?
   - *Resolution*: Analyzed device network interface enumeration options across Wi-Fi interfaces.
7. **Hardcode IP Address to 10.32... when Wi-Fi is turned ON**:
   - *User Request*: Remove auto-fetch IP functionality and hardcode the `10.32....` when Wi-Fi is turned ON.
   - *Resolution*: Displays configured IP address (default `10.32.165.233`) whenever Wi-Fi is enabled and connected.
8. **Gesture Trigger for Form (3 Fingers 5 Times on User Section)**:
   - *User Request*: Do not open form on clicking on Wi-Fi or device section. Open form only when clicked on User section with 3 fingers 5 times.
   - *Resolution*: Removed click handlers on Wi-Fi and device sections. Added multi-touch gesture listener detecting 3-touch points tapped 5 times within a reset window on `#user-section`.
9. **Persistent SOTI MobiControl Notification**:
   - *User Request*: When the app opens, add a persistent notification like in image `IMG-20260906-WA0011.jpg`.
   - *Resolution*: Created low-importance persistent notification on launch/resume with SOTI icon, ongoing status, and runtime notification permission request for Android 13+.
10. **Full SOTI Loading Page**:
   - *User Request*: Replace black screen from `Screenshot_20260906-050829_SOTI MobiControl` with a full loading page: blue background, logo, and loading bar at center.
   - *Resolution*: Configured `values-v31/styles.xml`, `activity_main.xml`, `MainActivity.java`, `index.html`, and `device_configuration_pwa.html` with `#0099DB` background, centered logo squircle badge, and centered white horizontal loading bar. Bumped service worker to `soti-config-v21`. Verified in browser and built release APK via GitHub Actions Run #14.

---

## 4. Local Conversation Transcript Locations

All full conversation transcripts and execution step logs are permanently saved locally:
- **Full Trajectory Transcript**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\.system_generated\logs\transcript_full.jsonl`
- **Compact Step Transcript**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\.system_generated\logs\transcript.jsonl`
- **Implementation & Walkthrough Artifacts**:  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\walkthrough.md`  
  `C:\Users\ASUS\.gemini\antigravity-ide\brain\97a780b9-2636-4365-8f48-ec344f84652b\implementation_plan.md`

