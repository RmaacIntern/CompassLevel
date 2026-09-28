---
title: "Compass & Level — Technical Specification (App A)"
app: com.aivigil.compasslevel
date: 2026-09-22
revised: 2026-09-28
author: Rizwan
lead: Shezrah Abbasi
version: "1.0.0"
status: "Active Specification & Verification Gate"
type: spec
---

# 1. Product Summary & System Goals

A minimalist, high-precision utility delivering a magnetic compass rose dial and dual-axis spirit bubble level on a unified dark-mode canvas. The utility targets Android 7.0+ (API 24+) through Target SDK 36 (Android 16), ensuring high-rate, low-latency sensor responsiveness across diverse hardware tiers without network or server dependencies.

---

# 2. Who It Is For (Personas & Use Cases)

| Persona | Core Goal | Primary Pain Point Addressed | Acceptance Threshold |
|---|---|---|---|
| **Tradesperson / Carpenter** | Quick horizontal/vertical leveling on construction materials | Bulky mechanical levels; phone camera bump causing wobble | Dual-axis pitch/roll readout within $\pm 0.1^\circ$ precision; tap-to-tare zeroing countering bump |
| **Outdoor Hiker / Scout** | Directional bearing navigation on trails without cellular coverage | Battery-draining apps that require online map tiles | 100% offline sensor operation; true vs. magnetic north toggle; zero location runtime prompts |
| **Field Engineer / Inspector** | Documenting equipment pitch, elevation grade, and alignment | Inability to freeze or record transient reading | Bearing-hold and angle-lock toggle; local in-memory notes; % grade slope calculation |
| **Everyday Homeowner** | Hanging picture frames, leveling home appliances, finding orientation | Cluttered ad-heavy utilities that crash or request intrusive permissions | Clean single-tap launch under 1.5s; immediate level snap highlight at $\pm 0.5^\circ$; zero runtime permissions |

---

# 3. Features & Quantitative Acceptance Criteria

| Feature ID | Feature Name | Description | Done When (Acceptance Criteria & Tolerances) | Priority | Gate |
|---|---|---|---|---|---|
| **FEAT-01** | Unified Compass Dial | 330dp compass rose with 360° tick track, 8 cardinal markers, and heading readout | Azimuth updates at 50 Hz; shortest-angular-delta smoothing eliminates 359°–0° spin; angular error $\le \pm 1.5^\circ$ against reference | P0 | Gate 1 |
| **FEAT-02** | Center Spirit Level | Concentric bullseye level inside compass hub with 18dp fluid bubble | Bubble centers within $\pm 0.5^\circ$ deadband; visual emerald aura triggers and haptic click fires within 25ms of level acquisition | P0 | Gate 1 |
| **FEAT-03** | Dual-Axis Spirit Level | Full-screen surface level with X/Y pitch/roll readouts and tubular guides | Digital readouts show decimal degrees ($\pm 0.1^\circ$ resolution) or % Grade ($0.1\%$ resolution); tap-to-tare zeroes offset | P0 | Gate 1 |
| **FEAT-04** | Magnetometer Fallback | Automated graceful degradation when magnetometer hardware is absent | Detects `hasMagnetometer == false` on launch; routes directly to Level mode; zero crashes; informs user with banner | P0 | Gate 1 |
| **FEAT-05** | Heading-Hold Buffer | Failsafe buffer freezing dial heading when sensor accuracy drops | When accuracy reports `SENSOR_STATUS_UNRELIABLE`, heading locks to `lastKnownGoodHeading`; visual warning indicator activates | P1 | Gate 1B |
| **FEAT-06** | Settings Screen | Configuration modal for north reference, units, and calibration | Allows toggle between Magnetic North and True North; manual declination step $\pm 1.0^\circ$ (range $-45^\circ$ to $+45^\circ$); unit toggle | P1 | Gate 1B |
| **FEAT-07** | AR Optical Clinometer | Horizon sighting viewfinder using camera line-of-sight vector | Eye-level horizon reads exactly $0.0^\circ \pm 0.2^\circ$ in upright portrait; pitch and slope track smoothly up to $\pm 90.0^\circ$ | P1 | Gate 2 |
| **FEAT-08** | Introductory Launch Screen | Branded launch entry showcasing tool suite with AdMob placement | Displays branded dial card, hardware diagnostics badge, and launch CTA; cold start to interactable $\le 1200\text{ ms}$ | P1 | Gate 3 |
| **FEAT-09** | Exit Rating Prompt | Back-press intercepted dialog offering 5-star rating or feedback | Back press on root displays dialog; 4-5 stars routes to Play Store review intent; 1-3 stars opens private feedback intent | P2 | Gate 3 |

---

# 4. Monetization Architecture & Placement Specifications

| Placement ID | Format | Screen / Trigger | Frequency & Cooldown | Offline / Fail Behavior | Consent / Policy |
|---|---|---|---|---|---|
| **AD-BAN-01** | Anchored Adaptive Banner | Bottom of Dashboard (above navigation bar) | Persistent refresh per Google AdMob policy (30–60s) | Collapses gracefully if offline; zero layout shift; reserved 50dp slot | Uses Google Mobile Ads SDK test ID; compliant with Play Families policy |
| **AD-BAN-02** | Anchored Adaptive Banner | Bottom of Animated Opening Splash Screen | Displayed during 3.2s launch loading sequence | Displays background placeholder card until ad loads | AdMob test banner unit ID |
| **AD-INT-01** | Full-Screen Interstitial | App launch: immediately following splash completion | 1x per app cold start session | If ad is not cached within 2.5s timeout, proceeds to intro without blocking user | Skippable after 5s or platform close button; zero sound on mute |
| **AD-INT-02** | Full-Screen Interstitial | Tool mode switch, theme selection, settings modal | Minimum 20.0s cooldown between presentations | If cooldown is active (<20s), proceeds immediately to action; zero UI blocking | Preloaded in background; cached single instance |

---

# 5. Non-Functional & Performance Requirements

| Metric | Target Specification | Max Allowable Tolerance | Verification Method |
|---|---|---|---|
| **Sensor Latency** | $\le 60\text{ ms}$ sensor-to-screen latency | $\le 85\text{ ms}$ on budget SoC (Helio G85) | High-speed video frame count @ 120fps; `SensorManager` timestamp delta |
| **Framerate / Jank** | 60.0 fps sustained animation | $\le 1.2\%$ dropped frames over 60s continuous rotation | `adb shell dumpsys gfxinfo com.aivigil.compasslevel framestats` (0 frames skipped) |
| **Recomposition Rate** | Event-throttled state emission ($\Delta \ge 0.05^\circ$) | 0 recompositions when stationary | Compose Layout Inspector; stationary recomposition count = 0 |
| **Cold Start Time** | $\le 1000\text{ ms}$ to first frame | $\le 1500\text{ ms}$ cold start time-to-interactive | `adb shell am start-W -n com.aivigil.compasslevel/.MainActivity` (`TotalTime`) |
| **Memory Footprint** | $\le 45\text{ MB}$ PSS heap usage | $\le 65\text{ MB}$ peak allocation during rotation | Android Studio Memory Profiler; `adb shell dumpsys meminfo` |
| **Release APK Size** | $\le 10.0\text{ MB}$ compressed download size | $\le 20.0\text{ MB}$ hard program ceiling | File size check on release build: Current release APK = **4.39 MB** [certain] |

---

# 6. Scope Boundaries & What We Are NOT Building

The scope boundaries are established on a week-by-week ratchet to maintain strict delivery velocity:

| Sprint Scope | Feature / Boundary | Scope Rule & Rationale | Current Status |
|---|---|---|---|
| **Week 1 Boundary** | No runtime GPS / Location permission | Inertial sensors only (`ROTATION_VECTOR` + `MAGNETOMETER`). Avoids permission prompt on first launch. | Maintained [certain] |
| **Week 1 Boundary** | No remote server / user login / cloud sync | Offline utility. Zero backend maintenance overhead, zero data privacy risk. | Maintained [certain] |
| **Week 1 Boundary** | No laboratory accuracy claims | Standard consumer MEMS hardware exhibits $\pm 1^\circ$ to $\pm 2^\circ$ magnetic drift. Do not claim survey-grade precision. | Disclaimed in About [certain] |
| **Week 2 Boundary** | No interactive mapping or satellite tiles | Mapbox / Google Maps SDK would add 18+ MB to APK and require network connectivity. | Deferred to v2.0 |
| **Week 2 Boundary** | No paywalls or unskippable interstitial lockouts | Ads must respect strict 20s cooldown and provide immediate skip option. | Maintained [certain] |
| **Permanent Boundary** | No background battery drain / persistent service | Sensors must unregister immediately when activity pauses or enters background (`onPause` / `onStop`). | Enforced in lifecycle [certain] |

---

# 7. Open Questions & Decision Log

| ID | Question | Owner | Raised Date | Status | Resolution / Decision |
|---|---|---|---|---|---|
| **Q-01** | Should manual declination persist across app restarts without Room DB? | Rizwan | 2026-09-22 | Resolved | Implemented `MeasurementNotesManager` and SharedPreferences for persistence without adding heavy database overhead. |
| **Q-02** | What alpha coefficient optimizes sensor smoothing vs. latency on 60Hz/90Hz displays? | Rizwan | 2026-09-22 | Resolved | Benchmarked $\alpha = 0.15$ (95ms latency) vs $\alpha = 0.18$ (58ms latency). Selected $\alpha = 0.18$ with $0.06^\circ$ non-linear deadband. |
| **Q-03** | How should AdMob unit IDs be managed between debug testing and production release? | Rizwan | 2026-09-27 | Open | Debug and QA builds use official Google test unit IDs. Production IDs to be injected via build config upon Play Store account approval. |
| **Q-04** | How to eliminate R8/ProGuard reflection crashes on Google Mobile Ads WorkManager? | Rizwan | 2026-09-27 | Resolved | Added keep rules for `androidx.work.**`, `androidx.room.**`, `androidx.startup.**` in `proguard-rules.pro`; release APK builds at 4.39 MB. |

---

# 8. Acceptance Verification & Gate Checkpoints

- [x] **Gate 1 (Day 1)**: Core compass rose rendering with shortest-angular-delta smoothing and level bubble snap.
- [x] **Gate 1B (Day 2)**: Settings screen, True North declination offset, Heading-Hold buffer, and % grade unit toggle.
- [x] **Gate 2 (Day 3)**: AR Clinometer 0.0° eye-level calibration, dual-axis tare zeroing, and gimbal-lock elimination.
- [x] **Gate 3 (Day 4)**: Introductory launch screen, 5-star exit rating prompt, and multi-device window insets.
- [x] **Gate 4 (Day 5)**: Google AdMob SDK integration (Adaptive Banner + Interstitial), 20s cooldown engine, and release APK packaging under 20MB (achieved 4.39 MB).
- [ ] **Gate 5 (Day 6 - In Progress)**: Final documentation audit pass, frame rate verification data capture, and Play Store pre-launch report.
- [ ] **Gate 6 (Day 7 - Scheduled)**: Release sign-off and Play Console production track submission.
