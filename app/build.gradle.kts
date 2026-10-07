plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.astra.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.astra.launcher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("astra-release.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("ASTRA_KEYSTORE_PASSWORD") ?: "astra_release_store_2026"
                keyAlias = System.getenv("ASTRA_KEY_ALIAS") ?: "astra-key"
                keyPassword = System.getenv("ASTRA_KEY_PASSWORD") ?: "astra_release_key_2026"
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            val releaseKeystore = file("astra-release.jks")
            signingConfig = if (releaseKeystore.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core-design"))
    implementation(project(":core-storage"))
    implementation(project(":core-performance"))
    implementation(project(":core-platform"))
    implementation(project(":feature-home"))
    implementation(project(":feature-search"))
    implementation(project(":feature-apps"))
    implementation(project(":feature-lock-preview"))
    implementation(project(":feature-notifications"))
    implementation(project(":feature-controls"))
    implementation(project(":feature-widgets"))
    implementation(project(":feature-recents"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-personalization"))
    implementation(project(":feature-onboarding"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
