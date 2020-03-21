package com.dm6801.framework.remote

import org.json.JSONArray
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