plugins {
    id("pos.kmp.compose")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:common"))
        api(project(":core:model"))
        api(project(":core:designsystem"))
        implementation(libs.lifecycle.runtime.compose)
    }
}
