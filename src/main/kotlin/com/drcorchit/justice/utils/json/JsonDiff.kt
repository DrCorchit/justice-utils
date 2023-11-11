package com.drcorchit.justice.utils.json

import com.drcorchit.justice.utils.assertDisjoint
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.gson.*
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass

abstract class JsonDiff<T : JsonElement>(private val type: KClass<T>) {

    fun applyUnchecked(input: JsonElement): T {
        return apply(type.java.cast(input), Stack<String>())
    }

    protected fun applyUnchecked(input: JsonElement, stack: Stack<String>): T {
        return apply(type.java.cast(input), stack)
    }

    protected abstract fun apply(input: T, stack: Stack<String>): T

    protected abstract fun serialize(): JsonElement

    class PrimitiveDiff(private val value: JsonPrimitive) : JsonDiff<JsonPrimitive>(JsonPrimitive::class) {

        public override fun apply(input: JsonPrimitive, stack: Stack<String>): JsonPrimitive {
            return value
        }

        public override fun serialize(): JsonElement {
            return value
        }
    }

    class ArrayDiff(info: JsonObject) : JsonDiff<JsonArray>(JsonArray::class) {
        private val remove: ImmutableSet<Int>
        private val add: ImmutableMap<Int, JsonElement>
        private val change: ImmutableMap<Int, JsonDiff<*>>

        init {
            require(!(!info.has("array") || !info["array"].asBoolean)) { "This JsonDiff is not appropriate for an array" }
            remove = if (info.has("remove")) {
                val rb: ImmutableSet.Builder<Int> = ImmutableSet.builder()
                info.getAsJsonArray("remove")!!
                    .forEach(Consumer { ele: JsonElement -> rb.add(ele.asInt) })
                rb.build()
            } else {
                ImmutableSet.of()
            }
            add = if (info.has("add")) {
                val ab: ImmutableMap.Builder<Int, JsonElement> = ImmutableMap.builder()
                info.getAsJsonObject("add").entrySet()
                    .forEach(Consumer<Map.Entry<String, JsonElement>> { (key, value): Map.Entry<String, JsonElement> ->
                        ab.put(Integer.valueOf(key), value)
                    })
                ab.build()
            } else {
                ImmutableMap.of()
            }
            change = if (info.has("change")) {
                val cb: ImmutableMap.Builder<Int, JsonDiff<*>> = ImmutableMap.builder()
                info.getAsJsonObject("change").entrySet()
                    .forEach { (key, value) -> cb.put(Integer.valueOf(key), of(value)) }
                cb.build()
            } else {
                ImmutableMap.of()
            }
            assertDisjoint(remove, add.keys, change.keys)
        }

        public override fun apply(input: JsonArray, stack: Stack<String>): JsonArray {
            val temp: ArrayList<JsonElement> = ArrayList<JsonElement>(input.size())
            input.forEach { e: JsonElement -> temp.add(e) }
            remove.forEach { i: Int -> temp[i] = JsonNull.INSTANCE }
            change.forEach { (i: Int, diff: JsonDiff<*>) ->
                stack.push(i.toString())
                if (i < 0 || i >= input.size()) {
                    val message = String.format("Cannot apply diff at %s", stack)
                    throw ArrayIndexOutOfBoundsException(message)
                }
                temp[i] = diff.applyUnchecked(input.get(i), stack)
                stack.pop()
            }
            add.forEach { (index: Int, element: JsonElement) -> temp.add(index, element) }
            temp.removeIf { obj: JsonElement -> obj.isJsonNull }
            val output = JsonArray(temp.size)
            temp.forEach { element: JsonElement -> output.add(element) }
            return output
        }

        public override fun serialize(): JsonElement {
            val output = JsonObject()
            output.addProperty("array", true)
            if (!remove.isEmpty()) {
                output.add("remove", remove.stream().map { JsonPrimitive(it) }.collect(JsonUtils.toArray()))
            }
            if (!add.isEmpty()) {
                val addInfo = JsonObject()
                add.forEach { (key: Int, value: JsonElement) -> addInfo.add(key.toString(), value) }
                output.add("add", addInfo)
            }
            if (!change.isEmpty()) {
                val changeInfo = JsonObject()
                change.forEach { (key: Int, value: JsonDiff<*>) -> changeInfo.add(key.toString(), value.serialize()) }
                output.add("change", changeInfo)
            }
            return output
        }
    }

    class ObjectDiff(info: JsonObject) : JsonDiff<JsonObject>(JsonObject::class) {
        private val remove: ImmutableSet<String>
        private val add: ImmutableMap<String, JsonElement>
        private val change: ImmutableMap<String, JsonDiff<*>>

        init {
            require(!(info.has("array") && info["array"].asBoolean)) { "This JsonDiff is not appropriate for an object" }
            remove = if (info.has("remove")) {
                val rb: ImmutableSet.Builder<String> = ImmutableSet.builder()
                info.getAsJsonArray("remove")
                    .forEach(Consumer { ele: JsonElement -> rb.add(ele.asString) })
                rb.build()
            } else {
                ImmutableSet.of()
            }
            add = if (info.has("add")) {
                val ab: ImmutableMap.Builder<String, JsonElement> = ImmutableMap.builder()
                info.getAsJsonObject("add").entrySet()
                    .forEach(Consumer<Map.Entry<String, JsonElement>>
                    { (key, value): Map.Entry<String, JsonElement> -> ab.put(key, value) })
                ab.build()
            } else {
                ImmutableMap.of()
            }
            change = if (info.has("change")) {
                val cb: ImmutableMap.Builder<String, JsonDiff<*>> = ImmutableMap.builder()
                info.getAsJsonObject("change").entrySet()
                    .forEach(Consumer<Map.Entry<String, JsonElement>>
                    { (key, value): Map.Entry<String, JsonElement> -> cb.put(key, of(value)) })
                cb.build()
            } else {
                ImmutableMap.of()
            }
            assertDisjoint(remove, add.keys, change.keys)
        }

        override fun apply(input: JsonObject, stack: Stack<String>): JsonObject {
            val output = input.deepCopy()
            remove.forEach(Consumer { property: String -> output.remove(property) })
            change.forEach { (key: String, diff: JsonDiff<*>) ->
                stack.push(key)
                if (!input.has(key)) {
                    val message = String.format("Cannot apply diff due to missing key at: %s", stack)
                    throw NullPointerException(message)
                }
                output.add(key, diff.applyUnchecked(input[key], stack))
                stack.pop()
            }
            add.forEach { (property: String, value: JsonElement) ->
                output.add(property, value)
            }
            return output
        }

        public override fun serialize(): JsonElement {
            val output = JsonObject()
            output.addProperty("array", false)
            if (!remove.isEmpty()) {
                output.add("remove", remove.stream().map { JsonPrimitive(it) }.collect(JsonUtils.toArray()))
            }
            if (!add.isEmpty()) {
                val addInfo = JsonObject()
                add.forEach { (property: String, value: JsonElement) -> addInfo.add(property, value) }
                output.add("add", addInfo)
            }
            if (!change.isEmpty()) {
                val changeInfo = JsonObject()
                change.forEach { (key: String, value: JsonDiff<*>) ->
                    changeInfo.add(key, value.serialize())
                }
                output.add("change", changeInfo)
            }
            return output
        }
    }

    companion object {
        @JvmStatic
        fun of(ele: JsonElement): JsonDiff<*> {
            return if (ele.isJsonObject) {
                val info: JsonObject = ele.asJsonObject
                if (info.has("array") && info["array"].asBoolean) {
                    ArrayDiff(info)
                } else {
                    ObjectDiff(info)
                }
            } else {
                PrimitiveDiff(ele.asJsonPrimitive)
            }
        }
    }
}