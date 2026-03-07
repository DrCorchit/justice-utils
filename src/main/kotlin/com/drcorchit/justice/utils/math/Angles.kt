package com.drcorchit.justice.utils.math

import com.drcorchit.justice.utils.math.MathUtils.clamp
import com.drcorchit.justice.utils.math.MathUtils.remainder
import kotlin.math.abs

fun normalizeAngle(angle: Double): Double {
	return remainder(angle, 360.0)
}

fun angleBetween(min: Float, a: Float, max: Float): Boolean {
	return angleBetween(min.toDouble(), a.toDouble(), max.toDouble())
}

fun angleBetween(min: Double, angle: Double, max: Double): Boolean {
	val nMin = normalizeAngle(min)
	val nAngle = normalizeAngle(angle)
	val nMax = normalizeAngle(max)
	if (nMin + 180 < nMax) return (nAngle < nMin || nAngle > nMax)
	else if (nMax + 180 < nMin) return (nAngle < nMax || nAngle > nMin)
	else if (nAngle > nMin && nAngle < nMax) return true
	return nAngle > nMax && nAngle < nMin
}

fun approachAngle(actual: Double, target: Double, amount: Double): Double {
	var actual = actual
	var target = target
	if (amount >= 180) return target
	actual = normalizeAngle(actual)
	target = normalizeAngle(target)

	if (actual - target > 180) {
		target += 360f
	} else if (actual - target < -180) {
		target -= 360f
	}

	return normalizeAngle(
		clamp(actual - amount, target, actual + amount)
	)
}

fun angleAverage(a1: Double, a2: Double): Double {
	var a1 = a1
	var a2 = a2
	a1 = normalizeAngle(a1)
	a2 = normalizeAngle(a2)
	var temp = (a1 + a2) / 2
	if (abs((a1 - a2).toDouble()) > 180) temp += 180f
	return normalizeAngle(temp)
}

//mirrors an angle around an axis
fun mirrorAngle(angle: Float, axis: Float): Float {
	val dif = axis - angle
	return remainder(angle + dif * 2.0, 360.0).toFloat()
}
