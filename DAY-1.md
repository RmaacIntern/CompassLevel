# Day 1: Decide, Design & Architecture

**Date:** 2026-09-18 / 2026-09-22  
**App:** `com.aivigil.compasslevel`  
**Platform:** Target SDK 36, Compile SDK 36, Min SDK 24  
**Hardware Verified:** Vivo Y27s (`V2322`) & Samsung SM-A065F [certain — `APPROVAL.md`]  

---

## 1. Gate 1 — Feature Specification (`SPEC.md`)
- Author `SPEC.md` strictly aligned with App A Brief (`Two App Briefs — Compass & Level`).
- Core feature set: Unified 60fps compass dial, live heading in degrees, concentric fluid spirit level, and Settings screen.
- Explicit non-features: Zero runtime permissions, zero GPS, zero network requests, zero background services, no degree accuracy claims.
- Monetization boundary: 50dp reserved bottom container, no interstitial ads.

---

## 2. Gate 1B — Multi-State UI & Design (`DESIGN.md`)
All five mandatory UI states implemented and verified inside Jetpack Compose:
1. **Loading State:** Radial glow, branded title, and leading dot spinner.
2. **Content State (Compass):** 360° precision tick track, laser-red North index, fluid glass bullseye bubble, tactile level snap ($\le 0.5^\circ$), decimal pitch/roll readouts.
3. **Content State (Spirit Level):** Dual-axis reticle level, tare zero calibration button, percentage grade / degree display.
4. **Fallback / Empty State:** Graceful transition when sensor motion is idle or rotation vector hardware is absent.
5. **Error / Calibration State:** Pulsing amber ring warning with figure-8 motion recalibration guide.
6. **Settings Screen:** True/Magnetic North toggle with manual declination, angle units toggle (Degrees vs. % Grade), and About dialog.

**Sign-off:** Shezrah Abbasi (Product Lead) formally signed off on Gate 1 & Gate 1B (recorded in `APPROVAL.md`).

---

## 3. Gate 2 — Architecture & Repository (`ARCHITECTURE.md`)
- Single Activity (`MainActivity.kt`), Compose Material3 UI.
- Sensor pipeline (`CompassSensorManager.kt`) utilizing `TYPE_ROTATION_VECTOR` and fallback `TYPE_ACCELEROMETER`.
- High-performance GPU rendering via `Modifier.graphicsLayer { rotationZ = -heading }` lambda.
- "Heading Hold" buffer: Holds last known reliable heading when sensor accuracy drops to `UNRELIABLE`.
- Shortest-angular-delta wrapping ($((\Delta + 540) \pmod{360}) - 180$) to eliminate 359° ↔ 0° snap spin.
- Repository hygiene: IDE blobs, machine-specific files, and test bloat purged.
