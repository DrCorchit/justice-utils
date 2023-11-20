package com.drcorchit.justice.utils.data

class Heap<E : Any>(type: Int) {
    private val type: Int
    private val nodes: ArrayList<HeapNode>

    private inner class HeapNode constructor(var value: E, var priority: Double) {

        override fun toString(): String {
            return value.toString()
        }
    }

    init {
        require(!(type != MAX && type != MIN)) { "Undefined heap type: must be max (1) or min (-1)" }
        this.type = type
        nodes = ArrayList<HeapNode>()
    }

    fun add(ele: E, priority: Double): Boolean {
        nodes.add(HeapNode(ele, priority))
        heapifyUp(nodes.size - 1)
        return true
    }

    fun peek(): E {
        return nodes[0].value
    }

    fun remove(): E {
        val output: E = nodes[0].value
        remove(output)
        return output
    }

    fun remove(o: Any): Boolean {
        val pos = indexOf(o)
        if (pos < 0) return false
        //give this node the worst priority
        nodes[pos].priority = worstPriority()
        swap(pos, size() - 1)
        heapifyDown(pos)
        nodes.removeAt(nodes.size - 1)
        return true
    }

    fun update(entry: E, newPriority: Double): Boolean {
        val pos = indexOf(entry)
        if (pos < 0) return false
        val oldPriority: Double = nodes[pos].priority
        nodes[pos].value = entry
        nodes[pos].priority = newPriority
        if (hasHigherPriority(oldPriority, newPriority)) heapifyUp(pos) else heapifyDown(pos)
        return true
    }

    //updates entry iff newPriority moves it closer to position 0
    //Results in a heapify up operation
    fun updateIfHigher(entry: E, newPriority: Double): Boolean {
        return if (!contains(entry) || hasHigherPriority(getPriority(entry), newPriority)) false else update(
            entry,
            newPriority
        )
    }

    //updates entry iff newPriority moves it closer to position size-1
    //Results in a heapify down operation
    fun updateIfLower(entry: E, newPriority: Double): Boolean {
        return if (!contains(entry) || !hasHigherPriority(getPriority(entry), newPriority)) false else update(
            entry,
            newPriority
        )
    }

    fun put(entry: E, priority: Double) {
        if (!update(entry, priority)) add(entry, priority)
    }

    fun putIfHigher(entry: E, priority: Double): Boolean {
        return if (contains(entry)) updateIfHigher(entry, priority) else add(entry, priority)
    }

    fun putIfLower(entry: E, priority: Double): Boolean {
        return if (contains(entry)) updateIfLower(entry, priority) else add(entry, priority)
    }

    operator fun iterator(): Iterator<E> {
        return nodes.stream().map { it.value }.iterator()
    }

    fun add(e: E): Boolean {
        return add(e, worstPriority())
    }

    operator fun contains(o: Any?): Boolean {
        if (o == null) return false
        for (node in nodes) if (node.value == o) return true
        return false
    }

    private fun indexOf(o: Any): Int {
        for (i in 0 until size()) if (nodes[i].value == o) return i
        return -1
    }

    fun getPriority(entry: E): Double {
        val index = indexOf(entry)
        return if (index == -1) worstPriority() else nodes[index].priority
    }

    fun size(): Int {
        return nodes.size
    }

    val isEmpty: Boolean
        get() = nodes.isEmpty()

    fun clear() {
        nodes.clear()
    }

    //detects errors in the heap
    fun check(): Boolean {
        for (i in 0 until size()) {
            val left = left(i)
            val right = right(i)
            if (left >= size()) continue
            if (!hasHigherOrSamePriority(i, left)) return false
            if (right < size() && !hasHigherOrSamePriority(i, right)) return false
        }
        return true
    }

    override fun toString(): String {
        return nodes.toString()
    }

    ////////////////////////////
    // PRIVATE METHODS FOLLOW //
    ////////////////////////////
    private fun heapify() {
        val temp: ArrayList<HeapNode> = ArrayList<HeapNode>(nodes)
        nodes.clear()
        for (i in temp.indices) {
            nodes.add(temp[i])
            heapifyUp(i)
        }
    }

    private fun heapifyUp(initialIndex: Int) {
        var currentIndex = initialIndex
        var parentIndex = parent(currentIndex)
        while (parentIndex != -1) {
            if (hasHigherPriority(currentIndex, parentIndex)) {
                swap(parentIndex, currentIndex)
                currentIndex = parentIndex
                parentIndex = parent(currentIndex)
            } else {
                return
            }
        }
    }

    private fun heapifyDown(pos: Int) {
        val lPos = left(pos)
        val rPos = right(pos)

        if (lPos >= nodes.size) {
            //No children, heapify complete
            return
        } else if (rPos >= nodes.size) {
            //Edge case: one child left
            if (hasHigherPriority(lPos, pos)) swap(pos, lPos)
            return
        }
        if (hasHigherPriority(lPos, pos) && hasHigherOrSamePriority(lPos, rPos)) {
            swap(pos, lPos)
            heapifyDown(lPos)
        } else if (hasHigherPriority(rPos, pos) && hasHigherOrSamePriority(rPos, lPos)) {
            swap(pos, rPos)
            heapifyDown(rPos)
        }
        //Both children are larger, so continue
    }

    private fun worstPriority(): Double {
        return if (type == MIN) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
    }

    private fun hasHigherOrSamePriority(index1: Int, index2: Int): Boolean {
        return hasHigherOrSamePriority(nodes[index1].priority, nodes[index2].priority)
    }

    private fun hasHigherPriority(index1: Int, index2: Int): Boolean {
        return hasHigherPriority(nodes[index1].priority, nodes[index2].priority)
    }

    private fun hasHigherOrSamePriority(d1: Double, d2: Double): Boolean {
        return d1 == d2 || hasHigherPriority(d1, d2)
    }

    private fun hasHigherPriority(d1: Double, d2: Double): Boolean {
        return java.lang.Double.compare(d1, d2) == type
    }

    private fun swap(index1: Int, index2: Int) {
        nodes[index1] = nodes.set(index2, nodes[index1])
    }

    companion object {
        const val MAX = 1
        const val MIN = -1
        private fun parent(pos: Int): Int {
            return (pos - 1) / 2
        }

        private fun left(pos: Int): Int {
            return pos * 2 + 1
        }

        private fun right(pos: Int): Int {
            return pos * 2 + 2
        }
    }
}