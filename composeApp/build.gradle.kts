plugins {
    id("pos.kmp.compose")
    id("pos.koin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    // The Kotlin package is pk.paksoft.pos.app, not the path-derived pk.paksoft.pos.composeApp.
    android {
        namespace = "pk.paksoft.pos.app"
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Koin's generated code sits outside pk.paksoft.pos.app, so the compiler can't infer it.
            binaryOption("bundleId", "pk.paksoft.pos.app.ComposeApp")
        }
    }

    sourceSets.commonMain.dependencies {
        // androidApp builds the AppConfig it passes to initKoin().
        api(project(":core:model"))
        implementation(project(":core:common"))
        implementation(project(":core:designsystem"))
        implementation(project(":core:ui"))
        implementation(project(":core:datastore"))
        implementation(project(":core:network"))
        implementation(project(":core:database"))
        implementation(project(":core:data"))
        implementation(project(":feature:auth"))
        implementation(project(":feature:settings"))
        implementation(libs.lifecycle.viewmodel.compose)
        implementation(libs.lifecycle.runtime.compose)
        implementation(libs.lifecycle.viewmodel.navigation3)
        implementation(libs.navigation3.ui)
    }
    sourceSets.commonTest.dependencies {
        implementation(project(":core:testing"))
    }
}

compose.resources {
    packageOfResClass = "pk.paksoft.pos.app.resources"
}
