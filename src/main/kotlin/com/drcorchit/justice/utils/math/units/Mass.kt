package com.drcorchit.justice.utils.math.units

enum class Mass(override val singular: String, override val plural: String, override val ratio: Double) : Unit {
    GRAM("Gram", "Grams", 1.0),
    KG("Kilogram", "Kilograms", GRAM.ratio * 1000),
    TONNE("Tonne", "Tonnes", KG.ratio * 1000),
    LB("Pound", "Pounds", 453.59),
    TON("Ton","Tons", LB.ratio * 2000)
}