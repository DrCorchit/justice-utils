package com.drcorchit.justice.utils.math.units

import com.google.gson.JsonElement

class Measurement<T : Unit>(val value: Double, val unit: T) {
    fun convert(newUnit: T): Measurement<T> {
        val newValue = value * newUnit.ratio / unit.ratio
        return Measurement(newValue, newUnit)
    }

    override fun toString(): String {
        return toString("%2f")
    }

    fun toString(format: String
    ): String {
        val valStr = String.format(format, value)
        val unitStr = if (value == 1.0) unit.singular else unit.plural
        return "$valStr $unitStr"
    }

    companion object {
        @JvmStatic
        inline fun <reified E : Unit> deserializeMeasurement(
            ele: JsonElement?,
            defUnit: E,
            parser: (String) -> E
        ): Measurement<E> {
            val p = ele?.asJsonPrimitive
            return if (p == null) {
                Measurement(0.0, defUnit)
            } else if (p.isString) {
                p.asString.parseAsMeasurement(parser)
            } else {
                Measurement(ele.asDouble, defUnit)
            }
        }

        @JvmStatic
        inline fun <reified E : Unit> String.parseAsMeasurement(
            parser: (String) -> E
        ): Measurement<E> {
            return this.split(" ")
                .let {
                    check(it.size == 2) { "Invalid measurement: $this" }
                    Measurement(it.first().toDouble(), parser.invoke(it.last()))
                }
        }
    }
}