package com.dm6801.framework.remote

import com.dm6801.framework.BuildConfig
import com.dm6801.framework.infrastructure.hideProgressBar
import com.dm6801.framework.infrastructure.showProgressBar
import com.dm6801.framework.utilities.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.net.*
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets

@Suppress(
    "BlockingMethodInNonBlockingContext",
    "DeferredIsResult",
    "SameParameterValue",
    "LocalVariableName"
)
class Http {

    companion object {
        val instance: Http by lazy { Http() }
    }

    var log: Boolean = BuildConfig.DEBUG || isUnitTest

    fun get(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null,
        progressBar: Boolean = true,
        isProgressBarBlocking: Boolean = true
    ) = CoroutineScope(Dispatchers.IO).async<Result<String>> {
        if (progressBar) showProgressBar(isProgressBarBlocking)
        return@async try {
            Result.success(internalGet(url, arguments, headers))
        } catch (t: Throwable) {
            t.printStackTrace()
            if (progressBar) hideProgressbar()
            Result.failure(HttpException(t))
        } finally {
            if (progressBar) hideProgressbar()
        }
    }

    @Throws(Exception::class)
    internal fun internalGet(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null
    ): String {
        val tag = "get"
        log(tag, "request", url, headers, arguments)
        return url.toURL(query = arguments?.toQueryString()).openConnection { connection ->
            headers?.forEach(connection::addHeader)
            connection.useCaches = false
            connection.responseOkOrThrow(tag, url)
            connection.inputStream?.bufferedReader()?.use { reader ->
                reader.readText()
                    .also { log(tag, "response", url, "\n$it") }
            } ?: throw Exception("could not read response")
        }
    }

    fun post(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null,
        progressBar: Boolean = true,
        isProgressBarBlocking: Boolean = true
    ) = CoroutineScope(Dispatchers.IO).async<Result<String>> {
        if (progressBar) showProgressBar(isProgressBarBlocking)
        return@async try {
            Result.success(internalPost(url, arguments, headers))
        } catch (t: Throwable) {
            t.printStackTrace()
            if (progressBar) hideProgressbar()
            Result.failure(HttpException(t))
        } finally {
            if (progressBar) hideProgressbar()
        }
    }

    @Throws(Exception::class)
    @Suppress("SpellCheckingInspection")
    internal fun internalPost(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null
    ): String {
        val tag = "post"
        log(tag, "request", url, headers, arguments)
        return URL(url).openConnection { connection ->
            headers?.forEach(connection::addHeader)
            connection.requestMethod = "POST"
            connection.useCaches = false
            connection.doInput = true
            connection.doOutput = true
            arguments?.toQueryString()?.takeIf { it.isNotBlank() }?.let { query ->
                connection.outputStream?.bufferedWriter()?.use { writer ->
                    writer.write(query)
                    writer.flush()
                }
            } ?: connection.outputStream.close()
            connection.responseOkOrThrow(tag, url)
            connection.inputStream?.bufferedReader()?.use { reader ->
                reader.readText()
                    .also { log(tag, "response", url, "\n$it") }
            } ?: throw Exception("could not read response")
        }
    }

    fun download(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null,
        progressBar: Boolean = true,
        isProgressBarBlocking: Boolean = true
    ): Deferred<Result<File>> = CoroutineScope(Dispatchers.IO).async<Result<File>> {
        if (progressBar) showProgressBar(isProgressBarBlocking)
        return@async try {
            Result.success(internalDownload(url, arguments, headers))
        } catch (t: Throwable) {
            t.printStackTrace()
            if (progressBar) hideProgressbar()
            Result.failure(HttpException(t))
        } finally {
            if (progressBar) hideProgressbar()
        }
    }

    @Throws(Exception::class)
    internal fun internalDownload(
        url: String,
        arguments: Map<String, Any?>? = null,
        headers: Map<String, Any?>? = null
    ): File {
        val tag = "download"
        log(tag, "request", url, headers, arguments)
        return URL(url).openConnection { connection ->
            headers?.forEach(connection::addHeader)
            connection.useCaches = false
            connection.connect()
            connection.responseOkOrThrow(tag, url)
            val file = connection.inputStream?.use { inputStream ->
                var filename = ""
                val disposition = connection.getHeaderField("Content-Disposition")
                val contentType = connection.contentType
                val contentLength = connection.contentLength
                if (disposition != null)
                    filename = disposition.substringAfterLast("filename=")
                if (filename.isBlank())
                    filename = url.substringAfterLast("/")
                log(tag, "response", filename, "length=$contentLength\ttype=$contentType")
                val file = createTempFile(filename)
                file.deleteOnExit()
                file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
                if (log) {
                    file.inputStream().use {
                        val sample = ByteArray(30)
                        it.read(sample, 0, 30.coerceAtMost(file.length().toInt()))
                        log(tag, "file", file.toString(), "\n" + sample.joinToString(",") + "...")
                    }
                }
                file
            }
            file ?: throw Exception("could not read response")
        }
    }

    fun multipart(
        url: String,
        arguments: Map<String, Any?>,
        headers: Map<String, Any?>? = null,
        progressBar: Boolean = true,
        isProgressBarBlocking: Boolean = true
    ) = CoroutineScope(Dispatchers.IO).async<Result<String>> {
        if (progressBar) showProgressBar(isProgressBarBlocking)
        return@async try {
            Result.success(internalMultipart(url, arguments, headers))
        } catch (t: Throwable) {
            t.printStackTrace()
            if (progressBar) hideProgressbar()
            Result.failure(HttpException(t))
        } finally {
            if (progressBar) hideProgressbar()
        }
    }

    @Throws(Exception::class)
    fun internalMultipart(
        url: String,
        arguments: Map<String, Any?>,
        headers: Map<String, Any?>? = null
    ) = internalMultipart(url, null, arguments, headers)

    @Throws(Exception::class)
    @Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
    fun internalMultipart(
        url: String,
        chunked: Int?,
        arguments: Map<String, Any?>,
        headers: Map<String, Any?>? = null
    ): String {
        val tag = "multipart"
        log(tag, "request", url, headers, arguments)
        val boundary = System.currentTimeMillis().toString()
        return URL(url).openConnection { connection ->
            connection.addRequestProperty("Accept", "*/*")
            connection.addRequestProperty("Accept-Charset", "UTF-8")
            connection.addRequestProperty("Accept-Encoding", "gzip, deflate, br")
            connection.addRequestProperty("Connection", "Keep-Alive")
            connection.addRequestProperty("Cache-Control", "no-cache")
            connection.addRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            chunked?.let(connection::setChunkedStreamingMode)
            connection.useCaches = false
            connection.doInput = true
            connection.doOutput = true
            connection.addParts(arguments, boundary)
            connection.responseOkOrThrow(tag, url)
            connection.inputStream?.bufferedReader()?.use { reader ->
                reader.readText()
                    .also { log(tag, "response", url, "\n$it") }
            } ?: throw Exception("could not read response")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun URLConnection.addParts(parts: Map<String, Any?>, boundary: String) = catch {
        if (parts.isEmpty()) {
            getOutputStream().close()
            return@catch
        }
        getOutputStream().bufferedWriter().use { writer ->
            parts.forEach { (field, value) ->
                when (value) {
                    is String -> writer.addFormField(field, value, boundary)
                    is File -> writer.addFile(field, value, boundary)
                    is ByteArray -> writer.addFile(field, value.toTempFile(), boundary)
                    else ->
                        (value as? Pair<ByteArray, String>)?.let { (byteArray, filename) ->
                            writer.addFile(
                                field,
                                byteArray.toTempFile(filename),
                                boundary,
                                filename,
                                guessMimeType(filename)
                            )
                        } ?: run {
                            writer.addFormField(field, value.toString(), boundary)
                        }
                }
            }
            writer.append("--$boundary--\r\n")
            writer.flush()
        }
    }

    private fun BufferedWriter.addFormField(field: String, value: Any, boundary: String) {
        append(
            "--$boundary\r\n" +
                    "Content-Disposition: form-data; name=\"$field\"\r\n" +
                    "\r\n" +
                    "$value\r\n"
        )
        flush()
    }

    private fun BufferedWriter.addFile(
        field: String,
        file: File,
        boundary: String,
        fileName: String? = null,
        mimeType: String? = null
    ) = catch {
        append(
            "--$boundary\r\n" +
                    "Content-Disposition: form-data; name=\"$field\"; filename=\"${fileName ?: file.name}\"\r\n" +
                    "Content-Type: ${mimeType ?: file.mimeType ?: "text/plain"}\r\n" +
                    "\r\n"
        )
        flush()
        file.useLines { lines ->
            val iterator = lines.iterator()
            while (true) {
                val line = iterator.next()
                if (iterator.hasNext()) {
                    append("$line\n")
                    flush()
                } else {
                    append("$line\r\n")
                    flush()
                    break
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun <T> URL.openConnection(block: (HttpURLConnection) -> T): T {
        var connection: HttpURLConnection? = null
        try {
            connection = openConnection() as HttpURLConnection
            return block(connection)
        } finally {
            connection?.disconnect()
        }
    }

    @Throws(HttpException::class)
    private fun HttpURLConnection.responseOkOrThrow(
        tag: String,
        url: String
    ) {
        val responseCode = responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            val error = errorStream?.bufferedReader()?.use { reader ->
                reader.readText().also { log(tag, "error", url, it) }
            } ?: ""
            throw HttpException(error, responseCode)
        }
    }

    private fun String.toURL(query: String? = null): URL {
        return URL(if (query.isNullOrBlank()) this else "$this?$query")
    }

    private fun Map<String, Any?>.toQueryString(): String? {
        return if (isNotEmpty())
            this.entries.joinToString("&") { entry ->
                val value = when (val value = entry.value) {
                    is Array<*> -> value.toQueryString()
                    is Iterable<*> -> value.toQueryString()
                    else -> value.toString().urlEncode()
                }
                "${entry.key.urlEncode()}=$value"
            }
        else null
    }

    private fun Array<*>.toQueryString(): String {
        return "[${joinToString(",") { it.toString().urlEncode() }}]"
    }

    private fun Iterable<*>.toQueryString(): String {
        return "[${joinToString(",") { it.toString().urlEncode() }}]"
    }

    private suspend fun showProgressBar(isBlocking: Boolean) {
        withContext(Dispatchers.Main) {
            showProgressBar(
                isContent = true,
                isBlocking = isBlocking
            )
        }
    }

    private suspend fun hideProgressbar() {
        withContext(Dispatchers.Main) { hideProgressBar() }
    }

    private fun String.justify(size: Int = 30): String = String.format("%-${size}s", this)

    private fun log(
        type: String,
        direction: String,
        url: String,
        headers: Map<String, Any?>? = null,
        args: Map<String, Any?>? = null
    ) {
        if (log)
            Log(
                type.justify(10) + direction.justify(10) + url.justify(50) + (headers?.toString()
                    ?.justify(
                        50
                    ) ?: "") + (args?.toString()?.justify(50) ?: "")
            )
    }

    private fun log(
        type: String,
        direction: String,
        url: String,
        result: String
    ) {
        if (log)
            Log(type.justify(10) + direction.justify(10) + url.justify(50) + result.justify(50))
    }

}

open class HttpException : Exception {
    constructor(code: Int? = null) : super("code=$code")

    constructor(
        message: String? = null,
        code: Int? = null
    ) : super(code?.let { "code=$code\t$message" } ?: message)

    constructor(cause: Throwable? = null, code: Int? = null) : super(code?.toString(), cause)

    constructor(
        message: String? = null,
        cause: Throwable? = null,
        code: Int? = null
    ) : super(code?.let { "code=$code\t$message" } ?: message, cause)
}

fun Deferred<Result<String>>.onResult(
    onFailure: ((Throwable?) -> Unit)? = null,
    onSuccess: (String) -> Unit
) = background {
    val result = await()
    if (result.isSuccess)
        result.getOrNull()
            ?.let { main { onSuccess(it) } }
            ?: onFailure?.invoke(result.exceptionOrNull())
    else
        onFailure?.invoke(result.exceptionOrNull())
}

fun String.urlEncode(charset: String = StandardCharsets.UTF_8.name()): String {
    return URLEncoder.encode(this, charset)
}

fun String.urlDecode(charset: String = StandardCharsets.UTF_8.name()): String {
    return URLDecoder.decode(this, charset)
}

private fun HttpURLConnection.addHeader(entry: Map.Entry<String, Any?>) {
    addRequestProperty(entry.key, entry.value.toString())
}

fun JSONArray.iterator(): Iterator<JSONObject> =
    (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()