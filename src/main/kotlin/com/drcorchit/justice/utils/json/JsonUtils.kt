package com.drcorchit.justice.utils.json

import com.drcorchit.justice.utils.aws.AWSClient
import com.drcorchit.justice.utils.IOUtils
import com.drcorchit.justice.utils.Utils
import com.drcorchit.justice.utils.Utils.createCache
import com.drcorchit.justice.utils.aws.AWSUtils
import com.google.common.cache.LoadingCache
import com.google.common.collect.ImmutableSet
import com.google.gson.*
import java.io.File
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Stream

object JsonUtils {
    @JvmStatic
    val GSON: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    @JvmStatic
    fun parseFromFile(path: String): TimestampedJson? {
        val file = File(path)
        return if (file.exists()) {
            JsonParser.parseString(IOUtils.loadFile(file)) to file.lastModified()
        } else null
    }

    @JvmStatic
    fun parseFromUrl(url: String): TimestampedJson? {
        return IOUtils.readUrl(url)?.toJson()
    }

    @JvmStatic
    fun parseFromS3(s3Url: String, client: AWSClient?): TimestampedJson? {
        return if (AWSUtils.isS3Url(s3Url) && client != null) {
            val location = AWSUtils.parseS3Url(s3Url)
            client.readS3Object(location.first, location.second).toJson()
        } else null
    }

    private fun parseFromAnywhere(path: String, client: AWSClient? = null): TimestampedJson {
        return parseFromFile(path) ?: parseFromS3(path, client) ?: parseFromUrl(path)!!
    }

    @JvmStatic
    fun toArray(): Collector<JsonElement, *, JsonArray> {
        return TO_ARRAY
    }

    @JvmStatic
    fun toObject(): Collector<Pair<String, JsonElement>, *, JsonObject> {
        return TO_OBJECT
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
        return Utils.stream(this, this.size().toLong())
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
}