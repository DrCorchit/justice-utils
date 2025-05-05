package com.drcorchit.justice.utils.math

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

fun Vector3.toVector2(): Vector2 {
	return Vector2(x, y)
}

fun Vector2.toVector3(z: Float = 0f): Vector3 {
	return Vector3(x, y, z)
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
	return add(dif.nor().scl(amount))
}

fun Vector2.accelerate(amount: Float, max: Float = Float.POSITIVE_INFINITY): Vector2 {
	return setLength(MathUtils.min(max, len() + amount))
}

fun Vector2.decelerate(amount: Float, min: Float = 0f): Vector2 {
	return setLength(MathUtils.max(min, len() - amount))
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