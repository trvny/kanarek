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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// CodeQL's default autobuild traces compiler processes. Reusing compiled task output from
// Gradle's local build cache makes a successful build look like "no source code seen".
if (System.getenv("CODEQL_ACTION_VERSION") != null) {
    buildCache {
        local {
            isEnabled = false
        }
    }
}

rootProject.name = "kanarek"
include(":app")
include(":shared")
