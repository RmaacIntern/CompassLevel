# ProGuard / R8 Rules for Compass & Spirit Level

# Compose runtime rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# Preserve App Data, Ads, and Sensor State Models
-keep class com.aivigil.compasslevel.data.** { *; }
-keep class com.aivigil.compasslevel.sensor.** { *; }
-keep class com.aivigil.compasslevel.ads.** { *; }
-keep class com.aivigil.compasslevel.ui.theme.** { *; }

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }

# AndroidX Camera
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Google Mobile Ads (AdMob)
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# AndroidX WorkManager, Room, and Startup (used transitively by Google Mobile Ads)
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class androidx.startup.** { *; }
-dontwarn androidx.startup.**
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    void <init>();
}

