package util

import com.android.tools.r8.graph.A
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier
import java.io.File

/**
 * Created by DengLongFei
 * 2024/12/26
 */
fun getAGPVersion(project: Project): String {
    val agpVersion =
        project.rootProject.buildscript.configurations.getByName(ScriptHandler.CLASSPATH_CONFIGURATION)
            .resolvedConfiguration
            .resolvedArtifacts
            .asSequence()
            .mapNotNull { artifact ->
                val identifier = artifact.id.componentIdentifier
                if (identifier is DefaultModuleComponentIdentifier &&
                    (identifier.group == "com.android.tools.build"
                            || identifier.group.hashCode() == 432891823) &&
                    identifier.module == "gradle"
                ) {
                    identifier.version
                } else null
            }.firstOrNull()

    return agpVersion ?: throw GradleException("Failed to get AGP version")
}
/**
 * 获取任务文件属性
 */
fun getProperty(project: Project, taskName: String, propertyName: String): Any? {
    val task = project.tasks.getByName(taskName)
    val propertyObject = task.property(propertyName)
    return propertyObject
}

/**
 * 获取任务文件属性
 */
fun getRegularFileProperty(project: Project, taskName: String, propertyName: String): File {
    val task = project.tasks.getByName(taskName)
    val propertyObject = task.property(propertyName)
    val propertyGet =
        propertyObject!!::class.java.getMethod("get").invoke(propertyObject)
    return propertyGet::class.java.getMethod("getAsFile").invoke(propertyGet) as File
}

/**
 * 获取任务目录属性
 */
fun getDirectoryProperty(project: Project, taskName: String, propertyName: String): File {
    val task = project.tasks.getByName(taskName)
    val propertyObject = task.property(propertyName)
    val propertyGet =
        propertyObject!!::class.java.getMethod("get").invoke(propertyObject)
    return propertyGet::class.java.getMethod("getAsFile").invoke(propertyGet) as File
}


fun getBundleResFile(project: Project, variantName: String): File {
    //InternalArtifactType.LINKED_RES_FOR_BUNDLE
    //LinkAndroidResForBundleTask
    val agpVersion = getAGPVersion(project)
    return when {
        //大于等于 8.6.0
        compareVersions(agpVersion, "8.6.0") != -1 -> {
            val taskName = "bundle" + variantName + "Resources"
            getDirectoryProperty(project, taskName, "linkedResourcesOutputFile")
        }

        else -> {
            val taskName = "bundle" + variantName + "Resources"
            getRegularFileProperty(project, taskName, "bundledResFile")
        }
    }.also {
        println("------------getBundleResFile $agpVersion $it")
    }
}

fun getApkResFiles(project: Project, variantName: String): File {
    //InternalArtifactType.PROCESSED_RES
    //LinkApplicationAndroidResourcesTask
    val agpVersion = getAGPVersion(project)
    return when {
        //大于等于 8.6.0
        compareVersions(agpVersion, "8.6.0") != -1 -> {
            val taskName = "process" + variantName + "Resources"
            getDirectoryProperty(project, taskName, "linkedResourcesOutputDir")
        }

        else -> {
            val taskName = "process" + variantName + "Resources"
            getDirectoryProperty(project, taskName, "resPackageOutputFolder")
        }
    }.also {
        println("------------getApkResFiles $agpVersion $it")
    }


}

fun getAptProguardFile(project: Project, variantName: String): File {
    //InternalArtifactType.AAPT_PROGUARD_FILE
    //LinkApplicationAndroidResourcesTask
    val taskName = "process" + variantName + "Resources"
    return getRegularFileProperty(project, taskName, "proguardOutputFile").also {
        println("------------getAptProguardFile $it")
    }
}


fun compareVersions(version1: String, version2: String): Int {
    // 将版本号拆分为整数列表
    val parts1 = version1.split(".").map {
        try {
            it.toInt()
        } catch (e: Exception) {
            0
        }
    }
    val parts2 = version2.split(".").map {
        try {
            it.toInt()
        } catch (e: Exception) {
            0
        }
    }
    // 找到最长的长度
    val maxLength = maxOf(parts1.size, parts2.size)
    // 逐一比较每个部分
    for (i in 0 until maxLength) {
        val v1 = parts1.getOrNull(i) ?: 0
        val v2 = parts2.getOrNull(i) ?: 0

        if (v1 < v2) return -1 // version1 小于 version2
        if (v1 > v2) return 1  // version1 大于 version2
    }
    return 0 // 相等
}


