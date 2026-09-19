# Day 1: Sensor Engine & Responsive Co-axial UI

## 1. Sensor Implementation (`sensor/CompassSensorManager.kt`)
- Integrated `TYPE_ROTATION_VECTOR` with matrix rotation (`getRotationMatrixFromVector`) and orientation calculation (`getOrientation`).
- Integrated low-pass alpha filter smoothing to eliminate sensor jitter on raw accelerometer/magnetic vectors.
- State preservation: Heading locks and retains the last reliable coordinate when sensor reports `SENSOR_STATUS_UNRELIABLE`.
- Accelerometer fallback calculation for pure spirit level when rotation vector hardware is unavailable.

## 2. Co-axial UI Architecture (`ui/`)
- **Visual Design Tokens (`Color.kt`):** OLED pure black (`#000000`), subtle borders (`#1C1C1E`), high-contrast cardinal accents (North Red `#FF3B30`), and spirit level target illumination (`#34C759`).
- **Responsive Layout (`CompassScreens.kt`):** 
  - Uses `safeDrawingPadding()` to avoid notch, status bar, and gesture navigation bar clipping.
  - Dynamically calculates dial diameter via `BoxWithConstraints` to support compact and tall screen densities without pushing pill cards or ads off-screen.
- **Dial & Bubble Mathematics (`SharedComponents.kt`):**
  - Clockwise compass rose ($N = 0^\circ, E = 90^\circ, S = 180^\circ, W = 270^\circ$).
  - Target reticle illuminated in accent green when $\vert{}pitch\vert{} < 4^\circ$ and $\vert{}roll\vert{} < 4^\circ$.
  - Fixed persistent 320×50 ad banner anchored at the bottom edge.

## 3. State Handling
- `ScreenLoading`: Hardware sensor initialization spinner.
- `ScreenContent`: Live interactive dial and spirit level.
- `ScreenFallbackLevel`: Accelerometer-only spirit level with reticle and tap-to-zero.
- `ScreenUnreliable`: Visual lock badge and figure-8 calibration instructions.

## 4. Gate 1B Wireframe Deliverable
- Interactive 4-state visual board generated and saved to `docs/wireframe_board.html`.
