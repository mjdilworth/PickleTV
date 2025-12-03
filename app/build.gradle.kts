import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pickletv.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.pickletv.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        // Provide default for all variants
        buildConfigField("String", "DEBUG_VIDEO_FILE_NAME", "\"h-6.mp4\"")
    }

    // Load signing configuration from keystore.properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(keystorePropertiesFile.inputStream())
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            val debugVideo = System.getenv("DEBUG_VIDEO_FILE_NAME") ?: "h-6.mp4"
            buildConfigField("String", "DEBUG_VIDEO_FILE_NAME", "\"$debugVideo\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Automatically push/generate a debug video before installing debug builds (not on assemble)
tasks.register<Exec>("prepareDebugVideo") {
    group = "pickletv"
    description = "Generate/push debug video to the emulator/device"
    workingDir(rootDir)
    commandLine("bash", "${rootDir}/tools/push_video.sh")
}

// Hook only into installDebug so builds don't require a device
// This triggers when the task graph includes installDebug (Android Studio run/debug)
// DISABLED: Uncomment if you want to auto-push h-6.mp4 video on install
/*
tasks.configureEach {
    if (name == "installDebug") {
        dependsOn("prepareDebugVideo")
    }
}
*/

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)

    // Networking and image loading
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}