# SPEC: Compass & Level (App A)

## 1. Product Summary
A minimalist, precision instrument utility providing a magnetic compass dial and dual-axis spirit bubble level on a unified obsidian screen. Target: Android 7.0+ (API 24+), Target SDK 36.

---

## 2. Features We Are Building
- **Unified Compass & Spirit Level Screen:**
  - Real-time heading display with cardinal direction subtitles.
  - Concentric dual-disc spirit level with visual snap and haptic feedback at ±0.5°.
  - Fallback level-only mode for devices lacking a magnetometer.
- **Dual-Axis Spirit Level Screen:**
  - Surface inclination readout in decimal degrees (or percentage grade).
  - Tap-to-tare surface zeroing to counter camera bump elevation.
  - Dual pitch & roll readouts with ±0.1° resolution.
- **Sensor Engine & Signal Conditioning:**
  - Fast-polling sensor stream (SENSOR_DELAY_GAME) with exponential moving average (EMA) smoothing (alpha = 0.18).
  - Shortest-angular-delta wrapping to eliminate 359°-0° snap jitter.
  - "Heading Hold" buffer: Dial freezes at last known good heading when accuracy drops to UNRELIABLE.
- **Settings Screen:**
  - True North vs. Magnetic North toggle with manual declination adjustment.
  - Angle units toggle (Degrees ° vs. Percentage Grade %).
  - About dialog with zero-permission privacy declaration and target SDK confirmation.
- **Monetization Isolation:**
  - Bottom-anchored 50dp ad container isolated from the dial layout via strict vertical constraints.
  - One rewarded video placement ("How to calibrate your compass").
  - Zero interstitial advertisements.

---

## 3. What We Are NOT Building
- **Zero GPS / Location Permissions:** No GPS, fine/coarse location APIs, or runtime permission requests.
- **Zero Network Dependency:** No servers, analytics pings, or database sync. The core utility is strictly offline.
- **No Mapping / Camera AR:** No Google Maps SDK, Mapbox, or camera preview layers.
- **No Degree Accuracy Claims:** Never claim laboratory or absolute degree-level accuracy; standard consumer MEMS sensors exhibit ±1°–2° drift.

---

## 4. Acceptance Criteria
- [x] Stable compass orientation without needle flicker or 360° spin at North.
- [x] Dual-axis spirit level smoothly tracks tilt, snaps green and vibrates at ±0.5°.
- [x] Level-only fallback functions gracefully when magnetometer hardware is absent.
- [x] Ad container never overlaps, crowds, or clips the dial viewport.
- [x] Heading-hold buffer freezes heading when magnetic sensor reports UNRELIABLE.
- [x] Settings screen provides True North toggle, declination adjust, and unit selection.
