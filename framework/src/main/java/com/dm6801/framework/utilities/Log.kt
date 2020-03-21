@file:Suppress("FunctionName")

package com.dm6801.framework.utilities

import android.os.Build
import android.widget.Toast
import com.dm6801.framework.infrastructure.AbstractApplication

private const val MAX_LOG_LENGTH = 4068

val isUnitTest: Boolean
    get() {
        return Build.BRAND == null && Build.DEVICE == null && Build.PRODUCT == null
    }

fun <E : Any> E.Log(arg: Any?, level: Int = android.util.Log.DEBUG, toast: Boolean = false) {
    log(javaClass.name.substringAfterLast(".").substringBefore("$"), arg, level, toast)
}

fun <E : Any> E.Log(
    title: Any,
    arg: Any?,
    level: Int = android.util.Log.DEBUG,
    toast: Boolean = false
) {
    val isUnitTest = isUnitTest
    log(
        tag =
        if (isUnitTest) title.toString()
        else javaClass.name.substringAfterLast(".").substringBefore("$"),
        arg = if (isUnitTest) arg else "$title: $arg",
        level = level,
        toast = toast
    )
}

fun log(
    tag: String,
    arg: Any?,
    level: Int = android.util.Log.DEBUG,
    toast: Boolean = false
) {
    val string = arg.toString()
    if (string.length <= MAX_LOG_LENGTH)
        _log(tag, string, level, toast)
    else
        string.chunkedSequence(MAX_LOG_LENGTH)
            .forEach { _log(tag, it.substring(0, it.length.coerceAtMost(100)), level, toast) }
}

private fun _log(
    tag: String,
    arg: Any?,
    level: Int = android.util.Log.DEBUG,
    toast: Boolean
) {
    if (isUnitTest) {
        println("$tag: $arg")
    } else {
        val string = arg.toString()
        android.util.Log.println(
            level,
            tag,
            string
        )
        if (toast) toast(string)
    }
}

fun Any?.justify(
    prefix: String? = null,
    suffix: String? = null,
    size: Int? = null
): String {
    return if (this != null) {
        val _string = (prefix ?: "") + toString() + (suffix ?: "")
        val _length = size ?: (kotlin.math.floor(_string.length / 10f) + 1).toInt() * 10
        String.format("%-${_length}s", _string)
    } else ""
}

fun Any?.hashCode(radix: Int) = hashCode().toString(radix)

fun toast(arg: Any) {
    try {
        if (!isUnitTest)
            Toast.makeText(AbstractApplication.instance, arg.toString(), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}