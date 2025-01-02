package com.kotlin.model

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input

/**
 * Created by DengLongFei
 * 2024/11/25
 */
open class ActivityGuardExtension {
    //是否开启
    var isEnable = true

    //白名单
    var whiteClassList = hashSetOf<String>()

    //类名混淆  com.activityGuard.a  -> a.b
    var obfuscatorClassFunction: ((String) -> String)? = null

}