# ProGuard / R8 Rules for Google Play Store Release Build

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Android Jetpack Compose Keep Rules
-keepclassmembers class * extends androidx.compose.ui.Modifier {
    <init>(...);
}

# Keep Data Models & Enums used in state serialization & game state
-keepclassmembers enum com.example.balloonpop.model.** { *; }
-keep class com.example.balloonpop.model.** { *; }

# Google Mobile Ads (AdMob)
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }

# Strip verbose and debug logs in release APK to prevent reverse-engineering
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# Suppress common harmless R8 warnings
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
