package com.dm6801.framework.utilities

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
inline fun <reified E : Enum<E>> enumValue(name: String): E? = catch {
    enumValues<E>().find { it.name.toLowerCase() == name.toLowerCase() }
}