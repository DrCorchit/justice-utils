package com.drcorchit.justice.utils

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.collections.HashSet
import kotlin.math.abs

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

	fun <K, V> createCache(
		size: Int,
		loader: java.util.function.Function<K, V>
	): LoadingCache<K, V> {
		var actualSize = size
		if (DISABLE_CACHING) actualSize = 0
		return CacheBuilder.newBuilder().initialCapacity(actualSize).softValues()
			.build(CacheLoader.from<K, V> { t: K -> loader.apply(t) })
	}

	//Returns the result of a binary search, searching for the element with the numeric value closest to the parameter
	//NOTE: This code assumes the input list is already sorted. Behavior on an unsorted list is not defined.
	fun <T> List<T>.binarySearch(desired: Number, converter: (T) -> Number): Int {
		if (size < 2) return 0

		val pivot = size / 2
		val index1 = subList(0, pivot).binarySearch(desired, converter)
		val index2 = subList(pivot, size).binarySearch(desired, converter) + pivot
		val delta1 = abs(converter(this[index1]).toDouble() - desired.toDouble())
		val delta2 = abs(converter(this[index2]).toDouble() - desired.toDouble())
		return if (delta1 <= delta2) index1 else index2
	}
}