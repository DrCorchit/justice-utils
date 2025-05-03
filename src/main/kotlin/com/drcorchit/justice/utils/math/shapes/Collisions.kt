package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.distanceTo
import com.drcorchit.justice.utils.math.midpoint

object Collisions {

	fun intersects(l1: Line, l2: Line): Boolean {
		if (!l1.boundingCircle.collides(l2.boundingCircle)) return false

		val a: Float = l1.p1.x
		val b: Float = l1.p1.y
		val c: Float = l1.p2.x
		val d: Float = l1.p2.y
		val p: Float = l2.p1.x
		val q: Float = l2.p1.y
		val r: Float = l2.p2.x
		val s: Float = l2.p2.y
		//determinant
		val det = (c - a) * (s - q) - (r - p) * (d - b)
		if (det == 0f) {
			//determinant == 0 means lines are parallel
			//this condition means lines are collinear
			if (l1.p1.angleDeg(l1.p2) == l1.p1.angleDeg(l2.p1)) {
				val m1: Vector2 = l1.p1.cpy().midpoint(l1.p2)
				val m2: Vector2 = l2.p1.cpy().midpoint(l2.p2)
				val dist: Float = m1.distanceTo(m2)
				val minDist: Float = (l1.p1.distanceTo(l1.p2) + l2.p1.distanceTo(l2.p2)) / 2
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

	fun collidesCircle(line: Line, circle: Circle): Boolean {
		if (!line.boundingCircle.collides(circle)) return false
		val nearest = line.closestPoint(circle.position)
		return nearest.distanceTo(circle.position) < circle.radius
	}

	fun collidesCircle(c1: Circle, c2: Circle): Boolean {
		val dist = c1.position.distanceTo(c2.position)
		return dist < c1.radius + c2.radius
	}

	fun collidesCircle(shape: Shape, circle: Circle): Boolean {
		if (!shape.boundingCircle.collides(circle)) return false
		if (shape.containsPoint(circle.position)) return true
		if (shape.vertices.any { circle.containsPoint(it) }) return true
		return shape.edges.any { collidesCircle(it, circle) }
	}

	fun collidesRectangle(r1: Rectangle, r2: Rectangle): Boolean {
		val r11 = r1.swCorner
		val r12 = r1.neCorner
		val r21 = r2.swCorner
		val r22 = r2.neCorner
		return (r11.x < r22.x && r12.x > r21.x && r11.y < r22.y && r12.y > r21.y)
	}

	fun collidesPolygon(s1: Shape, s2: Shape): Boolean {
		if (!s1.boundingCircle.collides(s2.boundingCircle)) return false

		//default collision
		if (s1.vertices.any { s2.containsPoint(it) }) return true
		if (s2.vertices.any { s1.containsPoint(it) }) return true
		return s1.edges.any { e1 -> s2.edges.any { e2 -> intersects(e1, e2) } }
	}

	fun collidesAny(s1: Shape, s2: Shape): Boolean {
		if (s1 is Circle && s2 is Circle) return collidesCircle(s1, s2)
		if (s1 is Rectangle && s2 is Rectangle) return collidesRectangle(s1, s2)
		if (s1 is Line && s2 is Line) return intersects(s1, s2)
		if (s1 is Line && s2 is Circle) return collidesCircle(s1, s2)
		if (s1 is Circle && s2 is Line) return collidesCircle(s2, s1)
		if (s1 is Circle) return this.collidesCircle(s2, s1)
		if (s2 is Circle) return this.collidesCircle(s1, s2)
		return collidesPolygon(s1, s2)
	}
}