pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Tabala"
include(":app")

// Presentation layer
include(":ui:components")
include(":ui:theme")

// Domain layer
include(":domain:models")

// Data layer
include(":data:network")
include(":data:database")
include(":data:repository")

// Core layer
include(":core:utils")
include(":core:analytics")
 