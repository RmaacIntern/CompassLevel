# Day 1 — Decide and design

**Date:** Friday, September 18, 2026  
**Check-in Time:** 12:09 PM PKT  
**Branch:** `day-1-spec-design`  
**Product Lead / Sign-off:** Shezrah Abbasi  

---

## Checklist & Gates

- [x] **Gate 1 · Write SPEC.md**  
  Documented exact scope, architecture, core features (Compass dial, dual-disc reticle level, tare calibration, monospace metric readouts), non-goals, and zero-permission compliance.
- [x] **Gate 1B · Draw every screen and all four states of each: loading, empty, error, content**  
  Implemented all 4 states in Jetpack Compose, validated directly on physical hardware (`SM-A065F`), captured via ADB, and compiled into `docs/gate-1b-screens.png`.
- [x] **Shezrah Abbasi (Product Lead) signs the design before you write UI code**  
  Formal approval executed by Product Lead Shezrah Abbasi in `APPROVAL.md` prior to core UI implementation.
- [x] **Gate 2 · Create the project, push to the repo, write ARCHITECTURE.md**  
  Project initialized with clean MVI/StateFlow architecture, repository synchronized on GitHub, and architectural principles fully detailed in `ARCHITECTURE.md`.
- [x] **Session log**  
  Documented all technical challenges, hardware sensor fallback discoveries, and runtime build resolutions.

---

## Gate Deliverable Artifacts

| Deliverable | File Path | Status |
| :--- | :--- | :--- |
| **Product Specification** | `SPEC.md` | Verified & Committed |
| **Design Wireframe Board (4 States)** | `docs/gate-1b-screens.png` | Verified on Physical Device |
| **Design Sign-Off Document** | `APPROVAL.md` | Signed by Shezrah Abbasi |
| **Architecture Specification** | `ARCHITECTURE.md` | Verified & Committed |
| **Screen State Captures** | `docs/screen_loading.png`<br>`docs/screen_content.png`<br>`docs/screen_empty.png`<br>`docs/screen_error.png` | Stored in Repository |

---

## Session Log & Engineering Resolutions

1. **Hardware Fallback Strategy:**
   - **Device Constraint:** Target test device (`Samsung Galaxy A06 / SM-A065F`) lacks a hardware magnetometer (`Sensor.TYPE_MAGNETIC_FIELD`).
   - **Resolution:** Engineered automatic fallback triggering `TYPE_ACCELEROMETER` with raw pitch/roll derived via `atan2` trigonometry. The compass rose cleanly collapses, highlighting total tilt angle and tare controls.

2. **Security Exception (`HIGH_SAMPLING_RATE_SENSORS`):**
   - **Issue:** Modern Android runtimes throw `SecurityException` when requesting `SENSOR_DELAY_FASTEST` without declaring special permissions.
   - **Resolution:** Adjusted sensor registration to `SensorManager.SENSOR_DELAY_GAME` (~50 Hz / 20 ms), delivering smooth spring-damping physics while remaining strictly zero-permission compliant.

3. **Kotlin 2.x Material 3 Opt-In:**
   - **Issue:** Class-level composition errors regarding experimental Material 3 APIs (`Scaffold`, `TopAppBarDefaults`).
   - **Resolution:** Explicitly annotated `MainActivity` with `@OptIn(ExperimentalMaterial3Api::class)`.

4. **Character Encoding & Visual Parity:**
   - **Issue:** PowerShell text redirection corrupted degree symbols (`\u00B0`) and status indicators into `?`.
   - **Resolution:** Enforced direct unicode string escapes, normalized font layouts, and fixed package resolution paths for deployment.

---

## Status at Day 1 Close

All Day 1 gates are completely fulfilled, hardware-tested, and synchronized to remote branch `day-1-spec-design`. Workspace is primed for Day 2 (`day-2-settings-ads`).
