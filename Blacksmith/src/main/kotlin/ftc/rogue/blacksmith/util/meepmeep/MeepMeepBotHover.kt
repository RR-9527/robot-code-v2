package ftc.rogue.blacksmith.util.meepmeep

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueLight
import com.noahbres.meepmeep.core.entity.BotEntity
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.core.util.FieldUtil
import ftc.rogue.blacksmith.internal.util.invokeMethodI
import ftc.rogue.blacksmith.units.AngleUnit
import ftc.rogue.blacksmith.util.toRad
import java.lang.reflect.Proxy
import kotlin.math.PI

object MeepMeepBotHover {
    var heading = Double.NaN
        private set

    @JvmOverloads
    fun use(
        mm: MeepMeep,
        width: Double,
        height: Double,
        colorScheme: ColorScheme = ColorSchemeBlueLight(),
        opacity: Double = .3,
    ): Entity {
        if (opacity !in 0.0..1.0) {
            throw IllegalArgumentException("Opacity not in range 0..1")
        }

        heading = 0.0

        val canvasMouseX = mm::class.java.getDeclaredField("canvasMouseX")
            .also { it.isAccessible = true }

        val canvasMouseY = mm::class.java.getDeclaredField("canvasMouseY")
            .also { it.isAccessible = true }

        val instance = Class.forName("java.awt.event.MouseWheelListener")

        val listener = Proxy.newProxyInstance(instance.classLoader, arrayOf(instance)) { _, method, args ->
            if (method.name != "mouseWheelMoved")
                return@newProxyInstance null

            val notches = args[0].invokeMethodI<Int>("getWheelRotation")

            val isShiftPressed = args[0].invokeMethodI<Int>("getModifiers") and 1 == 1

            heading = (heading - if (isShiftPressed) {
                notches
            } else {
                notches * 20
            }) % 360

            null
        }

        mm.canvas::class.java
            .methods
            .first { it.name == "addMouseWheelListener" }
            .invoke(mm.canvas, listener)

        return object : BotEntity(
            mm, width, height, Pose2d(), colorScheme, opacity
        ) {
            override fun update(deltaTime: Long) {
                val x = (canvasMouseX.get(mm) as Int).toDouble()
                val y = (canvasMouseY.get(mm) as Int).toDouble()
                val r = heading.toRad(from = AngleUnit.DEGREES)

                val inches = FieldUtil.screenCoordsToFieldCoords(Vector2d(x, y))

                pose = Pose2d(inches.x, inches.y, r)
            }
        }
    }
}
