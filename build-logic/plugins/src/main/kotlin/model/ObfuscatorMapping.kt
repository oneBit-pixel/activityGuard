package com.kotlin.model

import com.kotlin.util.logDebug
import com.kotlin.util.toJson
import org.objectweb.asm.Type

/**
 * Created by DengLongFei
 * 2024/11/25
 */
class ObfuscatorMapping(
    private val classMapping: Map<String, String> = mapOf()
) {


    /**
     * 混淆类名
     */
    fun obfuscatorName(name: String?): String? {
        return name.toObfuscatorName()
    }

    /**
     * 混淆类型描述
     */
    fun obfuscatorDescriptor(name: String?): String? {
        return name.toObfuscatorDescriptor()
    }

    /**
     * 混淆类型
     */
    fun obfuscatorType(type: Type): Type {
        return type.toObfuscatorType()
    }


    /**
     * 获取混淆后类名
     */
    private fun String?.toObfuscatorName(): String? {
        val name = this
        name ?: return null
        return if (name.contains("$")) {
            name.split("$").joinToString("$") {
                classMapping[it] ?: it
            }
        } else {
            classMapping[name] ?: name
        }.also {
            logDebug("----------getObfuscatorName  old  $name    new $it")
        }

    }

    /**
     * 获取混淆后描述符
     */
    private fun String?.toObfuscatorDescriptor(): String? {
        val descriptor = this
        descriptor ?: return null
//        val regex = Regex("L([a-zA-Z0-9&_/]+);")
        val regex = Regex("L([^;]+);")
        return regex.replace(descriptor) { matchResult ->
            val originalClass = matchResult.groupValues[1]
            val replacedClass = originalClass.toObfuscatorName()
            "L$replacedClass;" // 返回替换后的类描述符
        }
    }


    /**
     * 混淆类名或者描述符
     */
    private fun obfuscatorDescriptorOrName(name: String?): String? {
        name ?: return null
        val regex = Regex("L([^;]+);")
        return if (regex.matches(name)) {
            return regex.replace(name) { matchResult ->
                val originalClass = matchResult.groupValues[1]
                val replacedClass = originalClass.toObfuscatorName()
                "L$replacedClass;" // 返回替换后的类描述符
            }
        } else {
            name.toObfuscatorName()
        }

    }


    /**
     * 修改类型
     */
    private fun Type.toObfuscatorType(): Type {
        return when (this.sort) {
            Type.METHOD -> {
                // 方法类型，需要解析参数和返回类型
                val argumentTypes = Type.getArgumentTypes(this.descriptor)
                val returnType = Type.getReturnType(this.descriptor)
                logDebug("-----argumentTypes " + argumentTypes.toJson())
                logDebug("-----returnType " + returnType.toJson())
                // 替换参数和返回类型
                val newArgumentTypes = argumentTypes.map { type ->
                    type.toObfuscatorType()
                }.toTypedArray()
                val newReturnType = returnType.toObfuscatorType()
                // 使用新参数类型和返回类型构造新的方法类型
                Type.getMethodType(newReturnType, *newArgumentTypes)
            }

            Type.OBJECT, 12 -> {
                val newClassName = this.className.replace('.', '/').toObfuscatorName()
                return Type.getObjectType(newClassName)
            }

            else -> {
                this
            }
        }
    }

}