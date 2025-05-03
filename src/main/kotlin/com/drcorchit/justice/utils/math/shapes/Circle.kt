package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.distanceTo

class Circle(val position: Vector2, val radius: Float) : Shape {
	override val boundingBox by lazy {
		Rectangle(position, radius * 2, radius * 2)
	}
	override val boundingCircle = this
	override val vertices = setOf<Vector2>()
	override val edges = setOf<Line>()

	override fun containsPoint(point: Vector2): Boolean {
		return position.distanceTo(point) <= radius
	}
}