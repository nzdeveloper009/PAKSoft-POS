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

kotlin {
    android {
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    sourceSets.getByName("androidDeviceTest").dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.ext.junit)
    }
}
