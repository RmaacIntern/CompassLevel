# Design System & State Architecture — Compass & Level

## 1. Architectural Pattern
- **Pattern:** Unidirectional Data Flow (UDF) via Clean Architecture.
- **State Container:** `CompassLevelViewModel` exposing an immutable `StateFlow<CompassUiState>`.
- **UI Engine:** Jetpack Compose Material 3 with canvas-based pointer rendering.

## 2. The 4 Mandatory Screen States

### State 1: Content State (Normal Operation)
- **Condition:** All required sensors detected; accuracy is `SENSOR_STATUS_ACCURACY_MEDIUM` or `HIGH`.
- **UI Elements:**
- Dual gauge display: Circular compass rose (top) and 2D circular spirit bubble (center).
- Digital numeric readouts: Heading degree (°), Pitch (°), Roll (°).
- Visual snap indication: Bubble turns accent green with a subtle system haptic tick when within ±0.5° level.

### State 2: Sensor Unreliable State
- **Condition:** Sensor accuracy drops to `SENSOR_STATUS_UNRELIABLE` or `SENSOR_STATUS_ACCURACY_LOW`.
- **UI Elements:**
- Content remains visible but dimmed.
- Floating non-blocking banner/card: "Compass calibration required".
- Animated Figure-8 calibration diagram illustrating device rotation motion.

### State 3: No Sensor State
- **Condition:** `SensorManager.getDefaultSensor()` returns `null` for both `TYPE_ROTATION_VECTOR` and `TYPE_MAGNETIC_FIELD`.
- **UI Elements:**
- Full-screen friendly error illustration with a clean icon.
- Message: "Magnetic hardware unavailable on this device."
- Fallback level-only mode toggle (if accelerometer is present).

### State 4: Settings State
- **Condition:** User accesses the settings sheet/route.
- **UI Elements:**
- Damping filter coefficient slider (Low-pass smoothing vs. immediate responsiveness).
- Haptic toggle (Enable/disable level snap vibration).
- Angle unit selector (Degrees vs. Percentage slope).
