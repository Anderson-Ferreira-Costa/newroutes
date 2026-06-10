@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.osmand.net/releases") }
        maven { url = uri("https://mvnrepository.com/artifact/org.osmdroid") }
    }
}

rootProject.name = "NewRoutes"
include(":app")
