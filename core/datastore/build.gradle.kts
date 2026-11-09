plugins {
    id("pos.kmp.library")
    id("pos.koin")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:common"))
        api(libs.datastore.preferences.core)
        implementation(libs.kotlinx.coroutines.core)
    }
}
