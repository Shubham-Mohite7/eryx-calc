# Privacy Policy for eryx

**Last Updated:** November 25, 2026

**eryx** (`com.liquidglass.calculator`) is a free, open-source, ad-free, and offline-first Android calculator application. Your privacy is fundamental to the design of the app: **eryx collects, stores, sells, or shares zero personal data.**

---

## 1. Data Collection & Telemetry

- **No Analytics or Tracking:** eryx contains no analytics SDKs, crash telemetry reporters, advertising networks, or tracking pixels.
- **No Accounts Required:** There is no user registration, sign-in, or cloud synchronization.
- **No Personal Information:** We do not collect names, email addresses, device identifiers (such as Advertising ID or IMEI), location data, or usage metrics.

---

## 2. Local On-Device Storage

All app data stays strictly on your device inside the private Android app sandbox:

- **Calculation History:** Stored locally on your device (SQLite) so you can revisit or clear past calculations at any time.
- **Preferences & Settings:** Theme mode, number formatting, haptic feedback, and cached currency conversion rates are stored locally via Android DataStore / SharedPreferences.
- **Downloaded Updates:** When you choose to download an app update from within **About & Updates**, the installer APK is temporarily saved to the app's private cache directory before launching Android's system package installer.

You can clear calculation history inside the app at any time, or delete all local app data by uninstalling eryx or clearing storage in Android Settings.

---

## 3. Network Requests (User-Initiated Only)

eryx works completely offline for calculations, scientific functions, history, and unit conversions. Network access is used **only** for the following optional features:

1. **Live Currency Exchange Rates**
   - When you open the Currency Converter or request a rate refresh, eryx fetches public ECB exchange rate data from the Frankfurter API (`https://api.frankfurter.app`).
   - No personal or device-identifying information is sent beyond standard HTTP request headers required to retrieve the exchange rate response.

2. **In-App Update Checks**
   - When you check for updates (or view update availability in **About & Updates**), eryx fetches version metadata (`update.json`) and/or release information from the official GitHub repository (`https://github.com/Shubham-Mohite7/eryx-calc`) and downloads the APK from GitHub when you tap to install an update.
   - Please refer to [GitHub's Privacy Statement](https://docs.github.com/en/site-privacy/privacy-policies/github-general-privacy-statement) for how GitHub handles standard web/API requests.

---

## 4. Android Permissions Used

eryx requests only the minimal permissions required for its features:

| Permission | Purpose |
| --- | --- |
| `INTERNET` | Fetch live currency exchange rates and check/download user-requested app updates. |
| `ACCESS_NETWORK_STATE` | Check whether the device has an active network connection before attempting currency or update requests. |
| `VIBRATE` | Provide optional tactile haptic feedback on keypad presses (can be disabled in Settings). |
| `REQUEST_INSTALL_PACKAGES` | Allow Android's system installer to install a newer eryx APK downloaded via the built-in updater. |

---

## 5. Children's Privacy

Because eryx does not collect any personal information from any user, it does not knowingly or unknowingly collect data from children under the age of 13 (or the applicable age in your jurisdiction).

---

## 6. Changes to This Policy

If future updates introduce any change to how network requests or local storage work, this `privacy.md` document will be updated alongside the corresponding release in the repository.

---

## 7. Open Source & Contact

eryx is open source. You can inspect the complete source code, report issues, or ask privacy-related questions in the official repository:

- **Repository:** https://github.com/Shubham-Mohite7/eryx-calc
- **Issue Tracker:** https://github.com/Shubham-Mohite7/eryx-calc/issues
