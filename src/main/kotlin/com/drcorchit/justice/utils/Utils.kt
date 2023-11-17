package com.drcorchit.justice.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.collections.HashSet

object Utils {
    private class DoubleIterable<S, T>(
        private val source: Iterable<S>,
        private val converter: (S) -> Iterable<T>
    ) : Iterable<T> {
        override fun iterator(): Iterator<T> {
            return object : Iterator<T> {
                val outer: Iterator<S> = source.iterator()
                var inner: Iterator<T>? = null
                var next: T? = null

                init {
                    next = seekNext()
                }

                override fun hasNext(): Boolean {
                    return next != null
                }

                override fun next(): T {
                    val result: T = next!!
                    next = seekNext()
                    return result
                }

                private fun innerHasNext(): Boolean {
                    return inner?.hasNext() ?: false
                }

                private fun seekNext(): T? {
                    while (!innerHasNext() && outer.hasNext()) {
                        //Inner can't be null here, but it could be empty
                        inner = converter.invoke(outer.next()).iterator()
                    }
                    return if (innerHasNext()) inner!!.next() else null
                }
            }
        }
    }

    fun <T> stream(iter: Iterable<T>, size: Long): Stream<T> {
        return StreamSupport.stream(Spliterators.spliterator(iter.iterator(), size, 0), false)
    }

    fun checkDisjoint(vararg sets: Set<Any>): Boolean {
        val examined = HashSet<Any>()
        for (set in sets) {
            val prevSize = examined.size
            examined.addAll(set)
            require(examined.size == prevSize + set.size)
        }
        return true
    }

    fun assertDisjoint(vararg sets: Set<Any>) {
        val examined = HashSet<Any>()
        for (set in sets) {
            for (o in set) {
                require(examined.add(o)) { "Sets are not disjoint. Object $o appears at least twice." }
            }
        }
    }

    //converts a iterable/collection of S into T without allocating memory for each S in Iterable<S>
    fun <S, T> Iterable<S>.convert(converter: (S) -> T): Iterable<T> {
        return Iterable { this@convert.iterator().convert(converter) }
    }

    fun <S, T> Iterator<S>.convert(converter: (S) -> T): Iterator<T> {
        return object : Iterator<T> {
            override fun hasNext(): Boolean {
                return this@convert.hasNext()
            }

            override fun next(): T {
                return converter.invoke(this@convert.next())
            }
        }
    }

    fun <S, T> doubleIterator(
        source: Iterable<S>,
        rule: (S) -> Iterable<T>
    ): Iterable<T> {
        return DoubleIterable(source, rule)
    }

    //used for debugging caches
    private const val DISABLE_CACHING = false

    fun <K, V> createCache(size: Int, loader: java.util.function.Function<K, V>): LoadingCache<K, V> {
        var actualSize = size
        if (DISABLE_CACHING) actualSize = 0
        return CacheBuilder.newBuilder().initialCapacity(actualSize).softValues()
            .build<K, V>(CacheLoader.from<K, V> { t: K -> loader.apply(t) })
    }
}