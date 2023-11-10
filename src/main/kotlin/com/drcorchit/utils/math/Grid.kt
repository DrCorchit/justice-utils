package com.drcorchit.utils.math

class Grid<T>(clazz: Class<T>, val space: Space) {
    private val grid: Array<Array<T>>

    init {
        //height and width are intentionally reversed here.
        //this makes setRow possible with System.arraycopy()
        grid = java.lang.reflect.Array.newInstance(clazz, space.height, space.width) as Array<Array<T>>
    }

    val width: Int
        get() = grid.size

    val height: Int
        get() = grid[0].size

    operator fun get(i: Int, j: Int): T {
        return grid[i][j]
    }

    fun get(c: Space.Coordinate): T {
        return grid[c.x][c.y]
    }

    operator fun set(i: Int, j: Int, value: T) {
        set(space.coordinate(i, j), value)
    }

    fun set(c: Space.Coordinate, value: T) {
        grid[c.x][c.y] = value
    }

    fun forEach(action: (Space.Coordinate, T) -> Unit) {
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = space.coordinate(i, j)
                action.invoke(c, get(c))
            }
        }
    }

    @SafeVarargs
    fun setRow(rowIndex: Int, vararg row: T) {
        if (row.size == grid[rowIndex].size) {
            System.arraycopy(row, 0, grid[rowIndex], 0, row.size)
        } else throw IllegalArgumentException("Number of arguments does not match row size.")
    }

    @SafeVarargs
    fun setColumn(columnIndex: Int, vararg column: T) {
        if (column.size == grid.size) {
            for (i in column.indices) grid[i][columnIndex] = column[i]
        } else throw IllegalArgumentException("Number of arguments does not match column size.")
    }
}