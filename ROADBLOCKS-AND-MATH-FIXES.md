# Technical Roadblocks & Mathematical Solutions

**Product**: `CompassLevel`  
**Product Lead**: Shezrah Abbasi  
**Lead Developer**: Rizwan  
**Date**: September 23, 2026  

---

## 1. Euler Angle Gimbal Lock Singularity
- **Symptom**: Compass spinning wildly, azimuth jumping 180°, roll snapping between +2° and -178° when holding phone upright.
- **Root Cause**: `SensorManager.getOrientation` uses Euler angles. As pitch reaches 90° (upright portrait), Euler angles suffer mathematical gimbal lock singularity.
- **Solution**: Replaced Euler angles with 3D rotation matrix projections:
  - Top axis in world coords: `u_top = (R1, R4, R7)^T`
  - Sight axis in world coords: `u_sight = (-R2, -R5, -R8)^T`
  - Continuous forward blend: `E = (1 - R7^2)*R1 + R7^2*(-R2)`, `N = (1 - R7^2)*R4 + R7^2*(-R5)`
  - Heading: `atan2(E, N) * 180 / PI` with zero singularity across flat, tilted, and upright postures.

---

## 2. AR Clinometer Sighting Horizon Alignment
- **Symptom**: Clinometer displayed -89.4° and artificial horizon clamped off-screen when pointing camera straight ahead at horizon.
- **Root Cause**: Clinometer received Euler pitch (which is 0° flat on table, -90° upright). Eye-level horizon was offset by -90°.
- **Solution**: Derived true elevation from camera line of sight vector:
  - `Elevation = atan2(-R8, hypot(R2, R5)) * 180 / PI`
  - `CameraRoll = atan2(-R6, R7) * 180 / PI`
  - When upright looking horizontally: Elevation is exactly 0.0°, slope is 0.0%, and horizon line centers on reticle in emerald green.

---

## 3. Spirit Level Bubble Physics & Circular Clamping
- **Symptom**: Vertical Y-tube bubble sank downwards when top of phone was lifted; 2D bullseye bubble jammed in square corners.
- **Root Cause**: Screen coordinate Y-axis points downwards (+Y down). Code used `cy + yOffset`. Also, independent X/Y clamping created square box bounds.
- **Solution**:
  - Inverted Y translation: `cy - yOffset`. Lifting top edge moves bubble UP (-Y in screen coords).
  - Implemented circular radial clamping: `scale = if (dist > maxTravel) maxTravel / dist else 1f`.

---

## 4. Jetpack Compose 50 Hz Recomposition Churn
- **Symptom**: Logcat reported `Skipped 46 frames! The application may be doing too much work on its main thread.`
- **Root Cause**: `currentMeasurementSummary` in `MainActivity.kt` was in root Composable scope, invalidating on every 20ms sensor tick.
- **Solution**:
  - Moved `currentMeasurementSummary` calculation inside `if (showNotesModal)`.
  - Added 0.05° change deadband in `CompassSensorManager` to skip unchanged frames when stationary.

---

## 5. Dial Typography & Numeric Overlap
- **Symptom**: Numbers colliding with letters and ticks on compass rose.
- **Root Cause**: Duplicate 30° numeric labels drawn in same track as Cardinals.
- **Solution**: Removed redundant 30° numbers; expanded 8 bold Cardinals & Intercardinals (`N`, `NE`, `E`, `SE`, `S`, `SW`, `W`, `NW`) with 120 precision ticks per Porsche Sport Chrono instrument standards.\n