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

        maven {
            isAllowInsecureProtocol = true
            url = uri("http://localhost:8081/repository/maven-releases/")
            credentials {
                username = "zxy"
                password = "123456"
            }
        }

    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }


        maven {
            isAllowInsecureProtocol = true
            url = uri("http://localhost:8081/repository/maven-releases/")
            credentials {
                username = "zxy"
                password = "123456"
            }
        }
    }
}

rootProject.name = "activityGuard"
include(":app")
include(":model1")

include(":plugin_main")
include(":plugin_common")
include(":plugin_agp_v74")
include(":plugin_agp_v86")
include(":plugin_agp_v87")
