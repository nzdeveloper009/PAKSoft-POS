import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import pk.paksoft.pos.buildlogic.kotlinMultiplatform
import pk.paksoft.pos.buildlogic.libs
import pk.paksoft.pos.buildlogic.library

/** `pos.room`: Room with the bundled SQLite driver. Apply after `pos.kmp.library`. KSP is used only here. */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        kotlinMultiplatform {
            sourceSets.commonMain.dependencies {
                implementation(libs.library("room-runtime"))
                implementation(libs.library("sqlite-bundled"))
            }
        }

        dependencies {
            val roomCompiler = libs.library("room-compiler")
            add("kspAndroid", roomCompiler)
            add("kspIosArm64", roomCompiler)
            add("kspIosSimulatorArm64", roomCompiler)
        }
    }
}
