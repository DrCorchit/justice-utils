package com.drcorchit.justice.utils.math.units

object MassUnits : Units<MassUnits.Mass>() {
    class Mass(abbr: String, singular: String, plural: String, ratio: Double) :
        Unit(abbr, singular, plural, ratio)

    override fun create(abbr: String, singular: String, plural: String, ratio: Double): Mass {
        return Mass(abbr, singular, plural, ratio)
    }

    val GRAM = add("g", "Gram", "Grams", 1.0)
    val KG = add("kg", "Kilogram", "Kilograms", GRAM.ratio * 1000)
    val TONNE = add("tn", "Tonne", "Tonnes", KG.ratio * 1000)
    val POUND = add("lbs", "Pound", "Pounds", 453.59)
    val TON = add("ton", "Ton", "Tons", POUND.ratio * 2000)

    override var def = POUND
}