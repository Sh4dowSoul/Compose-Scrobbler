plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":common"))
    api(project(":repo"))

    implementation(Kotlin.stdlib.jdk8)
    implementation(KotlinX.coroutines.core)

    // Dagger
    implementation("androidx.hilt", "hilt-lifecycle-viewmodel", "_")
    kapt("androidx.hilt", "hilt-compiler", "_")
    implementation("com.google.dagger", "hilt-android", "_")
    kapt("com.google.dagger", "hilt-android-compiler", "_")
    implementation("androidx.hilt", "hilt-work", "_")
    implementation(AndroidX.work.runtimeKtx)
}