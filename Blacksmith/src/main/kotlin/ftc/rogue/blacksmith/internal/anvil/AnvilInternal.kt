@file:Suppress("FunctionName")

package ftc.rogue.blacksmith.internal.anvil

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.MarkerCallback
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.internal.*
import ftc.rogue.blacksmith.internal.proxies._SampleMecanumDrive
import ftc.rogue.blacksmith.internal.proxies._TrajectorySequenceBuilder
import ftc.rogue.blacksmith.internal.util.*
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toIn
import ftc.rogue.blacksmith.util.toRad
import ftc.rogue.blacksmith.util.toSec
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * **PREFIXES:**
 * - '_' -> Directly adds to the end of the deque
 * - '__' -> Calls other functions that adds to the deque
 * - '$' -> Directly modifies the deque or something else special
 * - else -> Doesn't touch the deque
 */
class AnvilInternal
    internal constructor(
        private val instance: Anvil,
        drive: Any,
        @get:JvmSynthetic internal val startPose: Pose2d,
    ) {

    companion object {
        private val builderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    private val driveProxy = _SampleMecanumDrive(drive)

    private val builderProxy = _TrajectorySequenceBuilder(driveProxy, startPose)

    private val builderDeque = ArrayDeque<() -> Unit>()

    private val preforgedTrajectories = mutableMapOf<Any, Deferred<Any>>()

    private fun enqueue(builderAction: () -> Unit) {
        builderDeque += builderAction
    }

    @JvmSynthetic
    internal fun run(trajectory: Any, async: Boolean) = if (async) {
        driveProxy.followTrajectorySequenceAsync(trajectory)
    } else {
        driveProxy.followTrajectorySequence(trajectory)
    }

    private fun getCurrentEndPose(): Pose2d {
        flushDeque()

        return builderProxy
            .getSequenceSegments()
            .last()
            .invokeMethod("getEndPose")
    }

    // -- Direct path mappings (Basic) --

    fun _forward(distance: Number) = enqueue {
        builderProxy.forward(distance.toIn())
    }

    fun _back(distance: Number) = enqueue {
        builderProxy.back(distance.toIn())
    }

    fun _turn(angle: Number) = enqueue {
        builderProxy.turn(angle.toRad())
    }

    fun _strafeLeft(distance: Number) = enqueue {
        builderProxy.strafeLeft(distance.toIn())
    }

    fun _strafeRight(distance: Number) = enqueue {
        builderProxy.strafeRight(distance.toIn())
    }

    // -- Direct path mappings (Lines) --

    fun _lineTo(x: Number, y: Number) = enqueue {
        builderProxy.lineTo( GlobalUnits.vec(x, y) )
    }

    fun _lineToLinearHeading(x: Number, y: Number, heading: Number) = enqueue {
        builderProxy.lineToLinearHeading( GlobalUnits.pos(x, y, heading) )
    }

    fun _lineToSplineHeading(x: Number, y: Number, heading: Number) = enqueue {
        builderProxy.lineToSplineHeading( GlobalUnits.pos(x, y, heading) )
    }

    // -- Direct path mappings (Splines) --

    fun _splineTo(x: Number, y: Number, endTangent: Number) = enqueue {
        builderProxy.splineTo(
            GlobalUnits.vec(x, y),
            endTangent.toRad(),
        )
    }

    fun _splineToConstantHeading(x: Number, y: Number, endTangent: Number) = enqueue {
        builderProxy.splineToConstantHeading(
            GlobalUnits.vec(x, y),
            endTangent.toRad(),
        )
    }

    fun _splineToLinearHeading(x: Number, y: Number, heading: Number, endTangent: Number) = enqueue {
        builderProxy.splineToLinearHeading(
            GlobalUnits.pos(x, y, heading),
            endTangent.toRad(),
        )
    }

    fun _splineToSplineHeading(x: Number, y: Number, heading: Number, endTangent: Number) = enqueue {
        builderProxy.splineToSplineHeading(
            GlobalUnits.pos(x, y, heading),
            endTangent.toRad(),
        )
    }

    // -- Advanced mappings --

    fun _waitTime(time: Number) = enqueue {
        builderProxy.waitSeconds(time.toSec())
    }

    fun _setReversed(reversed: Boolean) = enqueue {
        builderProxy.setReversed(reversed)
    }

    fun _setTangent(tangent: Number) = enqueue {
        builderProxy.setTangent(tangent.toDouble())
    }

    fun _addTrajectory(trajectory: Trajectory) = enqueue {
        builderProxy.addTrajectory(trajectory)
    }

    fun _addTrajectory(trajectory: () -> Trajectory) = enqueue {
        builderProxy.addTrajectory(trajectory())
    }

    // -- Markers --

    fun _addTemporalMarker(offset: Number, action: MarkerCallback) = enqueue {
        builderProxy.UNSTABLE_addTemporalMarkerOffset(
            offset.toSec(),
            action,
        )
    }

    fun _addDisplacementMarker(offset: Number, action: MarkerCallback) = enqueue {
        builderProxy.UNSTABLE_addDisplacementMarkerOffset(
            offset.toSec(),
            action,
        )
    }

    fun _addSpatialMarker(offsetX: Number, offsetY: Number, action: MarkerCallback) = enqueue {
        builderProxy.addSpatialMarker(
            GlobalUnits.vec(offsetX, offsetY),
            action,
        )
    }

    // -- Utilities --

    fun setPoseEstimate(pose: Pose2d) {
        driveProxy.setPoseEstimate(pose)
    }

    fun __setPoseEstimateInTemporalMarker(pose: Pose2d) {
        _addTemporalMarker(0.0) {
            driveProxy.setPoseEstimate(pose)
        }
    }

    fun __inReverse(pathsToDoInReverse: AnvilConsumer) {
        _setReversed(true)
        pathsToDoInReverse.consume(instance)
        _setReversed(false)
    }

    fun `$doInReverse`(num: Int, numPopped: AtomicInteger) {
        if (num < 0) {
            throw IllegalArgumentException("Can not pop a negative amount of things off of a stack")
        }

        if (builderDeque.isEmpty()) {
            throw IllegalStateException("Builder deque is empty, doInReverse does not work unless there is an action to pop.")
        }

        val thingsToDoInReverse = (0 until num)
            .mapNotNull {
                builderDeque.removeLastOrNull()
            }

        numPopped.set(thingsToDoInReverse.size)

        __inReverse {
            thingsToDoInReverse.reversed().forEach(::enqueue)
        }
    }

    fun _noop() = enqueue {}

    @Suppress("UNCHECKED_CAST")
    fun <T> _withRawBuilder(builder: Consumer<T>) = enqueue {
        (builderProxy.internalBuilder as T)
            .let {
                builder.consume(it)
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> `$getRawBuilder`(): T {
        flushDeque()
        return builderProxy.internalBuilder as T
    }

    fun doTimes(times: Int, pathsToDo: AnvilCycle) {
        repeat(times) { iteration ->
            pathsToDo.consume(instance, iteration)
        }
    }

    // -- Constraints --

    fun _resetConstraints() = enqueue {
        builderProxy.resetConstraints()
    }

    fun _setVelConstraint(velConstraint: TrajectoryVelocityConstraint) = enqueue {
        builderProxy.setVelConstraint(velConstraint)
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     */
    fun _setVelConstraint(maxVel: Number, maxAngularVel: Number, trackWidth: Number) = enqueue {
        builderProxy.setVelConstraint(driveProxy.getVelocityConstraint(maxVel, maxAngularVel, trackWidth))
    }

    fun _resetVelConstraint() = enqueue {
        builderProxy.resetVelConstraint()
    }

    fun _setAccelConstraint(accelConstraint: TrajectoryAccelerationConstraint) = enqueue {
        builderProxy.setAccelConstraint(accelConstraint)
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     */
    fun _setAccelConstraint(maxAccel: Number) = enqueue {
        builderProxy.setAccelConstraint(driveProxy.getAccelerationConstraint(maxAccel))
    }

    fun _resetAccelConstraint() = enqueue {
        builderProxy.resetAccelConstraint()
    }

    fun _setTurnConstraint(maxAngVel: Number, maxAngAccel: Number) = enqueue {
        builderProxy.setTurnConstraint(maxAngVel.toDouble(), maxAngAccel.toDouble())
    }

    fun _resetTurnConstraint() = enqueue {
        builderProxy.resetTurnConstraint()
    }

    // -- Building, creating, running --

    @JvmOverloads
    fun `$thenRun`(
        nextTrajectory: (Pose2d) -> Anvil,
        configBuilder: AnvilRunConfigBuilder = AnvilRunConfig.DEFAULT
    ) {
        val config = AnvilRunConfig()
        configBuilder.run { AnvilRunConfig().build() }

        flushDeque()

        val key = Any()

        if (!config.buildsSynchronously) {
            builderDeque.addFirst {
                // Can't use '_addTemporalMarker' as that adds to the end of the deque
                builderProxy.UNSTABLE_addTemporalMarkerOffset(0.0) {
                    val nextStartPose = config.startPoseSupplier?.invoke()
                        ?: getCurrentEndPose()

                    preforgedTrajectories[key] = builderScope.async { nextTrajectory( nextStartPose ).build() }
                }
            }
        }

        _addTemporalMarker(0) {
            if (!config.predicate()) return@_addTemporalMarker

            val nextTrajectoryBuilt =
                if (config.buildsSynchronously) {
                    val nextStartPose = config.startPoseSupplier?.invoke()
                        ?: getCurrentEndPose()

                    nextTrajectory(nextStartPose).build()
                } else {
                    runBlocking { preforgedTrajectories[key]?.await() }!!
                }

            run( nextTrajectoryBuilt, !config.runsSynchronously )
        }
    }

    private fun flushDeque() {
        for (i in builderDeque.indices) {
            builderDeque.removeFirst().invoke()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> `$build`(): T {
        flushDeque()
        return builderProxy.build() as T
    }
}
