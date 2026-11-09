package pk.paksoft.pos.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("No library '$alias' in libs.versions.toml") }

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).orElseThrow { IllegalStateException("No version '$alias' in libs.versions.toml") }
        .requiredVersion.toInt()

/** Android namespace derived from the module path: `:core:ui` → `pk.paksoft.pos.core.ui`. */
internal val Project.posNamespace: String
    get() = "pk.paksoft.pos." + path.removePrefix(":").replace(':', '.')

internal fun Project.kotlinMultiplatform(action: KotlinMultiplatformExtension.() -> Unit) =
    extensions.configure(action)

/** The `kotlin { android { … } }` target added by `com.android.kotlin.multiplatform.library`. */
internal fun KotlinMultiplatformExtension.androidLibrary(action: KotlinMultiplatformAndroidLibraryTarget.() -> Unit) =
    (this as ExtensionAware).extensions.configure(action)
