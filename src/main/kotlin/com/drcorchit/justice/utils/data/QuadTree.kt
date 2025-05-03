package com.drcorchit.justice.utils.data

import com.drcorchit.justice.utils.math.shapes.Rectangle
import com.drcorchit.justice.utils.math.shapes.Shape

class QuadTree<T>(rect: Rectangle) {

	fun insert(shape: Shape, item: T) {
		root.insert(shape to item)
	}

	operator fun get(shape: Shape): List<Pair<Shape, T>> {
		return root.get(shape)
	}

	val size get() = root.size

	private var root = Node<T>(rect)

	private class Node<T>(val rect: Rectangle) {
		private var items = mutableListOf<Pair<Shape, T>>()
		private var children = mutableListOf<Node<T>>()

		val isLeafNode get() = children.isEmpty()
		val size: Int get() = items.size + children.sumOf { it.size }

		fun insert(item: Pair<Shape, T>) {
			if (size == 10 && isLeafNode && rect.w > 1 && rect.h > 1) {
				//divide the rectangle into four quadrants
				val w2 = rect.w / 2
				val h2 = rect.h / 2
				val r1 = Rectangle(rect.position.cpy().add(-w2 / 2, h2 / 2), w2, h2)
				val r2 = Rectangle(rect.position.cpy().add(w2 / 2, h2 / 2), w2, h2)
				val r3 = Rectangle(rect.position.cpy().add(-w2 / 2, -h2 / 2), w2, h2)
				val r4 = Rectangle(rect.position.cpy().add(w2 / 2, -h2 / 2), w2, h2)
				children.addAll(listOf(r1, r2, r3, r4).map { Node(it) })

				val temp = items
				items = mutableListOf()
				temp.forEach { insert(it) }
			}

			if (isLeafNode) {
				items.add(item)
			} else {
				val collisions = children.filter { it.rect.collides(item.first) }
				if (collisions.size == 1) {
					collisions.first().insert(item)
				} else {
					items.add(item)
				}
			}
		}

		fun get(shape: Shape): List<Pair<Shape, T>> {
			return items.filter { it.first.collides(shape) } + children.filter {
					it.rect.collides(
						shape
					)
				}.flatMap { it.get(shape) }
		}
	}
}