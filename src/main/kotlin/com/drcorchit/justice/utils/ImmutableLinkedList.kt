package com.drcorchit.justice.utils

//Good ol' Lisp-style data structure
data class ImmutableLinkedList<T> constructor(val head: T, val tail: ImmutableLinkedList<out T>?) : Collection<T> {

    override val size: Int by lazy { if (tail == null) 1 else tail.size + 1 }

    override fun isEmpty(): Boolean {
        return false
    }

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            var pointer: ImmutableLinkedList<out T>? = this@ImmutableLinkedList

            override fun hasNext(): Boolean {
                return pointer != null
            }

            override fun next(): T {
                val out = pointer!!.head
                pointer = pointer!!.tail
                return out
            }

        }
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        val temp = HashSet(elements)
        temp.removeAll(this)
        return temp.isEmpty()
    }

    override fun contains(element: T): Boolean {
        return if (head == element) true else tail?.contains(element) ?: false
    }

    operator fun get(index: Int): T {
        return if (index == 0) head else tail!![index-1]
    }

    fun cons(head: T): ImmutableLinkedList<T> {
        return ImmutableLinkedList(head, this)
    }

    companion object {
        fun <T> create(head: T): ImmutableLinkedList<T> {
            return ImmutableLinkedList(head, null)
        }
    }
}

