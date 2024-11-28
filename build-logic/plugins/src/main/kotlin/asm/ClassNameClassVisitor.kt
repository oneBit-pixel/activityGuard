package com.kotlin.asm

import com.kotlin.model.ObfuscatorMapping
import com.kotlin.util.isDebug
import com.kotlin.util.logDebug
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.TypePath

/**
 * Created by DengLongFei
 * 2024/11/18
 */

class ClassNameClassVisitor(
    val apiVersion: Int,
    cv: ClassVisitor,
    private val obfuscatorMapping: ObfuscatorMapping,
) : ClassVisitor(apiVersion, cv) {
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
//        isDebug = name == "com/ndk/view/PlayView"
        println("----------visit " + " version " + version + "   " + " access " + access + "   " + " name " + name + "   " + " signature " + signature + "   " + " superName " + superName + "   " + " interfaces " + interfaces?.joinToString { it })
        super.visit(
            version,
            access,
            obfuscatorMapping.obfuscatorName(name),
            signature,
            obfuscatorMapping.obfuscatorName(superName),
            interfaces
        )
    }

    override fun visitSource(source: String?, debug: String?) {
        logDebug("----------visitSource  source $source    debug $debug obfuscatorClassSourceName ")
        super.visitSource(source, debug)
    }

    override fun visitOuterClass(owner: String?, name: String?, descriptor: String?) {
        super.visitOuterClass(
            obfuscatorMapping.obfuscatorName(owner),
            obfuscatorMapping.obfuscatorName(name),
            obfuscatorMapping.obfuscatorDescriptor(descriptor)
        )

        logDebug("----------visitOuterClass  owner $owner    name $name    descriptor $descriptor   ")
    }

    override fun visitInnerClass(
        name: String?, outerName: String?, innerName: String?, access: Int
    ) {
        super.visitInnerClass(
            obfuscatorMapping.obfuscatorName(name),
            obfuscatorMapping.obfuscatorName(outerName),
            obfuscatorMapping.obfuscatorName(innerName),
            access
        )
        logDebug(buildString {
            append("----------visitInnerClass ")
            append(" name ")
            append(name)
            append("   ")
            append(" outerName ")
            append(outerName)
            append("   ")
            append(" innerName ")
            append(innerName)
            append("   ")
            append(" access ")
            append(access)
        })
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        logDebug("----------visitAnnotation  descriptor $descriptor    visible $visible")
        // 仅处理 Kotlin Metadata 注解
        if (descriptor == "Lkotlin/Metadata;") {
            return handleAnnotation(descriptor, visible)
        }
        return super.visitAnnotation(descriptor, visible)
    }


    override fun visitTypeAnnotation(
        typeRef: Int, typePath: TypePath?, descriptor: String?, visible: Boolean
    ): AnnotationVisitor {
        logDebug("----------visitTypeAnnotation  typeRef $typeRef    typePath $typePath    descriptor $descriptor    visible $visible")
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitField(
        access: Int, name: String?, descriptor: String?, signature: String?, value: Any?
    ): FieldVisitor {

        logDebug("----------visitField  access $access    name $name    descriptor $descriptor    signature $signature    value $value")
        return super.visitField(
            access,
            name,
            obfuscatorMapping.obfuscatorDescriptor(descriptor),
            signature,
            value
        )

    }


    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        logDebug("----------visitMethod  access $access    name $name    descriptor $descriptor    signature $signature    exceptions $exceptions")
        val methodVisitor = super.visitMethod(
            access, name, obfuscatorMapping.obfuscatorDescriptor(descriptor), signature, exceptions
        )
        return ClassNameMethodVisitor(apiVersion, methodVisitor, obfuscatorMapping)
    }


    /**
     * 处理注解
     */
    private fun handleAnnotation(
        descriptor: String,
        visible: Boolean
    ): AnnotationVisitor {
        return object :
            AnnotationVisitor(apiVersion, super.visitAnnotation(descriptor, visible)) {
            override fun visit(name: String, value: Any?) {
                logDebug("----------visitAnnotation visit1  name $name    value $value" + "    " + value?.javaClass?.name)
                super.visit(name, value)
            }

            override fun visitArray(name: String): AnnotationVisitor {
                logDebug("----------visitAnnotation visitArray  name $name")
                // 处理数组类型的字段
                return object : AnnotationVisitor(apiVersion, super.visitArray(name)) {
                    override fun visit(name: String?, value: Any?) {
                        logDebug("----------visitAnnotation visit2  name $name    value $value" + "  " + value?.javaClass?.name)
                        if (value is String) {
                            super.visit(name, obfuscatorMapping.obfuscatorDescriptor(value))
                        } else {
                            super.visit(name, value)
                        }
                    }

                }
            }
        }
    }


}
