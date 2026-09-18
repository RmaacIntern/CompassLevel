# Specification — App A: Compass & Level

**App ID:** `com.aivigil.compasslevel`  
**Target SDK:** 36 | **Compile SDK:** 37 | **Min SDK:** 24  
**Date:** 2026-09-18  

## 1. Problem Statement & Functional Goals
Deliver a low-latency, battery-conscious utility providing dual real-time physical measurements:
- **Magnetic Heading (Azimuth):** Real-time degree heading (0°–359°) with cardinal/intercardinal direction markers.
- **Surface Level (Tilt & Roll):** 2D bubble level and numeric degrees of inclination on Pitch (X) and Roll (Y) axes.

## 2. Hardware Sensor Contracts
- **Primary Source:** `Sensor.TYPE_ROTATION_VECTOR` (fused sensor).
- **Fallback Source:** `Sensor.TYPE_ACCELEROMETER` paired with `Sensor.TYPE_MAGNETIC_FIELD` (processed via `SensorManager.getRotationMatrix` and `SensorManager.getOrientation`).
- **Sampling Rate:** `SensorManager.SENSOR_DELAY_UI` (balances 60fps UI fluidity with battery conservation).
- **Lifecycle Management:** Unregister listeners inside `onPause`/`DisposableEffect` to guarantee 0% background sensor power drain.

## 3. Scope Boundaries: What We Are NOT Building This Week

| Feature | Exclusion Rationale |
| :--- | :--- |
| **True North (GPS/Declination)** | Requires runtime location permissions (`ACCESS_FINE_LOCATION`), GPS power draw, and geoid lookups. Out of scope for a pure hardware sensor utility. |
| **Camera Viewfinder / AR Mode** | Adds unnecessary camera hardware access, complexity, and battery drain. |
| **Map Overlays / Tile Integration** | Demands external SDKs (Google Maps/Mapbox) and network overhead. |
| **Cloud Telemetry / Remote Sync** | Violates offline-first architectural purity. |
| **Complex Haptic Polyphony** | Level alignment uses standard system tick feedback only. |

## 4. Acceptance Criteria
1. Compass displays continuous, filtered heading with smooth low-pass interpolation.
2. Level indicators show Pitch and Roll deviation ±0.5° accuracy against flat surface testing.
3. App transitions automatically across all 4 lifecycle and sensor availability states.
