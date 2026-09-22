# Compass & Level (CompassLevel)
A high-precision, 60fps GPU-accelerated, zero-permission compass and dual-axis spirit level built with 100% native Jetpack Compose for Android 16 (SDK 36).

---

## Master Dossier & Post-Mortem
For the complete engineering journey from Day 0 setup through Day 1 gate violations, UI overhaul, competitor benchmarking, and 60fps performance optimization:
👉 **[Read the Full POST-MORTEM.md](POST-MORTEM.md)**

---

## Where everything is

| System | Where | Who has access |
|---|---|---|
| **Org Repo (Submission)** | `https://github.com/RmaacIntern/CompassLevel.git` | Organization / Tech Lead / Shezrah Abbasi |
| **Personal Repo (Backup)** | `https://github.com/riz5y/CompassLevel.git` | Rizwan (`riz5y`) |
| **Play listing** | Draft app pending creation (Logged blocker in `DAY-1.md`) | Product Lead (Shezrah Abbasi) |
| **Firebase project** | None (Zero-network privacy policy — no analytics or remote logging) | none |
| **AdMob app** | Test App ID `ca-app-pub-3940256099942544~3347511713` (SDK v23.6.0) | Production ID pending Shezrah Abbasi |
| **Keystore** | Debug keystore (`~/.android/debug.keystore`) — Release vault pending Gate 12 | none |
| **Privacy policy** | Zero permissions requested; 100% offline sensor utility | Public domain |
| **Interactive Showcase** | `docs/screens_interactive_showcase.html` | Browser-runnable for all 5 UI states |

---

## Build

Exact, copy-pasteable build instructions verified on Windows 11 with PowerShell:

```powershell
# 1. Ensure Java 25 (Android Studio bundled JBR) is in your session path
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# 2. Verify Android SDK location
# Ensure local.properties contains: sdk.dir=C:\Users\RIZWANPC\AppData\Local\Android\Sdk

# 3. Clean and build Debug APK
.\gradlew.bat assembleDebug

# 4. Binary output location (11.8 MB [certain])
# .\app\build\outputs\apk\debug\app-debug.apk

# 5. Sideload directly to connected physical hardware (Samsung SM-A065F)
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

---

## The three files that matter most

| File | What it does |
|---|---|
| [`CompassSensorManager.kt`](app/src/main/java/com/aivigil/compasslevel/sensor/CompassSensorManager.kt) | Core sensor pipeline: captures `TYPE_ROTATION_VECTOR` with accelerometer fallback, applies shortest-angular-delta wrapping $((\Delta+540)\%360)-180$, low-pass smoothing (EMA $\alpha=0.18$), Heading-Hold buffer on `UNRELIABLE`, and surface tare offset. |
| [`SharedComponents.kt`](app/src/main/java/com/aivigil/compasslevel/ui/SharedComponents.kt) | High-performance 60fps GPU dial rendering: uses `Modifier.graphicsLayer { rotationZ = -heading }` lambda to bypass Compose recomposition, drawing 330dp dial, 360° ticks, 30° labels, and 136dp bullseye level with 18dp 3D fluid bubble. |
| [`CompassScreens.kt`](app/src/main/java/com/aivigil/compasslevel/ui/CompassScreens.kt) | Multi-state screen views: Content view (Compass & Level switch), Standalone Spirit Level (with Tare Zero button), Settings modal (True North toggle, declination step adjust, % Grade units), and Error/Loading states. |

---

## Gotchas

1. **AAPT Resource Linking Breaks on Deleted XMLs:**
   - In an attempt to reduce file count to 36, `themes.xml`, `backup_rules.xml`, and `data_extraction_rules.xml` were untracked.
   - `./gradlew assembleDebug` immediately failed with `AAPT: error: resource style/Theme.CompassLevel not found` because `AndroidManifest.xml` explicitly references them.
   - *Fix:* Never delete XML files referenced by the manifest.
2. **Reverse Needle Spin Crossing North ($359^\circ \leftrightarrow 0^\circ$):**
   - Linear interpolation between 359° and 1° causes a violent -358° spin.
   - *Fix:* Must use shortest-angular-delta wrapping: `((delta + 540) % 360) - 180`.
3. **Compose Recomposition Stutter at 60fps:**
   - Passing live heading directly into Canvas parameters triggers full layout re-measurement every sensor event.
   - *Fix:* Wrap rotation in `Modifier.graphicsLayer { rotationZ = -heading }` lambda to draw straight to the GPU RenderNode.
4. **PowerShell Parameter Collisions with Git:**
   - Running git commands with PowerShell flags like `-ErrorAction SilentlyContinue` causes `fatal: unknown switch 'E'`.
   - *Fix:* Run git commands strictly with native git arguments.

---

## Documentation Index

- **[`POST-MORTEM.md`](POST-MORTEM.md):** Complete master post-mortem & engineering audit (Phases 1-4).
- **[`SPEC.md`](SPEC.md):** Gate 1 feature boundaries & out-of-scope declarations.
- **[`DESIGN.md`](DESIGN.md):** Gate 1B UI states & visual hierarchy specification.
- **[`ARCHITECTURE.md`](ARCHITECTURE.md):** Gate 2 technical decisions, data flows, and known weaknesses.
- **[`APPROVAL.md`](APPROVAL.md):** Product Lead (Shezrah Abbasi) design sign-off records.
- **[`DAY-0.md`](DAY-0.md):** Day 0 toolchain setup & audit checklist.
- **[`DAY-1.md`](DAY-1.md):** Day 1 specification, design, and architecture audit log.
- **[`SESSION-LOG-2026-09-22.md`](SESSION-LOG-2026-09-22.md):** Daily session log with `# What I got wrong`.
