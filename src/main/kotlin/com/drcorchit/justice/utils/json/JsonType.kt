package com.drcorchit.justice.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

//Used by GenericValidator and ImmutableJson
enum class JsonType {
    ANY,  //Represents a map from strings to values

    //Certain keys and their values may be
    //known in advance. Values may be of different types.
    OBJECT,  //Similar to OBJECT, but keys are not

    //expected to be known in advance and values
    //should be of the same type.
    DICTIONARY, ARRAY, NUMBER, STRING, BOOLEAN, NULL;

    fun matches(other: JsonType): Boolean {
        if (this == ANY || other == ANY) return true
        if (this == OBJECT && other == DICTIONARY) return true
        return if (this == DICTIONARY && other == OBJECT) true else this == other
    }

    companion object {
        @JvmStatic
        fun of(input: JsonElement?): JsonType {
            if (input == null || input.isJsonNull) return NULL
            if (input.isJsonObject) return OBJECT
            return if (input.isJsonArray) ARRAY else of(input.asJsonPrimitive)
        }

        @JvmStatic
        fun of(input: JsonPrimitive): JsonType {
            if (input.isString) return STRING
            if (input.isNumber) return NUMBER
            if (input.isBoolean) return BOOLEAN
            throw IllegalArgumentException(input.toString())
        }

        @JvmStatic
        fun of(input: String): JsonType? {
            return try {
                valueOf(input.uppercase())
            } catch (e: Exception) {
                null
            }
        }
    }
}