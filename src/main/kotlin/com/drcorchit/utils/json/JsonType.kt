package com.drcorchit.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.util.*

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
        fun of(`in`: JsonElement?): JsonType {
            if (`in` == null || `in`.isJsonNull) return NULL
            if (`in`.isJsonObject) return OBJECT
            return if (`in`.isJsonArray) ARRAY else of(`in`.asJsonPrimitive)
        }

        fun of(`in`: JsonPrimitive): JsonType {
            if (`in`.isString) return STRING
            if (`in`.isNumber) return NUMBER
            if (`in`.isBoolean) return BOOLEAN
            throw IllegalArgumentException(`in`.toString())
        }

        fun of(`in`: String): JsonType? {
            return try {
                valueOf(`in`.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                null
            }
        }
    }
}