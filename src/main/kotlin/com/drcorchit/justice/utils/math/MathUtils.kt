package com.drcorchit.justice.utils.math

import kotlin.math.*
import kotlin.random.Random

object MathUtils {
	private val ORDINAL_SUFFIXES =
		arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")


	@JvmStatic
	fun ordinalSuffix(i: Long): String {
		val mod = modulus(i, 100).toInt()
		//edge case for teen values
		if (mod in 11..19) return ORDINAL_SUFFIXES[0]
		val mod10 = modulus(mod.toLong(), 10).toInt()
		return ORDINAL_SUFFIXES[mod10]
	}

	@JvmStatic
	fun ordinal(i: Long): String {
		return i.toString() + ordinalSuffix(i)
	}

	@JvmStatic
	fun <T> Array<T>.randomEntry(r: Random): T {
		return this[r.nextInt(this.size)]
	}

	@JvmStatic
	fun <T> List<T>.randomEntry(r: Random): T {
		return this[r.nextInt(this.size)]
	}

	@JvmStatic
	fun <T : Number> clamp(min: T, value: T, max: T): T {
		return if (value.toDouble() > max.toDouble()) max else if (value.toDouble() < min.toDouble()) min else value
	}

	@JvmStatic
	fun modulus(n: Int, divisor: Int): Int {
		return if (n < 0) n % divisor + divisor else n % divisor
	}

	@JvmStatic
	fun modulus(n: Long, divisor: Long): Long {
		return if (n < 0) n % divisor + divisor else n % divisor
	}

	@JvmStatic
	fun remainder(n: Double, divisor: Double): Double {
		val temp = floor(n / divisor) * divisor
		return n - temp
	}

	fun fractionalPart(n: Number): Double {
		return n.toDouble() - n.toLong()
	}

	fun between(min: Number, value: Number, max: Number): Boolean {
		return value.toDouble() > min.toDouble() && value.toDouble() < max.toDouble()
	}

	fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		val x = x2 - x1
		val y = y2 - y1
		return hypot(x, y)
	}

	fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		val x = x2 - x1
		val y = y2 - y1
		return atan2(y, x)
	}

	fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
		val x = x2 - x1
		val y = y2 - y1
		return hypot(x, y)
	}

	fun angle(x1: Double, y1: Double, x2: Double, y2: Double): Double {
		val x = x2 - x1
		val y = y2 - y1
		return atan2(y, x)
	}

	fun lerp(p: Double, q: Double, factor: Double): Double {
		return p + factor * (q - p)
	}

	fun decelerate(value: Float, decel: Float): Float {
		return if (value == 0f) value
		else if (value < 0) min(0f, value + decel)
		else max(0f, value - decel)
	}

	@SafeVarargs
	fun <T : Number?> max(vararg nums: T): T {
		require(nums.isNotEmpty()) { "No arguments to max()" }
		var output = nums[0]
		for (i in 1 until nums.size) {
			if (nums[i]!!.toDouble() > output!!.toDouble()) output = nums[i]
		}
		return output
	}

	@SafeVarargs
	fun <T : Number?> min(vararg nums: T): T {
		require(nums.isNotEmpty()) { "No arguments to max()" }
		var output = nums[0]
		for (i in 1 until nums.size) {
			if (nums[i]!!.toDouble() < output!!.toDouble()) output = nums[i]
		}
		return output
	}

	fun avg(vararg nums: Number): Double {
		return nums.sumOf { it.toDouble() } / nums.size
	}

	fun Int.getBit(place: Int): Boolean {
		return (this shr place and 1) == 1
	}

	fun Int.setBit(place: Int, value: Boolean): Int {
		val mask = (if (value) 1 else 0) shl place
		return this and mask
	}
}
