package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2

interface Shape {
	val boundingBox: Rectangle
	val boundingCircle: Circle
	val vertices: Set<Vector2>
	val edges: Set<Line>
	fun containsPoint(point: Vector2): Boolean
	fun collides(other: Shape): Boolean {
		return Collisions.collidesAny(this, other)
	}
}