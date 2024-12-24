package com.kotlin.util

import com.kotlin.model.ClassInfo

/**
 * Created by DengLongFei
 * 2024/12/16
 */
class ObfuscatorUtil(private val obfuscatorClassFunction: ((String) -> String)?) {
    private val alphabet = "abcdefghijklmnopqrstuvwxyz"
    private val digitAlphabet = "abcdefghijklmnopqrstuvwxyz0123456789"


    // 目录 和 混淆后目录
    private var dirMapping = LinkedHashMap<String, String>()

    // 原目录名称为key  对应下的类名和混淆（不包含目录的
    private val dirAndClassList = LinkedHashMap<String, LinkedHashMap<String, String>>()

    fun initMap(
        classMapping: LinkedHashMap<String, ClassInfo>,
        dirMapping: LinkedHashMap<String, String>
    ) {
        this.dirMapping = dirMapping
        classMapping.forEach {
            val (dir, name) = getClassDirAndName(it.key)
            val (obfuscatorDir, obfuscatorName) = getClassDirAndName(it.value.obfuscatorClassName)
            dirAndClassList.getOrPut(dir) { LinkedHashMap() }[name] = obfuscatorName
            dirMapping[dir] = obfuscatorDir
        }
    }

    /**
     *  com.activityGuard.a to a.b
     */
    fun getObfuscatedClassName(name: String): String {
        if (obfuscatorClassFunction != null) {
            return obfuscatorClassFunction.invoke(name)
        }
        val (dir, name) = getClassDirAndName(name)
        val newDir = dirMapping[dir] ?: let {
            val tem = generateDirName(dir)
            addDirMappingItem(dir, tem)
            tem
        }
        val newName = generateClassName(dir, name)
        dirAndClassList.getOrPut(dir) { linkedMapOf() }[name] = newName
        return if (newDir.isNotEmpty()) {
            "$newDir.$newName"
        } else {
            "$newName"
        }
    }


    private fun generateDirName(dir: String): String {
        return generateName(dirMapping.size, minLength = 2, charset = alphabet)
    }

    private fun generateClassName(dir: String, name: String): String {
        val size = dirAndClassList.getOrPut(dir) { linkedMapOf() }.size
        return generateName(size, minLength = 1, charset = digitAlphabet)
    }


    private fun addDirMappingItem(dir: String, tem: String) {
        dirMapping[dir] = tem
        dirAndClassList.getOrPut(dir) { linkedMapOf() }
    }


    private fun generateName(counter: Int, minLength: Int, charset: String): String {
        val sb = StringBuilder()
        var value = counter
        value += (minLength-1) * charset.length
        //首为字母
        val firstCharIndex = value % alphabet.length
        sb.append(alphabet[firstCharIndex])
        value /= alphabet.length

        while (value > 0) {
            val charIndex = value % charset.length
            sb.append(charset[charIndex])
            value /= charset.length
        }
        return sb.toString()
    }

}



