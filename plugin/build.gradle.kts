plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.9.0"
}


dependencies {
    compileOnly("com.google.code.gson:gson:2.8.5")
    compileOnly(kotlin("stdlib"))
    compileOnly(gradleKotlinDsl())
    //compileOnly("org.ow2.asm:asm-util:9.7")
    compileOnly("org.ow2.asm:asm-commons:9.7.1")
    compileOnly("com.google.protobuf:protobuf-java:3.21.12")
    compileOnly("com.google.guava:guava:27.0.1-jre")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    compileOnly("com.android.tools.build:aapt2-proto:8.1.4-10154469")
    compileOnly("com.android.tools.build:bundletool:1.14.0")
    compileOnly("com.android.tools.build:gradle:8.1.4")
    compileOnly("com.android.tools:common:31.6.0")
    compileOnly("com.android.tools:sdk-common:31.1.4")
    testImplementation("junit:junit:4.13")
}





java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val mVersion = project.property("mVersion").toString()
group = "com.github.denglongfei"
version = mVersion
gradlePlugin {
    plugins {
        create("actGuardPlugin") {
            id = "activityGuard"
            group = "com.github.denglongfei"
            description = "activityGuard"
            version = mVersion
            implementationClass = "com.kotlin.ObfuscatorPlugin"
        }
    }
}
publishing {
    publications {
        register("release", MavenPublication::class) {
            from(components["java"])
            groupId = "com.github.denglongfei"
            artifactId = "activityGuard"
            version = mVersion
        }
        repositories {
            maven { url = uri("../repo") }
        }
    }
}









