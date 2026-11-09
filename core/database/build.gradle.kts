plugins {
    id("pos.kmp.library")
    id("pos.koin")
    id("pos.room")
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":core:common"))
        implementation(libs.kotlinx.coroutines.core)
    }
}
