package com.drcorchit.justice.utils.math.units

object TimeUnits : Units<TimeUnits.Time>() {
    class Time(
        abbr: String,
        singular: String,
        plural: String,
        ratio: Double
    ) : Unit(abbr, singular, plural, ratio)

    override fun create(abbr: String, singular: String, plural: String, ratio: Double): Time {
        return Time(abbr, singular, plural, ratio)
    }

    val SEC = add("", "Second", "Seconds", 1.0)
    val ROUND = add("", "Round", "Rounds", SEC.ratio * 5)
    val MIN = add("", "Minute", "Minutes", SEC.ratio * 60)
    val HOUR = add("", "Hour", "Hours", MIN.ratio * 60)
    val DAY = add("", "Day", "Days", HOUR.ratio * 24)
    val MONTH = add("", "Month", "Months", DAY.ratio * 30)
    val YEAR = add("", "Year", "Years", DAY.ratio * 365)
    val CENTURY = add("", "Century", "Centuries", YEAR.ratio * 100)
    val MILLENNIA = add("", "Millennium", "Millennia", YEAR.ratio * 1000)

    override var def = SEC

}

