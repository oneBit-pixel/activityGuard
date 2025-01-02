package com.kotlin

import com.android.build.gradle.internal.tasks.BaseTask
import com.kotlin.handle.HandleClassFile
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Created by DengLongFei
 * 2024/12/26
 */
abstract class ActivityGuardClassTask : BaseTask() {

    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Input
    abstract val classMapping: MapProperty<String, String>


    @TaskAction
    fun taskAction() {
        //class文件处理
        val handleClassFile = HandleClassFile(
            allJars.get(),
            allDirectories.get(),
            output.get().asFile,
            classMapping.get()
        )
        handleClassFile.chaneClassFile()

    }


}