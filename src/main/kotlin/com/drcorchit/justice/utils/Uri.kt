package com.drcorchit.justice.utils

data class Uri(val parent: Uri?, val value: String) {

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
}