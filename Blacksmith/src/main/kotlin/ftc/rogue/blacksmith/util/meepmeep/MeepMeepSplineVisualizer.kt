package ftc.rogue.blacksmith.util.meepmeep

import android.provider.Settings.Global
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueLight
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.Constraints
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.entity.TrajectorySequenceEntity
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MeepMeepSplineVisualizer
    private constructor (
        val mm: MeepMeep,
        var startVec: Vector2d,         /* Stored & displayed in GlobalUnits */
        var endVec: Vector2d,           /* Stored & displayed in GlobalUnits */
        val textCoords: Pair<Int, Int>, /* Stored & displayed in pixels */
        tangentLength: Double,          /* Stored & displayed in GlobalUnits */
        private val shouldHighlight: Boolean,
        private val shouldInitiallyRotate: Boolean,
    ) {
    private val tangentLength = GlobalUnits.distance.toIn(tangentLength)

    private var endTangent = 0.0
    private var heading = 90.0

    private val drive = DriveShim(
        DriveTrainType.MECANUM,
        Constraints(10.0, 10.0, 10.0, 10.0, 10.0),
        Pose2d()
    )

    fun get() =
        object : ReflectedEntity {
            override val tag: String
                get() = "SPLINE_VISUALIZER"

            override val meepMeep: MeepMeep
                get() = mm

            val sansFont = GraphicsHelper.newFont("Sans", 1, 20)

            val txtColor = GraphicsHelper.getColor("GRAY_100")

            override fun render(gfx: GraphicsHelper, canvasWidth: Int, canvasHeight: Int) {
                gfx.setFont(sansFont)
                gfx.setColor(txtColor)
                gfx.drawString("End tangent: $endTangent deg", textCoords.first, textCoords.second)
            }
        }.asEntity()

    fun start() {
        var oldTrajectories = makeTrajectories()

        fun update() {
            val newTrajectorySequences = makeTrajectories()

            (0..3).forEach {
                mm.requestToRemoveEntity(oldTrajectories[it])
                mm.requestToAddEntity(newTrajectorySequences[it])
            }
            oldTrajectories = newTrajectorySequences
        }

        val exec = Executors.newSingleThreadScheduledExecutor()

        var shouldRotate = shouldInitiallyRotate

        exec.scheduleAtFixedRate({
            update()

            if (shouldRotate) {
                endTangent += 2.5
                endTangent %= 360
            }
        }, 0, 50, TimeUnit.MILLISECONDS)

        val mouseCoordsSupplier = getCanvasMouseSupplier(mm)

        scrollWheelMovedListener(mm) { notches, isShiftPressed, _ ->
            val sCoords = mouseCoordsSupplier()
            val fCoords = FieldUtil.screenCoordsToFieldCoords(sCoords)

            val endDiff   = endVec   - fCoords
            val startDiff = startVec - fCoords

            if (abs(endDiff.x) < 5 && abs(endDiff.y) < 5) {
                shouldRotate = false

                endTangent = (endTangent - if (isShiftPressed) {
                    notches
                } else {
                    notches * 20
                }) % 360
            } else if (abs(startDiff.x) < 5 && abs(startDiff.y) < 5) {
                heading = (heading - if (isShiftPressed) {
                    notches
                } else {
                    notches * 20
                }) % 360
            }
        }

        mouseDraggedListener(mm) { x, y ->
            val sCoords = Vector2d(x.toDouble(), y.toDouble())
            val fCoords = FieldUtil.screenCoordsToFieldCoords(sCoords)

            val endDiff   = endVec   - fCoords
            val startDiff = startVec - fCoords

            if (abs(endDiff.x) < 5 && abs(endDiff.y) < 5) {
                endVec   = fCoords
            } else if (abs(startDiff.x) < 5 && abs(startDiff.y) < 5) {
                startVec = fCoords
            }
        }
    }

    private fun makeTrajectories() = arrayOf(
        TrajectorySequenceEntity(mm, spline(), ColorSchemeBlueLight()),
        TrajectorySequenceEntity(mm, tangent(), ColorSchemeRedDark()),
        TrajectorySequenceEntity(mm, arrowLeft(), ColorSchemeRedDark()),
        TrajectorySequenceEntity(mm, arrowRight(), ColorSchemeRedDark()),
    ).onEach { if (shouldHighlight) it.trajectoryProgress = 0.1 }

    private fun spline() =
        drive.trajectorySequenceBuilder(Pose2d(startVec.x, startVec.y, heading.toRad()))
            .splineTo(endVec, endTangent.toRad())
            .build()

    private fun tangent(): TrajectorySequence {
        val xOffset = tangentLength * cos(endTangent.toRad())
        val yOffset = tangentLength * sin(endTangent.toRad())

        val x1 = endVec.x + xOffset
        val y1 = endVec.y + yOffset

        val x2 = endVec.x - xOffset
        val y2 = endVec.y - yOffset

        return drive.trajectorySequenceBuilder(Pose2d(x1, y1))
            .lineTo(Vector2d(x2, y2))
            .build()
    }

    private fun arrowLeft(): TrajectorySequence {
        val x1 = endVec.x + tangentLength * cos(endTangent.toRad())
        val y1 = endVec.y + tangentLength * sin(endTangent.toRad())

        val x2 = endVec.x + tangentLength * .8 * cos((endTangent + 15).toRad())
        val y2 = endVec.y + tangentLength * .8 * sin((endTangent + 15).toRad())

        return drive.trajectorySequenceBuilder(Pose2d(x1, y1))
            .lineTo(Vector2d(x2, y2))
            .build()
    }

    private fun arrowRight(): TrajectorySequence {
        val x1 = endVec.x + tangentLength * cos(endTangent.toRad())
        val y1 = endVec.y + tangentLength * sin(endTangent.toRad())

        val x2 = endVec.x + tangentLength * .8 * cos((endTangent - 15).toRad())
        val y2 = endVec.y + tangentLength * .8 * sin((endTangent - 15).toRad())

        return drive.trajectorySequenceBuilder(Pose2d(x1, y1))
            .lineTo(Vector2d(x2, y2))
            .build()
    }
    
    private fun Vector2d.pos() = Pose2d(x, y)

    class Builder {
        private var mm: MeepMeep? = null

        private var startVec = Vector2d()
        private var endVec = Vector2d(30.0, 30.0)

        private var textPose = 300 to 200

        private var tangentLine: Double? = null

        private var shouldHighlight = false
        private var shouldInitiallyRotate = false

        fun setMeepMeep(mm: MeepMeep) = apply {
            this.mm = mm
        }

        fun setStartPose(x: Number, y: Number) = apply {
            this.startVec = Vector2d(x.toDouble(), y.toDouble())
        }

        fun setEndPose(x: Number, y: Number) = apply {
            this.endVec = Vector2d(x.toDouble(), y.toDouble())
        }

        fun setTextPose(x: Int, y: Int) = apply {
            this.textPose = x to y
        }

        fun setTangentLineLength(length: Number) = apply {
            this.tangentLine = GlobalUnits.distance.toIn(length)
        }

        fun enableHighlighting() = apply {
            this.shouldHighlight = true
        }

        fun enableRotation() = apply {
            this.shouldInitiallyRotate = true
        }

        fun build(): MeepMeepSplineVisualizer {
            require(mm != null) { "MeepMeep instance must be set" }

            tangentLine = tangentLine ?: (endVec.norm() * .375)

            return MeepMeepSplineVisualizer(
                mm!!,
                GlobalUnits.vec(startVec),
                GlobalUnits.vec(endVec),
                textPose,
                GlobalUnits.distance.toIn(tangentLine!!),
                shouldHighlight,
                shouldInitiallyRotate,
            )
        }

        fun getAndStart(): Entity {
            val visualizer = build()
            visualizer.start()
            return visualizer.get()
        }
    }
}
