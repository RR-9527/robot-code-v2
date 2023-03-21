package ftc.rogue.blacksmith.util.meepmeep

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorPalette
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueLight
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark
import com.noahbres.meepmeep.roadrunner.Constraints
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.entity.TrajectorySequenceEntity
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import java.util.concurrent.Executors
import kotlin.math.cos
import kotlin.math.sin

class MeepMeepEndTangentVisualizer
    @JvmOverloads
    constructor(
        val mm: MeepMeep,
        val endX: Double = 25.0,
        val endY: Double = endX,
        val txtX: Int = 300,
        val txtY: Int = 200,
        val tangentLength: Double = 20.0,
        val shouldHighlight: Boolean = true,
    ) {

    private var endTangent = 0.0

    private val exec = Executors.newSingleThreadScheduledExecutor()

    private val drive = DriveShim(
        DriveTrainType.MECANUM,
        Constraints(10.0, 10.0, 10.0, 10.0, 10.0),
        Pose2d()
    )

    fun get() =
        object : ReflectedEntity {
            override val tag: String
                get() = "CURR_END_TANGENT"

            override val meepMeep: MeepMeep
                get() = mm

            val sansFont = Class.forName("java.awt.Font")
                .getConstructor(String::class.java, Int::class.java, Int::class.java)
                .newInstance("Sans", 1, 20)

            val txtColor = ColorPalette::class.java
                .getMethod("getGRAY_100")
                .invoke(ColorManager.COLOR_PALETTE)

            override fun render(gfx: GraphicsHelper, canvasWidth: Int, canvasHeight: Int) {
                gfx.setFont(sansFont)
                gfx.setColor(txtColor)
                gfx.drawString("End tangent: $endTangent deg", txtX, txtY)
            }
        }.asEntity()

    fun start() {
        var oldTrajectories = makeTrajectories()

        val cache = mutableMapOf(endTangent to oldTrajectories)

        exec.scheduleAtFixedRate({
            val newTrajectorySequences = cache.getOrPut(endTangent, ::makeTrajectories)

            (0..3).forEach {
                mm.requestToRemoveEntity(oldTrajectories[it])
                mm.requestToAddEntity(newTrajectorySequences[it])
            }
            oldTrajectories = newTrajectorySequences

            endTangent += 2.5
            endTangent %= 360
        }, 0, 50, java.util.concurrent.TimeUnit.MILLISECONDS)
    }

    private fun makeTrajectories() = arrayOf(
        TrajectorySequenceEntity(mm, spline(endTangent), ColorSchemeBlueLight()),
        TrajectorySequenceEntity(mm, tangent(endTangent), ColorSchemeRedDark()),
        TrajectorySequenceEntity(mm, arrowLeft(endTangent), ColorSchemeRedDark()),
        TrajectorySequenceEntity(mm, arrowRight(endTangent), ColorSchemeRedDark()),
    ).onEach { if (shouldHighlight) it.trajectoryProgress = 0.1 }

    private fun spline(endTangent: Double) =
        Anvil.forgeTrajectory(drive, Pose2d())
            .splineTo(endX, endY, endTangent)
            .build<TrajectorySequence>()

    private fun tangent(endTangent: Double): TrajectorySequence {
        val xOffset = tangentLength * cos(endTangent.toRad())
        val yOffset = tangentLength * sin(endTangent.toRad())

        val x1 = endX + xOffset
        val y1 = endY + yOffset

        val x2 = endX - xOffset
        val y2 = endY - yOffset

        return Anvil.forgeTrajectory(drive, GlobalUnits.pos(x1, y1))
            .lineTo(x2, y2)
            .build()
    }

    private fun arrowLeft(endTangent: Double): TrajectorySequence {
        val x1 = endX + tangentLength * cos(endTangent.toRad())
        val y1 = endY + tangentLength * sin(endTangent.toRad())

        val x2 = endX + tangentLength * .8 * cos((endTangent + 15).toRad())
        val y2 = endY + tangentLength * .8 * sin((endTangent + 15).toRad())

        return Anvil.forgeTrajectory(drive, GlobalUnits.pos(x1, y1))
            .lineTo(x2, y2)
            .build()
    }

    private fun arrowRight(endTangent: Double): TrajectorySequence {
        val x1 = endX + tangentLength * cos(endTangent.toRad())
        val y1 = endY + tangentLength * sin(endTangent.toRad())

        val x2 = endX + tangentLength * .8 * cos((endTangent - 15).toRad())
        val y2 = endY + tangentLength * .8 * sin((endTangent - 15).toRad())

        return Anvil.forgeTrajectory(drive, GlobalUnits.pos(x1, y1))
            .lineTo(x2, y2)
            .build()
    }
}
