package com.drcorchit.justice.utils.math

enum class Layout {
    CARTESIAN,  //8-way movement
    HEXAGONAL,  //6-way movement, odd numbered rows are right-shifted by .5
    ISOMETRIC,  //8-way movement, odd numbered rows are right-shifted by .5
    ORTHOGONAL;
}