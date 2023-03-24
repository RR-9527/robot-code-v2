package ftc.rogue.blacksmith.util.meepmeep

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.core.util.FieldUtil
import ftc.rogue.blacksmith.units.AngleUnit
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.meepmeep.utils.GraphicsHelper
import ftc.rogue.blacksmith.util.meepmeep.utils.ReflectedEntity
import ftc.rogue.blacksmith.util.meepmeep.utils.getCanvasMouseSupplier

object MeepMeepCustomMouseCoords {
    @JvmOverloads
    fun use(
        mm: MeepMeep,
        distanceUnit: DistanceUnit = GlobalUnits.distance,
        angleUnit: AngleUnit = GlobalUnits.angle,
    ): Entity {
        val c = mm.canvas

        val height = c::class.java.getMethod("getHeight").invoke(c) as Int

        val mouseCoordsSupplier = getCanvasMouseSupplier(mm)

        val sansFont = GraphicsHelper.newFont("Sans", 1, 20)

        val txtColor = GraphicsHelper.getColor("GRAY_100")

        return object : ReflectedEntity {
            override val meepMeep = mm

            override val tag = "CUSTOM_UNITS_MOUSE_COORDS"

            override fun render(gfx: GraphicsHelper, canvasWidth: Int, canvasHeight: Int) {
                val mouseCoords = mouseCoordsSupplier()
                val mouseToFieldCoords = FieldUtil.screenCoordsToFieldCoords(mouseCoords)

                gfx.setFont(sansFont)
                gfx.setColor(txtColor)

                var str = "(%s: %4.1f, %4.1f)".format(
                    distanceUnit::class.java.simpleName.lowercase(),
                    DistanceUnit.INCHES.toOtherDistanceUnit(mouseToFieldCoords.x, distanceUnit),
                    DistanceUnit.INCHES.toOtherDistanceUnit(mouseToFieldCoords.y, distanceUnit),
                )

                if (MeepMeepBotHover.heading.isFinite()) {
                    str += " (%s: %3.1f)".format(
                        distanceUnit::class.java.simpleName.lowercase(),
                        AngleUnit.DEGREES.to(angleUnit, MeepMeepBotHover.heading),
                    )
                }

                gfx.drawString(str, 10, height - 30)
            }
        }.asEntity()
    }
}
