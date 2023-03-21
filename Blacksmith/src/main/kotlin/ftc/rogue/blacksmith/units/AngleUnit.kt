package ftc.rogue.blacksmith.units

import kotlin.math.PI

/**
 * [Docs link](https://blacksmithftc.vercel.app/etc/units/angle)
 */
enum class AngleUnit(private val degreesConversionFactor: Double) {
    DEGREES    (degreesConversionFactor = 1.0),
    RADIANS    (degreesConversionFactor = 180.0 / PI),
    GRADIANS   (degreesConversionFactor = 0.9),
    REVOLUTIONS(degreesConversionFactor = 360.0),
    ARCSECONDS (degreesConversionFactor = 360.0 * 60.0),
    ARCMINUTES (degreesConversionFactor = 360.0),
    MILIRADIANS(degreesConversionFactor = 180.0 / PI * 1000.0);

    fun toDeg(x: Number) = x.toDouble() * degreesConversionFactor

    fun toRad(x: Number) = toDeg(x) * PI / 180

    fun to(unit: AngleUnit, x: Number) = unit.toDeg(x) / degreesConversionFactor
}

class AngleUnit2(val degreesConversionFactor: Double)

val Degrees     = AngleUnit2(degreesConversionFactor = 1.0)
val Radians     = AngleUnit2(degreesConversionFactor = 180.0 / PI)
val Gradians    = AngleUnit2(degreesConversionFactor = 0.9)
val Revolutions = AngleUnit2(degreesConversionFactor = 360.0)
val Arcseconds  = AngleUnit2(degreesConversionFactor = 360.0 * 60.0)
val Arcminutes  = AngleUnit2(degreesConversionFactor = 360.0)
val Miliradians = AngleUnit2(degreesConversionFactor = 180.0 / PI * 1000.0)
