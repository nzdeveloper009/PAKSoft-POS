plugins {
    `kotlin-dsl`
}

group = "pk.paksoft.pos.buildlogic"

kotlin {
    jvmToolchain(17)
}

dependencies {
    // `implementation`, not `compileOnly`: the convention plugins apply and configure these plugins,
    // and the Koin plugin's extension classes must be on the runtime classpath.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.compose.compiler.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.koin.compiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "pos.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "pos.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("koin") {
            id = "pos.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("kmpFeature") {
            id = "pos.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("room") {
            id = "pos.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("androidApplication") {
            id = "pos.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
