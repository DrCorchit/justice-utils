package com.drcorchit.justice.utils.math.units

enum class Distance(
    override val singular: String,
    override val plural: String,
    override val ratio: Double
) : Unit {
    CM("Centimeter", "Centimeters", 1.0),
    M("Meter", "Meters", CM.ratio * 100),
    KM("Kilometer", "Kilometers", M.ratio * 1000),
    IN("Inch", "Inches", 2.54),
    FT("Foot", "Feet", IN.ratio * 12),
    YD("Yard", "Yards", FT.ratio * 3),
    MI("Mile", "Miles", FT.ratio * 5280);

    //Nudges metric units so that whole numbers of imperial lengths translate to whole numbers of metric units
    enum class Simplified(
        override val singular: String,
        override val plural: String,
        override val ratio: Double
    ) : Unit {
        FT("Foot", "Feet", 1.0),
        IN("Inch", "Inches", FT.ratio / 12),
        YD("Yard", "Yards", FT.ratio * 3),
        MI("Mile", "Miles", FT.ratio * 5280),
        M("Meter", "Meters", FT.ratio * 3),
        CM("Centimeter", "Centimeters", M.ratio / 100),
        KM("Kilometer", "Kilometers", M.ratio * 1000),
    }
}