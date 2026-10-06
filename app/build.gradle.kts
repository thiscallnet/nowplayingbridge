plugins {
    id("com.android.application")
}

android {
    namespace = "dev.nowplayingbridge"
    compileSdk = 37

    defaultConfig {
        // Must be an unused package from Pixel Now Playing's allowlist.
        applicationId = "com.pandora.android"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        debug {
            buildConfigField("boolean", "LOG_INCOMING_INTENTS", "true")
        }
        release {
            buildConfigField("boolean", "LOG_INCOMING_INTENTS", "false")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
