import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import pk.paksoft.pos.buildlogic.intVersion
import pk.paksoft.pos.buildlogic.libs

/** `pos.android.application`: the Android app shell (AGP 9 built-in Kotlin) hosting the shared Compose UI. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.intVersion("android-compileSdk")
            defaultConfig {
                minSdk = libs.intVersion("android-minSdk")
                targetSdk = libs.intVersion("android-targetSdk")
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                buildConfig = true
                compose = true
            }
        }
    }
}
