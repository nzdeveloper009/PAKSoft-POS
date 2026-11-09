plugins {
    id("pos.kmp.library")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:common"))
        api(project(":core:model"))
        api(project(":core:data"))
        implementation(project(":core:datastore"))
        implementation(project(":core:network"))
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
    }
}
