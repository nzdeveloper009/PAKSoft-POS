plugins {
    id("pos.kmp.library")
    id("pos.koin")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(libs.kotlinx.coroutines.core)
    }
}
