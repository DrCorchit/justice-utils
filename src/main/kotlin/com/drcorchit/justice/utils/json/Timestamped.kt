package com.drcorchit.justice.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonParser

typealias TimestampedJson = Pair<JsonElement, Long>

val TimestampedJson.info get() = first
val TimestampedJson.lastModified get() = second

fun TimestampedJson.toBytes(): TimestampedBytes {
    return info.toString().encodeToByteArray() to lastModified
}

typealias TimestampedBytes = Pair<ByteArray, Long>

val TimestampedBytes.bytes get() = first
val TimestampedBytes.timestamp get() = second

fun TimestampedBytes.toJson(): TimestampedJson {
    return JsonParser.parseString(String(bytes)) to timestamp
}