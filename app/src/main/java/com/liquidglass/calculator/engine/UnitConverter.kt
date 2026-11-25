package com.liquidglass.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val title: String, val subtitle: String) {
    LENGTH("Length", "Meters, km, miles, feet"),
    WEIGHT("Weight", "Kilograms, grams, pounds"),
    TEMPERATURE("Temperature", "Celsius, Fahrenheit, Kelvin"),
    CURRENCY("Currency", "Live & offline exchange rates"),
    AREA("Area", "Sq meters, acres, hectares"),
    VOLUME("Volume", "Liters, gallons, cups"),
    SPEED("Speed", "km/h, mph, m/s, knots"),
    TIME("Time", "Seconds, hours, days, years"),
    DATA("Data", "Bytes, MB, GB, TB"),
    ENERGY("Energy", "Joules, calories, kWh"),
    PRESSURE("Pressure", "Pascals, bar, atm, psi"),
    ANGLE("Angle", "Degrees, radians, gradians")
}

data class UnitDef(
    val id: String,
    val symbol: String,
    val name: String,
    val factorToBase: Double = 1.0
)

object UnitConverter {

    val defaultCurrencies: Map<String, Double> = linkedMapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "INR" to 84.40,
        "JPY" to 154.20,
        "CNY" to 7.24,
        "AUD" to 1.54,
        "CAD" to 1.40,
        "CHF" to 0.88,
        "SGD" to 1.34,
        "AED" to 3.6725,
        "SAR" to 3.75,
        "KRW" to 1398.0,
        "HKD" to 7.78,
        "NZD" to 1.69,
        "SEK" to 10.95,
        "NOK" to 11.05,
        "DKK" to 6.87,
        "BRL" to 5.79,
        "MXN" to 20.35,
        "ZAR" to 18.10,
        "TRY" to 34.50,
        "PLN" to 4.08,
        "THB" to 34.70,
        "IDR" to 15880.0,
        "MYR" to 4.47,
        "PHP" to 58.90,
        "VND" to 25400.0
    )

    private val currencyNames = mapOf(
        "USD" to "US Dollar",
        "EUR" to "Euro",
        "GBP" to "British Pound",
        "INR" to "Indian Rupee",
        "JPY" to "Japanese Yen",
        "CNY" to "Chinese Yuan",
        "AUD" to "Australian Dollar",
        "CAD" to "Canadian Dollar",
        "CHF" to "Swiss Franc",
        "SGD" to "Singapore Dollar",
        "AED" to "UAE Dirham",
        "SAR" to "Saudi Riyal",
        "KRW" to "South Korean Won",
        "HKD" to "Hong Kong Dollar",
        "NZD" to "New Zealand Dollar",
        "SEK" to "Swedish Krona",
        "NOK" to "Norwegian Krone",
        "DKK" to "Danish Krone",
        "BRL" to "Brazilian Real",
        "MXN" to "Mexican Peso",
        "ZAR" to "South African Rand",
        "TRY" to "Turkish Lira",
        "PLN" to "Polish Zloty",
        "THB" to "Thai Baht",
        "IDR" to "Indonesian Rupiah",
        "MYR" to "Malaysian Ringgit",
        "PHP" to "Philippine Peso",
        "VND" to "Vietnamese Dong"
    )

    fun unitsFor(
        category: UnitCategory,
        currencyRates: Map<String, Double> = defaultCurrencies
    ): List<UnitDef> = when (category) {
        UnitCategory.LENGTH -> listOf(
            UnitDef("m", "m", "Meters", 1.0),
            UnitDef("km", "km", "Kilometres", 1000.0),
            UnitDef("cm", "cm", "Centimetres", 0.01),
            UnitDef("mm", "mm", "Millimetres", 0.001),
            UnitDef("um", "µm", "Micrometres", 1e-6),
            UnitDef("nm", "nm", "Nanometres", 1e-9),
            UnitDef("mi", "mi", "Miles", 1609.344),
            UnitDef("yd", "yd", "Yards", 0.9144),
            UnitDef("ft", "ft", "Feet", 0.3048),
            UnitDef("in", "in", "Inches", 0.0254),
            UnitDef("nmi", "nmi", "Nautical Miles", 1852.0)
        )
        UnitCategory.WEIGHT -> listOf(
            UnitDef("kg", "kg", "Kilograms", 1.0),
            UnitDef("g", "g", "Grams", 0.001),
            UnitDef("mg", "mg", "Milligrams", 1e-6),
            UnitDef("t", "t", "Metric Tonnes", 1000.0),
            UnitDef("lb", "lb", "Pounds", 0.45359237),
            UnitDef("oz", "oz", "Ounces", 0.028349523125),
            UnitDef("st", "st", "Stone", 6.35029318)
        )
        UnitCategory.TEMPERATURE -> listOf(
            UnitDef("C", "°C", "Celsius", 1.0),
            UnitDef("F", "°F", "Fahrenheit", 1.0),
            UnitDef("K", "K", "Kelvin", 1.0),
            UnitDef("R", "°R", "Rankine", 1.0)
        )
        UnitCategory.CURRENCY -> {
            val merged = defaultCurrencies.toMutableMap()
            merged.putAll(currencyRates)
            merged.entries.map { (code, rateFromUsd) ->
                UnitDef(
                    id = code,
                    symbol = code,
                    name = currencyNames[code] ?: code,
                    factorToBase = if (rateFromUsd > 0.0) 1.0 / rateFromUsd else 1.0
                )
            }
        }
        UnitCategory.AREA -> listOf(
            UnitDef("sqm", "m²", "Square Meters", 1.0),
            UnitDef("sqkm", "km²", "Square Kilometres", 1_000_000.0),
            UnitDef("sqcm", "cm²", "Square Centimetres", 0.0001),
            UnitDef("ha", "ha", "Hectares", 10_000.0),
            UnitDef("ac", "ac", "Acres", 4046.8564224),
            UnitDef("sqmi", "mi²", "Square Miles", 2_589_988.110336),
            UnitDef("sqyd", "yd²", "Square Yards", 0.83612736),
            UnitDef("sqft", "ft²", "Square Feet", 0.09290304),
            UnitDef("sqin", "in²", "Square Inches", 0.00064516)
        )
        UnitCategory.VOLUME -> listOf(
            UnitDef("L", "L", "Litres", 1.0),
            UnitDef("mL", "mL", "Millilitres", 0.001),
            UnitDef("m3", "m³", "Cubic Meters", 1000.0),
            UnitDef("cm3", "cm³", "Cubic Centimetres", 0.001),
            UnitDef("gal", "gal", "US Gallons", 3.785411784),
            UnitDef("qt", "qt", "US Quarts", 0.946352946),
            UnitDef("pt", "pt", "US Pints", 0.473176473),
            UnitDef("cup", "cup", "US Cups", 0.2365882365),
            UnitDef("floz", "fl oz", "Fluid Ounces", 0.0295735295625),
            UnitDef("impgal", "imp gal", "Imperial Gallons", 4.54609)
        )
        UnitCategory.SPEED -> listOf(
            UnitDef("mps", "m/s", "Meters per second", 1.0),
            UnitDef("kmh", "km/h", "Kilometres per hour", 1.0 / 3.6),
            UnitDef("mph", "mph", "Miles per hour", 0.44704),
            UnitDef("kn", "kn", "Knots", 1852.0 / 3600.0),
            UnitDef("fps", "ft/s", "Feet per second", 0.3048),
            UnitDef("mach", "Ma", "Mach (std)", 340.29)
        )
        UnitCategory.TIME -> listOf(
            UnitDef("s", "s", "Seconds", 1.0),
            UnitDef("ms", "ms", "Milliseconds", 0.001),
            UnitDef("us", "µs", "Microseconds", 1e-6),
            UnitDef("min", "min", "Minutes", 60.0),
            UnitDef("h", "h", "Hours", 3600.0),
            UnitDef("d", "d", "Days", 86400.0),
            UnitDef("wk", "wk", "Weeks", 604800.0),
            UnitDef("mo", "mo", "Months (30.44d)", 2629746.0),
            UnitDef("yr", "yr", "Years (365.25d)", 31557600.0)
        )
        UnitCategory.DATA -> listOf(
            UnitDef("B", "B", "Bytes", 1.0),
            UnitDef("KB", "KB", "Kilobytes (10³)", 1_000.0),
            UnitDef("MB", "MB", "Megabytes (10⁶)", 1_000_000.0),
            UnitDef("GB", "GB", "Gigabytes (10⁹)", 1_000_000_000.0),
            UnitDef("TB", "TB", "Terabytes (10¹²)", 1_000_000_000_000.0),
            UnitDef("KiB", "KiB", "Kibibytes (2¹⁰)", 1024.0),
            UnitDef("MiB", "MiB", "Mebibytes (2²⁰)", 1048576.0),
            UnitDef("GiB", "GiB", "Gibibytes (2³⁰)", 1073741824.0),
            UnitDef("bit", "bit", "Bits", 0.125)
        )
        UnitCategory.ENERGY -> listOf(
            UnitDef("J", "J", "Joules", 1.0),
            UnitDef("kJ", "kJ", "Kilojoules", 1000.0),
            UnitDef("cal", "cal", "Calories", 4.184),
            UnitDef("kcal", "kcal", "Kilocalories", 4184.0),
            UnitDef("Wh", "Wh", "Watt-hours", 3600.0),
            UnitDef("kWh", "kWh", "Kilowatt-hours", 3_600_000.0),
            UnitDef("BTU", "BTU", "British Thermal Units", 1055.05585262)
        )
        UnitCategory.PRESSURE -> listOf(
            UnitDef("Pa", "Pa", "Pascals", 1.0),
            UnitDef("kPa", "kPa", "Kilopascals", 1000.0),
            UnitDef("bar", "bar", "Bar", 100_000.0),
            UnitDef("atm", "atm", "Atmospheres", 101_325.0),
            UnitDef("psi", "psi", "Pounds per sq inch", 6894.757293168),
            UnitDef("mmHg", "mmHg", "Millimetres of Hg", 133.322387415)
        )
        UnitCategory.ANGLE -> listOf(
            UnitDef("deg", "°", "Degrees", 1.0),
            UnitDef("rad", "rad", "Radians", 180.0 / kotlin.math.PI),
            UnitDef("grad", "gon", "Gradians", 0.9),
            UnitDef("arcmin", "′", "Arcminutes", 1.0 / 60.0),
            UnitDef("arcsec", "″", "Arcseconds", 1.0 / 3600.0),
            UnitDef("turn", "tr", "Turns", 360.0)
        )
    }

    fun convert(
        value: Double,
        category: UnitCategory,
        from: UnitDef,
        to: UnitDef
    ): Double {
        if (value.isNaN() || value.isInfinite()) return Double.NaN
        if (from.id == to.id) return value
        if (category == UnitCategory.TEMPERATURE) {
            val celsius = when (from.id) {
                "C" -> value
                "F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                "R" -> (value - 491.67) * 5.0 / 9.0
                else -> value
            }
            return when (to.id) {
                "C" -> celsius
                "F" -> celsius * 9.0 / 5.0 + 32.0
                "K" -> celsius + 273.15
                "R" -> (celsius + 273.15) * 9.0 / 5.0
                else -> celsius
            }
        }
        val base = BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(from.factorToBase), MathContext.DECIMAL128)
        val result = base.divide(BigDecimal.valueOf(to.factorToBase), MathContext.DECIMAL128)
        return result.toDouble()
    }

    fun formatConverted(
        value: Double,
        category: UnitCategory,
        useGrouping: Boolean = true,
        precisionMode: DecimalPrecisionMode = DecimalPrecisionMode.AUTO
    ): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val bd = BigDecimal.valueOf(value)
        if (category == UnitCategory.CURRENCY && precisionMode == DecimalPrecisionMode.AUTO) {
            val rounded = bd.setScale(2, RoundingMode.HALF_UP)
            val pattern = if (useGrouping) "#,##0.00" else "0.00"
            return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(rounded)
        }
        return MathEngine.formatNumber(bd, precisionMode, useGrouping)
    }
}
