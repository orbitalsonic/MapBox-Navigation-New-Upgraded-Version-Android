# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Retrofit 2
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes *Annotation*,Signature

# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**

-dontwarn okio.**
-dontwarn okhttp3.**
-keep class retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
-dontwarn retrofit.**

# --- Retrofit ---
# Retain service method parameters.
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# GSON
-keep class com.google.gson.** { *; }

-dontnote com.squareup.**

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn okio.BufferedSink
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Mapbox
-keep class com.mapbox.android.telemetry.**
-keep class com.mapbox.android.telemetry.** { *; }
-keep class com.mapbox.android.core.location.**
-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-dontnote com.mapbox.mapboxsdk.**
-dontnote com.mapbox.android.gestures.**
-dontnote com.mapbox.mapboxsdk.plugins.**
# MAS data models that will be serialized/deserialized over Gson
-keep class com.mapbox.services.api.directionsmatrix.v1.models.** { *; }

-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-keep class com.mapbox.mapboxsdk.** { *; }