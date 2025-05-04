package com.drcorchit.justice.utils.math

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.cos
import kotlin.math.sin

fun Vector2.mult(scalar: Float): Vector2 {
	this.x *= scalar
	this.y *= scalar
	return this
}

fun Vector3.mult(scalar: Float): Vector3 {
	this.x *= scalar
	this.y *= scalar
	this.z *= scalar
	return this
}

fun Vector3.toVector2(): Vector2 {
	return Vector2(x, y)
}

fun Vector2.toVector3(z: Float = 0f): Vector3 {
	return Vector3(x, y, 0f)
}

fun Vector3.setZ(z: Float): Vector3 {
	this.z = z
	return this
}

fun Vector3.setZRelative(z: Float): Vector3 {
	this.z += z
	return this
}

fun Vector2.approach(dest: Vector2, amount: Float): Vector2 {
	val dif: Vector2 = dest.cpy().sub(this)
	if (dif.len() < amount) return dest.cpy()
	return add(dif.nor().mult(amount))
}

fun Vector2.accelerate(amount: Float, max: Float = Float.POSITIVE_INFINITY): Vector2 {
	return nor().mult(MathUtils.min(max, len() + amount))
}

fun Vector2.decelerate(amount: Float, min: Float = 0f): Vector2 {
	return this.nor().mult(MathUtils.max(min, len() - amount))
}

fun Vector2.midpoint(dest: Vector2): Vector2 {
	return this.lerp(dest, 0.5f)
}

fun Vector2.angleTo(dest: Vector2): Float {
	val angle = MathUtils.angle(x, y, dest.x, dest.y)
	return Math.toDegrees(angle.toDouble()).toFloat()
}

fun Vector2.distanceTo(dest: Vector2): Float {
	return this.cpy().sub(dest).len()
}

// returns true if the line from (a,b)->(c,d) intersects with (p,q)->(r,s)
//https://stackoverflow.com/questions/9043805/test-if-two-lines-intersect-javascript-function
fun intersects(l1p1: Vector2, l1p2: Vector2, l2p1: Vector2, l2p2: Vector2): Boolean {
	val a: Float = l1p1.x
	val b: Float = l1p1.y
	val c: Float = l1p2.x
	val d: Float = l1p2.y
	val p: Float = l2p1.x
	val q: Float = l2p1.y
	val r: Float = l2p2.x
	val s: Float = l2p2.y
	//determinant
	val det = (c - a) * (s - q) - (r - p) * (d - b)
	if (det == 0f) {
		//determinant == 0 means lines are parallel
		//this condition means lines are collinear
		if (l1p1.angleDeg(l1p2) == l1p1.angleDeg(l2p1)) {
			val m1: Vector2 = l1p1.cpy().midpoint(l1p2)
			val m2: Vector2 = l2p1.cpy().midpoint(l2p2)
			val dist: Float = m1.distanceTo(m2)
			val minDist: Float = (l1p1.distanceTo(l1p2) + l2p1.distanceTo(l2p2)) / 2
			return dist < minDist
		}
		//parallel but not collinear means no collision
		return false
	} else {
		val lambda = ((s - q) * (r - a) + (p - r) * (s - b)) / det
		val gamma = ((b - d) * (r - a) + (c - a) * (s - b)) / det
		return (0 < lambda && lambda < 1) && (0 < gamma && gamma < 1)
	}
}

fun mirrorPoint(point: Vector2, origin: Vector2, reflectionAngle: Float): Vector2 {
	val internalAngle: Float = origin.angle(point) - reflectionAngle
	val leg: Float = point.cpy().sub(origin).len()
	val theta = reflectionAngle - 90
	val r = (sin(Math.toRadians(internalAngle.toDouble())) * leg).toFloat()
	val x = (point.x + 2 * r * cos(Math.toRadians(theta.toDouble())))
	val y = (point.y + 2 * r * sin(Math.toRadians(theta.toDouble())))
	return Vector2(x.toFloat(), y.toFloat())
}