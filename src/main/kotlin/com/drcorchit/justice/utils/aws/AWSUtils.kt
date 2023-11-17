package com.drcorchit.justice.utils.aws

import com.amazonaws.services.dynamodbv2.document.Item
import com.drcorchit.justice.utils.json.JsonUtils.toJsonArray
import com.drcorchit.justice.utils.json.JsonUtils.toJsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object AWSUtils {
    @JvmStatic
    fun parseS3Url(url: String): Pair<String, String> {
        if (url.startsWith("s3://")) {
            val remain = url.substring(5)
            val slashPos = remain.indexOf("/")
            return Pair(remain.substring(0, slashPos), remain.substring(slashPos + 1))
        }
        throw IllegalArgumentException()
    }

    @JvmStatic
    fun isS3Url(url: String): Boolean {
        return url.matches("[sS]3://\\w+(/\\w+)+".toRegex())
    }

    @JvmStatic
    fun dynamoDBItemToJson(item: Item): JsonObject {
        return objectToJson(item.asMap()).asJsonObject
    }

    private fun objectToJson(input: Any?): JsonElement {
        if (input == null) return JsonNull.INSTANCE
        return when (input) {
            is Map<*, *> -> {
                input.mapKeys { it.key as String }.mapValues { objectToJson(it.value) }.toJsonObject()
            }

            is List<*> -> input.map { objectToJson(it) }.toJsonArray()
            is String -> JsonPrimitive(input)
            is Number -> JsonPrimitive(input)
            is Boolean -> JsonPrimitive(input)
            else -> throw IllegalArgumentException("Unknown JSON component: ${input.javaClass}")
        }
    }
}