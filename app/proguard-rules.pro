# Release optimization is currently disabled (see app/build.gradle.kts). These rules are
# staged so turning it on later is a one-line change.

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep,includedescriptorclasses class com.palmlens.**$$serializer { *; }
-keepclassmembers class com.palmlens.** {
    *** Companion;
}
-keepclasseswithmembers class com.palmlens.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# --- OkHttp / okhttp-sse (OpenAI client, Phase 3) ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Play Billing (Phase 6) ---
-keep class com.android.billingclient.** { *; }

# --- AdMob + UMP (Phase 7). The GMS libs ship consumer rules; this just matches the file. ---
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.**
