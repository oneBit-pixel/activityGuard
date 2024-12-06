package com.kotlin.util

import com.google.gson.Gson
import java.io.File

/**
 * Created by DengLongFei
 * 2024/10/23
 */

var isDebug = true

val gson by lazy { Gson() }

inline fun <reified T> T.toJson(): String {
    return try {
//        return  this.toString()
        gson.toJson(this, T::class.java)
    } catch (e: Exception) {
        this.toString()
    }
}

/**
 * json转对象
 */
inline fun <reified T> String.toObject(): T {
    return gson.fromJson<T>(this, T::class.java)
}

var logFileUtil: LogFileUtil? = null

fun logDebug(log: String) {
    if (isDebug) {
        logFileUtil?.sendLog(log)
    }
}

var logFile: File? = null



