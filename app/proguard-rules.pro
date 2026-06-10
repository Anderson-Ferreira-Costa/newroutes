# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android Gradle plugin proguard rules.

# OSMDroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Hilt
-keep attributes $HiltComponents

# Room
-keep interface androidx.room.** { *; }
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *

# Retrofit
-keepattributes Signature
-keepattributes Annotations
-keepattributes Broadcast
-keepattributes Exceptions

# Moshi
-keep class com.newroutes.app.** { *; }

# Coroutines
-keepattributes SourceFile, LineNumberTable
