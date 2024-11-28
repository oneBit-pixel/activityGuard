package com.kotlin.util

/**
 * Created by DengLongFei
 * 2024/11/27
 */
object ObfuscatorUtil {
    private val usedNames = mutableMapOf<String, String>() // 原始段 -> 混淆段的映射
    private val allGeneratedNames = mutableSetOf<String>()
    private val letters = ('a'..'z')+ ('A'..'Z') // 仅字母字符集
    private val charset = letters + ('0'..'9') // 全字符集，首字母需限制为字母

    /**
     * 生成随机类名
     */
    private fun generateRandomName(length: Int = 5): String {
        while (true) {
            val className = buildString {
                append(letters.random())
                repeat(length - 1) { append(charset.random()) }
            }
            // 确保生成的类名唯一
            if (allGeneratedNames.add(className)) {
                return className
            }
        }
    }


    /**
     * 获取混淆目录
     */
    fun getObfuscatedClassDir(originalPath: String, length: Int = 4): String {
        val obfuscatedSegments = originalPath.split(".").map { item ->
            usedNames.getOrPut(item) { generateRandomName(length) }
        }
        return obfuscatedSegments.joinToString(".")
    }


    /**
     * 获取混淆名称
     */
    fun getObfuscatedClassName(length: Int = 4): String {
        return generateRandomName(length)
    }

}