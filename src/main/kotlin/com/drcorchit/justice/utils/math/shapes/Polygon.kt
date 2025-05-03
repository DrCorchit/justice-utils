package com.drcorchit.justice.utils.math.shapes

import com.badlogic.gdx.math.Vector2
import com.drcorchit.justice.utils.math.distanceTo

class Polygon(vararg points: Vector2) : Shape {
	val minX by lazy { points.minOf { it.x } }
	val minY by lazy { points.minOf { it.y } }
	val maxX by lazy { points.maxOf { it.x } }
	val maxY by lazy { points.maxOf { it.y } }

	val midpoint by lazy {
		Vector2((minX + maxX) / 2, (minY + maxY) / 2)
	}

	override val boundingBox by lazy {
		Rectangle(midpoint, maxX - minX, maxY - minY)
	}

	override val boundingCircle by lazy {
		val r = points.maxOf { midpoint.distanceTo(it) }
		Circle(midpoint, r)
	}
	override val vertices = points.toSet()
	override val edges by lazy {
		points.mapIndexed { index, p ->
			val next = points[index + 1 % points.size]
			Line(p, next)
		}.toSet()
	}

	/** Not guaranteed for concave polygons. */
	override fun containsPoint(point: Vector2): Boolean {
		val test = Line(point, midpoint)
		//If the line intersects, the point is outside the polygon
		return edges.none { Collisions.intersection(it, test) != null }
	}
}