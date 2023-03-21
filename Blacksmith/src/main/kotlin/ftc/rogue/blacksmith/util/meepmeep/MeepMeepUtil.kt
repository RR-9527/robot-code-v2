package ftc.rogue.blacksmith.util.meepmeep

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark
import com.noahbres.meepmeep.core.entity.Entity
import ftc.rogue.blacksmith.units.AngleUnit
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.units.GlobalUnits

class MeepMeepUtil(private val mm: MeepMeep) {
    fun persistence(
        saveInterval: Long = 2000L,
        defaultFilePath: String = "./meepmeep.properties",
    ): MeepMeepPersistence {
        return MeepMeepPersistence(mm, saveInterval, defaultFilePath)
    }

    @JvmOverloads
    fun customMouseCoords(
        distanceUnit: DistanceUnit = GlobalUnits.distance,
        angleUnit: AngleUnit = GlobalUnits.angle,
    ): Entity {
        return MeepMeepCustomMouseCoords.use(mm, distanceUnit, angleUnit)
    }

    @JvmOverloads
    fun botShadowOnHover(
        width: Double, /* Inches */
        height: Double, /* Inches */
        colorScheme: ColorScheme = ColorSchemeRedDark(),
        opacity: Double = .6,
    ): Entity {
        return MeepMeepBotHover.use(mm, width, height, colorScheme, opacity)
    }

    @JvmOverloads
    fun endTangentVisualizer(
        endX: Double = 25.0, /* GlobalUnits */
        endY: Double = endX, /* GlobalUnits */
        txtX: Int = 300, /* Pixels */
        txtY: Int = 200, /* Pixels */
        tangentLength: Double = 20.0, /* GlobalUnits */
        shouldHighlight: Boolean = true, /* IMPORTANT: HIGHLIGHTING CAN CAUSE MEEPMEEP FREEZING AFTER A LITTLE ESP W/ LOW MEMORY */
    ): Entity {
        return MeepMeepEndTangentVisualizer(mm, endX, endY, txtX, txtY, tangentLength, shouldHighlight,).run {
            start()
            get()
        }
    }
}
