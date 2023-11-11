package com.drcorchit.justice.utils.json

import com.drcorchit.justice.utils.IOUtils
import com.drcorchit.justice.utils.createCache
import com.drcorchit.justice.utils.json.JsonUtils.Companion.GSON
import com.drcorchit.justice.utils.stream
import com.google.common.cache.LoadingCache
import com.google.common.collect.ImmutableSet
import com.google.gson.*
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collector
import java.util.stream.Stream

class JsonUtils {
    companion object {
        @JvmStatic
        val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

        private val JSON_CACHE: LoadingCache<String, JsonElement> =
            createCache(1000) { path: String -> parseJsonFromAnywhere(path) }

        @JvmStatic
        fun parseFromUrl(url: String): JsonElement {
            return JSON_CACHE.getUnchecked(url)
        }

        private fun parseJsonFromAnywhere(path: String): JsonElement {
            return JsonParser.parseString(String(IOUtils.loadFileFromAnywhere(path).second))
        }

        @JvmStatic
        fun clearCache() {
            JSON_CACHE.invalidateAll()
        }

        @JvmStatic
        fun clearCache(key: String) {
            JSON_CACHE.invalidate(key)
        }

        @JvmStatic
        fun toArray(): Collector<JsonElement, *, JsonArray> {
            return TO_ARRAY
        }

        @JvmStatic
        fun toObject(): Collector<Pair<String, JsonElement>, *, JsonObject> {
            return TO_OBJECT
        }
    }
}

private val TO_ARRAY = object : Collector<JsonElement, JsonArray, JsonArray> {
    override fun supplier(): Supplier<JsonArray> {
        return Supplier<JsonArray> { JsonArray() }
    }

    override fun accumulator(): BiConsumer<JsonArray, JsonElement> {
        return BiConsumer<JsonArray, JsonElement> { obj: JsonArray, element: JsonElement -> obj.add(element) }
    }

    override fun combiner(): BinaryOperator<JsonArray> {
        return BinaryOperator<JsonArray> { arr1: JsonArray, arr2: JsonArray ->
            arr1.addAll(arr2)
            arr1
        }
    }

    override fun finisher(): Function<JsonArray, JsonArray> {
        return Function.identity()
    }

    override fun characteristics(): Set<Collector.Characteristics> {
        return ImmutableSet.of(Collector.Characteristics.IDENTITY_FINISH)
    }
}

private val TO_OBJECT = object : Collector<Pair<String, JsonElement>, JsonObject, JsonObject> {
    override fun supplier(): Supplier<JsonObject> {
        return Supplier<JsonObject> { JsonObject() }
    }

    override fun accumulator(): BiConsumer<JsonObject, Pair<String, JsonElement>> {
        return BiConsumer<JsonObject, Pair<String, JsonElement>> { obj: JsonObject, pair: Pair<String, JsonElement> ->
            obj.add(pair.first, pair.second)
        }
    }

    override fun combiner(): BinaryOperator<JsonObject> {
        return BinaryOperator<JsonObject> { obj1: JsonObject, obj2: JsonObject ->
            obj2.entrySet().forEach { obj1.add(it.key, it.value) }
            obj1
        }
    }

    override fun finisher(): Function<JsonObject, JsonObject> {
        return Function.identity()
    }

    override fun characteristics(): Set<Collector.Characteristics> {
        return ImmutableSet.of(Collector.Characteristics.IDENTITY_FINISH)
    }
}

fun JsonElement.prettyPrint(): String {
    return GSON.toJson(this)
}

fun <T> JsonObject.getOrDefault(key: String, def: () -> T, rule: (JsonElement) -> T): T {
    return if (this.has(key)) rule.invoke(this[key]) else def.invoke()
}

fun JsonObject.getBool(key: String, def: Boolean): Boolean {
    return this.getOrDefault(key, { def }, { it.asBoolean })
}

fun JsonObject.getInt(key: String, def: Number): Int {
    return this.getOrDefault(key, { def.toInt() }, { it.asInt })
}

fun JsonObject.getLong(key: String, def: Number): Long {
    return this.getOrDefault(key, { def.toLong() }, { it.asLong })
}

fun JsonObject.getDouble(key: String, def: Number): Double {
    return this.getOrDefault(key, { def.toDouble() }, { it.asDouble })
}

fun JsonObject.getString(key: String, def: String): String {
    return this.getOrDefault(key, { def }, { it.asString })
}

fun JsonObject.getArray(key: String): JsonArray {
    return this.getOrDefault(key, { JsonArray() }, { it.asJsonArray })
}

fun JsonObject.getObject(key: String): JsonObject {
    return this.getOrDefault(key, { JsonObject() }, { it.asJsonObject })
}

fun JsonArray.stream(): Stream<JsonElement> {
    return stream(this, this.size().toLong())
}

fun Map<String, JsonElement>.toJsonObject(): JsonObject {
    val output = JsonObject()
    this.entries.forEach { output.add(it.key, it.value) }
    return output
}

fun Iterable<JsonElement>.toJsonArray(): JsonArray {
    val output = JsonArray()
    this.forEach { output.add(it) }
    return output
}