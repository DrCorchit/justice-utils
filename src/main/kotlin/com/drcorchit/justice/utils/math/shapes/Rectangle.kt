package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import kotlin.math.hypot

class Rectangle(val position: Vector2, val w: Float, val h: Float) : Shape {
	val neCorner = position.cpy().add(w / 2, h / 2)
	val nwCorner = position.cpy().add(-w / 2, h / 2)
	val seCorner = position.cpy().add(w / 2, -h / 2)
	val swCorner = position.cpy().add(-w / 2, -h / 2)

	override val boundingBox = this

	override val boundingCircle by lazy {
		val r = hypot(w / 2, h / 2)
		Circle(position, r)
	}

	override val vertices = setOf(neCorner, nwCorner, seCorner, swCorner)
	override val edges = setOf(
		Line(neCorner, nwCorner),
		Line(nwCorner, swCorner),
		Line(swCorner, seCorner),
		Line(seCorner, neCorner)
	)

	override fun containsPoint(point: Vector2): Boolean {
		val xBounds = point.x >= position.x && point.x < position.x + w
		val yBounds = point.y >= position.y && point.y < position.y + h
		return xBounds && yBounds
	}
}