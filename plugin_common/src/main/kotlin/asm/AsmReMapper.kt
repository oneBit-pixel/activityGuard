package com.kotlin.asm

import org.objectweb.asm.commons.Remapper


/**
 * Created by DengLongFei
 * 2024/12/23
 */
class AsmReMapper(
    private val classMapping: Map<String, String> = mapOf()
) : Remapper() {
    override fun map(name: String?): String? {
        return obfuscatorDescriptorOrName(name)
    }

    override fun mapValue(value: Any?): Any? {
        return if (value is String) {
            map(value)
        } else {
            super.mapValue(value)
        }
    }

    /**
     * 混淆类名或者描述符
     */
    @Synchronized
    fun obfuscatorDescriptorOrName(name: String?): String? {
        name ?: return null
        return if (name.startsWith("L") || name.startsWith("(")
            || name.startsWith("[") || name.startsWith("<")
        ) {
            name.toObfuscatorDescriptor()
        } else {
            name.toObfuscatorName()
        }
    }

    /**
     * 获取混淆后描述符
     */
    private fun String?.toObfuscatorDescriptor(): String? {
        val descriptor = this
        descriptor ?: return null
        val regex = Regex("L([a-zA-Z0-9_/]+/)([a-zA-Z0-9_$]+)")
        return regex.replace(descriptor) { matchResult ->
            val path = matchResult.groups[1]?.value // 捕获路径部分
            val className = matchResult.groups[2]?.value // 捕获类名部分
            val replacedClass = (path + className).toObfuscatorName()
            "L$replacedClass" // 返回替换后的类描述符
        }
    }

    /**
     * 获取混淆后类名
     */
    private fun String?.toObfuscatorName(): String? {
        val name = this
        name ?: return null
        return if (name.contains("$")) {
            name.split("$")
                .joinToString("$") { classMapping[it] ?: it }
        } else {
            classMapping[name] ?: name
        }

    }
}