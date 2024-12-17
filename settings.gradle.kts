import java.net.URI

include(":model1")




pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url= URI("https://maven.aliyun.com/repository/google") }
        maven {
            url  = URI("https://maven.aliyun.com/repository/public/")
        }
        mavenCentral()
        google()
        maven { url= URI( "https://maven.google.com") }
        maven { url= URI( "https://jitpack.io") }
    }
}

rootProject.name = "ConfuseApp"
include(":app")
