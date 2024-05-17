package com.drcorchit.justice.utils.math.units

class Measurement<T : Unit>(val value: Double, val unit: T) {
    fun convert(newUnit: T): Measurement<T> {
        val newValue = value * newUnit.ratio / unit.ratio
        return Measurement(newValue, newUnit)
    }

    fun isZero(): Boolean {
        return value == 0.0
    }

    override fun toString(): String {
        return toString("%2f")
    }

    fun toString(format: String): String {
        val valStr = String.format(format, value)
        val unitStr = if (value == 1.0) unit.singular else unit.plural
        return "$valStr $unitStr"
    }
}