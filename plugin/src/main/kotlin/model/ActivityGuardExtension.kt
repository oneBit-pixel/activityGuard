package com.kotlin.model

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

/**
 * Created by DengLongFei
 * 2024/11/25
 */
abstract class ActivityGuardExtension {
    /**
     * Whether to enable the main logic.
     *
     * Defaults to `true`.
     */
    @get:Input
    abstract val enable: Property<Boolean>

    @get:Input
    abstract val whiteClassList: SetProperty<String>

    //类名混淆
    var obfuscatorClassFunction: ((String, String) -> String)? = null

    //目录混淆
    var obfuscatorDirFunction: ((String) -> String)? = null
}