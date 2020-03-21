package com.dm6801.framework.utilities

import android.os.Build
import android.webkit.MimeTypeMap
import com.dm6801.framework.infrastructure.foregroundApplication
import java.io.File
import java.net.URLConnection
import java.nio.file.Files

val TEMP_FILES_DIR: String?
    get() {
        return catch(silent = true) { foregroundApplication.cacheDir.toString() }
            ?: System.getProperty("java.io.tmpdir")
    }

fun File?.isNotEmpty(): Boolean {
    return try {
        this?.inputStream().use { it?.read() } != null
    } catch (_: Exception) {
        false
    }
}

val File.mimeType: String?
    get() = catch {
        var result: String?
        result = catch { inputStream().use { URLConnection.guessContentTypeFromStream(it) } }
        if (result.isNullOrBlank())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                result = Files.probeContentType(toPath())
        if (result.isNullOrBlank())
            result = URLConnection.guessContentTypeFromName(name)
        if (result.isNullOrBlank())
            result = MimeTypeMap.getSingleton()
                ?.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(absolutePath))

        if (result.isNullOrBlank()) null
        else result
    }

fun guessMimeType(fileName: String): String? {
    return MimeTypeMap.getSingleton()?.getMimeTypeFromExtension(fileName.substringAfterLast("."))
}

@Throws(Exception::class)
fun createTempFile(filename: String? = null): File {
    return File(TEMP_FILES_DIR + "/" + (filename ?: "tmp${System.nanoTime()}.tmp")).apply {
        try {
            parentFile?.mkdirs()
            createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            deleteOnExit()
        }
    }
}

@Throws(Exception::class)
fun ByteArray.toTempFile(filename: String? = null): File {
    return createTempFile(filename).apply {
        writeBytes(this@toTempFile)
    }
}

