package com.drcorchit.justice.utils.math.units

object DistanceUnits : Units<DistanceUnits.Distance>() {
    override fun create(abbr: String, singular: String, plural: String, ratio: Double): Distance {
        return Distance(abbr, singular, plural, ratio)
    }

    class Distance(abbr: String, singular: String, plural: String, ratio: Double) :
        Unit(abbr, singular, plural, ratio)

    val CM = add("cm", "Centimeter", "Centimeters", 1.0)
    val M = add("m", "Meter", "Meters", CM.ratio * 100)
    val KM = add("km", "Kilometer", "Kilometers", M.ratio * 1000)
    val IN = add("in", "Inch", "Inches", 2.54)
    val FT = add("ft", "Foot", "Feet", IN.ratio * 12)
    val YD = add("yd", "Yard", "Yards", FT.ratio * 3)
    val MI = add("mi", "Mile", "Miles", FT.ratio * 5280)

    override var def = FT
}

object SimplifiedDistanceUnits : Units<SimplifiedDistanceUnits.SimplifiedDistance>() {
    override fun create(abbr: String, singular: String, plural: String, ratio: Double): SimplifiedDistance {
        return SimplifiedDistance(abbr, singular, plural, ratio)
    }

    class SimplifiedDistance(abbr: String, singular: String, plural: String, ratio: Double) :
        Unit(abbr, singular, plural, ratio)

    val FT = add("ft", "Foot", "Feet", 1.0)
    val IN = add("in", "Inch", "Inches", FT.ratio / 12)
    val YD = add("yd", "Yard", "Yards", FT.ratio * 3)
    val MI = add("mi", "Mile", "Miles", FT.ratio * 5280)
    val M = add("m", "Meter", "Meters", FT.ratio * 3)
    val CM = add("cm", "Centimeter", "Centimeters", M.ratio / 100)
    val KM = add("km", "Kilometer", "Kilometers", M.ratio * 1000)

    override var def = FT
}