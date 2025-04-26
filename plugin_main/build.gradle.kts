plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}


dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(gradleKotlinDsl())
    compileOnly("com.android.tools:common:31.6.0")
    compileOnly("com.android.tools:sdk-common:31.1.4")
    testImplementation("junit:junit:4.13")

    implementation("com.opb.plugin.guard:plugin_agp_v74:1.0.0-250406-5")
    implementation("com.opb.plugin.guard:plugin_common:1.0.0-250406-5")
    implementation("com.opb.plugin.guard:plugin_agp_v86:1.0.0-250406-5")
    implementation("com.opb.plugin.guard:plugin_agp_v87:1.0.0-250406-5")
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
ext {
    // 直接通过 project.property 读取属性
    set("mavenGroup", "com.opb.plugin")
    set("mavenId", "activityGurad")
    set("mavenVersion", "1.0.2")
}
apply(from = "${rootDir}/script/common-plugin.gradle")




gradlePlugin {
    plugins {
        create("actGuardPlugin") {
            id = "com.opb.plugin"
            implementationClass = "com.kotlin.ActivityGuardPlugin"
            version = "1.0.2"
        }
    }
}






