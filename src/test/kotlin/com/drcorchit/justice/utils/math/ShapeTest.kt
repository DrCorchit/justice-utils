package com.drcorchit.justice.utils.math

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.shapes.Circle
import com.drcorchit.justice.utils.math.shapes.Line
import com.drcorchit.justice.utils.math.shapes.Rectangle
import kotlin.test.Test

class ShapeTest {
	@Test
	fun lineCollisionTest() {
		val a = Vector2(0f, 0f)
		val b = Vector2(0f, 1f)
		val c = Vector2(0f, 2f)
		val d = Vector2(1f, 2f)
		val e = Vector2(2f, 2f)
		val f = Vector2(2f, 1f)
		val g = Vector2(2f, 0f)

		val l1 = Line(b, d)
		val l2 = Line(a, e)
		val l3 = Line(c, g)
		val l4 = Line(c, f)

		fun check(l1: Line, l2: Line, expected: Boolean) {
			val r1 = l1.collides(l2)
			val r2 = l2.collides(l1)
			assert(r1 == expected && r2 == expected)
		}

		check(l1, l2, false)
		check(l1, l3, true)
		check(l1, l4, true)

		check(l2, l3, true)
		check(l2, l4, true)

		check(l3, l4, true)
	}

	@Test
	fun circleCollisionTest() {
		val a = Vector2(0f, 0f)
		val b = Vector2(2f, 0f)
		val line = Line(Vector2(0f, -1f), Vector2(2f, -1f))

		val c1 = Circle(a, 1f)
		val c2 = Circle(b, 1f)
		val c3 = Circle(b, 0.5f)

		assert(c1.containsPoint(a))
		assert(!c1.containsPoint(b))
		assert(c1.collides(line))
		assert(c1.collides(c2))
		assert(!c1.collides(c3))

		assert(!c2.containsPoint(a))
		assert(c2.containsPoint(b))
		assert(c2.collides(line))
		assert(c2.collides(c3))

		assert(!c3.containsPoint(a))
		assert(c3.containsPoint(b))
		assert(!c3.collides(line))
	}

	@Test
	fun rectangleCollisionTest() {
		val p1 = Vector2(0f, 0f)
		val p2 = Vector2(2f, 2f)

		val r1 = Rectangle(p1, 4f, 2f)
		val r2 = Rectangle(p1, 2f, 4f)
		val r3 = Rectangle(p2, 4f, 1f)
		val circle = Circle(Vector2(1f, -3f), 2f)

		assert(r1.collides(r2))
		assert(!r1.collides(r3))
		assert(r1.collides(circle))

		assert(r2.collides(r1))
		assert(r2.collides(r3))
		assert(r2.collides(circle))

		assert(!r3.collides(r1))
		assert(r3.collides(r2))
		assert(!r3.collides(circle))
	}
}