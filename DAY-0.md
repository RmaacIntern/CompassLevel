# Day 0: Accounts, Access & Machine Setup

**Date:** 2026-09-17  
**App:** `com.aivigil.compasslevel`  
**Standard:** `SETUP-day-0.md`  

---

## 1. Project Specifications
- **Project Name:** CompassLevel
- **Application ID / Namespace:** `com.aivigil.compasslevel`
- **Minimum SDK:** 24 (Android 7.0) [certain — `app/build.gradle.kts`]
- **Compile SDK / Target SDK:** 36 (Android 16) [certain — `app/build.gradle.kts`]
- **Architecture:** Native Kotlin, Jetpack Compose Material3, pure sensor-only architecture (zero permissions)

---

## 2. Toolchain Verification (§5.3 Evidence)

| Tool | Version / Command Output | Status |
| :--- | :--- | :--- |
| **Git** | `git version 2.54.0.windows.1` [certain] | PASS |
| **Java / JDK** | `openjdk version "25.0.3" 2026-04-21` (Android Studio bundled JBR at `C:\Program Files\Android\Android Studio\jbr`) [certain] | PASS |
| **ADB** | `Android Debug Bridge version 1.0.41` (Version 37.0.1-15733141 at `platform-tools\adb.exe`) [certain] | PASS |
| **Claude Code** | `2.1.178 (Claude Code)` [certain] | PASS |
| **Gradle** | `Gradle 9.6.0 via wrapper (./gradlew)` [certain] | PASS |

---

## 3. Exit Checklist (§8 Audit)

| Check | Status | Evidence / Note |
| :--- | :--- | :--- |
| 2FA on Google and GitHub | [certain] User responsibility | Verified active by user |
| Verification commands produce output | PASS | All 5 commands verified and pasted in §2 above |
| SSH authentication (`ssh -T git@github.com`) | ⚠️ HTTPS Fallback | Repo cloned and authenticated via HTTPS with GitHub credential helper |
| Repo in clean org (`RmaacIntern/CompassLevel`) | PASS | Remote tracking `https://github.com/RmaacIntern/CompassLevel.git` |
| Play Console access | ⚠️ BLOCKER | Logged to Product Lead Shezrah Abbasi (draft app creation pending) |
| AdMob & Firebase briefed on no-click rule | PASS | Understand never click own ads or trigger invalid traffic |
| `.gitignore` contains §6.2 block | PASS | Verified no `.jks`, `.keystore`, or `google-services.json` tracked |
| `./gradlew assembleDebug` returns BUILD SUCCESSFUL | PASS | Verified command-line build in 34s |
| Debug APK running on real phone | PASS | Verified on Samsung SM-A065F (Day 0 test app) |
| Read INTERN-PROGRAM.md §3 | PASS | Zero unauthorized releases, no committing signing keys |
