package com.dm6801.framework

import com.dm6801.framework.remote.Http
import org.junit.BeforeClass
import org.junit.Test

class HttpTests : Tests() {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            setupDispatchers()
        }
    }

    @Test
    fun get() = runTest {
        Http.instance.get("http://httpbin.org/get").await()
    }

    @Test
    fun `get with arguments`() = runTest {
        Http.instance.get(
            url = "http://httpbin.org/get",
            arguments = mapOf("argument1" to arrayOf(1, 2), "argument2" to "string")
        ).await()
    }

    @Test
    fun post() = runTest {
        Http.instance.post("http://httpbin.org/post").await()
    }

    @Test
    fun `post with arguments`() = runTest {
        Http.instance.post(
            url = "http://httpbin.org/post",
            arguments = mapOf("argument1" to arrayOf(1, 2), "argument2" to "string")
        ).await()
    }

    @Test
    fun `download file`() = runTest {
        Http.instance.download(
                url = "http://httpbin.org/image/png",
                arguments = mapOf("argument1" to arrayOf(1, 2), "argument2" to "string")
            )
            .await()
    }

    @Test
    fun `multipart byte array`() = runTest {
        Http.instance.multipart(
                url = "http://httpbin.org/anything",
                arguments = mapOf(
                    "argument1" to "string",
                    "file1" to "abc".toByteArray()
                )
            )
            .await()
    }

    @Test
    fun `multipart byte array with filename`() = runTest {
        Http.instance.multipart(
                url = "http://httpbin.org/anything",
                arguments = mapOf(
                    "argument1" to "string",
                    "file1" to ("abc".toByteArray() to "file_1.txt")
                )
            )
            .await()
    }

}