package ftc.rogue.blacksmith.units

/**
 * [Docs link](https://blacksmithftc.vercel.app/etc/units/time)
 */
//enum class TimeUnit(private val msConversionFactor: Double) {
//    NANOSECONDS (msConversionFactor = 1.0 / 1e+6),
//    MICROSECONDS(msConversionFactor = 1.0 / 1e+3),
//    MILLISECONDS(msConversionFactor = 1.0),
//    SECONDS     (msConversionFactor = 1000.0),
//    MINUTES     (msConversionFactor = 60000.0),
//    HOURS       (msConversionFactor = 3600000.0),
//    DAYS        (msConversionFactor = 86400000.0),
//    WEEKS       (msConversionFactor = 604800000.0),
//    MONTHS      (msConversionFactor = 2.628e+9),
//    YEARS       (msConversionFactor = 3.154e+10),
//    CENTURIES   (msConversionFactor = 3.154e+11),
//    MILLENNIA   (msConversionFactor = 3.154e+12);
//
//    fun toSec(x: Number) = x.toDouble() * msConversionFactor / 1000.0
//
//    fun toMs(x: Number) = (x.toDouble() * msConversionFactor).toInt()
//
//    fun to(unit: TimeUnit, x: Number) = unit.toMs(x) / msConversionFactor
//}

class TimeUnit(val millisecConversionFactor: Double) : UnitType() {
    companion object {
        @JvmField val NANOSECONDS  = TimeUnit(millisecConversionFactor = 1.0 / 1e+6)
        @JvmField val MICROSECONDS = TimeUnit(millisecConversionFactor = 1.0 / 1e+3)
        @JvmField val MILLISECONDS = TimeUnit(millisecConversionFactor = 1.0)
        @JvmField val SECONDS      = TimeUnit(millisecConversionFactor = 1000.0)
        @JvmField val MINUTES      = TimeUnit(millisecConversionFactor = 60000.0)
        @JvmField val HOURS        = TimeUnit(millisecConversionFactor = 3600000.0)
        @JvmField val DAYS         = TimeUnit(millisecConversionFactor = 86400000.0)
        @JvmField val WEEKS        = TimeUnit(millisecConversionFactor = 604800000.0)
        @JvmField val MONTHS       = TimeUnit(millisecConversionFactor = 2.628e+9)
        @JvmField val YEARS        = TimeUnit(millisecConversionFactor = 3.154e+10)
        @JvmField val CENTURIES    = TimeUnit(millisecConversionFactor = 3.154e+11)
        @JvmField val MILLENNIA    = TimeUnit(millisecConversionFactor = 3.154e+12)
    }

    fun toSec(x: Number) = x.toDouble() * millisecConversionFactor / 1000.0

    fun toMs(x: Number) = (x.toDouble() * millisecConversionFactor).toInt()

    fun to(unit: TimeUnit, x: Number) = unit.toMs(x) / millisecConversionFactor
}
