plugins {
    id("pos.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:common"))
    }
}
