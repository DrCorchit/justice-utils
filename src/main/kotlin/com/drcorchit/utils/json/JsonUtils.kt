package com.drcorchit.utils.json

import com.drcorchit.utils.createCache
import com.drcorchit.utils.stream
import com.drcorchit.utils.loadFileFromAnywhere
import com.drcorchit.utils.whitelist
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
        fun toArray(): Collector<JsonElement, *, JsonArray> {
            return TO_ARRAY
        }

        @JvmStatic
        fun toObject(): Collector<Pair<String, JsonElement>, *, JsonObject> {
            return TO_OBJECT
        }
    }
}

val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
private val JSON_CACHE: LoadingCache<String, JsonElement> =
    createCache(1000) { path: String -> parseJsonFromAnywhere(path) }

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
        return BiConsumer<JsonObject, Pair<String, JsonElement>> { obj: JsonObject, pair: Pair<String?, JsonElement?> ->
            obj.add(
                pair.first,
                pair.second
            )
        }
    }

    override fun combiner(): BinaryOperator<JsonObject> {
        return BinaryOperator<JsonObject> { obj1: JsonObject, obj2: JsonObject ->
            obj2.entrySet().forEach(
                Consumer<Map.Entry<String?, JsonElement?>> { (key, value): Map.Entry<String?, JsonElement?> ->
                    obj1.add(
                        key, value
                    )
                })
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

fun parseFromUrl(url: String): JsonElement {
    return JSON_CACHE.getUnchecked(url)
}

private fun parseJsonFromAnywhere(path: String): JsonElement {
    return JsonParser.parseString(String(loadFileFromAnywhere(path).second))
}

fun clearCache() {
    JSON_CACHE.invalidateAll()
}

fun clearCache(key: String) {
    JSON_CACHE.invalidate(key)
}

fun JsonElement.prettyPrint(): String {
    return GSON.toJson(this)
}

operator fun <T> JsonObject.get(
    key: String,
    def: Supplier<out T>,
    rule: Function<in JsonElement, out T>
): T {
    return if (this.has(key)) rule.apply(this[key]) else def.get()
}

fun JsonObject.getBool(key: String, def: Boolean): Boolean {
    return this[key, { def }, { it.asBoolean }]
}

fun JsonObject.getInt(key: String, def: Number): Int {
    return this[key, { def.toInt() }, {it.asInt }]
}

fun JsonObject.getLong(key: String, def: Number): Long {
    return this[key, { def.toLong() }, { it.asLong }]
}

fun JsonObject.getDouble(key: String, def: Number): Double {
    return this[key, { def.toDouble() }, { it.asDouble }]
}

fun JsonObject.getString(key: String, def: String): String {
    return this[key, { def }, { it.asString }]
}

fun JsonObject.getArray(key: String): JsonArray {
    return this[key, { JsonArray() }, { it.asJsonArray }]
}

fun JsonObject.getObject(key: String): JsonObject {
    return this[key, { JsonObject() }, { it.asJsonObject }]
}

fun JsonArray.stream(input: JsonArray): Stream<JsonElement> {
    return stream(input, input.size().toLong())
}

fun Iterable<JsonElement>.toJsonArray(): JsonArray {
    val output = JsonArray()
    this.forEach { output.add(it) }
    return output
}