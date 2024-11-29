package com.kotlin

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

abstract class TransformClassTask : DefaultTask() {
    @get:InputFiles
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @Internal
    val jarPaths = mutableSetOf<String>()

    @TaskAction
    fun taskAction() {

        println("----------" + output.get().asFile.absolutePath)
        val list = arrayListOf<String>()
        val jarOutput = JarOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    output.get().asFile
                )
            )
        )
        allJars.get().forEach { file ->
            list.add("handling " + file.asFile.absolutePath)
            val jarFile = JarFile(file.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                list.add("Adding from jar ${jarEntry.name}")
                jarOutput.writeEntity(jarEntry.name, jarFile.getInputStream(jarEntry))
            }
            jarFile.close()
        }

        allDirectories.get().forEach { directory ->
            list.add("handling " + directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).getPath()
                    list.add(
                        "Adding from directory ${
                            relativePath.replace(
                                File.separatorChar,
                                '/'
                            )
                        }"
                    )
                    jarOutput.writeEntity(
                        relativePath.replace(File.separatorChar, '/'),
                        file.inputStream()
                    )
                }
            }
        }
        jarOutput.close()


    }

    private fun JarOutputStream.writeEntity(name: String, inputStream: InputStream) {
        if (jarPaths.contains(name)) {
            //printDuplicatedMessage(name)
        } else {
            putNextEntry(JarEntry(name))
            inputStream.copyTo(this)
            closeEntry()
            jarPaths.add(name)
        }
    }

    private fun JarOutputStream.writeEntity(relativePath: String, byteArray: ByteArray) {
        if (jarPaths.contains(relativePath)) {
            // printDuplicatedMessage(relativePath)
        } else {
            putNextEntry(JarEntry(relativePath))
            write(byteArray)
            closeEntry()
            jarPaths.add(relativePath)
        }
    }

    private fun printDuplicatedMessage(name: String) =
        println("Cannot add ${name}, because output Jar already has file with the same name.")
}
