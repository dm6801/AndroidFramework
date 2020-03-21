package com.dm6801.framework.utilities

fun <E, R> E.catch(silent: Boolean = false, action: E.() -> R): R? {
    return try {
        action()
    } catch (t: Throwable) {
        if (!silent) t.printStackTrace()
        null
    }
}

fun <R> catch(silent: Boolean = false, action: () -> R): R? {
    return try {
        action()
    } catch (t: Throwable) {
        if (!silent) t.printStackTrace()
        null
    }
}