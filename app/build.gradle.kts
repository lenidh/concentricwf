plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.jaredsburrows.license") version "0.9.7"
}

android {
    namespace = "de.lenidh.concentricwf"
    compileSdk = 34

    defaultConfig {
        applicationId = "de.lenidh.concentricwf"
        minSdk = 30
        targetSdk = 34
        versionCode = 4
        versionName = "1.2.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
        }
    }
}

licenseReport {
    copyHtmlReportToAssets = true
    copyJsonReportToAssets = true
    copyTextReportToAssets = true
}

dependencies {
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.wear.compose:compose-foundation:1.4.0")
    implementation("androidx.wear.compose:compose-material:1.4.0")
    implementation("androidx.wear.compose:compose-navigation:1.4.0")
    implementation("androidx.wear.compose:compose-ui-tooling:1.4.0")
    implementation("androidx.wear.watchface:watchface:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-data:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-rendering:1.2.1")
    implementation("androidx.wear.watchface:watchface-editor:1.2.1")
    implementation("androidx.wear.watchface:watchface-style:1.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear:wear-remote-interactions:1.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    debugRuntimeOnly("androidx.compose.ui:ui-test-manifest")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
}