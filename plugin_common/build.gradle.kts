plugins {
    id("java-library")
    kotlin("jvm")
}

ext {
    // 直接通过 project.property 读取属性
    set("mavenGroup", "com.opb.plugin.guard")
    set("mavenId", "plugin_common")
    set("mavenVersion", "1.0.0-250406-5")
}
apply(from = "${rootDir}/script/common-plugin.gradle")

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.5")
    compileOnly(kotlin("stdlib"))
    compileOnly(gradleKotlinDsl())
    compileOnly("org.ow2.asm:asm-commons:9.7.1")
    compileOnly("com.google.protobuf:protobuf-java:3.21.12")
    compileOnly("com.google.guava:guava:27.0.1-jre")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    compileOnly("com.android.tools.build:aapt2-proto:8.1.4-10154469")
    compileOnly("com.android.tools.build:bundletool:1.14.0")
    compileOnly("com.android.tools.build:gradle:4.2.0")
    compileOnly("com.android.tools:common:31.6.0")
    compileOnly("com.android.tools:sdk-common:31.1.4")
    testImplementation("junit:junit:4.13")
}



java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}









