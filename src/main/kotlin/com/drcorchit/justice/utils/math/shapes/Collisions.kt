package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.distanceTo

object Collisions {

	fun intersection(l1: Line, l2: Line): Vector2? {
		if (!guard(l1, l2)) return null

		val x1: Float = l1.p1.x
		val y1: Float = l1.p1.y
		val x2: Float = l1.p2.x
		val y2: Float = l1.p2.y
		val x3: Float = l2.p1.x
		val y3: Float = l2.p1.y
		val x4: Float = l2.p2.x
		val y4: Float = l2.p2.y

		val temp = ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1))
		val uA: Float = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / temp
		val uB: Float = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / temp

		return if (uA in 0.0..1.0 && uB in 0.0..1.0) {
			val intersectionX = x1 + (uA * (x2 - x1))
			val intersectionY = y1 + (uA * (y2 - y1))
			Vector2(intersectionX, intersectionY)
		} else null
	}

	fun collidesLine(line: Line, shape: Shape): Boolean {
		if (shape.containsPoint(line.p1)) return true
		if (shape.containsPoint(line.p2)) return true
		if (shape is Circle) return collidesLineCircle(line, shape)
		return shape.edges.any { intersection(line, it) != null }
	}

	fun collidesLineCircle(line: Line, circle: Circle): Boolean {
		if (!line.boundingCircle.collides(circle)) return false
		val nearest = line.closestPoint(circle.position)
		return nearest.distanceTo(circle.position) <= circle.radius
	}

	fun collidesCircle(circle: Circle, shape: Shape): Boolean {
		if (shape is Line) return collidesLineCircle(shape, circle)
		if (shape is Circle) {
			val dist = circle.position.distanceTo(shape.position)
			return dist <= circle.radius + shape.radius
		}

		if (!shape.boundingCircle.collides(circle)) return false
		if (shape.containsPoint(circle.position)) return true
		if (shape.vertices.any { circle.containsPoint(it) }) return true
		return shape.edges.any { collidesLineCircle(it, circle) }
	}

	fun collidesRectangle(r1: Rectangle, r2: Rectangle): Boolean {
		val r11 = r1.swCorner
		val r12 = r1.neCorner
		val r21 = r2.swCorner
		val r22 = r2.neCorner
		return (r11.x <= r22.x && r12.x >= r21.x && r11.y <= r22.y && r12.y >= r21.y)
	}

	//fails if either shape is a circle
	fun collidesPolygon(s1: Shape, s2: Shape): Boolean {
		if (!guard(s1, s2)) return false

		//default collision
		if (s1.vertices.any { s2.containsPoint(it) }) return true
		if (s1.containsPoint(s2.vertices.first())) return true
		return s1.edges.any { e1 -> s2.edges.any { e2 -> intersection(e1, e2) != null } }
	}

	fun collidesAny(s1: Shape, s2: Shape): Boolean {
		if (s1 is Line) return collidesLine(s1, s2)
		if (s2 is Line) return collidesLine(s2, s1)
		if (s1 is Circle) return this.collidesCircle(s1, s2)
		if (s2 is Circle) return this.collidesCircle(s2, s1)
		if (s1 is Rectangle && s2 is Rectangle) return collidesRectangle(s1, s2)
		return collidesPolygon(s1, s2)
	}

	private fun guard(s1: Shape, s2: Shape): Boolean {
		return s1.boundingBox.collides(s2.boundingBox)
	}
}