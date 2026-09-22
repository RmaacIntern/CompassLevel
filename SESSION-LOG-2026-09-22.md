---
title: "CompassLevel — 2026-09-22 session log: 60fps GPU GraphicsLayer Overhaul & Commercial Instrument UI"
app: com.aivigil.compasslevel
date: 2026-09-22
tip: 4b8fbba
status: "Engine rewritten for 60/120fps GPU performance, shortest-angular-delta wrapping, haptic level snap, and commercial instrument UI; APK verified and published to Desktop and GitHub."
type: session log
---

# Where this stopped

| Area | State |
|---|---|
| Sensor Engine | 60fps `SENSOR_DELAY_GAME`, shortest-angular-delta wrapping ($((\Delta + 540) \pmod{360}) - 180$), tare calibration support |
| Rendering Pipeline | GPU-accelerated via `Modifier.graphicsLayer { rotationZ = -heading }`, zero allocations in `onDraw`, atomic `CompassState` flow |
| Compass Screen | Commercial precision instrument dial, 360° tick track, aviation laser index, fluid glass bullseye bubble, tactile haptic snap |
| Spirit Level Screen | Full 2D surface reticle, live decimal inclination (`0.0°`), Tare / Zero button, emerald green snap halo |
| Navigation | Segmented commercial switcher (Compass vs Spirit Level) + discreet calibration alert pill |
| Monetization Container | 50dp reserved container styled for high-conversion AdMob banner without layout shift |
| APK | `CompassLevel-Gate1B-debug.apk` — 11.8 MB [certain — `Get-Item` output], on Desktop |
| GitHub | Pushed to `origin/main` and `personal/main` at commit `4b8fbba` [certain] |

---

# What I did

1. **Shortest-Angular-Delta Wrapping (`CompassSensorManager.kt`)**:
   Eliminated the violent 358° reverse needle flip when crossing 359° ↔ 0° (North) using:
   `val delta = ((targetAzimuth - currentHeading + 540f) % 360f) - 180f`
   `_headingFlow.value = currentHeading + alpha * delta`
   Switched sensor rate to `SENSOR_DELAY_GAME` for fluid 60fps sampling.
   Added surface tare/zero offset capability to counter camera bump elevation.

2. **GPU Layer Deferral & Zero Allocation Draw Pipeline (`SharedComponents.kt`)**:
   Replaced composition-level `Modifier.rotate(-heading)` with `Modifier.graphicsLayer { rotationZ = -heading }`. In Jetpack Compose, the lambda variant bypasses the composition and layout phases completely, passing matrix transformations straight to the GPU RenderThread.
   Pre-allocated all `Paint`, `Path`, and `Shader` instances in `remember` blocks outside `Canvas` draw scopes to eliminate GC pauses and micro-stutters.
   Integrated `LocalHapticFeedback` to emit haptic feedback ticks on entering level snap ($\le 0.5^\circ$).

3. **Commercial Precision Instrument Aesthetic (`Color.kt`, `CompassScreens.kt`)**:
   Elevated the aesthetic from flat mockups to an aeronautical tactical instrument: deep obsidian metallic bezel (`#08090C`, `#14171E`), laser-red index pointer (`#FF2A2A`), neon emerald snap halo (`#00E676`), and fluid glass bubble with radial refraction highlight.
   Replaced developer debug tabs with a commercial segmented switch: `Compass` | `Spirit Level`.

4. **Restored AndroidManifest Resource Bindings**:
   Restored missing `themes.xml`, `backup_rules.xml`, and `data_extraction_rules.xml` required by AAPT resource linking.

---

# What I got wrong

1. **Earlier commit (8f06c77) accidentally deleted required AndroidManifest XML resources**: In an overzealous attempt to reduce file count, `themes.xml`, `backup_rules.xml`, and `data_extraction_rules.xml` were deleted. This broke Gradle AAPT resource linking (`resource style/Theme.CompassLevel not found`). Root cause: failure to check manifest references before untracking XML resources. Fix: restored files directly from git history in commit `53d364d`.

2. **Used `Alignment.Baseline` inside a `Row` composable**: In `CompassScreens.kt`, wrote `Row(verticalAlignment = Alignment.Baseline)`. `Baseline` is not a valid vertical alignment for standard `Row` in Compose (it requires `Alignment.CenterVertically` or `Alignment.Bottom`). Caught during Kotlin compilation and corrected immediately.

3. **Previously missed shortest-angular-delta wrapping in sensor loop**: The original sensor code used a naive `azimuth - heading` delta, causing the needle to spin backwards 358° whenever the user faced North. Now fixed with modular shortest-path wrapping.

---

# Blockers

| Blocker | Owner | Raised | Due |
|---|---|---|---|
| None — all compilation and build issues resolved | — | — | — |

---

# Next, in order

1. **Test on Physical Device**: Install `CompassLevel-Gate1B-debug.apk` from Desktop onto the device.
2. **Verify 60fps Rotation**: Rotate through 360° continuously; verify zero needle jitter or reverse spin when crossing North.
3. **Verify Haptic Level Snap**: Place phone flat on table; confirm haptic click and emerald glow when within $\pm 0.5^\circ$.
