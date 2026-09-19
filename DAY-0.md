# Day 0: Project Setup & Environment Verification

## 1. Project Specifications
- **Project Name:** CompassLevel
- **Application ID / Package:** `com.aivigil.compasslevel`
- **Minimum SDK:** 26 (Android 8.0)
- **Target SDK / Compile SDK:** 34 / 35
- **Tooling:** Android Studio Hedgehog+ / Ladybug+, Kotlin DSL (`build.gradle.kts`), Jetpack Compose Material3

## 2. Sensor Strategy & Zero-Permission Model
- **Primary Heading:** `Sensor.TYPE_ROTATION_VECTOR`
- **Fallback / Level Sensor:** `Sensor.TYPE_ACCELEROMETER`
- **Permissions:** None (`android.permission.ACCESS_FINE_LOCATION` and network access deliberately excluded per zero-permission design guidelines).

## 3. Environment Checks Completed
- [x] Android Studio environment configured with JDK 17/21+.
- [x] Gradle wrapper initialized and operational (`gradlew`).
- [x] Physical device connected via ADB with USB debugging enabled.
- [x] Repository initialized with `.gitignore` for Android/Gradle/IntelliJ artifacts.
