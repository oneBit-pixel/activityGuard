package com.kotlin.model

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

/**
 * Created by DengLongFei
 * 2024/11/25
 */
abstract class ActivityGuardExtension {

    @get:Input
    abstract val enable: Property<Boolean>

    @get:Input
    abstract val whiteClassList: SetProperty<String>

    //类名混淆  com.activityGuard.a  -> a.b
    var obfuscatorClassFunction: ((String) -> String)? = null

}