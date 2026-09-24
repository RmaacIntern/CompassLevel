# ProGuard / R8 Rules for Compass & Spirit Level

# Compose runtime rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Preserve App Data and Sensor State Models
-keep class com.aivigil.compasslevel.data.** { *; }
-keep class com.aivigil.compasslevel.sensor.** { *; }
-keep class com.aivigil.compasslevel.ui.theme.** { *; }

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }

# AndroidX Camera
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
