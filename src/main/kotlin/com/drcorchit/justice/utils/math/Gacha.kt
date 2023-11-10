package com.drcorchit.justice.utils.math

import java.util.function.Predicate

class Gacha<T> {
    private val weights = LinkedHashMap<T, Double>()
    private var totalWeight = 0.0

    fun size(): Int {
        return weights.size
    }

    val isEmpty: Boolean
        get() = weights.isEmpty()

    fun containsKey(key: T): Boolean {
        return weights.containsKey(key)
    }

    operator fun get(key: T): Double {
        return weights.getOrDefault(key, 0.0)
    }

    fun add(key: T, value: Double): Double {
        return set(key, get(key) + value)
    }

    operator fun set(key: T, value: Double): Double {
        require(value >= 0) { "Cannot assign negative value to gacha entry." }
        val prevWeight = weights.put(key, value) ?: 0.0
        totalWeight -= prevWeight
        totalWeight += value
        return prevWeight
    }

    fun setAll(weights: Map<out T, Double>) {
        weights.forEach { (key: T, value: Double) -> this[key] = value }
    }

    fun remove(key: T): Double {
        val oldVal = weights[key] ?: return 0.0
        weights.remove(key)
        totalWeight -= oldVal
        return oldVal
    }

    fun clear() {
        weights.clear()
        totalWeight = 0.0
    }

    fun getRandom(rng: Rng): T {
        check(totalWeight != 0.0) { "Gacha is empty!" }
        var target = rng.nextReal() * totalWeight
        for ((key, value) in weights) {
            target -= value
            if (target <= 0) return key
        }
        throw IllegalStateException("Target value exceeded total weights.")
    }

    fun filter(filter: Predicate<T>): Gacha<T> {
        val output = Gacha<T>()
        weights.forEach { (key: T, `val`: Double?) ->
            if (filter.test(key)) {
                output[key] = `val`
            }
        }
        return output
    }
}