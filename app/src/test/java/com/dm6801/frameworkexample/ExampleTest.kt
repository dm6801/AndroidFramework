package com.dm6801.frameworkexample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.BeforeClass

@Suppress("EXPERIMENTAL_API_USAGE")
class ExampleTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            Dispatchers.setMain(TestCoroutineDispatcher())
        }
    }
}