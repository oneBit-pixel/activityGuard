package com.kotlin.handle

import com.android.aapt.Resources
import com.android.builder.packaging.JarFlinger
import com.kotlin.model.ClassInfo
import util.changeLayoutXmlName
import util.changeXmlNodeAttribute
import util.createDirAndFile
import util.readByte
import java.io.File
import java.util.zip.ZipFile
import kotlin.io.path.Path

/**
 * Created by DengLongFei
 * 2024/12/26
 */
object HandleApkResFile {



    /**
     * 修改layout和AndroidManifest
     */
     fun obfuscatorRes(
        inputFile: File,
        classMapping: Map<String, ClassInfo>
    ) {
        val dirName = inputFile.parentFile.absolutePath + "/bundleRes" + inputFile.name
        File(dirName).also {
            if (it.exists()) {
                it.delete()
            }
        }
        val bundleZip = ZipFile(inputFile)
        bundleZip.entries().asSequence().forEach { zipEntry ->
            val path = zipEntry.name
            when {
                path == "resources.pb" -> {
                    val resourceTableByte = readByte(bundleZip, path)
                    createDirAndFile(dirName, path).outputStream().use { out ->
                        out.write(resourceTableByte)
                    }
                }

                path.startsWith("res/layout") -> {
                    val xmlNode = changeLayoutXmlName(bundleZip, path.toString(), classMapping)
                    createDirAndFile(dirName, path.toString()).outputStream()
                        .use { xmlNode.writeTo(it) }
                }

                path == "AndroidManifest.xml" -> {
                    val xmlNode = Resources.XmlNode.parseFrom(readByte(bundleZip, path))
                    var newXmlNode = xmlNode
                    mapOf(
                        "activity" to "name",
                        "service" to "name",
                        "application" to "name",
                        "provider" to "name",
                    ).forEach {
                        newXmlNode =
                            changeXmlNodeAttribute(newXmlNode, it.key, it.value, classMapping)
                    }
                    createDirAndFile(dirName, path).outputStream()
                        .use { newXmlNode.writeTo(it) }
                }

                else -> {
                    createDirAndFile(dirName, path.toString()).outputStream().use { out ->
                        out.write(readByte(bundleZip, path.toString()))
                    }
                }
            }
        }
        //关闭bundleResFiles文件
        bundleZip.close()
        //保存并修改bundleResFiles
        val outBundleResFilePath = inputFile.absolutePath
        File(outBundleResFilePath).delete()
        JarFlinger(
            Path(outBundleResFilePath),
            null
        ).use { jarCreator ->
            jarCreator.addDirectory(
                Path(dirName)
            )
        }

    }
}