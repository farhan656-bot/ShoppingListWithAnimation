plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.shoppinglist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shoppinglist"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures { compose = true }

    // (opsional, tapi biasanya aman ditambahkan)
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    // --- Compose BOM: jaga versi konsisten ---
    implementation(platform(libs.androidx.compose.bom))

    // Compose core
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // --- Navigation Compose (WAJIB untuk NavHost) ---
    // Pakai versi-catalog kalau ada:
    // implementation(libs.androidx.navigation.compose)
    // Jika alias katalog belum ada, pakai artifact langsung:
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- (Opsional) Animations untuk efek di list/screen ---
    // Pakai katalog kalau ada: implementation(libs.androidx.compose.animation)
    implementation("androidx.compose.animation:animation")

    // Testing/debug (biarkan seperti semula)
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
