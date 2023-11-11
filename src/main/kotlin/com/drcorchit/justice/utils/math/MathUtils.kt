package com.drcorchit.justice.utils.math

import java.util.*

class MathUtils {
    companion object {
        private val ORDINAL_SUFFIXES = arrayOf("th", "st", "nd", "rd")


        @JvmStatic
        fun <T> randomEntry(array: Array<T>, r: Random): T {
            return array[r.nextInt(array.size)]
        }

        @JvmStatic
        fun <T> randomEntry(list: List<T>, r: Random): T {
            return list[r.nextInt(list.size)]
        }

        @JvmStatic
        fun ordinalSuffix(i: Long): String {
            val mod = modulus(i, 100).toInt()
            //edge case for teen values
            if (mod in 11..19) return ORDINAL_SUFFIXES[0]
            val mod10 = modulus(mod.toLong(), 10).toInt()
            return if (mod10 < 4) ORDINAL_SUFFIXES[mod10] else ORDINAL_SUFFIXES[0]
        }

        @JvmStatic
        fun ordinal(i: Long): String {
            return i.toString() + ordinalSuffix(i)
        }

        @JvmStatic
        fun <T : Number> clamp(min: T, value: T, max: T): T {
            return if (value.toDouble() > max.toDouble()) max else if (value.toDouble() < min.toDouble()) min else value
        }

        @SafeVarargs
        @JvmStatic
        fun <T : Number> max(vararg values: T): T {
            var max = values[0]
            for (value in values) if (value.toDouble() > max.toDouble()) max = value
            return max
        }

        @SafeVarargs
        @JvmStatic
        fun <T : Number> min(vararg values: T): T {
            var min = values[0]
            for (value in values) if (value.toDouble() < min.toDouble()) min = value
            return min
        }

        @JvmStatic
        fun modulus(n: Int, divisor: Int): Int {
            return if (n < 0) n % divisor + divisor else n % divisor
        }

        @JvmStatic
        fun modulus(n: Long, divisor: Long): Long {
            return if (n < 0) n % divisor + divisor else n % divisor
        }
    }
}