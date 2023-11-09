package com.drcorchit.utils.math

import com.drcorchit.utils.math.Compass.Companion.fromComponents
import com.drcorchit.utils.modulus
import com.google.gson.JsonObject
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

class Space(
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val wrapHoriz: Boolean,
    @JvmField val wrapVert: Boolean,
    @JvmField val layout: Layout
) {
    val size = width * height

    fun coordinate(x: Int, y: Int): Coordinate {
        return Coordinate(x, y)
    }

    fun coordinates(vararg coordinates: Pair<Int, Int>): List<Coordinate> {
        return coordinates.map { coordinate(it.first, it.second) }
    }

    inner class Coordinate internal constructor(x: Int, y: Int) {
        @JvmField
        val space = this@Space
        @JvmField
        val x: Int
        @JvmField
        val y: Int

        init {
            this.x = if (wrapHoriz) modulus(x, width) else x
            this.y = if (wrapVert) modulus(y, height) else y
            require(this.x in 0 until width) { "X coordinate out of bounds: $x" }
            require(this.y in 0 until height) { "Y coordinate out of bounds: $y" }
        }

        //Returns the correct distance between coordinates, adjusted for hex layout if necessary.
        fun distance(other: Coordinate): Double {
            var x1: Double
            var y1: Double
            var x2: Double
            var y2: Double
            if (layout === Layout.HEXAGONAL) {
                //Odd numbered rows are right-shifted by .5
                x1 = if (y % 2 == 0) x.toDouble() else x + .5
                x2 = if (other.y % 2 == 0) other.x.toDouble() else other.x + .5
                y1 = y * HEX_VERT_ADJUSTMENT
                y2 = other.y * HEX_VERT_ADJUSTMENT
            } else {
                x1 = x.toDouble()
                x2 = other.x.toDouble()
                y1 = y.toDouble()
                y2 = other.y.toDouble()
            }
            if (wrapHoriz) {
                if (x1 - x2 > width / 2.0) {
                    x2 += width.toDouble()
                } else if (x2 - x1 > width / 2.0) {
                    x1 += width.toDouble()
                }
            }
            if (wrapVert) {
                if (y1 - y2 > height / 2.0) {
                    y2 += height.toDouble()
                } else if (y2 - y1 > height / 2.0) {
                    y1 += height.toDouble()
                }
            }
            return hypot(x1 - x2, y1 - y2)
        }

        fun getNeighbor(dir: Compass): Coordinate? {
            var targetX = x
            var targetY = y
            when (layout) {
                Layout.HEXAGONAL -> {
                    val oddRow = y % 2 == 1
                    when (dir) {
                        Compass.NORTHWEST -> {
                            if (!oddRow) targetX--
                            targetY--
                        }
                        Compass.NORTHEAST -> {
                            if (oddRow) targetX++
                            targetY--
                        }
                        Compass.EAST -> targetX++
                        Compass.SOUTHEAST -> {
                            if (oddRow) targetX++
                            targetY++
                        }
                        Compass.SOUTHWEST -> {
                            if (!oddRow) targetX--
                            targetY++
                        }
                        Compass.WEST -> targetX--
                        else -> return null
                    }
                }
                Layout.ISOMETRIC -> throw UnsupportedOperationException("Not yet implemented")
                Layout.CARTESIAN, Layout.ORTHOGONAL -> {
                    targetX = x + dir.horiz
                    targetY = y + dir.vert
                }
            }
            return if (within(targetX, targetY)) {
                coordinate(targetX, targetY)
            } else {
                null
            }
        }

        fun getWithinRange(range: Int): java.util.HashSet<Coordinate> {
            require(range >= 0) { "range must be non-negative" }
            val output = java.util.HashSet<Coordinate>()
            for (i in x - range..x + range) {
                for (j in y - range..y + range) {
                    if (within(i, j)) {
                        val temp = coordinate(i, j)
                        if (getRange(temp) <= range) output.add(temp)
                    }
                }
            }
            return output
        }

        fun getRing(range: Int): java.util.HashSet<Coordinate>? {
            require(range >= 0) { "range must be non-negative" }
            val output = HashSet<Coordinate>()
            for (i in x - range..x + range) {
                for (j in y - range..y + range) {
                    try {
                        val temp = Coordinate(i, j)
                        if (getRange(temp) == range) output.add(temp)
                    } catch (e: Exception) {
                        //The neighbor would have been out of bounds
                    }
                }
            }
            return output
        }

        fun getRange(other: Coordinate): Int {
            var xDif = Math.abs(x - other.x)
            var yDif = Math.abs(y - other.y)
            if (wrapHoriz && xDif > width / 2) xDif = width - xDif
            if (wrapVert && yDif > height / 2) yDif = width - yDif
            return when (layout) {
                Layout.CARTESIAN -> max(xDif, yDif)
                Layout.HEXAGONAL -> hexRange(other)
                Layout.ORTHOGONAL -> xDif + yDif
                else -> throw IllegalArgumentException()
            }
        }

        //I don't know why this works, but it does.
        private fun hexRange(other: Coordinate): Int {
            var x2 = other.x
            var y2 = other.y
            if (wrapHoriz) {
                if (x - x2 > width / 2) {
                    x2 += width
                } else if (x2 - x > width / 2) {
                    x2 -= width
                }
            }
            if (wrapVert) {
                if (y - y2 > height / 2) {
                    y2 += height
                } else if (y2 - y > height / 2) {
                    y2 -= height
                }
            }
            return rawHexRange(x, y, x2, y2)
        }

        fun getDirection(other: Coordinate): Compass {
            var xDif = (other.x - x).toDouble()
            var yDif = (other.y - y).toDouble()
            if (wrapHoriz && xDif > width / 2.0) xDif -= width.toDouble()
            if (wrapVert && yDif > height / 2.0) yDif -= height.toDouble()
            if (layout === Layout.HEXAGONAL || layout === Layout.ISOMETRIC) {
                val startOdd = y % 2 == 1
                val destOdd = other.y % 2 == 1
                val shift: Double = if (startOdd == destOdd) 0.0 else if (destOdd) .5 else -.5
                xDif += shift
            }
            return fromComponents(xDif, yDif)
        }

        fun serialize(): JsonObject {
            val output = JsonObject()
            output.addProperty("x", x)
            output.addProperty("y", y)
            return output
        }

        override fun toString(): String {
            return "[$x, $y]"
        }

        override fun equals(other: Any?): Boolean {
            if (other is Coordinate) {
                return other.space == space && other.x == x && other.y == y
            }
            return false
        }

        override fun hashCode(): Int {
            return space.hashCode() + x * 10000 + y
        }
    }

    fun parse(s: String): Coordinate {
        val p: Pair<Int, Int> = parseString(s)
        return Coordinate(p.first, p.second)
    }

    fun deserialize(info: JsonObject): Coordinate {
        val x: Int = info.get("x").asInt
        val y: Int = info.get("y").asInt
        return Coordinate(x, y)
    }

    fun within(x: Int, y: Int): Boolean {
        if (!wrapHoriz && (x < 0 || x >= width)) return false
        if (!wrapVert && (y < 0 || y >= height)) return false
        return true
    }

    companion object {

        val HEX_VERT_ADJUSTMENT = sqrt(3.0) / 2 //Based on a 30-60-90 triangle

        private fun floor2(x: Int): Int {
            return if (x >= 0) x shr 1 else (x - 1) / 2
        }

        private fun ceil2(x: Int): Int {
            return if (x >= 0) x + 1 shr 1 else x / 2
        }

        //HexRange but without wrapping
        private fun rawHexRange(c1x: Int, c1y: Int, c2x: Int, c2y: Int): Int {
            val ax: Int = c1x - floor2(c1y)
            val ay: Int = c1x + ceil2(c1y)
            val bx: Int = c2x - floor2(c2y)
            val by: Int = c2x + ceil2(c2y)
            val dx = bx - ax
            val dy = by - ay
            return if (dx * dy > 0) max(abs(dx), abs(dy)) else abs(dx) + abs(dy)
        }

        private fun parseString(s: String): Pair<Int, Int> {
            require(s.matches(Regex("\\s*\\d+\\s*,\\s*\\d+\\s*"))) { "Unparsable coordinate: $s" }
            val parts = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val x = parts[0].trim { it <= ' ' }.toInt()
            val y = parts[1].trim { it <= ' ' }.toInt()
            return Pair(x, y)
        }

        private fun hasSameSign(x: Int, y: Int): Boolean {
            return (x < 0 && y < 0) || (x > 0 && y > 0)
        }
    }
}