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

    val SEC = Time("", "Second", "Seconds", 1.0)
    val ROUND = Time("", "Round", "Rounds", SEC.ratio * 5)
    val MIN = Time("", "Minute", "Minutes", SEC.ratio * 60)
    val HOUR = Time("", "Hour", "Hours", MIN.ratio * 60)
    val DAY = Time("", "Day", "Days", HOUR.ratio * 24)
    val MONTH = Time("", "Month", "Months", DAY.ratio * 30)
    val YEAR = Time("", "Year", "Years", DAY.ratio * 365)
    val CENTURY = Time("", "Century", "Centuries", YEAR.ratio * 100)
    val MILLENNIA = Time("", "Millennium", "Millennia", YEAR.ratio * 1000)

    override var def = SEC

}

