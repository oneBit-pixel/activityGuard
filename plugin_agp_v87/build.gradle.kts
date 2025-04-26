plugins {
    id("java-library")
    kotlin("jvm")
}


ext {
    // 直接通过 project.property 读取属性
    set("mavenGroup", "com.opb.plugin.guard")
    set("mavenId", "plugin_agp_v87")
    set("mavenVersion", "1.0.0-250406-5")
}
apply(from = "${rootDir}/script/common-plugin.gradle")

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools.build:gradle:8.7.0")
    compileOnly("com.android.tools:common:31.6.0")
     implementation("com.opb.plugin.guard:plugin_common:1.0.0-250406-5")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}








