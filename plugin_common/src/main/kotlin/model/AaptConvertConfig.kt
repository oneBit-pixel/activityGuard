package com.kotlin.model

import com.android.utils.FileUtils
import com.google.common.collect.ImmutableList
import java.io.File

data class AaptConvertConfig(
    val inputFile: File,
    val outputFile: File,
    val convertToProtos: Boolean = false
)
fun makeConvertCommand(config: AaptConvertConfig): ImmutableList<String> {
    val builder = ImmutableList.builder<String>()

    builder.add("--output-format")
    if (config.convertToProtos) {
        builder.add("proto")
    } else {
        builder.add("binary")
    }

    FileUtils.mkdirs(config.outputFile.parentFile)
    builder.add("-o").add(config.outputFile.absolutePath)

    builder.add(config.inputFile.absolutePath)

    return builder.build()
}