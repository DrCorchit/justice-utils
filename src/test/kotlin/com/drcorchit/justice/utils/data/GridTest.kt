package com.drcorchit.justice.utils.data

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GridTest {

    @Test
    fun dimensionsTest() {
        val grid: Grid<String> = Grid(10, 2)

        Assertions.assertEquals(10, grid.width)
        Assertions.assertEquals(2, grid.height)
        Assertions.assertEquals(20, grid.size)
        grid[9, 1] = "Test"
        Assertions.assertEquals("Test", grid[9, 1])
    }

    @Test
    fun iteratorTest() {
        val grid: Grid<String> = Grid(3, 3)
        grid[0, 0] = "a"
        grid[2, 1] = "b"
        grid[1, 2] = "c"
        Assertions.assertEquals(listOf("a", "b", "c"), grid.toList())
    }

    @Test
    fun setRowTest() {
        val grid: Grid<String> = Grid(3, 3)
        grid.setRow(1, "a", "b", "c")
        Assertions.assertEquals("a", grid[0, 1])
        Assertions.assertEquals("b", grid[1, 1])
        Assertions.assertEquals("c", grid[2, 1])
    }

    @Test
    fun setColumnTest() {
        val grid: Grid<String> = Grid(3, 3)
        grid.setColumn(1, "a", "b", "c")
        Assertions.assertEquals("a", grid[1, 0])
        Assertions.assertEquals("b", grid[1, 1])
        Assertions.assertEquals("c", grid[1, 2])
    }
}