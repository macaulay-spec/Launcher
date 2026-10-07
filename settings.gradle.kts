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
    }
}

rootProject.name = "AstraLauncher"

include(
    ":app",
    ":core-design",
    ":core-storage",
    ":core-performance",
    ":core-platform",
    ":feature-home",
    ":feature-search",
    ":feature-apps",
    ":feature-lock-preview",
    ":feature-notifications",
    ":feature-controls",
    ":feature-widgets",
    ":feature-recents",
    ":feature-settings",
    ":feature-personalization",
    ":feature-onboarding"
)
