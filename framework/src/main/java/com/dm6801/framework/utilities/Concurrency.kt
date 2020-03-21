@file:Suppress("unused")

package com.dm6801.framework.utilities

import com.dm6801.framework.infrastructure.AbstractApplication
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

val coRoutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

fun runOnUiThread(block: () -> Unit) {
    AbstractApplication.activity?.runOnUiThread(block)
}

fun main(block: suspend CoroutineScope.() -> Unit): Job =
    CoroutineScope(Dispatchers.Main).safeLaunch(block)

fun background(block: suspend CoroutineScope.() -> Unit): Job =
    CoroutineScope(Dispatchers.IO).safeLaunch(block)

suspend fun <T> CoroutineScope.withMain(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Main + coRoutineExceptionHandler, block)

suspend fun <T> CoroutineScope.withBackground(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO + coRoutineExceptionHandler, block)

fun delay(
    ms: Long,
    dispatcher: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
): Job =
    CoroutineScope(dispatcher).safeLaunch {
        delay(ms)
        block()
    }

fun schedule(
    period: Long,
    delay: Long = 0,
    timeout: Long = 0,
    dispatcher: CoroutineContext = Dispatchers.Main,
    block: suspend CoroutineScope.() -> Unit
): Job = CoroutineScope(Dispatchers.IO).safeLaunch {
    if (timeout == 0L) {
        if (delay != 0L) delay(delay)
        while (true) {
            withContext(dispatcher + coRoutineExceptionHandler, block)
            delay(period)
        }
    } else {
        withTimeout(timeout) {
            withContext(dispatcher + coRoutineExceptionHandler, block)
        }
    }
}

fun CoroutineScope.safeLaunch(
    work: suspend CoroutineScope.() -> Unit
): Job {
    return launch(this.coroutineContext + coRoutineExceptionHandler, block = work)
}

private val continuationExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        if (throwable !is IllegalStateException)
            throwable.printStackTrace()
    }

fun <T> Continuation<T>.safeResume(result: Result<T>) =
    CoroutineScope(context + continuationExceptionHandler).launch {
        try {
            resumeWith(result)
        } catch (e: Exception) {
            throw e
        }
    }