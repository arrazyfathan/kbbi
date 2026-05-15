# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#removing logging from release build
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --- Data Model Protection ---

# Keep Networking DTOs (Retrofit/Gson)
# These must not be obfuscated because Gson uses reflection to map JSON keys to field names.
-keep class com.arrazyfathan.kbbi.core.data.source.remote.response.** { *; }

# Keep Room Entities
# Room needs to find the constructor and fields of your entities.
-keep class com.arrazyfathan.kbbi.core.data.source.local.entity.** { *; }

# Keep Domain Models (Optional but safe for Parcelable/UI state)
-keep class com.arrazyfathan.kbbi.core.domain.model.** { *; }

# --- General Library Rules ---

# Preserve Annotations and Signatures
# Critical for Retrofit, Room, Koin, and Gson to function correctly.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Gson specific rules
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit specific rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Room specific rules
-dontwarn androidx.room.**

# Koin specific rules
-keep class io.insertkoin.** { *; }

# Lottie specific rules
-keep class com.airbnb.lottie.** { *; }