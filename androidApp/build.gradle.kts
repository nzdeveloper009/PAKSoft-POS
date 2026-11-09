plugins {
    id("pos.android.application")
}

android {
    namespace = "pk.paksoft.pos.android"

    defaultConfig {
        applicationId = "pk.paksoft.pos"
        versionCode = 1
        versionName = "0.1.0"
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
}
