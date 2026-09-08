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
-keep class com.example.balloonpop.data.** { *; }

# Suppress common harmless R8 warnings
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
