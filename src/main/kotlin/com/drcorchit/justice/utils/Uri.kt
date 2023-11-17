package com.drcorchit.justice.utils

data class Uri(val parent: Uri?, val value: String) {

    init {
        check(value.matches(URI_PART_REGEX))
    }

    fun extend(value: String): Uri {
        return Uri(this, value)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Uri) {
            return value == other.value && parent == other.parent
        }
        return false
    }

    override fun hashCode(): Int {
        return value.hashCode() + parent.hashCode() * 17
    }

    override fun toString(): String {
        return if (parent == null) value
        else "$parent.$value"
    }

    companion object {
        val URI_PART_REGEX = "[a-zA-Z0-9_]+".toRegex()
        val URI_REGEX = "$URI_PART_REGEX(\\.$URI_PART_REGEX)*".toRegex()

        fun parse(str: String): Uri {
            check(str.matches(URI_REGEX))
            val parts = str.split('.')
            return parseHelper(parts.last(), parts.dropLast(1))
        }

        private fun parseHelper(str: String, parts: List<String>): Uri {
            return if (parts.isEmpty()) Uri(null, str)
            else parseHelper(parts.last(), parts.dropLast(1)).extend(str)
        }
    }
}