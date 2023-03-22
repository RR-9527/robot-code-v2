package ftc.rogue.blacksmith.util.meepmeep

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
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.meepmeep.utils.*
import ftc.rogue.blacksmith.util.toRad
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class MeepMeepSplineVisualizer
    private constructor (
        val mm: MeepMeep,
        var startPos: Pose2d,      /* Stored & displayed in GlobalUnits */
        var endVec: Vector2d,      /* Stored & displayed in GlobalUnits */
        val textCoords: Vector2d,  /* Stored & displayed in pixels */
        val tangentLength: Double, /* Stored & displayed in GlobalUnits */
        private val shouldHighlight: Boolean,
        private val shouldInitiallyRotate: Boolean,
        private val shouldShowTangentLine: Boolean,
        persistence: MeepMeepPersistence?,
    ) {
    private var endTangent = 0.0
    private var heading = 90.0

    private var isDirty = true

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
                gfx.drawString("End tangent: $endTangent deg", textCoords.x.toInt(), textCoords.y.toInt())
            }
        }.asEntity()

    fun start() {
        var oldTrajectories = makeTrajectories()

        fun update() {
            if (!isDirty) return

            val newTrajectorySequences = makeTrajectories()

            (0..3).forEach {
                oldTrajectories[it]?.let { it1 -> mm.requestToRemoveEntity(it1) }
                newTrajectorySequences[it]?.let { it1 -> mm.requestToAddEntity(it1) }
            }
            oldTrajectories = newTrajectorySequences

            isDirty = false
        }

        var shouldRotate = shouldInitiallyRotate

        ScheduledMeepMeepExecutor.EXECUTOR.scheduleAtFixedRate({
            update()

            if (shouldRotate) {
                endTangent += 5
                endTangent %= 360
                isDirty = true
            }
        }, 0, 100, TimeUnit.MILLISECONDS)

        val mouseCoordsSupplier = getCanvasMouseSupplier(mm)

        scrollWheelMovedListener(mm) { notches, isShiftPressed, _ ->
            val sCoords = mouseCoordsSupplier()
            val (startOverlaps, endOverlaps) = getIsMouseOverlappingEndpoints(sCoords, 2)

            if (endOverlaps) {
                isDirty = true
                shouldRotate = false

                endTangent = (endTangent - if (isShiftPressed) {
                    notches
                } else {
                    notches * 20
                }) % 360
            } else if (startOverlaps) {
                isDirty = true

                heading = (heading - if (isShiftPressed) {
                    notches
                } else {
                    notches * 20
                }) % 360
            }
        }

        mouseDraggedListener(mm) { x, y ->
            val sCoords = Vector2d(x.toDouble(), y.toDouble())
            val (startOverlaps, endOverlaps, fCoords) = getIsMouseOverlappingEndpoints(sCoords, 2)

            if (endOverlaps) {
                endVec = fCoords
                isDirty = true
            } else if (startOverlaps) {
                startPos = fCoords.pos(heading.toRad())
                isDirty = true
            }
        }
    }

    private fun makeTrajectories() = arrayOf(
        TrajectorySequenceEntity(mm, spline(), ColorSchemeBlueLight()),
        tangent()?.let { TrajectorySequenceEntity(mm, it, ColorSchemeRedDark()) },
        arrowLeft()?.let { TrajectorySequenceEntity(mm, it, ColorSchemeRedDark()) },
        arrowRight()?.let { TrajectorySequenceEntity(mm, it, ColorSchemeRedDark()) },
    ).onEach { if (shouldHighlight) it?.trajectoryProgress = 0.1 }

    private fun spline() =
        drive.trajectorySequenceBuilder(startPos)
            .splineTo(endVec, endTangent.toRad())
            .build()

    private fun tangent(): TrajectorySequence? {
        if (!shouldShowTangentLine)
            return null

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

    private fun arrowLeft(): TrajectorySequence? {
        if (!shouldShowTangentLine)
            return null

        val x1 = endVec.x + tangentLength * cos(endTangent.toRad())
        val y1 = endVec.y + tangentLength * sin(endTangent.toRad())

        val x2 = endVec.x + tangentLength * .8 * cos((endTangent + 15).toRad())
        val y2 = endVec.y + tangentLength * .8 * sin((endTangent + 15).toRad())

        return drive.trajectorySequenceBuilder(Pose2d(x1, y1))
            .lineTo(Vector2d(x2, y2))
            .build()
    }

    private fun arrowRight(): TrajectorySequence? {
        if (!shouldShowTangentLine)
            return null

        val x1 = endVec.x + tangentLength * cos(endTangent.toRad())
        val y1 = endVec.y + tangentLength * sin(endTangent.toRad())

        val x2 = endVec.x + tangentLength * .8 * cos((endTangent - 15).toRad())
        val y2 = endVec.y + tangentLength * .8 * sin((endTangent - 15).toRad())

        return drive.trajectorySequenceBuilder(Pose2d(x1, y1))
            .lineTo(Vector2d(x2, y2))
            .build()
    }
    
    private fun Vector2d.pos(r: Double) = Pose2d(x, y, r)

    @Suppress("SameParameterValue")
    private fun getIsMouseOverlappingEndpoints(
        mouseCoords: Vector2d,
        deadzone: Number
    ): Triple<Boolean, Boolean, Vector2d> {
        val fCoords = FieldUtil.screenCoordsToFieldCoords(mouseCoords)

        val startDiffs = fCoords - startPos.vec()
        val endDiffs   = fCoords - endVec

        val startsDiffInIn = GlobalUnits.vec(startDiffs)
        val endDiffsInIn = GlobalUnits.vec(endDiffs)

        val startOverlaps = abs(startsDiffInIn.x) < deadzone.toDouble() && abs(startsDiffInIn.y) < deadzone.toDouble()
        val endOverlaps = abs(endDiffsInIn.x) < deadzone.toDouble() && abs(endDiffsInIn.y) < deadzone.toDouble()

        return Triple(
            startOverlaps,
            endOverlaps,
            fCoords,
        )
    }

    data class Builder(
       val mm: MeepMeep? = null,

       val startPos: Pose2d = Pose2d(),
       val endVec: Vector2d = GlobalUnits.vec(30, 30),

       val textPose: Vector2d = Vector2d(200.0, 300.0),

       val tangentLine: Double? = null,

       val shouldHighlight: Boolean = false,
       val shouldInitiallyRotate: Boolean = false,
       val shouldShowTangentLine: Boolean = true,

       val persistence: MeepMeepPersistence? = null,
    ) {
        fun setMeepMeep(mm: MeepMeep) = copy(
            mm = mm
        )

        @JvmOverloads
        fun setStartPose(x: Number, y: Number, heading: Double = 90.0) = copy(
            startPos = GlobalUnits.pos(x, y, heading)
        )

        fun setEndVec(x: Number, y: Number) = copy(
            endVec = GlobalUnits.vec(x, y)
        )

        fun setTextPose(x: Int, y: Int) = copy(
            textPose = Vector2d(x.toDouble(), y.toDouble())
        )

        fun setTangentLineLength(length: Number) = copy(
            tangentLine = GlobalUnits.distance.toIn(length)
        )

        fun enableHighlighting() = copy(
            shouldHighlight = true
        )

        fun disableHighlighting() = copy(
            shouldHighlight = false
        )

        fun enableRotation() = copy(
            shouldInitiallyRotate = true
        )

        fun disableRotation() = copy(
            shouldInitiallyRotate = false
        )

        fun enableTangentLine() = copy(
            shouldShowTangentLine = true
        )

        fun disableTangentLine() = copy(
            shouldShowTangentLine = false
        )

        fun setPersistence(persistence: MeepMeepPersistence) = copy(
            persistence = persistence
        )

        fun build(): MeepMeepSplineVisualizer {
            require(mm != null) { "MeepMeep instance must be set" }

            val actualTangentLine = tangentLine ?: (endVec.norm() * .375)

            return MeepMeepSplineVisualizer(
                mm,
                startPos,
                endVec,
                textPose,
                actualTangentLine,
                shouldHighlight,
                shouldInitiallyRotate,
                shouldShowTangentLine,
                persistence,
            )
        }

        fun getAndStart(): Entity {
            val visualizer = build()
            visualizer.start()
            return visualizer.get()
        }
    }
}
