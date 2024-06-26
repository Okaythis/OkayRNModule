package com.rnokaysdk.utils

import com.facebook.react.bridge.ReadableMap
import kotlinx.serialization.json.*

fun Any?.toJsonElement(): JsonElement {
  return when (this) {
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is String -> JsonPrimitive(this)
    is Array<*> -> this.toJsonArray()
    is List<*> -> this.toJsonArray()
    is Map<*, *> -> this.toJsonObject()
    is JsonElement -> this
    else -> JsonNull
  }
}

fun Array<*>.toJsonArray(): JsonArray {
  val array = mutableListOf<JsonElement>()
  this.forEach { array.add(it.toJsonElement()) }
  return JsonArray(array)
}

fun List<*>.toJsonArray(): JsonArray {
  val array = mutableListOf<JsonElement>()
  this.forEach { array.add(it.toJsonElement()) }
  return JsonArray(array)
}

fun Map<*, *>.toJsonObject(): JsonObject {
  val map = mutableMapOf<String, JsonElement>()
  this.forEach {
    if (it.key is String) {
      map[it.key as String] = it.value.toJsonElement()
    }
  }
  return JsonObject(map)
}

fun ReadableMap.getBooleanOrNull(key: String): Boolean? = takeIf { hasKey(key) }?.run { getBoolean(key) }
fun ReadableMap.getIntOrNull(key: String): Int? = takeIf { hasKey(key) }?.run { getInt(key) }

