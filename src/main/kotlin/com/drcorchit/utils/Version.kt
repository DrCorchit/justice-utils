package com.drcorchit.utils

class Version(value: String) : Comparable<Version> {
    val value: String
    private val values: IntArray

    init {
        require(value.matches(VERSION_REGEX)) { "Version $value does not match $VERSION_REGEX" }
        values = value.split(".").map { it.toInt() }.toIntArray()
        this.value = values.joinToString(".")
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Version) {
            other.value == value
        } else false
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value
    }

    override fun compareTo(other: Version): Int {
        for (i in 0 until N) {
            val temp = values[i] - other.values[i]
            if (temp != 0) return temp
        }
        return 0
    }

    companion object {
        val VERSION_REGEX = "\\d+\\.\\d+\\.\\d+".toRegex()
        private const val N = 3
    }
}