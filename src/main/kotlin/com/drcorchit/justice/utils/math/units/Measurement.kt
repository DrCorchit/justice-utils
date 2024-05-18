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
        return toString("%.0f", false)
    }

    fun toString(format: String = "%.0f", coerceSingular: Boolean = false): String {
        val unitStr = if (value == 1.0 || coerceSingular) unit.singular else unit.plural
        return "${format.format(value)} ${unitStr.lowercase()}"
    }
}