plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersion = rootProject.file("../VERSION").readText().trim()
val versionMatch = requireNotNull(
    Regex("""^(\d+)\.(\d+)\.(\d+)(?:-modified\.(\d+))?$""").matchEntire(appVersion)
) { "VERSION must use MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-modified.BUILD" }
val (major, minor, patch, modifiedBuild) = versionMatch.destructured
val computedVersionCode =
    major.toInt() * 1_000_000 +
        minor.toInt() * 10_000 +
        patch.toInt() * 100 +
        modifiedBuild.ifEmpty { "0" }.toInt()

android {
    namespace = "com.sidescreen.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sidescreen.app"
        minSdk = 26
        targetSdk = 34
        versionCode = computedVersionCode
        versionName = appVersion
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Wireless mode (0.8.0)
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    testImplementation("junit:junit:4.13.2")
}
