package com.drcorchit.justice.utils.math.units

object MassUnits : Units<MassUnits.Mass>() {
    class Mass(abbr: String, singular: String, plural: String, ratio: Double) :
        Unit(abbr, singular, plural, ratio)

    override fun create(abbr: String, singular: String, plural: String, ratio: Double): Mass {
        return Mass(abbr, singular, plural, ratio)
    }

    val GRAM = Mass("g", "Gram", "Grams", 1.0)
    val KG = Mass("kg", "Kilogram", "Kilograms", GRAM.ratio * 1000)
    val TONNE = Mass("tn", "Tonne", "Tonnes", KG.ratio * 1000)
    val POUND = Mass("lbs", "Pound", "Pounds", 453.59)
    val TON = Mass("ton", "Ton", "Tons", POUND.ratio * 2000)

    override var def = POUND
}