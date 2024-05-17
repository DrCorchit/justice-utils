package com.drcorchit.justice.utils.math.units

enum class Volume(
    override val singular: String,
    override val plural: String,
    override val ratio: Double
) : Unit {
    DROP("Drop", "Drops", 1.0),
    ML("Milliliter", "Milliliters", DROP.ratio * 20),
    L("Liter", "Liters", ML.ratio * 1000),
    OZ("Ounce", "Ounces", ML.ratio * 29.57353),
    CUP("Cup", "Cups", OZ.ratio * 8),
    PT("Pint", "Pints", CUP.ratio * 2),
    QT("Quart", "Quarts", PT.ratio * 2),
    GAL("Gallon", "Gallons", QT.ratio * 4),
}