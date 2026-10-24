plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hyperos.redmia5"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hyperos.redmia5"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }
}
