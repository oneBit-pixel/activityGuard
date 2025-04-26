buildscript {
    repositories {
        maven {
            isAllowInsecureProtocol = true
            url = uri("http://localhost:8081/repository/maven-releases/")
            credentials {
                username = "zxy"
                password = "123456"
            }
        }
    }
    dependencies {
        classpath("com.opb.plugin:activityGurad:1.0.2")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
}



