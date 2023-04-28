plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "me.seebrock3r.elevationtester"
        minSdk = 26
        targetSdk = 33
        versionCode = 7
        versionName = "3.2.2"
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += setOf("META-INF/atomicfu.kotlin_module")
        }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "me.seebrock3r.elevationtester"
}

repositories {
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.github.sephiroth74:android-target-tooltip:2.0.4")
}
