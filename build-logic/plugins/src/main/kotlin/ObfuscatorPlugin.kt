package com.kotlin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.res.Aapt2FromMaven.Companion.create
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.services.getBuildService
import com.android.build.gradle.internal.utils.setDisallowChanges
import com.kotlin.model.ActivityGuardExtension
import com.kotlin.model.ObfuscatorMapping
import com.kotlin.util.createDirAndFile
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Created by DengLongFei
 * 2024/11/18
 * actGuard
 */
class ObfuscatorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("actGuard", ActivityGuardExtension::class.java)
        project.plugins.withType(AppPlugin::class.java) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val artifacts = variant.artifacts as? ArtifactsImpl ?: return@onVariants
                if (!variant.isMinifyEnabled || !variant.shrinkResources) {
                    println("activityGuard:Not executed, please open isMinifyEnabled shrinkResources")
                    return@onVariants
                }

                //资源Bundle混淆
                val taskBundleName = "activityGuard${variant.name.capitalized()}BundleResTask"
                println("activityGuard: start executed...$taskBundleName")
                val taskBundleProvider =
                    project.tasks.register<ObfuscatorBundleResTask>(taskBundleName)
                taskBundleProvider.configure {
                    it.bundleResFiles.set(artifacts.get(InternalArtifactType.LINKED_RES_FOR_BUNDLE))
                    it.aaptProguardFile.set(artifacts.get(InternalArtifactType.AAPT_PROGUARD_FILE))
                    it.outputFile.set(project.layout.buildDirectory.file("intermediates/${taskBundleName}/mapping.txt"))
                }

                //混淆apk资源
                val taskApkName = "activityGuard${variant.name.capitalized()}ApkResTask"
                val taskApkProvider = project.tasks.register<ObfuscatorApkTask>(taskApkName)
                val transformationRequest = variant.artifacts.use(taskApkProvider)
                    .wiredWithDirectories(
                        ObfuscatorApkTask::inputProcessedRes,
                        ObfuscatorApkTask::outputProcessedRes
                    )
                    .toTransformMany(InternalArtifactType.PROCESSED_RES)
                taskApkProvider.configure {
                    it.aapt2.let { aapt2Input ->
                        aapt2Input.buildService.setDisallowChanges(
                            getBuildService(project.gradle.sharedServices)
                        )
                        aapt2Input.threadPoolBuildService.setDisallowChanges(
                            getBuildService(project.gradle.sharedServices)
                        )
                        val aapt2Bin =
                            create(project) { option -> System.getenv(option.propertyName) }
                        aapt2Input.binaryDirectory.setFrom(aapt2Bin.aapt2Directory)
                        aapt2Input.version.setDisallowChanges(aapt2Bin.version)
                        aapt2Input.maxWorkerCount.setDisallowChanges(
                            project.gradle.startParameter.maxWorkerCount
                        )
                    }
                    it.temDirectory.set(project.layout.buildDirectory.dir("intermediates/${taskApkName}"))
                    it.transformationRequest.set(transformationRequest)
                    it.classMapping.set(
                        taskBundleProvider.flatMap { task ->
                            task.outputFile.map { out ->
                                val file = out.asFile
                                fileToClassMappingMap(file,false)
                            }
                        })
                }
                //混淆class
                val transformClassTask =
                    project.tasks.register<ObfuscatorClassTask>("activityGuard${variant.name}ClassTask")
                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(transformClassTask)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        ObfuscatorClassTask::allJars,
                        ObfuscatorClassTask::allDirectories,
                        ObfuscatorClassTask::output
                    )
                transformClassTask.configure { params ->
                    params.logFile.set(
                        createDirAndFile(
                            project.layout.buildDirectory.get().asFile.absolutePath,
                            "intermediates/${taskBundleName}/transformLog.txt"
                        )
                    )
                    params.classMapping.set(
                        taskBundleProvider.flatMap {
                            it.outputFile.map { out ->
                                val file = out.asFile
                                fileToClassMappingMap(file)
                            }
                        })
                }


            }
        }
    }

    /**
     * 读取混淆后规则为map
     */
    private fun fileToClassMappingMap(
        file: File,
        isReplace: Boolean = true
    ): HashMap<String, String> {
        val hashMap = hashMapOf<String, String>()
        file.forEachLine { line ->
            if (line.contains("->")) {
                val parts = line.split("->").map { it.trim() }
                if (parts.size == 2) {
                    val (original, obfuscated) = if (isReplace) {
                        val original = parts[0].replace(".", "/")
                        val obfuscated = parts[1].replace(".", "/")
                        original to obfuscated
                    } else {
                        parts[0] to parts[1]
                    }
                    hashMap[original] = obfuscated
                    //兼容butterKnife
                    hashMap[original + "_ViewBinding"] = obfuscated + "_ViewBinding"
                }
            }
        }
        return hashMap
    }
}