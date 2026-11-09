import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import pk.paksoft.pos.buildlogic.androidLibrary
import pk.paksoft.pos.buildlogic.intVersion
import pk.paksoft.pos.buildlogic.kotlinMultiplatform
import pk.paksoft.pos.buildlogic.libs
import pk.paksoft.pos.buildlogic.library
import pk.paksoft.pos.buildlogic.posNamespace

/** `pos.kmp.library`: a Kotlin Multiplatform module for Android and iOS (device + Apple-silicon simulator). */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")

        kotlinMultiplatform {
            jvmToolchain(17)

            androidLibrary {
                namespace = posNamespace
                compileSdk = libs.intVersion("android-compileSdk")
                minSdk = libs.intVersion("android-minSdk")
                withHostTest { }
            }
            iosArm64()
            iosSimulatorArm64()

            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }

            sourceSets.commonTest.dependencies {
                implementation(libs.library("kotlin-test"))
                implementation(libs.library("kotlinx-coroutines-test"))
                implementation(libs.library("turbine"))
            }
        }

        tasks.withType<KotlinNativeSimulatorTest>().configureEach {
            device.set(providers.gradleProperty("pos.iosTestDevice").orElse("iPhone 17 Pro"))
        }
    }
}
