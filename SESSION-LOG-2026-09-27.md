---
title: "CompassLevel — 2026-09-27 Session Log: Pure AdMob Standardization, Video-Matched Splash Redesign & 20s Interstitial Engine"
app: com.aivigil.compasslevel
date: 2026-09-27
revised: 2026-09-28
tip: f793533
lead: Shezrah Abbasi
developer: Rizwan
status: "All simulated mock ad copy purged; 100% real AdMob SDK only; video-matched splash screen deployed; interstitials wired to all tool/theme/settings actions with 20s cooldown; release APK at 4.39 MB."
type: session log
---

# Where This Stopped

| Area | State |
|---|---|
| AdMob Architecture | 100% genuine Google Mobile Ads SDK; all mock creatives (`BannerAdView`, `InterstitialAdDialog`) deleted [certain] |
| Splash Screen Design | Branded splash screen modeled after production reference video (`VR Player` app): elevated logo card, tagline, progress bar, bottom banner [certain] |
| Post-Splash Interstitial | Guaranteed post-splash ad presentation with in-flight timeout buffer (up to 2.5s) [certain] |
| Interaction Interstitials | Triggered on all tool tab switches, theme toggles, skin selections, and settings opens (20s cooldown) [certain] |
| Release APK | `CompassLevel-v1.0.apk` (**4.39 MB** release build), verified, and placed on Desktop (`C:\Users\RIZWANPC\Desktop\CompassLevel-v1.0.apk`) [certain] |
| Git Remotes | Synchronized to `origin/main` and `personal/main` at commit `f793533` [certain] |

---

# What Was Accomplished

1. **Purged All Mock & Simulated Ad Implementations**:
   - Completely deleted `BannerAdView.kt` and `InterstitialAdDialog.kt`.
   - Removed all `AdCreative` data classes and mock campaign dictionaries from `AdManager.kt`.
   - The app now requests and renders 100% genuine Google AdMob views via the Google Mobile Ads SDK.

2. **Video-Matched Splash Screen Redesign (`ScreenOpeningAnimatedView.kt`)**:
   - Inspected user's reference video (`VR Player` app launch sequence).
   - Redesigned the launch sequence to match standard Android utility production apps:
     - Deep space dark background with subtle ambient radial backlight.
     - Centered elevated logo card (`110dp` squircle with glowing cyan/neon border, dynamic rotating compass needle).
     - Bold title: **`Compass Level`** (28sp bold white text).
     - Subtitle: **"Welcome to Compass Level – Precision Dual-Axis Level & 3D Magnetic Compass"** (clean 13.5sp white text, 72% opacity).
     - Modern slim horizontal progress bar with live percentage and status text (0% -> 100% over 2.9s).
     - Single anchored adaptive AdMob banner at the bottom edge.

3. **Guaranteed Post-Splash Interstitial Engine (`AdManager.kt`)**:
   - Preloads interstitial ad during the 3.2s splash sequence.
   - If the network takes an extra moment on cold start, buffers the transition for up to 2.5s to display the full-screen Google AdMob Interstitial ad before entering the app, matching the exact behavior seen in the reference video.

4. **Multi-Action Monetization Trigger Pipeline (`MainActivity.kt`)**:
   - Wired `maybeShowInterstitial()` to:
     - Tool navigation switches (Compass ⇄ Level ⇄ Clinometer ⇄ Location)
     - Introductory tool launch buttons
     - Theme & Skin modal and skin selection
     - Dark / Light Mode toggles
     - Settings, Notes, and Calibration buttons
   - Enforced a 20-second cooldown period to prevent rapid repeated ad impressions while ensuring consistent monetization opportunities.

5. **Release Build Optimization**:
   - Release compilation executed via `./gradlew assembleRelease --no-daemon`.
   - Resulting production APK measures **4.39 MB** (well below the 20 MB program limit).

---

# What I Got Wrong

1. **Left Orphaned References to Deleted Mock Dialog in Root BackHandler**:
   - *Error*: After deleting `InterstitialAdDialog.kt` and its state in `AdManager.kt`, left references to `adManager.isInterstitialVisible` and `adManager.dismissInterstitial()` inside `MainActivity.kt`'s `BackHandler`.
   - *Result*: Release build failed with `Unresolved reference 'isInterstitialVisible'`.
   - *Fix*: Removed the orphaned references. Google AdMob full-screen interstitials handle system back press natively within their own platform activity window.

2. **Initially Squeezed the Splash Screen with Unnatural Dual Banners**:
   - *Error*: Initially placed an adaptive banner at the very top AND at the bottom of the splash screen, which squeezed the central compass dial and looked cluttered and non-standard.
   - *Correction*: Inspected the user's reference video and observed that commercial production apps maintain clean top/center branding and place a single banner at the bottom edge. Redesigned to a single bottom banner, creating a balanced, high-end presentation.

3. **Did Not Account for Network In-Flight Delays on First Interstitial Presentation**:
   - *Error*: Initial `showPostSplashInterstitial` simply checked `if (interstitialAd != null)` and immediately bailed to `onProceed()` if null. On slow cold starts where AdMob took 2.8s to load, the splash would finish at 3.0s and skip the ad entirely.
   - *Fix*: Implemented an in-flight wait buffer in `AdManager.kt` that waits up to 2.5s if the ad is currently loading, guaranteeing the interstitial fires as seen in the video.

---

# Blockers

| Blocker | Owner | Raised | Due | Status |
|---|---|---|---|---|
| AdMob Account Production App ID & Unit IDs | Product Lead (Shezrah Abbasi) | 2026-09-23 | Gate 5 (Day 5) | Active blocker; test IDs currently active |
| Play Console production track setup | Product Lead (Shezrah Abbasi) | 2026-09-25 | Gate 6 (Day 6) | Pending console access |

---

# Next, in order

1. **Execute full documentation remediation pass (Gate 5)** addressing all findings in `correction-2026-09-28-the-review-marked-the-wrong-artefacts.md`.
2. **Reconcile SPEC.md tables and ARCHITECTURE.md filter discrepancies**.
3. **Capture before/after framerate telemetry** and package final audit submission.
