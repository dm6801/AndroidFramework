package com.dm6801.framework.utilities

inline fun <reified T : Enum<T>> enumValueOf(name: String): T? =
    catch { kotlin.enumValueOf<T>(name) }