plugins {
    id("pos.kmp.library")
    id("pos.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":core:model"))
        api(project(":core:common"))
        implementation(project(":core:network"))
        implementation(project(":core:database"))
        implementation(project(":core:datastore"))
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kermit)
    }
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
    }
    // core:database keeps Room as `implementation`, so the in-memory database test needs it directly.
    sourceSets.iosTest.dependencies {
        implementation(libs.room.runtime)
        implementation(libs.sqlite.bundled)
    }
}
