package com.drcorchit.justice.utils.math.units

import com.google.gson.JsonElement

//Not a concrete class because you're supposed to extend it, so that units of different types don't get mixed up.
abstract class Unit(
    val abbr: String,
    val singular: String,
    val plural: String,
    val ratio: Double
)

abstract class Units<T : Unit> {
    private val map = mutableMapOf<String, T>()

    abstract var def: T

    fun lookup(abbr: String): T {
        return map[abbr]!!
    }

    protected abstract fun create(abbr: String, singular: String, plural: String, ratio: Double): T

    fun add(abbr: String, singular: String, plural: String, ratio: Double): T {
        return create(abbr, singular, plural, ratio).let { map[it.abbr] = it; it }
    }

    fun deserialize(ele: JsonElement?, defOverride: T = def): Measurement<T> {
        val p = ele?.asJsonPrimitive
        return if (p == null || p.isJsonNull) {
            Measurement(0.0, defOverride)
        } else if (p.isString) {
            parse(p.asString)
        } else {
            Measurement(ele.asDouble, defOverride)
        }
    }

    fun parse(str: String, defOverride: T = def): Measurement<T> {
        val split = str.split(" ")
        return when (split.size) {
            1 -> Measurement(split.first().toDouble(), defOverride)
            2 -> {
                Measurement(split.first().toDouble(), lookup(split.last()))
            }

            else -> throw IllegalArgumentException("Unable to parse measurement: $str")
        }
    }
}