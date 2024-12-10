plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.9.0"

}


repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.5")
    implementation(kotlin("stdlib"))
    implementation(gradleKotlinDsl())
    implementation("org.ow2.asm:asm-util:9.7")
    implementation("com.google.protobuf:protobuf-java:3.21.12")
    implementation("com.google.guava:guava:27.0.1-jre")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    testImplementation("junit:junit:4.13")

    compileOnly("com.android.tools.build:aapt2-proto:8.1.4-10154469")
    compileOnly("com.android.tools.build:bundletool:1.14.0")
    compileOnly("com.android.tools.build:gradle:8.1.4")
    implementation("com.android.tools:common:31.6.0")


}


gradlePlugin {
    plugins {
        create("actGuardPlugin") {
            id = "com.obfuscator.activityGuard"
            implementationClass = "com.kotlin.ObfuscatorPlugin"
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("publishToMavenLocal") {
            from(components["java"])
            groupId = "com.obfuscator"
            artifactId = "activityGuard"
            version = "1.0.0"
        }
        repositories {
            mavenLocal()
        }
    }


//    publications {
//        create<MavenPublication>("publishToMavenService") {
//            from(components["java"])
//            groupId = "com.obfuscator.activityGuard"
//            artifactId = "activityGuard"
//            version = "1.0.0"
//        }
//
//        repositories {
//            maven {
//                isAllowInsecureProtocol = true
//                url = uri("http://192.168.3.241:8081/repository/maven-releases/")
//                credentials {
//                    username = "admin"
//                    password = "Bnic2SLEf"
//                }
//            }
//        }
//    }


}








