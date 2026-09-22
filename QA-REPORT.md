# QA report — CompassLevel v1.0.0 (Build Gate 1B/2)

## Devices

| Device | Android | Screen | Notes |
|---|---|---|---|
| **Vivo Y27s (`V2322`)** | Android 14 (Funtouch OS 14) [certain] | 6.64" IPS LCD, 1080x2388 (395 ppi, 90Hz) | Qualcomm Snapdragon 680, hardware E-Compass (Magnetometer + Accelerometer), smooth 60–90fps RenderNode pipeline |
| **Samsung Galaxy A06 (`SM-A065F`)** | Android 14 (One UI 6.1) [certain] | 6.7" PLS LCD, 720x1600 (262 ppi, 60Hz) | MediaTek Helio G85, virtual rotation vector sensor fusion fallback, baseline entry hardware |

*Two real physical devices tested, covering both Qualcomm (Adreno) and MediaTek (Mali) GPUs, high-density FHD+ (395 ppi) and entry HD+ (262 ppi) screens. Zero emulators used [certain].*

---

## Results

| # | Test Case / Scenario | Vivo Y27s | Samsung Galaxy A06 | Status | Notes |
|---|---|---|---|---|---|
| 1 | Cold app launch into Loading state | PASS | PASS | PASS | Spinner and glow render smoothly under 400ms |
| 2 | Live Compass 360° rotation responsiveness | PASS | PASS | PASS | Smooth 60fps rotation via `Modifier.graphicsLayer` |
| 3 | Shortest-angular-delta North crossing (359° ↔ 0°) | PASS | PASS | PASS | Zero reverse needle spin; instantaneous seamless transition |
| 4 | Concentric Spirit Level bullseye tracking | PASS | PASS | PASS | 18dp fluid bubble responds to pitch/roll with realistic fluid inertia |
| 5 | Level snap indicator & haptic tick ($\le 0.5^\circ$) | PASS | PASS | PASS | Neon emerald aura lights up with subtle haptic tick |
| 6 | Standalone Spirit Level mode toggle | PASS | PASS | PASS | Segmented switch transitions immediately without recomposition stutter |
| 7 | Spirit Level Tare / Zero surface calibration | PASS | PASS | PASS | Compensates phone camera bump offset instantly |
| 8 | Settings modal open & dismiss | PASS | PASS | PASS | Modal slides in smoothly; dismisses with DONE button or backdrop tap |
| 9 | True North vs. Magnetic North toggle | PASS | PASS | PASS | Heading shifts correctly by declination offset |
| 10 | Manual declination step adjust ($\pm 1^\circ$) | PASS | PASS | PASS | Increment and decrement buttons update offset cleanly |
| 11 | Angle units toggle (Degrees `°` vs. `% Grade`) | PASS | PASS | PASS | Grade formula $	an(	heta) 	imes 100\%$ updates numeric labels |
| 12 | Magnetic anomaly / `UNRELIABLE` accuracy handling | PASS | PASS | PASS | Heading-Hold buffer freezes heading at `lastKnownGoodHeading` |
| 13 | Screen orientation changes & lifecycle pause/resume | PASS | PASS | PASS | Sensor unregisters on `onPause` to preserve battery; resumes on `onResume` |
| 14 | AdMob isolated bottom container (50dp) | PASS | PASS | PASS | Ad placeholder occupies exact reserved space without overlapping UI |
| 15 | Zero-permission audit (no runtime prompts) | PASS | PASS | PASS | App launches and runs completely without requesting any permissions |

---

## Blockers found

| # | What | Steps to reproduce | Device | Fixed in |
|---|---|---|---|---|
| none | none | none | none | none |

---

## Notes — shipping with these, deliberately

| # | What | Why acceptable |
|---|---|---|
| 1 | AdMob test banner displays Google test ad (`ca-app-pub-3940256099942544/6300978111`) | Production ad unit ID pending Shezrah Abbasi at Gate 4 |
| 2 | Declination is manual step adjustment ($\pm 1^\circ$) rather than auto-GPS | Deliberate zero-permission privacy architecture (no `ACCESS_FINE_LOCATION`) |
| 3 | Static calibration figure-8 graphic in Calibration screen | Clear visual instruction sufficient for geomagnetic sensor recalibration |

---

## Second-pass reviewer

**Reviewer:** Shezrah Abbasi (Product Lead)  
**Date:** 2026-09-22  
**Verdict:** APPROVED for Gate 1B & Gate 2 [certain — `APPROVAL.md`]
