package com.kotlin

import com.android.build.gradle.internal.services.Aapt2Input
import com.kotlin.handle.HandleAaptProguardFile
import com.kotlin.handle.HandleBundleResFile
import com.kotlin.model.ActivityGuardExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Created by DengLongFei
 * 2024/12/27
 */
abstract class ActivityGuardBundleTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val bundleResFiles: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val aaptProguardFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    private val actGuard: ActivityGuardExtension by lazy {
        project.extensions.getByType(ActivityGuardExtension::class.java)
    }

    @TaskAction
    fun taskAction() {
        val outFile = outputFile.get().asFile.also { it.createNewFile() }
        val taskDir = File(outFile.parentFile.absolutePath)

        //aaptProguardFile文件处理
        val handleAaptProguardFile =
            HandleAaptProguardFile(project, aaptProguardFile.get().asFile, taskDir, actGuard)
        //生成混淆类名
        val (classMapping, dirMapping) = handleAaptProguardFile.getClassMapping()

        //bundleResFiles处理
        val handleBundleResFile = HandleBundleResFile(bundleResFiles.get().asFile, taskDir)
        handleBundleResFile.obfuscatorRes(classMapping)

        //替换aaptProguardFile
        handleAaptProguardFile.replaceProguardFile(classMapping, dirMapping)


    }
}
