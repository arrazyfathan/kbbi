pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "KBBI Kamus Besar Bahasa Indonesia"
include(":app")
include(":core:data")
include(":core:domain")
include(":core:di")
include(":core:logging")
include(":core:presentation:designsystem")
include(":core:presentation:ui")
include(":core:utils")
include(":feature:bookmark:presentation")
include(":feature:detail:presentation")
include(":feature:home:data")
include(":feature:home:domain")
include(":feature:home:presentation")
include(":feature:splash:presentation")
include(":feature:words:presentation")
