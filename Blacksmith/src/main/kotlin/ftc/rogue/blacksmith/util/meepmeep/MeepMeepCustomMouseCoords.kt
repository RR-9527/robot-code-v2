package ftc.rogue.blacksmith.util.meepmeep

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorPalette
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.core.util.FieldUtil
import ftc.rogue.blacksmith.units.AngleUnit
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.units.GlobalUnits

object MeepMeepCustomMouseCoords {
    @JvmOverloads
    fun use(
        mm: MeepMeep,
        distanceUnit: DistanceUnit = GlobalUnits.distance,
        angleUnit: AngleUnit = GlobalUnits.angle,
    ): Entity {
        val c = mm.canvas

        val height = c::class.java.getMethod("getHeight").invoke(c) as Int

        val canvasMouseX = mm::class.java.getDeclaredField("canvasMouseX")
            .also { it.isAccessible = true }

        val canvasMouseY = mm::class.java.getDeclaredField("canvasMouseY")
            .also { it.isAccessible = true }

        val sansFont = Class.forName("java.awt.Font")
            .getConstructor(String::class.java, Int::class.java, Int::class.java)
            .newInstance("Sans", 1, 20)

        val txtColor = ColorPalette::class.java
            .getMethod("getGRAY_100")
            .invoke(ColorManager.COLOR_PALETTE)

        return object : ReflectedEntity {
            override val meepMeep = mm

            override val tag = "CUSTOM_UNITS_MOUSE_COORDS"

            override fun render(gfx: GraphicsHelper, canvasWidth: Int, canvasHeight: Int) {
                val fieldCoords = Vector2d(
                    (canvasMouseX.get(mm) as Int).toDouble(),
                    (canvasMouseY.get(mm) as Int).toDouble(),
                )

                val mouseToFieldCoords = FieldUtil.screenCoordsToFieldCoords(fieldCoords)

                gfx.setFont(sansFont)
                gfx.setColor(txtColor)

                var str = "(%s: %4.1f, %4.1f)".format(
                    distanceUnit.name.lowercase(),
                    DistanceUnit.INCHES.toOtherDistanceUnit(mouseToFieldCoords.x, distanceUnit),
                    DistanceUnit.INCHES.toOtherDistanceUnit(mouseToFieldCoords.y, distanceUnit),
                )

                if (MeepMeepBotHover.heading.isFinite()) {
                    str += " (%s: %3.1f)".format(
                        angleUnit.name.lowercase(),
                        AngleUnit.DEGREES.to(angleUnit, MeepMeepBotHover.heading),
                    )
                }

                gfx.drawString(str, 10, height - 30)
            }
        }.asEntity()
    }
}
