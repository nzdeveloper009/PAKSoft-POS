plugins {
    id("pos.kmp.compose")
}

kotlin {
    sourceSets.androidMain.dependencies {
        implementation(libs.lifecycle.runtime.compose)
    }
}
