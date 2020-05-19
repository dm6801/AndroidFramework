package com.dm6801.framework.remote

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.toMap(): Map<String, Any?> {
    return keys().asSequence().map { key ->
        var value = get(key as String)
        when {
            value is String && value.startsWith("{") ->
                value = JSONObject(value).toMap()
            value is String && value.startsWith("[") ->
                value = JSONArray(value).toList()
        }
        key to value
    }.toMap()
}

fun JSONArray.toList(): List<Any?> {
    return (0 until length()).map { get(it) }
}

@Suppress("UNCHECKED_CAST")
@Throws(JSONException::class)
fun <IN, OUT> JSONObject.getList(
    name: String,
    transform: IN.() -> OUT
): List<OUT> {
    return optJSONArray(name)?.run {
        (0 until length()).asSequence().map { (get(it) as IN).transform() }.toList()
    } ?: emptyList()
}

fun JSONObject.string(key: String): String? {
    return if (isNull(key)) null else optString(key)
}