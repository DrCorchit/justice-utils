package com.drcorchit.justice.utils.math

import com.drcorchit.justice.utils.math.MathUtils.clamp
import com.drcorchit.justice.utils.math.MathUtils.remainder
import kotlin.math.abs

fun normalizeAngle(angle: Float): Float {
	return remainder(angle.toDouble(), 360.0).toFloat()
}

fun angleBetween(min: Float, a: Float, max: Float): Boolean {
	var min = min
	var a = a
	var max = max
	min = normalizeAngle(min)
	a = normalizeAngle(a)
	max = normalizeAngle(max)
	if (min + 180 < max) return (a < min || a > max)
	else if (max + 180 < min) return (a < max || a > min)
	else if (a > min && a < max) return true
	return a > max && a < min
}

fun approachAngle(actual: Float, target: Float, amount: Float): Float {
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

fun angleAverage(a1: Float, a2: Float): Float {
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
