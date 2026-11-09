import org.gradle.api.Plugin
import org.gradle.api.Project
import pk.paksoft.pos.buildlogic.kotlinMultiplatform
import pk.paksoft.pos.buildlogic.libs
import pk.paksoft.pos.buildlogic.library

/**
 * `pos.koin`: Koin Annotations checked by the Koin Compiler Plugin. Apply after `pos.kmp.library`.
 * No KSP: the compiler plugin replaces koin-ksp-compiler.
 */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("io.insert-koin.compiler.plugin")

        kotlinMultiplatform {
            sourceSets.commonMain.dependencies {
                implementation(project.dependencies.platform(libs.library("koin-bom")))
                implementation(libs.library("koin-core"))
                implementation(libs.library("koin-annotations"))
            }
        }

        pluginManager.withPlugin("org.jetbrains.compose") {
            kotlinMultiplatform {
                sourceSets.commonMain.dependencies {
                    implementation(libs.library("koin-compose"))
                    implementation(libs.library("koin-compose-viewmodel"))
                }
            }
        }
    }
}
