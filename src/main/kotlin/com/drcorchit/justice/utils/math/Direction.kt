package com.drcorchit.justice.utils.math

import com.badlogic.gdx.utils.Align
import com.google.common.collect.ImmutableList
import java.util.*

enum class Compass(val textAlign: Int, val horiz: Int, val vert: Int) {
    NORTHWEST(Align.right, -1, 1),
    NORTH(Align.center, 0, 1),
    NORTHEAST(Align.left, 1, 1),
    EAST(Align.left, 1, 0),
    CENTER(Align.center, 0, 0),
    WEST(Align.right, -1, 0),
    SOUTHWEST(Align.right, -1, -1),
    SOUTH(Align.center, 0, -1),
    SOUTHEAST(Align.left, 1, -1);

    val percentVert = (vert + 1) / 2f
    val percentHoriz = (horiz + 1) / 2f

    companion object {
        var DEFAULT = SOUTHEAST
        val HEX_DIRS: ImmutableList<Compass> = ImmutableList.of(EAST, NORTHEAST, NORTHWEST, WEST, SOUTHWEST, SOUTHEAST)
        val CARDINAL_DIRS: ImmutableList<Compass> =
            ImmutableList.of(EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST)

        fun fromComponents(horiz: Double, vert: Double): Compass {
            return if (horiz < 0) {
                if (vert < 0) NORTHWEST else if (vert > 0) SOUTHWEST else WEST
            } else if (horiz > 0) {
                if (vert < 0) NORTHEAST else if (vert > 0) SOUTHEAST else EAST
            } else {
                if (vert < 0) NORTH else if (vert > 0) SOUTH else DEFAULT
            }
        }

        fun fromAbbreviation(abbrev: String): Compass {
            return when (abbrev.lowercase(Locale.getDefault())) {
                "n" -> NORTH
                "ne" -> NORTHEAST
                "e" -> EAST
                "es", "se" -> SOUTHEAST
                "s" -> SOUTH
                "sw" -> SOUTHWEST
                "w" -> WEST
                "nw", "wn" -> NORTHWEST
                else -> throw IllegalArgumentException("Unknown compass direction: $abbrev")
            }
        }
    }
}