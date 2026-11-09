pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PaksoftPos"

include(":composeApp", ":androidApp")
include(":core:common", ":core:model", ":core:designsystem", ":core:ui", ":core:datastore", ":core:network")
