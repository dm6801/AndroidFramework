@file:Suppress("LocalVariableName")

package com.dm6801.framework.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dm6801.framework.infrastructure.foregroundActivity
import com.dm6801.framework.infrastructure.foregroundApplication

private val context: Context get() = foregroundActivity ?: foregroundApplication.applicationContext

fun openWebBrowser(url: String) = catch {
    val _url =
        if (!url.startsWith("http://") && !url.startsWith("https://")) "http://$url" else url
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(_url))
        .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
}

@SuppressLint("MissingPermission")
fun call(phone: String) = catch {
    foregroundActivity?.apply {
        ensurePermissions(mapOf(
            Manifest.permission.CALL_PHONE to {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
            }
        ))
    }
}

@SuppressLint("MissingPermission")
fun dial(phone: String) = catch {
    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
}