package com.drcorchit.utils.math

import com.google.gson.JsonObject
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

class Space(val width: Int, val height: Int, val wrapHoriz: Boolean, val wrapVert: Boolean, val layout: Layout) {
    val size = width * height

    inner class Coordinate(val x: Int, val y: Int) {
        val space = this@Space

        init {
            require(!(x < 0 || x >= width)) { "X coordinate out of bounds: $x" }

            require(!(y < 0 || y >= height)) { "Y coordinate out of bounds: $y" }
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