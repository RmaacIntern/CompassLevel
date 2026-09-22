---
title: "CompassLevel — 2026-09-22 session log: UI overhaul, competitor research, APK build"
app: com.aivigil.compasslevel
date: 2026-09-22
tip: c01ae9d
status: "Debug APK built and installed; all 4 screens upgraded to competitor-grade UI; pushed to main."
type: session log
---

# Where this stopped

| Area | State |
|---|---|
| Compass screen | Live — upgraded compass rose (72 ticks, glowing north needle, glass bubble) |
| Level-only screen | Live — crosshair reticle, animated degree color, TAP TO ZERO button styled |
| Loading screen | Live — radial glow + branded title + leading dot on spinner |
| Error screen | Live — pulsing amber ring + figure-8 `∞` instruction card |
| APK | `app-debug.apk` — 11.24 MB [certain — `Get-Item` output], at `app/build/outputs/apk/debug/` |
| GitHub | Pushed to `origin/main` at commit `c01ae9d` [certain] |
| Settings screen | Not built — deferred to next sprint per plan |
| AdMob integration | Not integrated — ad banner is a placeholder box only |

---

# What I did

**Competitor research**
Surveyed top Play Store compass & level apps: Digital Compass (Axiomatic), Bubble Level (MaslonLabs), Compass Pro, Compass – Beautiful Minimal. Identified five concrete gaps vs. the existing codebase: no glow on north needle, no glass-gradient bubble, no color-coded pitch/roll pills, no pulsing error animation, 12 ticks vs. 72 on the degree ring.

**Color.kt** — added 13 new tokens for glow and glass effects: `GlowGreen / GlowRed / GlowAmber` (33% alpha for glow layers), `GlowGreenSoft / GlowRedSoft / GlowAmberSoft` (12% alpha for outer halos), `BubbleGlassLight / BubbleGlassMid / BubbleGlassDark` (radial-gradient fill for the spirit bubble), `AccentBlue / GlowBlue`, `BannerBorder`. Why: the original palette had no way to express layered glow — everything would have needed hardcoded color literals.

**SharedComponents.kt** — rewrote the three drawing composables:
- `CompassRoseDial`: 12-tick ring → 72-tick ring (5° each, labeled every 30°); north needle gains `BlurMaskFilter` glow via `drawIntoCanvas`; glass bubble uses `Brush.radialGradient` for highlight; snap ring emits a `GlowGreen` halo when levelled.
- `ReticleSpiritLevel`: outer crosshair extends to ring edge; concentric tick marks at every 10°; glass bubble; green glow halo at level.
- `PillCard`: numeric value extracted and compared; `animateColorAsState` drives green → amber → red based on absolute degree value.

**CompassScreens.kt** — rewrote all four screens:
- `ScreenLoadingView`: radial `Brush.radialGradient` glow behind spinner, leading dot on arc computed from rotation angle.
- `ScreenLiveCompassView`: 80sp heading, cardinal badge background switches to `GlowRed` + `NorthRed` border when pointing N.
- `ScreenContentView`: `animateColorAsState` on the inclination degree (white → amber → green), amber `⚠ MAGNETOMETER UNAVAILABLE` badge.
- `ScreenErrorView`: `animateFloat` on `pulseAlpha` and `pulseScale` to pulse the amber ring; figure-8 instruction card with `∞` Unicode glyph.

**Build environment**: had to write `local.properties` with `sdk.dir` and set `JAVA_HOME` to the Android Studio bundled JBR — neither was in the system environment. Fixed by prefixing the Gradle command in PowerShell.

---

# What I got wrong

1. **Wrong `Canvas` import.** Wrote `import androidx.compose.ui.Canvas` in both `SharedComponents.kt` and `CompassScreens.kt`. There is no `@Composable` named `Canvas` in `androidx.compose.ui`; the correct package is `androidx.compose.foundation.Canvas`. This killed the first full build — ~42 seconds wasted plus the import-fix cycle. The check: when a `@Composable` block stops seeing `size`, `toPx()`, and draw functions, the `Canvas` it is inside is not a `DrawScope` receiver — wrong import.

2. **Also pulled in `androidx.compose.ui.graphics.drawscope.rotate`** alongside `Modifier.rotate`. These are different: one is a `DrawScope` transform (for use inside a `Canvas` block), the other is a `Modifier`. Having both imported caused ambiguity warnings. Removed the `DrawScope` variant because the rotating compass rose is handled by `Modifier.rotate(-heading)` on the `Canvas` composable, not by a draw-time rotation.

3. **`JAVA_HOME` and `ANDROID_HOME` not in system PATH.** Assumed Gradle would resolve them; it does not on this machine. Cost one failed build to discover. Fix: explicit PowerShell env vars before every Gradle invocation — not a permanent fix, user should add these to system environment variables.

---

# Blockers

| Blocker | Owner | Raised | Due |
|---|---|---|---|
| `JAVA_HOME` / `ANDROID_HOME` not set system-wide — every Gradle run needs manual env var prefix in PowerShell | User (machine config) | 2026-09-22 | Before next build session |

---

# Next, in order

1. **Install APK on a physical device and verify all 4 screen states render correctly** — copy `app\build\outputs\apk\debug\app-debug.apk` to phone or run `adb install app\build\outputs\apk\debug\app-debug.apk` with USB debugging enabled.
2. **Verify compass rose rotation** — open Live tab, rotate the phone slowly through 360°; confirm the needle stays stationary (points N) while the rose rotates underneath.
3. **Verify spirit level snap** — lay phone flat on a table; confirm the bubble centers and the snap ring turns green.
4. **Fix JAVA_HOME/ANDROID_HOME permanently** — add both to Windows system environment variables so `.\gradlew` works without the PowerShell prefix: `JAVA_HOME = C:\Program Files\Android\Android Studio\jbr`, `ANDROID_HOME = C:\Users\RIZWANPC\AppData\Local\Android\Sdk`.
5. **Build Settings screen** (Day 2 item from SPEC.md) — wire Magnetic vs. True North toggle to `CompassSensorManager`, add manual declination input field, add sensor health dialog.
