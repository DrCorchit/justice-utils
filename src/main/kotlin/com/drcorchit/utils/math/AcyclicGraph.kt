package com.drcorchit.utils.math

import com.drcorchit.utils.exceptions.CyclicDependencyException
import com.google.common.collect.ImmutableSet
import java.util.*
import java.util.function.Consumer

//models an acyclic graph. when adding edges, the addition methods
//return false if the desired addition would create a cycle
class AcyclicGraph<T : Any> {
    private val nodesToEdges: HashMap<T, MutableSet<T>> = HashMap()

    private fun add(node: T) {
        if (!nodesToEdges.containsKey(node)) nodesToEdges[node] = HashSet()
    }

    private fun add(node: T, target: T) {
        add(node)
        nodesToEdges[node]!!.add(target)
    }

    private fun add(node: T, target: Set<T>) {
        add(node)
        nodesToEdges[node]!!.addAll(target)
    }

    private fun reachable(start: T): HashSet<T> {
        return HashSet(nodesToEdges.getOrDefault(start, ImmutableSet.of()))
    }

    @Synchronized
    fun addIfAcyclic(start: T, dest: T): Boolean {
        return addIfAcyclic(start, ImmutableSet.of(dest))
    }

    @Synchronized
    fun addIfAcyclic(start: T, edges: MutableSet<T>): Boolean {
        //Check for self-reference
        var edges = edges
        if (edges.contains(start)) return false
        //copy edges to avoid mutating the argument
        edges = HashSet(edges)
        //We know that these edges do not cause cycles because they're already in the graph
        edges.removeAll(reachable(start))
        //If all edges are already in the graph, we're safe.
        if (edges.isEmpty()) return true
        val visited = HashSet(edges)
        val remain = LinkedList(edges)
        while (!remain.isEmpty()) {
            val current = remain.pop()
            visited.add(current)
            val reachable = reachable(current)
            //we only need to visit each node once
            reachable.removeAll(visited)
            remain.forEach(Consumer { o: T -> reachable.remove(o) })
            //if we reached the start, then we tried to introduce a cycle.
            for (reachableNode in reachable) if (reachableNode == start) return false
            //queue all unexplored nodes for exploration
            remain.addAll(reachable)
        }
        add(start, edges)
        return true
    }

    fun assertAcyclic(start: T, dest: T) {
        if (!addIfAcyclic(start, dest)) throw CyclicDependencyException(start, dest)
    }

    fun assertAcyclic(start: T, edges: MutableSet<T>) {
        if (!addIfAcyclic(start, edges)) throw CyclicDependencyException(start, edges)
    }
}