package com.kotlin.util

import com.google.gson.Gson

/**
 * Created by DengLongFei
 * 2024/10/23
 */

var isDebug = false

val gson by lazy { Gson() }

inline fun <reified T> T.toJson(): String {
    return gson.toJson(this, T::class.java)
}

/**
 * json转对象
 */
inline fun <reified T> String.toObject(): T {
    return gson.fromJson<T>(this, T::class.java)
}


inline fun logDebug(log: String) {
    if (isDebug) {
        println(log)
    }
}