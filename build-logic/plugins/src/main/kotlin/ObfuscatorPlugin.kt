package com.kotlin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.artifact.impl.ArtifactsImpl
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.kotlin.asm.ClassNameTransform
import com.kotlin.model.ActivityGuardExtension
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

                if (variant.outputs.size != 1) {
                    println("activityGuard:Not executed, please close splits")
                    return@onVariants
                }
                //资源混淆
                val taskName = "activityGuard${variant.name.capitalized()}"
                println("activityGuard: start executed...$taskName")
                val taskProvider = project.tasks.register<ResourcesObfuscatorTask>(taskName)
                taskProvider.configure {
                    it.bundleResFiles.set(artifacts.get(InternalArtifactType.LINKED_RES_FOR_BUNDLE))
                    it.aaptProguardFile.set(artifacts.get(InternalArtifactType.AAPT_PROGUARD_FILE))
                    it.outputFile.set(project.layout.buildDirectory.file("intermediates/${taskName}/mapping.txt"))
                }

                //注册asm
                variant.instrumentation.transformClassesWith(
                    ClassNameTransform::class.java,
                    InstrumentationScope.ALL
                ) { params ->
                    params.classMapping.set(
                        taskProvider.flatMap {
                            it.outputFile.map { out ->
                                val file = out.asFile
                                fileToClassMappingMap(file)
                            }
                        })

                }
                variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                    .use(project.tasks.register<TransformClassTask>("${variant.name}TransformTask"))
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        TransformClassTask::allJars,
                        TransformClassTask::allDirectories,
                        TransformClassTask::output
                    )


            }
        }
    }

    /**
     * 读取混淆后规则为map
     */
    private fun fileToClassMappingMap(file: File): HashMap<String, String> {
        val hashMap = hashMapOf<String, String>()
        file.forEachLine { line ->
            if (line.contains("->")) {
                val parts = line.split("->").map { it.trim() }
                if (parts.size == 2) {
                    val original = parts[0].replace(".", "/")
                    val obfuscated = parts[1].replace(".", "/")
                    hashMap[original] = obfuscated
                }
            }
        }
        return hashMap
    }
}