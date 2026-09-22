# Design Sign-Off: Compass & Level (App A)

- **Product:** Compass & Level (CompassLevel)
- **Product Lead:** Shezrah Abbasi
- **Date:** September 18, 2026
- **Status:** APPROVED
- **Target Platform:** Target SDK 36 (Android 16) | Compile SDK 36 | Min SDK 24 (Android 7.0)
- **Verified Hardware:** Samsung SM-A065F

## Review Summary
1. **Gate 1 (SPEC.md):** Approved. Scope locked strictly to offline sensor utility; zero GPS/camera/map permissions enforced. Platform configurations updated to Target & Compile SDK 36.
2. **Gate 1B (DESIGN.md):** Approved. All 4 UI states (Loading, Content, Empty/No-Magnetometer Fallback, Error/Unreliable Calibration) verified and screens updated.
3. **Monetization Isolation:** Approved. 50dp reserved bottom container running live Google Mobile Ads (AdMob v23.6.0) test banner, strictly decoupled from coordinate bounds.

**Sign-off:** Shezrah Abbasi (Product Lead)

---

## Amendment — UI Overhaul Sign-Off

- **Date:** 2026-09-22
- **Status:** APPROVED
- **Scope:** Competitor-grade UI uplift across all 4 screen states

### Changes approved
| Screen | Change |
|---|---|
| Loading | Radial green glow, branded title, leading dot spinner |
| Live Compass | 72-tick degree ring, glowing north needle, glass spirit bubble, cardinal badge |
| Level-Only | Animated degree color, TAP TO ZERO button, crosshair reticle with ticks |
| Error | Pulsing amber ring animation, figure-8 `∞` instruction card |
| Pitch/Roll pills | Color-coded: green <2°, amber <10°, red beyond |

**APK reviewed:** `app-debug.apk` — 11.24 MB — built commit `c01ae9d`

**Sign-off:** Shezrah Abbasi (Product Lead) — 2026-09-22
