package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.MathUtils
import com.drcorchit.justice.utils.math.distanceTo
import com.drcorchit.justice.utils.math.midpoint
import kotlin.math.abs

class Line(val p1: Vector2, val p2: Vector2) : Shape {
	val length = p1.distanceTo(p2)
	val midpoint = p1.cpy().midpoint(p2)

	override val boundingBox by lazy {
		val w = abs(p1.x - p2.x)
		val h = abs(p1.y - p2.y)
		Rectangle(midpoint, w, h)
	}

	override val boundingCircle by lazy {
		Circle(midpoint, length / 2)
	}

	override val vertices = setOf(p1, p2)
	override val edges = setOf(this)

	override fun containsPoint(point: Vector2): Boolean {
		val d1 = point.distanceTo(p1)
		val d2 = point.distanceTo(p2)
		return d1 + d2 == length
	}

	fun dot(point: Vector2): Float {
		val adjustedPos: Vector2 = point.cpy().sub(midpoint)
		val thing1 = ((adjustedPos.x - p1.x) * (p2.x - p1.x))
		val thing2 = ((adjustedPos.y - p1.y) * (p2.y - p1.y))
		val dot = (thing1 + thing2) / (length * length)
		return dot
	}

	fun lerp(amount: Float): Vector2 {
		return p1.cpy().lerp(p2, amount)
	}

	fun closestPoint(point: Vector2): Vector2 {
		return lerp(MathUtils.clamp(0f, dot(point), 1f))
	}

	override fun equals(other: Any?): Boolean {
		if (other == null) return false
		if (other is Line) {
			return (p1 == other.p1 && p2 == other.p2) || (p1 == other.p2 && p2 == other.p1)
		}
		return false
	}

	override fun hashCode(): Int {
		return p1.hashCode() + p2.hashCode()
	}

	override fun toString(): String {
		return "Line($p1 -> $p2)"
	}
}