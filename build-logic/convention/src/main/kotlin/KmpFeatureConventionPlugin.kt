import org.gradle.api.Plugin
import org.gradle.api.Project
import pk.paksoft.pos.buildlogic.kotlinMultiplatform
import pk.paksoft.pos.buildlogic.libs
import pk.paksoft.pos.buildlogic.library

/** `pos.kmp.feature`: a feature module (Compose UI, Koin, serializable Navigation 3 keys, the core modules). */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("pos.kmp.compose")
        pluginManager.apply("pos.koin")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        kotlinMultiplatform {
            sourceSets.commonMain.dependencies {
                implementation(project(":core:common"))
                implementation(project(":core:model"))
                implementation(project(":core:data"))
                implementation(project(":core:designsystem"))
                implementation(project(":core:ui"))
                implementation(libs.library("lifecycle-viewmodel-compose"))
                implementation(libs.library("lifecycle-runtime-compose"))
                implementation(libs.library("navigation3-ui"))
            }
            sourceSets.commonTest.dependencies {
                implementation(project(":core:testing"))
            }
        }
    }
}
