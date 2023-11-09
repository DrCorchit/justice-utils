package com.drcorchit.utils.math

import com.badlogic.gdx.utils.Align
import com.google.common.collect.ImmutableList

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
        val HEX_DIRS: ImmutableList<Compass> = ImmutableList.of(EAST, NORTHEAST, NORTHWEST, WEST, SOUTHWEST, SOUTHEAST)
        val CARDINAL_DIRS: ImmutableList<Compass> =
            ImmutableList.of(EAST, NORTHEAST, NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST)
    }
}