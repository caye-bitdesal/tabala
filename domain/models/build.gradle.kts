plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.bitdesal.tabala.domain.models"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Pure domain — no module dependencies
    implementation(libs.androidx.core.ktx)
}
