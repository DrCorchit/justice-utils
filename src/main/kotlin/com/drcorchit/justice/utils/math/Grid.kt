package com.drcorchit.justice.utils.math

class Grid<T>(val space: Space) : Iterable<T> {
    //height and width are intentionally reversed here.
    //this makes setRow possible with System.arraycopy()
    private val grid: Array<Array<T>> =
        java.lang.reflect.Array.newInstance(Any::class.java, space.height, space.width) as Array<Array<T>>

    constructor(width: Int, height: Int): this(
        Space(width, height,
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

    fun get(c: Space.Coordinate): T? {
        return grid[c.y][c.x]
    }

    operator fun set(i: Int, j: Int, value: T) {
        set(space.coordinate(i, j), value)
    }

    fun set(c: Space.Coordinate, value: T) {
        grid[c.y][c.x] = value
    }

    fun forEach(action: (Space.Coordinate, T?) -> Unit) {
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = space.coordinate(i, j)
                action.invoke(c, get(c))
            }
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
        return object : Iterator<T> {
            var i = 0
            var j = 0
            var next: T? = null

            init {
                seekNext()
            }

            fun advance() {
                i++
                if (i >= width) {
                    i = 0
                    j++
                }
            }

            fun seekNext() {
                do {
                    next = get(i, j)
                    advance()
                } while(next == null && space.within(i, j))
            }

            override fun hasNext(): Boolean {
                return next != null
            }

            override fun next(): T {
                val out = next
                if (out == null) {
                    throw IllegalStateException("No next element in grid!")
                } else {
                    seekNext()
                    return out
                }
            }
        }
    }
}