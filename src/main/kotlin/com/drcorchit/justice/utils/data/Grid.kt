package com.drcorchit.justice.utils.data

import com.drcorchit.justice.utils.math.Layout
import com.drcorchit.justice.utils.math.Space

class Grid<T>(val space: Space) : Iterable<T> {
	//height and width are intentionally reversed here.
	//this makes setRow possible with System.arraycopy()
	private val grid: Array<Array<T>> =
		java.lang.reflect.Array.newInstance(
			Any::class.java,
			space.height,
			space.width
		) as Array<Array<T>>

	constructor(width: Int, height: Int) : this(
		Space(
			width, height,
			wrapHoriz = false,
			wrapVert = false,
			layout = Layout.CARTESIAN
		)
	)

	val width: Int
		get() = grid[0].size

	val height: Int
		get() = grid.size

	val size: Int
		get() = width * height

	operator fun get(i: Int, j: Int): T? {
		return get(space.coordinate(i, j))
	}

	fun getOrDefault(i: Int, j: Int, default: T): T {
		return if(space.within(i, j)) this[i, j]!! else default
	}

	fun get(c: Space.Coordinate): T? {
		return grid[c.y][c.x]
	}

	operator fun set(i: Int, j: Int, value: T) {
		set(space.coordinate(i, j), value)
	}

	fun set(c: Space.Coordinate, value: T) {
		grid[c.y][c.x] = value
	}

	fun forEach(action: (Space.Coordinate, T) -> Unit) {
		val iter = GridIterator()
		while (iter.hasNext()) {
			val next = iter.nextIndexed()
			action.invoke(next.first, next.second)
		}
	}

	@SafeVarargs
	fun setRow(rowIndex: Int, vararg row: T) {
		if (row.size == width) {
			System.arraycopy(row, 0, grid[rowIndex], 0, row.size)
		} else throw IllegalArgumentException("Number of arguments does not match row size.")
	}

	@SafeVarargs
	fun setColumn(columnIndex: Int, vararg column: T) {
		if (column.size == height) {
			for (i in column.indices) grid[i][columnIndex] = column[i]
		} else throw IllegalArgumentException("Number of arguments does not match column size.")
	}

	override fun iterator(): Iterator<T> {
		return GridIterator()
	}

	override fun toString(): String {
		return (0..<height)
			.joinToString("\n") { i ->
				(0..<width)
					.joinToString(", ") { j -> this[i, j].toString() }
			}
	}

	inner class GridIterator : Iterator<T> {
		var i = 0
		var j = 0
		var next: T? = get(i, j)

		init {
			if (next == null) seekNext()
		}

		private fun advance() {
			i++
			if (i >= width) {
				i = 0
				j++
			}
		}

		private fun seekNext() {
			do {
				advance()
			} while (space.within(i, j) && get(i, j) == null)
			next = if (space.within(i, j)) get(i, j) else null
		}

		override fun hasNext(): Boolean {
			return next != null
		}

		override fun next(): T {
			val out = next!!
			seekNext()
			return out
		}

		fun nextIndexed(): Pair<Space.Coordinate, T> {
			return space.coordinate(i, j) to next()
		}
	}
}