package com.drcorchit.utils

import java.util.*

private val ORDINAL_SUFFIXES = arrayOf("th", "st", "nd", "rd")

fun <T> randomEntry(array: Array<T>, r: Random): T {
    return array[r.nextInt(array.size)]
}

fun <T> randomEntry(list: List<T>, r: Random): T {
    return list[r.nextInt(list.size)]
}

fun ordinalSuffix(i: Long): String {
    val mod = modulus(i, 100).toInt()
    //edge case for teen values
    if (mod > 10 && mod < 20) return ORDINAL_SUFFIXES[0]
    val mod10 = modulus(mod.toLong(), 10).toInt()
    return if (mod10 < 4) ORDINAL_SUFFIXES[mod10] else ORDINAL_SUFFIXES[0]
}

fun ordinal(i: Long): String {
    return i.toString() + ordinalSuffix(i)
}

fun <T : Number?> clamp(min: T, `val`: T, max: T): T {
    return if (`val`!!.toDouble() > max!!.toDouble()) max else if (`val`.toDouble() < min!!.toDouble()) min else `val`
}

@SafeVarargs
fun <T : Number?> max(vararg vals: T): T {
    var max = vals[0]
    for (`val` in vals) if (`val`!!.toDouble() > max!!.toDouble()) max = `val`
    return max
}

@SafeVarargs
fun <T : Number?> min(vararg vals: T): T {
    var min = vals[0]
    for (`val` in vals) if (`val`!!.toDouble() < min!!.toDouble()) min = `val`
    return min
}

fun modulus(n: Long, divisor: Long): Long {
    return if (n < 0) n % divisor + divisor else n % divisor
}