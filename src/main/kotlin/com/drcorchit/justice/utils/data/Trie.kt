package com.drcorchit.justice.utils.data

import kotlin.streams.toList

class Trie<K, V> : AbstractMutableMap<List<K>, V>() {
    private val root = TrieEntry()

    override fun isEmpty(): Boolean {
        return root.size() == 0
    }

    //All traversals are postorder
    override val entries: MutableSet<MutableMap.MutableEntry<List<K>, V>>
        get() {
            val accumulator = mutableSetOf<MutableMap.MutableEntry<List<K>, V>>()
            root.getEntries(accumulator, null)
            return accumulator
        }

    override val size: Int get() = root.size()

    fun has(key: List<K>): Boolean {
        return get(key) != null
    }

    override fun get(key: List<K>): V? {
        return root.get(key)?.value
    }

    override fun put(key: List<K>, value: V): V? {
        return root.put(key, value)
    }

    fun getPrefix(key: List<K>): List<V> {
        return root.get(key)?.traverse()?.mapNotNull { it.value } ?: mutableListOf()
    }

    fun computeIfAbsent(key: List<K>, compute: (List<K>) -> V): V {
        val child = root.get(key)
        return if (child?.value != null) {
            child.value!!
        } else {
            val value = compute.invoke(key)
            if (child != null) child.value = value
            else set(key, value)
            value
        }
    }

    private inner class TrieEntry {
        var value: V? = null
        val map = mutableMapOf<K, TrieEntry>()

        fun size(): Int {
            val subSize = map.values.sumOf { it.size() }
            return if (value == null) subSize else subSize + 1
        }

        fun put(key: List<K>, value: V): V? {
            return if (key.isEmpty()) {
                val out = this.value
                this.value = value
                out
            } else {
                val child = map.computeIfAbsent(key.first()) { TrieEntry() }
                child.put(key.subList(1, key.size), value)
            }
        }

        fun get(key: List<K>): TrieEntry? {
            return if (key.isEmpty()) {
                this
            } else {
                map[key.first()]?.get(key.subList(1, key.size))
            }
        }

        fun traverse(): MutableList<TrieEntry> {
            val output = mutableListOf<TrieEntry>()
            if (value != null) output.add(this)
            map.values.stream()
                .flatMap { it.traverse().stream() }
                .forEach { output.add(it) }
            return output
        }

        fun toEntry(key: ImmutableLinkedList<K>?): MutableMap.MutableEntry<List<K>, V>? {
            return if (value == null) null
            else {
                object : MutableMap.MutableEntry<List<K>, V> {
                    override val key: List<K>
                        get() = key?.toList()?.reversed() ?: listOf()
                    override val value: V
                        get() = this@TrieEntry.value!!

                    override fun setValue(newValue: V): V {
                        val oldVal = this@TrieEntry.value!!
                        this@TrieEntry.value = newValue
                        return oldVal
                    }
                }
            }
        }

        fun getEntries(output: MutableSet<MutableMap.MutableEntry<List<K>, V>>, key: ImmutableLinkedList<K>?) {
            val self = toEntry(key)
            if (self != null) output.add(self)
            map.entries.forEach {
                val childKey = key?.cons(it.key) ?: ImmutableLinkedList(it.key, null)
                it.value.getEntries(output, childKey)
            }
        }
    }
}