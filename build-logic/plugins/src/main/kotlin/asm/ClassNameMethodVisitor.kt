package com.kotlin.asm

import com.kotlin.model.ObfuscatorMapping
import com.kotlin.util.logDebug
import com.kotlin.util.toJson
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

/**
 * Created by DengLongFei
 * 2024/11/25
 * 处理方法
 */
class ClassNameMethodVisitor(
    apiVersion: Int,
    methodVisitor: MethodVisitor,
    private val obfuscatorMapping: ObfuscatorMapping,
) : MethodVisitor(apiVersion, methodVisitor) {


    override fun visitFrame(
        type: Int,
        numLocal: Int,
        local: Array<out Any>?,
        numStack: Int,
        stack: Array<out Any>?
    ) {
        logDebug("----------visitFrame " + " type " + type + "   " + " numLocal " + numLocal + "   " + " local " + local.toJson() + "   " + " numStack " + numStack + "   " + " stack " + stack.toJson() + "   ")
        val newLocal = local?.map {
            if (it is String) {
                obfuscatorMapping.obfuscatorName(it)
            } else {
                it
            }
        }?.toTypedArray()
        val newStack = stack?.map {
            if (it is String) {
                obfuscatorMapping.obfuscatorName(it)
            } else {
                it
            }
        }?.toTypedArray()
        super.visitFrame(type, numLocal, newLocal, numStack, newStack)
    }

    //访问 try catch 块。
    override fun visitTryCatchBlock(
        start: Label?, end: Label?, handler: Label?, type: String?
    ) {

        logDebug("----------visitTryCatchBlock " + " start " + start + "   " + " end " + end + "   " + " handler " + handler.toJson() + "   " + " type " + type)
        super.visitTryCatchBlock(start, end, handler, type)
    }


    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
        logDebug("----------visitInvokeDynamicInsn " + " name " + name + "   " + " descriptor " + descriptor + "   " + " bootstrapMethodHandle " + bootstrapMethodHandle + "   " + "\n bootstrapMethodArguments " + bootstrapMethodArguments.toJson())
        val newBootstrapMethodArguments =
            obfuscatorBootstrapMethodArguments(bootstrapMethodArguments)
        logDebug("-----------sss1 " + bootstrapMethodArguments.toJson())
        logDebug("-----------sss2 " + newBootstrapMethodArguments.toJson())
        super.visitInvokeDynamicInsn(
            name, obfuscatorMapping.obfuscatorDescriptor(descriptor), bootstrapMethodHandle?.let {
                Handle(
                    it.tag,
                    obfuscatorMapping.obfuscatorName(it.owner),
                    it.name,
                    obfuscatorMapping.obfuscatorDescriptor(it.desc)
                )
            }, *newBootstrapMethodArguments
        )
    }

    /**
     * 修改引导方法参数
     */
    private fun obfuscatorBootstrapMethodArguments(bootstrapMethodArguments: Array<out Any?>): Array<Any?> {
        val newBootstrapMethodArguments = bootstrapMethodArguments.map {
            when (it) {
                is Type -> {
                    obfuscatorMapping.obfuscatorType(it)
                }

                is Handle -> {
                    Handle(
                        it.tag,
                        obfuscatorMapping.obfuscatorName(it.owner),
                        it.name,
                        obfuscatorMapping.obfuscatorDescriptor(it.desc)
                    )
                }

                else -> it
            }

        }.toTypedArray()
        return newBootstrapMethodArguments
    }


    override fun visitLocalVariable(
        name: String?,
        descriptor: String?,
        signature: String?,
        start: Label?,
        end: Label?,
        index: Int
    ) {

        logDebug(
            "----------visitLocalVariable  name $name    descriptor $descriptor    signature $signature    start $start end $end  index $index"
        )
        super.visitLocalVariable(
            name, obfuscatorMapping.obfuscatorDescriptor(descriptor), signature, start, end, index
        )
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        // 类型引用
        super.visitTypeInsn(opcode, obfuscatorMapping.obfuscatorName(type))
        logDebug("----------visitTypeInsn  opcode $opcode    type $type")
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(
            opcode,
            obfuscatorMapping.obfuscatorName(owner),
            name,
            obfuscatorMapping.obfuscatorDescriptor(descriptor),
            isInterface
        )

        logDebug(
            "----------visitMethodInsn  opcode $opcode    owner $owner    name $name    descriptor $descriptor    isInterface $isInterface"
        )
    }

    override fun visitFieldInsn(
        opcode: Int, owner: String?, name: String?, descriptor: String?
    ) {
        super.visitFieldInsn(
            opcode,
            obfuscatorMapping.obfuscatorName(owner),
            name,
            obfuscatorMapping.obfuscatorDescriptor(descriptor)
        )
        logDebug(
            "----------visitFieldInn  opcode $opcode    owner $owner    name $name    descriptor $descriptor"
        )
    }
}