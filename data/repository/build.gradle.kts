plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.bitdesal.tabala.data.repository"
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
    implementation(project(":data:network"))
    implementation(project(":data:database"))
    implementation(project(":core:utils"))
    implementation(libs.androidx.core.ktx)
}
