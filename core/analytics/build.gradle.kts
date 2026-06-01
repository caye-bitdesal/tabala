plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.bitdesal.tabala.core.analytics"
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
    implementation(project(":domain:models"))
    implementation(project(":core:utils"))
    implementation(project(":data:repository"))
    implementation(libs.androidx.core.ktx)
}
