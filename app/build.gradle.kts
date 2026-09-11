import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Local, gitignored config. Values fall back to Google's public AdMob test IDs so the
// app builds and runs for anyone without secrets. See local.properties.sample.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String, default: String): String = localProps.getProperty(key) ?: default

// Google's public AdMob test units — https://developers.google.com/admob/android/test-ads
val ADMOB_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
val ADMOB_TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
val ADMOB_TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

android {
    namespace = "com.palmlens"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.palmlens"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- OpenAI (Phase 3). Key ships in the APK — keep a hard monthly usage limit on it
        //     at platform.openai.com and rotate if abused. Override the model / base URL in
        //     local.properties without touching code; point OPENAI_BASE_URL at a proxy later.
        buildConfigField("String", "OPENAI_API_KEY", "\"${secret("OPENAI_API_KEY", "")}\"")
        buildConfigField(
            "String",
            "OPENAI_BASE_URL",
            "\"${secret("OPENAI_BASE_URL", "https://api.openai.com/v1")}\"",
        )
        // VERIFY current model ids at platform.openai.com/docs/models and set in local.properties.
        buildConfigField(
            "String",
            "OPENAI_VISION_MODEL",
            "\"${secret("OPENAI_VISION_MODEL", "gpt-5.6-luna")}\"",
        )
        buildConfigField(
            "String",
            "OPENAI_TEXT_MODEL",
            "\"${secret("OPENAI_TEXT_MODEL", "gpt-5.6-luna")}\"",
        )

        // --- AdMob (Phase 7). local.properties ids are used in RELEASE only; debug is pinned
        //     to Google's public test ids below (clicking a live ad on your own build is a
        //     policy violation). Missing release keys also fall back to test ids.
        buildConfigField("String", "ADMOB_APP_ID", "\"${secret("ADMOB_APP_ID", ADMOB_TEST_APP_ID)}\"")
        buildConfigField(
            "String",
            "ADMOB_BANNER_UNIT",
            "\"${secret("ADMOB_BANNER_UNIT", ADMOB_TEST_BANNER)}\"",
        )
        buildConfigField(
            "String",
            "ADMOB_INTERSTITIAL_UNIT",
            "\"${secret("ADMOB_INTERSTITIAL_UNIT", ADMOB_TEST_INTERSTITIAL)}\"",
        )
        manifestPlaceholders["admobAppId"] = secret("ADMOB_APP_ID", ADMOB_TEST_APP_ID)
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "ADMOB_APP_ID", "\"$ADMOB_TEST_APP_ID\"")
            buildConfigField("String", "ADMOB_BANNER_UNIT", "\"$ADMOB_TEST_BANNER\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_UNIT", "\"$ADMOB_TEST_INTERSTITIAL\"")
            manifestPlaceholders["admobAppId"] = ADMOB_TEST_APP_ID
        }
        release {
            optimization {
                enable = false
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.okhttp)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.exifinterface)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
