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

fun toArray(): Collector<JsonElement, *, JsonArray> {
    return TO_ARRAY
}

fun toObject(): Collector<Pair<String, JsonElement>, *, JsonObject> {
    return TO_OBJECT
}

fun Iterable<JsonElement>.toJsonArray(): JsonArray {
    val output = JsonArray()
    this.forEach { output.add(it) }
    return output
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

fun prettyPrintJson(input: JsonElement): String {
    return GSON.toJson(input)
}

fun getBoolean(input: JsonObject, key: String, def: Boolean): Boolean {
    return if (input.has(key)) input[key].asBoolean else def
}

operator fun <T> JsonObject.get(
    key: String,
    def: Supplier<out T>,
    rule: Function<in JsonElement, out T>
): T {
    return if (this.has(key)) rule.apply(this[key]) else def.get()
}

fun getInt(input: JsonObject, key: String, def: Number): Int {
    return input[key, { def.toInt() }, { obj: JsonElement -> obj.asInt }]
}

fun getLong(input: JsonObject, key: String, def: Number): Long {
    return input[key, { def.toLong() }, { obj: JsonElement -> obj.asLong }]
}

fun getDouble(input: JsonObject, key: String, def: Number): Double {
    return input[key, { def.toDouble() }, { obj: JsonElement -> obj.asDouble }]
}

fun getString(input: JsonObject, key: String, def: String): String {
    return input[key, { def }, { it.asString }]
}

fun getArray(input: JsonObject, key: String): JsonArray {
    return input[key, { JsonArray() }, { it.asJsonArray }]
}

fun getObject(input: JsonObject, key: String): JsonObject {
    return input[key, { JsonObject() }, { it.asJsonObject }]
}

//Throws a null pointer exception if the key is missing
fun getWhitelistedString(input: JsonObject, key: String): String {
    return input[key].asString.whitelist()
}

fun getWhitelistedString(input: JsonObject, key: String, def: String): String {
    return getString(input, key, def).whitelist()
}

fun JsonArray.stream(input: JsonArray): Stream<JsonElement> {
    return stream(input, input.size().toLong())
}