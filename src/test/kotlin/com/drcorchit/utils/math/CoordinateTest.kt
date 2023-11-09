package com.drcorchit.utils.math

import com.drcorchit.utils.math.Space.Coordinate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class CoordinateTest {
    @Test
    fun metaTest() {
        val space = Space(100, 100, false, false, Layout.HEXAGONAL)
        for (i in 3..19) {
            for (j in 3..19) {
                val ref = space.coordinate(i, j)
                val results = rangeHelper(ref, 3)
                val expected = if (ref.y % 2 == 0) RES_EVEN else RES_ODD
                Assertions.assertArrayEquals(expected, results)
            }
        }
    }

    @Test
    fun getRangeTest() {
        val space = Space(100, 100, false, false, Layout.HEXAGONAL)
        for (i in 0..19) {
            val ref = getRandomRef(space, 3)
            val results = rangeHelper(ref, 3)
            val expected = if (ref.y % 2 == 0) RES_EVEN else RES_ODD
            Assertions.assertArrayEquals(expected, results)
        }
    }

    @Test
    fun neighborsTest() {
        val space = Space(100, 100, true, false, Layout.HEXAGONAL)
        val points = space.coordinates(34 to 13, 9 to 29, 79 to 50, 99 to 50, 50 to 99, 0 to 0)
        val expected = ArrayList<Set<Coordinate>>()
        expected.add(space.coordinates(34 to 12, 35 to 12, 33 to 13, 34 to 13, 35 to 13, 34 to 14, 35 to 14).toSet())
        expected.add(space.coordinates(9 to 28, 10 to 28, 8 to 29, 9 to 29, 10 to 29, 9 to 30, 10 to 30).toSet())
        expected.add(space.coordinates(78 to 49, 79 to 49, 78 to 50, 79 to 50, 80 to 50, 78 to 51, 79 to 51).toSet())
        expected.add(space.coordinates(99 to 50, 0 to 50, 99 to 49, 98 to 49, 98 to 50, 98 to 51, 99 to 51).toSet())
        expected.add(space.coordinates(50 to 99, 51 to 99, 49 to 99, 51 to 98, 50 to 98).toSet())
        expected.add(space.coordinates(0 to 0, 1 to 0, 99 to 0, 99 to 1, 0 to 1).toSet())
        for (i in points.indices) {
            val point = points[i]
            val result = point.getWithinRange(1)
            Assertions.assertEquals(expected[i], result, "Incorrect neighbors for coordinate $point")
        }
    }

    @Test
    fun getRangeWithinTest() {
        val space = Space(100, 100, false, false, Layout.HEXAGONAL)
        for (i in 0..19) {
            getRangeWithinHelper(getRandomRef(space, 0))
        }
    }

    private fun getRandomRef(space: Space, buffer: Int): Coordinate {
        val r = Random()
        val x = buffer + r.nextInt(space.width - buffer * 2)
        val y = buffer + r.nextInt(space.height - buffer * 2)
        return space.coordinate(x, y)
    }

    companion object {
        private val RES_EVEN = arrayOf(
            arrayOf(4, 4, 3, 3, 3, 4, 4),
            arrayOf(3, 3, 2, 2, 2, 3, 3),
            arrayOf(3, 2, 1, 1, 1, 2, 3),
            arrayOf(3, 2, 1, 0, 1, 2, 3),
            arrayOf(3, 2, 2, 1, 2, 2, 3),
            arrayOf(4, 3, 3, 2, 3, 3, 4),
            arrayOf(5, 4, 4, 3, 4, 4, 5)
        )
        private val RES_ODD = arrayOf(
            arrayOf(5, 4, 4, 3, 4, 4, 5),
            arrayOf(4, 3, 3, 2, 3, 3, 4),
            arrayOf(3, 2, 2, 1, 2, 2, 3),
            arrayOf(3, 2, 1, 0, 1, 2, 3),
            arrayOf(3, 2, 1, 1, 1, 2, 3),
            arrayOf(3, 3, 2, 2, 2, 3, 3),
            arrayOf(4, 4, 3, 3, 3, 4, 4)
        )

        private fun rangeHelper(ref: Coordinate, size: Int): Array<Array<Int?>> {
            val results = Array(1 + 2 * size) { arrayOfNulls<Int>(1 + 2 * size) }
            for (i in 0 until 1 + 2 * size) {
                for (j in 0 until 1 + 2 * size) {
                    val other = ref.space.coordinate(ref.x + i - size, ref.y + j - size)
                    val result = ref.getRange(other)
                    //check reflexivity
                    Assertions.assertEquals(result, other.getRange(ref), "This distance from $ref to $other is not reflexive")
                    results[i][j] = result
                }
            }
            return results
        }

        private fun getRangeWithinHelper(ref: Coordinate) {
            val neighbors = ref.getWithinRange(1)
            for (neighbor in neighbors) {
                if (neighbor == ref) {
                    Assertions.assertEquals(0, ref.getRange(neighbor))
                } else {
                    Assertions.assertEquals(1, ref.getRange(neighbor))
                }
            }
        }
    }
}