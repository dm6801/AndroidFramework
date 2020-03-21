package com.dm6801.framework

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain

@Suppress("EXPERIMENTAL_API_USAGE")
open class Tests {

    companion object {
        fun setupDispatchers() {
            Dispatchers.setMain(TestCoroutineDispatcher())
        }
    }

    fun <T> runTest(block: suspend CoroutineScope.() -> T?) {
        runBlocking {
            val result = block()
            if (result is Job) result.join()
        }
    }

}