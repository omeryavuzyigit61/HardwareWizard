# ==============================================================================
# HardwareWizard — Production R8 / ProGuard Configuration
# ==============================================================================

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.panda.myapplication.**$$serializer { *; }
-keepclassmembers class com.panda.myapplication.** {
    *** Companion;
}
-keepclasseswithmembers class com.panda.myapplication.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep DTOs and Domain serializable models intact for kotlinx.serialization
-keep @kotlinx.serialization.Serializable class com.panda.myapplication.** { *; }

# Room Database & SQLite Bundled Driver
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class com.panda.myapplication.** { *; }
-keep @androidx.room.Dao interface com.panda.myapplication.** { *; }
-keep class com.panda.myapplication.data.local.**_Impl { *; }
-keep class androidx.sqlite.driver.bundled.** { *; }
-dontwarn androidx.sqlite.driver.bundled.**

# Ktor Client & OkHttp Engine
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Koin DI
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Coil 3 Image Loader
-dontwarn coil3.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Preserve source file names and line numbers for crash stacktraces without leaking paths
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable