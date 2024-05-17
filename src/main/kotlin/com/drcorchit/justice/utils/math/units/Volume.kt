package com.drcorchit.justice.utils.math.units

object VolumeUnits : Units<VolumeUnits.Volume>() {
    class Volume(abbr: String, singular: String, plural: String, ratio: Double) : Unit(abbr, singular, plural, ratio)

    override fun create(abbr: String, singular: String, plural: String, ratio: Double): Volume {
        return Volume(abbr, singular, plural, ratio)
    }

    val DROP = Volume("drops", "Drop", "Drops", 1.0)
    val ML = Volume("ml", "Milliliter", "Milliliters", DROP.ratio * 20)
    val L = Volume("l", "Liter", "Liters", ML.ratio * 1000)
    val OZ = Volume("oz", "Ounce", "Ounces", ML.ratio * 29.57353)
    val CUP = Volume("cup", "Cup", "Cups", OZ.ratio * 8)
    val PT = Volume("pt", "Pint", "Pints", CUP.ratio * 2)
    val QT = Volume("qt", "Quart", "Quarts", PT.ratio * 2)
    val GAL = Volume("gal", "Gallon", "Gallons", QT.ratio * 4)

    override var def = OZ
}