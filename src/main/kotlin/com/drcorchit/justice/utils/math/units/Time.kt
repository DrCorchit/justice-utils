package com.drcorchit.justice.utils.math.units

enum class Time(override val singular: String, override val plural: String, override val ratio: Double) : Unit {
    SEC("Second", "Seconds", 1.0),
    MIN("Minute", "Minutes", SEC.ratio * 60),
    HOUR("Hour", "Hours", MIN.ratio * 60),
    DAY("Day", "Days", HOUR.ratio * 24),
    MONTH("Month", "Months", DAY.ratio * 30),
    YEAR("Year", "Years", DAY.ratio * 365),
    CENTURY("Century", "Centuries", YEAR.ratio * 100),
    MILLENNIA("Millennium", "Millennia", YEAR.ratio * 1000)
}