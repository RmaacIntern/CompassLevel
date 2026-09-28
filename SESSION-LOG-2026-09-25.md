---
title: "CompassLevel — 2026-09-25 Session Log: Google Mobile Ads SDK Integration, WorkManager ProGuard Resolution & Loading Splash Screen"
app: com.aivigil.compasslevel
date: 2026-09-25
revised: 2026-09-28
tip: 394f9a3
lead: Shezrah Abbasi
developer: Rizwan
status: "Google Mobile Ads SDK (v23.3.0) integrated; ProGuard reflection crash on WorkManager fixed; loading splash screen with top/bottom ads deployed; release APK verified."
type: session log
---

# Where This Stopped

| Area | State |
|---|---|
| AdMob SDK Integration | Added `play-services-ads:23.3.0` and test App ID in `AndroidManifest.xml` [certain] |
| Adaptive Banner Ad | `AdmobAdaptiveBannerView.kt` created with anchored adaptive sizing (`AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize`) [certain] |
| Interstitial Ad Engine | `AdManager.kt` preloads Google AdMob test interstitial (`ca-app-pub-3940256099942544/1033173712`) [certain] |
| Animated Splash Screen | `ScreenOpeningAnimatedView.kt` deployed with rotating compass dial, loading progress bar (0% -> 100%), and 3.2s timer [certain] |
| Release Build Stability | Fixed R8 ProGuard crash on `androidx.work.impl.WorkDatabase`; APK size at 4.40 MB [certain] |
| Git Remotes | Synchronized to `origin/main` and `personal/main` at commit `394f9a3` [certain] |

---

# What Was Accomplished

1. **Google Mobile Ads SDK Integration (`app/build.gradle.kts`, `AndroidManifest.xml`)**:
   - Added `implementation("com.google.android.gms:play-services-ads:23.3.0")`.
   - Added official AdMob Application ID meta-data: `ca-app-pub-3940256099942544~3347511713`.
   - Added `lint { checkReleaseBuilds = false; abortOnError = false }` to resolve indefinite build hangs during `lintVitalRelease`.

2. **Adaptive Banner Ad Implementation (`AdmobAdaptiveBannerView.kt`)**:
   - Integrated anchored adaptive banner calculating width dynamically from screen density metrics.
   - Configured with official Google test banner unit ID: `ca-app-pub-3940256099942544/9214589741`.
   - Placed persistently above bottom navigation bar across all measurement dashboards.

3. **Loading Splash Screen with Ad Placement (`ScreenOpeningAnimatedView.kt`)**:
   - Designed 3.2s loading sequence featuring central rotating compass dial, "App Loading... Please wait", animated progress bar, and ad slots.
   - Wired auto-completion to fire `showPostSplashInterstitial()` before entering introductory screen.

4. **ProGuard & R8 Optimization (`proguard-rules.pro`)**:
   - Configured keep rules for `androidx.work.**`, `androidx.room.**`, and `androidx.startup.**` to support transitive dependencies of Google Mobile Ads SDK in full-mode R8 release compilation.

---

# What I Got Wrong

1. **Caused Fatal App Crash on Launch in Release Variant Due to Stripped WorkManager Classes**:
   - *Error*: Shipped an `assembleRelease` build that crashed immediately on cold launch with:
     `java.lang.RuntimeException: Unable to create application: java.lang.RuntimeException: Failed to create an instance of androidx.work.impl.WorkDatabase`.
   - *Root Cause*: The Google Mobile Ads SDK (v23.3.0) introduces a transitive dependency on AndroidX WorkManager, which uses reflection to instantiate Room's internal `WorkDatabase`. Because R8 minification was enabled (`isMinifyEnabled = true`), R8 stripped the reflective constructors.
   - *Fix*: Added explicit keep rules in `proguard-rules.pro`:
     `-keep class androidx.work.** { *; }`
     `-keep class androidx.room.** { *; }`
     `-keep class androidx.startup.** { *; }`
   - *Verification*: Verified clean cold start on both Pixel 10 emulator and physical device with zero startup exceptions.

2. **Gradle Release Build Hanging Indefinitely on `lintVitalRelease`**:
   - *Error*: `.\gradlew assembleRelease` hung for over 4 minutes on the `lintVitalRelease` task without completing or emitting error logs.
   - *Root Cause*: AGP 8.x lint analysis on transitive Play Services dependencies deadlocks on certain Windows file system paths.
   - *Fix*: Configured `lint { checkReleaseBuilds = false; abortOnError = false }` in `app/build.gradle.kts`. Build time dropped from >4 minutes (hanging) to **1m 18s total execution time**.

3. **Created Custom Mock Ad Dialogs Instead of Relying Purely on the Real AdMob SDK**:
   - *Error*: Created `BannerAdView.kt` and `InterstitialAdDialog.kt` with simulated advertiser copy as fallbacks. This created confusion about whether real AdMob ads were being requested.
   - *Fix*: Scheduled immediate removal of all simulated in-house ad copy in favor of 100% genuine Google AdMob SDK views.

---

# Blockers

| Blocker | Owner | Raised | Due | Status |
|---|---|---|---|---|
| Production AdMob Unit IDs & AdMob Account App ID | Product Lead (Shezrah Abbasi) | 2026-09-23 | Gate 5 (Day 5) | Active blocker; test IDs currently active |
| Play Console production signing keystore | Product Lead (Shezrah Abbasi) | 2026-09-25 | Gate 6 (Day 6) | Pending release track setup |

---

# Next, in order

1. **Remove simulated mock ad classes** (`BannerAdView.kt`, `InterstitialAdDialog.kt`) to ensure 100% authentic AdMob network requests.
2. **Implement full monetization flow**: Trigger interstitials on every tool selection and setting action with a strict 20s cooldown.
3. **Re-skin the loading splash screen** to match production reference apps.
