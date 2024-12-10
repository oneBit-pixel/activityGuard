package com.kotlin

import com.android.aapt.Resources
import com.android.builder.packaging.JarFlinger
import com.kotlin.model.ActivityGuardExtension
import com.kotlin.model.ClassInfo
import com.kotlin.util.ObfuscatorUtil
import com.kotlin.util.changeLayoutXmlName
import com.kotlin.util.changeXmlNodeAttribute
import com.kotlin.util.createDirAndFile
import com.kotlin.util.mappingFileToMap
import com.kotlin.util.readByte
import com.kotlin.util.saveClassMappingFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.path.Path

/**
 * Created by DengLongFei
 * 2024/11/18
 */
abstract class ObfuscatorBundleResTask : DefaultTask() {


    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val bundleResFiles: RegularFileProperty


    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val aaptProguardFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty


    private val actGuard: ActivityGuardExtension by lazy {
        project.extensions.getByType(ActivityGuardExtension::class.java) ?: ActivityGuardExtension()
    }

    @TaskAction
    fun taskAction() {
        println("activityGuard:ResourcesObfuscatorTask")
        val outFile = outputFile.get().asFile.also { it.createNewFile() }
        val proguardFile = aaptProguardFile.get().asFile

        //保存在app目录下的mapping
        val mappingFile = project.layout.projectDirectory.file("mapping.txt").asFile
        if (!mappingFile.exists()) {
            mappingFile.createNewFile()
        }
        //生成混淆类名
        val (classMapping, dirMapping) = generateClassMapping(proguardFile, mappingFile)
        //修改layout和AndroidManifest
        obfuscatorRes(outFile, classMapping)
        //使用的映射
        val useClassMapping =
            classMapping.filter { it.value.isUse }.mapValues { it.value.obfuscatorClassName }

        //修改proguardFile文件
        replaceProguardFile(proguardFile.absolutePath, useClassMapping)

        //保存混淆后的类名文件
        saveClassMappingFile(outFile.absolutePath, useClassMapping, dirMapping)
        saveClassMappingFile(mappingFile.absolutePath, useClassMapping, dirMapping)

        //保存未使用的类名
        saveClassMappingFile(
            createDirAndFile(
                outFile.parentFile.absolutePath,
                "unused.txt"
            ).also { it.createNewFile() }.absolutePath,
            classMapping.filter { !it.value.isUse }.mapValues { it.value.obfuscatorClassName },
            mapOf()
        )
    }

    /**
     * 修改layout和AndroidManifest
     */
    private fun obfuscatorRes(outFile: File, classMapping: Map<String, ClassInfo>) {
        val dirName = outFile.parentFile.absolutePath + "/bundleRes"
        File(dirName).also {
            if (it.exists()) {
                it.delete()
            }
        }
        val bundleZip = ZipFile(bundleResFiles.get().asFile)
        //保存resources.pb
        val resourcesPb = "resources.pb"
        val resourceTableByte = readByte(bundleZip, resourcesPb)
        createDirAndFile(dirName, resourcesPb).outputStream().use { out ->
            out.write(resourceTableByte)
        }
        //修改并保存res
        bundleZip.entries().asSequence().filter { it.name.startsWith("res/") }.forEach { zipEntry ->
            val path = zipEntry.name
            if (path.startsWith("res/layout")) {
                val xmlNode = changeLayoutXmlName(bundleZip, path.toString(), classMapping)
                createDirAndFile(dirName, path.toString()).outputStream()
                    .use { xmlNode.writeTo(it) }
            } else {
                createDirAndFile(dirName, path.toString()).outputStream().use { out ->
                    out.write(readByte(bundleZip, path.toString()))
                }
            }
        }
//        //修改并保存res
//        val resourceTable = Resources.ResourceTable.parseFrom(resourceTableByte)
//        ResourcesUtils.getAllFileReferences(resourceTable).map { path ->
//            if (path.startsWith("res/layout")) {
//                val xmlNode = changeLayoutXmlName(bundleZip, path.toString(), classMapping)
//                createDirAndFile(dirName, path.toString()).outputStream()
//                    .use { xmlNode.writeTo(it) }
//            } else {
//                createDirAndFile(dirName, path.toString()).outputStream().use { out ->
//                    out.write(readByte(bundleZip, path.toString()))
//                }
//            }
//        }
        //修改并保存 AndroidManifest.xml
        val manifest = "AndroidManifest.xml"
        val xmlNode = Resources.XmlNode.parseFrom(readByte(bundleZip, manifest))
        //val newXmlNode = changeManifestActivityName(xmlNode, classMapping)
        var newXmlNode = xmlNode
        mapOf(
            "activity" to "name",
            "service" to "name",
            "application" to "name",
            "provider" to "name",
        ).forEach {
            newXmlNode = changeXmlNodeAttribute(newXmlNode, it.key, it.value, classMapping)
        }
        createDirAndFile(dirName, manifest).outputStream()
            .use { newXmlNode.writeTo(it) }
        //关闭bundleResFiles文件
        bundleZip.close()

        //保存并修改bundleResFiles
        val outBundleResFilePath = bundleResFiles.get().asFile.absolutePath
        File(outBundleResFilePath).delete()
        JarFlinger(
            Path(outBundleResFilePath),
            null
        ).use { jarCreator ->
            jarCreator.addDirectory(
                Path(dirName),
                null,
                null,
                null
            )
        }

        val outBundledResNew =
            File(outFile.parentFile.absolutePath, "bundled-res_new.aab").also { it.createNewFile() }
        JarFlinger(
            outBundledResNew.toPath(),
            null
        ).use { jarCreator ->
            jarCreator.addDirectory(
                Path(dirName),
                null,
                null,
                null
            )
        }
    }


    /**
     * 生成混淆后名称 键值对
     */
    private fun generateClassMapping(
        proguardFile: File,
        mappingFile: File
    ): Pair<LinkedHashMap<String, ClassInfo>, LinkedHashMap<String, String>> {
        //读取保存的
        val (classMapping, dirMapping) = mappingFileToMap(mappingFile)
        val classRegex = Regex("-keep class ([\\w\\.]+) \\{")
        proguardFile.readLines().forEach { line ->
            val matchResult = classRegex.find(line)
            if (matchResult != null) {
                val className = matchResult.groupValues[1]
                val obfuscatorClassName = obfuscatorClassName(className, dirMapping)
                if (obfuscatorClassName != className) {
                    classMapping[className] = ClassInfo(obfuscatorClassName, false)
                }
            }
        }
        //过滤白名单
        val mapping = classMapping.filter { !inWhiteList(it.key) } as LinkedHashMap
        return mapping to dirMapping

    }


    /**
     * 混淆后的类名
     */
    private fun obfuscatorClassName(
        className: String,
        dirMapping: MutableMap<String, String>
    ): String {
        if (className.startsWith("androidx.")
            || className.startsWith("android.")
            || className.startsWith("com.google.")
        ) {
            return className
        }
        //在白名单
        if (inWhiteList(className)) {
            return className
        }
        val lastDotIndex = className.lastIndexOf('.')
        val dirName = className.substring(0, lastDotIndex)
        val nameClass = className.substring(lastDotIndex + 1)

        //混淆目录
        val newDirName = dirMapping[dirName] ?: let {
            val newDir = generateDirName(dirName)
            dirMapping[dirName] = newDir
            newDir
        }
        //混淆类名
        val newClassName = generateClassName(nameClass)
        return "$newDirName.$newClassName"

    }


    /**
     * 生成混淆后目录
     */
    private fun generateDirName(dirName: String): String {
        return dirName
        val function = actGuard.obfuscatorDirFunction
        return function?.invoke(dirName) ?: ObfuscatorUtil.getObfuscatedClassDir(dirName)
    }

    /**
     * 生成混淆后目录
     */
    private fun generateClassName(className: String): String {
        return "Aaaaa$className"
        val function = actGuard.obfuscatorClassFunction
        return function?.invoke(className) ?: ObfuscatorUtil.getObfuscatedClassName()

    }


    //白名单正则表达式
    private val regexPatterns by lazy {
        actGuard.whiteList.map { pattern ->
            pattern
                .replace("*", ".*") // 将 '*' 替换为 '.*'（匹配零个或多个字符）
                .replace("?", ".?") // 将 '?' 替换为 '.?'（匹配零个或一个字符）
                .replace("+", ".+") // 将 '+' 替换为 '.+'（匹配一个或多个字符）
                .toRegex() // 将其转换为正则表达式
        }
    }

    /**
     * 是否在白名单中
     */
    private fun inWhiteList(className: String): Boolean {
        for (regex in regexPatterns) {
            if (regex.matches(className)) {
                return true
            }
        }
        return false
    }


    /**
     * 修改proguardFile文件
     */
    private fun replaceProguardFile(filePath: String, mapping: Map<String, String>) {
        val file = File(filePath)
        var content = file.readText()
        for ((oldText, newText) in mapping) {
            content = content.replace(" $oldText ", " $newText ")
        }
        file.writeText(content)
    }
}