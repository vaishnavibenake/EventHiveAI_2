
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.eventhiveai"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.eventhiveai"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // Note: firebase-storage intentionally removed — photos are now stored in ImageKit
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // Required for Tasks.await() when fetching Firebase ID tokens on background threads
    implementation("com.google.android.gms:play-services-tasks:18.2.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Charts for Analytics
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
