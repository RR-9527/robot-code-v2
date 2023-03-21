@file:Suppress("SpellCheckingInspection")

package ftc.rogue.blacksmith.units

/**
 * [Docs link](https://blacksmithftc.vercel.app/etc/units/distance)
 */
enum class DistanceUnit(private val inchConversionFactor: Double) {
    CENTIMETERS   (inchConversionFactor = 1.0 / 2.54),
    INCHES        (inchConversionFactor = 1.0),
    METERS        (inchConversionFactor = 1.0 / 0.0254),
    FEET          (inchConversionFactor = 12.0),
    YARDS         (inchConversionFactor = 36.0),
    MILES         (inchConversionFactor = 63360.0),
    KILOMETERS    (inchConversionFactor = 39370.0),
    NAUTICAL_MILES(inchConversionFactor = 72913.4),
    LIGHT_YEARS   (inchConversionFactor = 5.878e+25),
    PARSECS       (inchConversionFactor = 1.97e+26),
    ANGSTROMS     (inchConversionFactor = 1.0 / 2.54e-8),
    FURLONGS      (inchConversionFactor = 7920.0),
    FERMATS       (inchConversionFactor = 1.0 / 2.54e-15),
    SMOOTS        (inchConversionFactor = 1 / 67.0),
    AUS           (inchConversionFactor = 1.0 / 0.00000484813681109536),
    FATHOMS       (inchConversionFactor = 6.0 * 12.0),
    HANDS         (inchConversionFactor = 4.0 * 12.0),
    LINKS         (inchConversionFactor = 0.01 * 12.0),
    PACES         (inchConversionFactor = 5.0 * 12.0),
    RODS          (inchConversionFactor = 16.5 * 12.0),
    SPANS         (inchConversionFactor = 9.0 * 12.0),
    LEAGUES       (inchConversionFactor = 3.0 * 63360.0);

    fun toIn(x: Number) = x.toDouble() * inchConversionFactor

    fun toCm(x: Number) = x.toDouble() * inchConversionFactor * 2.54

    fun toOtherDistanceUnit(x: Number, other: DistanceUnit) = x.toDouble() * (inchConversionFactor / other.inchConversionFactor)
}

class DistanceUnit2(val inchConversionFactor: Double)

val Centimeters   = DistanceUnit2(inchConversionFactor = 1.0 / 2.54)
val Inches        = DistanceUnit2(inchConversionFactor = 1.0)
val Meters        = DistanceUnit2(inchConversionFactor = 1.0 / 0.0254)
val Feet          = DistanceUnit2(inchConversionFactor = 12.0)
val Yards         = DistanceUnit2(inchConversionFactor = 36.0)
val Miles         = DistanceUnit2(inchConversionFactor = 63360.0)
val Kilometers    = DistanceUnit2(inchConversionFactor = 39370.0)
val NauticalMiles = DistanceUnit2(inchConversionFactor = 72913.4)
val LightYears    = DistanceUnit2(inchConversionFactor = 5.878e+25)
val Parsecs       = DistanceUnit2(inchConversionFactor = 1.97e+26)
val Angstroms     = DistanceUnit2(inchConversionFactor = 1.0 / 2.54e-8)
val Furlongs      = DistanceUnit2(inchConversionFactor = 7920.0)
val Fermats       = DistanceUnit2(inchConversionFactor = 1.0 / 2.54e-15)
val Smoots        = DistanceUnit2(inchConversionFactor = 1 / 67.0)
val Aus           = DistanceUnit2(inchConversionFactor = 1.0 / 0.00000484813681109536)
val Fathoms       = DistanceUnit2(inchConversionFactor = 6.0 * 12.0)
val Hands         = DistanceUnit2(inchConversionFactor = 4.0 * 12.0)
val Links         = DistanceUnit2(inchConversionFactor = 0.01 * 12.0)
val Paces         = DistanceUnit2(inchConversionFactor = 5.0 * 12.0)
val Rods          = DistanceUnit2(inchConversionFactor = 16.5 * 12.0)
val Spans         = DistanceUnit2(inchConversionFactor = 9.0 * 12.0)
val Leagues       = DistanceUnit2(inchConversionFactor = 3.0 * 63360.0)
