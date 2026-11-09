import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import pk.paksoft.pos.buildlogic.androidLibrary
import pk.paksoft.pos.buildlogic.kotlinMultiplatform
import pk.paksoft.pos.buildlogic.libs
import pk.paksoft.pos.buildlogic.library
import pk.paksoft.pos.buildlogic.posNamespace

/** `pos.kmp.compose`: `pos.kmp.library` with Compose Multiplatform, Material 3 and Compose resources. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("pos.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        kotlinMultiplatform {
            androidLibrary {
                androidResources { enable = true }
            }

            sourceSets.commonMain.dependencies {
                implementation(libs.library("compose-runtime"))
                implementation(libs.library("compose-foundation"))
                implementation(libs.library("compose-ui"))
                implementation(libs.library("compose-material3"))
                implementation(libs.library("compose-material-icons-core"))
                implementation(libs.library("compose-components-resources"))
                implementation(libs.library("compose-ui-tooling-preview"))
            }
        }

        extensions.configure<ComposeExtension> {
            (this as ExtensionAware).extensions.configure<ResourcesExtension> {
                packageOfResClass = "$posNamespace.resources"
                // Features show strings from core:ui, so every module's Res class is public.
                publicResClass = true
            }
        }
    }
}
