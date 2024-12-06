package com.kotlin.asm

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.kotlin.model.ObfuscatorMapping
import com.kotlin.util.LogFileUtil
import com.kotlin.util.logFileUtil
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.objectweb.asm.ClassVisitor
import java.io.File

/**
 * Created by DengLongFei
 * 2024/11/18
 */
abstract class ClassNameTransform :
    AsmClassVisitorFactory<ClassNameTransform.ClassNameTransformParams> {
    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        if (logFileUtil == null) {
            logFileUtil = LogFileUtil(parameters.get().logFile.outputStream())
        }
        return ClassNameClassVisitor(
            instrumentationContext.apiVersion.get(),
            nextClassVisitor,
            ObfuscatorMapping(parameters.get().classMapping.get())
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return true
    }


    interface ClassNameTransformParams : InstrumentationParameters {
        @get:Input
        val classMapping: MapProperty<String, String>

        @get:Internal
        var logFile: File
    }


}

