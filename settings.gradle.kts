pluginManagement {
    repositories {
        google()             // Google's Maven repository for plugins
        mavenCentral()       // Central Maven repository
        gradlePluginPortal() // Gradle Plugin Portal
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Koratuwa"
include(":app")
