@file:Suppress("SpellCheckingInspection")

package ftc.rogue.blacksmith.units

interface UnitProvider<T : UnitType> {
    fun get(): T
}

sealed class UnitType

class Burgers : UnitProvider<DistanceUnit> {
    companion object {
        val UNIT = DistanceUnit(3.3)
    }

    override fun get() = UNIT
}
