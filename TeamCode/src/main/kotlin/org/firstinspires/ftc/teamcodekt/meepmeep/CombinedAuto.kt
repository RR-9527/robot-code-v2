package org.firstinspires.ftc.teamcodekt.meepmeep

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.MeepMeep.Background
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorPalette
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.meepmeep.MeepMeepPersistence
import ftc.rogue.blacksmith.util.toCm
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants.*
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException
import java.lang.reflect.Proxy

fun main() {
    val mm = MeepMeep(800, 60)

    val c = mm.canvas

    val b = c::class.java.getMethod("getBufferStrat").invoke(c)
    val g = b::class.java.getMethod("getDrawGraphics").invoke(b)

    val height = c::class.java.getMethod("getHeight").invoke(c) as Int

    val someInterface = Class.forName("java.awt.event.MouseMotionListener")

    val canvasMouseX = mm::class.java.getDeclaredField("canvasMouseX")
        .also { it.isAccessible = true }
    val canvasMouseY = mm::class.java.getDeclaredField("canvasMouseY")
        .also { it.isAccessible = true }

    val drawString = g::class.java.getMethod("drawString", String::class.java, Int::class.java, Int::class.java)

    val setFont = g::class.java.methods.first { it.name == "setFont" }
    val setColor = g::class.java.methods.first { it.name == "setColor" }

    val font = Class.forName("java.awt.Font").getConstructor(String::class.java, Int::class.java, Int::class.java)

    val sans = font.newInstance("Sans", 1, 14)

    val instance = Proxy.newProxyInstance(someInterface.classLoader, arrayOf(someInterface)) { proxy, method, args ->
        if (method.name == "mouseMoved") {
            val mouseToFieldCoords = FieldUtil.screenCoordsToFieldCoords(
                Vector2d(
                    (canvasMouseX.get(mm) as Int).toDouble(),
                    (canvasMouseY.get(mm) as Int).toDouble(),
                )
            )

            setFont(g, sans)
            setColor(g, ColorPalette::class.java.getMethod("getGRAY_100").invoke(ColorManager.COLOR_PALETTE))

            drawString.invoke(g,
                "(%.1f, %.1f)".format(
                    mouseToFieldCoords.x.toCm(from = DistanceUnit.INCHES),
                    mouseToFieldCoords.y.toCm(from = DistanceUnit.INCHES),
                ), 10, height - 21)
        }
    }

    mm.canvas::class.java
        .methods
        .first { it.name == "addMouseMotionListener" }
        .invoke(mm.canvas, instance)

    MeepMeepPersistence(mm).restore()

    val bot = DefaultBotBuilder(mm)
        .setColorScheme(ColorSchemeBlueDark())
        .setConstraints(52.0, MAX_ACCEL, MAX_ANG_VEL, MAX_ANG_ACCEL, TRACK_WIDTH)
        .setDimensions(12.0, 12.0)
        .followTrajectorySequence(::mainTraj)

    mm.setBackground(Background.FIELD_POWERPLAY_OFFICIAL)
        .setDarkMode(true)
        .setBackgroundAlpha(0.95f)
        .addEntity(bot)
        .start()
}

private val startPose = GlobalUnits.pos(-86.25, -163, 90)

private fun mainTraj(drive: DriveShim) =
    Anvil.forgeTrajectory(drive, startPose)
        .initialGoToDeposit()
        .awaitDeposit()

        .doTimes(RogueBaseAuto.NUM_CYCLES) {
            goToIntake(it)
            awaitIntake()
            goToDeposit(it)
            awaitDeposit()
        }

        .apply { when (1) {
            1 -> inReverse {
                splineTo(-143, -30, 180)
            }
            2 -> inReverse {
                splineTo(-87, -30, 180)
            }
            3 -> inReverse {
                splineTo(-27, -30, 90)
            }
        } }

        .build<TrajectorySequence>()

private fun Anvil.initialGoToDeposit() = this
    .splineTo(-77, -46, -40)

private fun Anvil.goToDeposit(it: Int) = when (it) {
    in 0..4 -> {
        splineTo(-120, -30, -0)
        splineTo(-19, -49, -28)
    }
    else -> throw CycleException()
}

private fun Anvil.goToIntake(it: Int) = when (it) {
    in 0..4 -> {
        splineTo(-120, -30, 180)
        splineTo(-157, -30, 180)
    }
    else -> throw CycleException()
}.doInReverse(2)

private fun Anvil.awaitIntake() = this
    .waitTime(350)

private fun Anvil.awaitDeposit() = this
    .waitTime(250)
