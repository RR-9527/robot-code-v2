@file:Suppress("MemberVisibilityCanBePrivate", "KDocUnresolvedReference", "unused")

package ftc.rogue.blacksmith

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.MarkerCallback
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint
import ftc.rogue.blacksmith.internal.*
import ftc.rogue.blacksmith.internal.anvil.AnvilInternal
import ftc.rogue.blacksmith.internal.anvil.AnvilRunConfig
import ftc.rogue.blacksmith.internal.anvil.AnvilRunner
import ftc.rogue.blacksmith.internal.util.AnvilConsumer
import ftc.rogue.blacksmith.internal.util.AnvilCycle
import ftc.rogue.blacksmith.internal.util.AnvilRunConfigBuilder
import ftc.rogue.blacksmith.internal.util.Consumer
import ftc.rogue.blacksmith.util.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * [**LINK TO OFFICIAL DOCS (click on me) (please read) (I like cars)**](https://blacksmithftc.vercel.app/anvil/overview)
 *
 * A component that wraps around the [TrajectorySequenceBuilder] to provide a much cleaner API
 * with powerful features such as building trajectories in parallel for quick creation on the fly,
 * and implicitly converting units to any one you like! Program your auto in furlongs and arcseconds
 * if you'd like.
 *
 * Works well with the [Scheduler API][Scheduler].
 *
 * Basic example:
 * ```java
 * /* BaseAuto.java */
 *
 * abstract class BaseAuto extends BlackOp {
 *     @CreateOnGo
 *     protected AutoBotComponents bot;
 *
 *     @CreateOnGo(passHwMap = true)
 *     protected SampleMecanumDrive drive;
 *
 *     protected Pose2d startPose;
 *     protected abstract Anvil mainTraj(Pose2d startPose);
 *
 *     @Override
 *     public void go() {
 *         Anvil startTraj = mainTraj(startPose);
 *
 *         Anvil
 *              .startAutoWith(startTraj)
 *              .onSchedulerLaunch();
 *
 *         Scheduler.launchOnStart(this, () -> {
 *             drive.update();
 *             bot.update();
 *         });
 *     }
 * }
 *
 * /* AutoDemo.java */
 *
 * @Autonomous
 * class AutoDemo extends BaseAuto {
 *     public AutoDemo() {
 *         startPose = GlobalUnits.pos(x, y, r);
 *     }
 *
 *     @Override
 *     protected Anvil mainTraj(Pose2d startPose) {
 *         return Anvil.forgeTrajectory(drive, startPose)
 *             .forward(10)
 *             .addTemporalMarker(100, () -> {
 *                 // Do something
 *             })
 *             .back(10)
 *             .thenRun(this::parkTraj);
 *     }
 *
 *     private Anvil parkTraj(Pose2d startPose) {
 *         // Create and return parking trajectory
 *     }
 * }
 * ```
 *
 * @author KG
 *
 * @see Scheduler
 * @see TrajectorySequenceBuilder
 */
class Anvil
    @PublishedApi
    internal constructor (
        drive: Any,
        startPose: Pose2d,
    ) {

    companion object {
        /**
         * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/creating-and-running#anvilforgetrajectory)
         */
        @JvmStatic
        @JvmOverloads
        inline fun forgeTrajectory(
            drive: Any,
            startPose: Pose2d,
            builder: Anvil.() -> Anvil = { this }
        ): Anvil {
            return builder( Anvil(drive, startPose) )
        }

        /**
         * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/creating-and-running#running-the-initial-trajectory)
         */
        @JvmStatic
        fun startAutoWith(instance: Anvil): AnvilRunner {
            return AnvilRunner().startAutoWith(instance)
        }
    }

    @PublishedApi
    @get:JvmSynthetic
    internal val internal = AnvilInternal(this, drive, startPose)

    // -- Direct path mappings (Basic) --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilforward)
     */
    fun forward(distance: Number) = tap {
        internal._forward(distance)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilback)
     */
    fun back(distance: Number) = tap {
        internal._back(distance)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilturn)
     */
    fun turn(angle: Number) = tap {
        internal._turn(angle)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilstrafeleft)
     */
    fun strafeLeft(distance: Number) = tap {
        internal._strafeLeft(distance)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilstraferight)
     */
    fun strafeRight(distance: Number) = tap {
        internal._strafeRight(distance)
    }

    // -- Direct path mappings (Lines) --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvillineto)
     */
    fun lineTo(x: Number, y: Number) = tap {
        internal._lineTo(x, y)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilstrafeto)
     */
    fun strafeTo(x: Number, y: Number) = tap {
        internal._lineTo(x, y)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvillinetoconstantheading)
     */
    fun lineToConstantHeading(x: Number, y: Number) = tap {
        internal._lineTo(x, y)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvillinetolinearheading)
     */
    fun lineToLinearHeading(x: Number, y: Number, heading: Number) = tap {
        internal._lineToLinearHeading(x, y, heading)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvillinetosplineheading)
     */
    fun lineToSplineHeading(x: Number, y: Number, heading: Number) = tap {
        internal._lineToSplineHeading(x, y, heading)
    }

    // -- Direct path mappings (Splines) --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilsplineto)
     */
    fun splineTo(x: Number, y: Number, endTangent: Number) = tap {
        internal._splineTo(x, y, endTangent)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilsplinetoconstantheading)
     */
    fun splineToConstantHeading(x: Number, y: Number, endTangent: Number) = tap {
        internal._splineToConstantHeading(x, y, endTangent)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilsplinetolinearheading)
     */
    fun splineToLinearHeading(x: Number, y: Number, heading: Number, endTangent: Number) = tap {
        internal._splineToLinearHeading(x, y, heading, endTangent)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/pathing-mappings#anvilsplinetosplineheading)
     */
    fun splineToSplineHeading(x: Number, y: Number, heading: Number, endTangent: Number) = tap {
        internal._splineToSplineHeading(x, y, heading, endTangent)
    }

    // -- Advanced mappings --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/advanced-mappings#anvilwaittime)
     */
    fun waitTime(time: Number) = tap {
        internal._waitTime(time)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/advanced-mappings#anvilsetreversed)
     */
    fun setReversed(reversed: Boolean) = tap {
        internal._setReversed(reversed)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/advanced-mappings#anvilsettangent)
     */
    fun setTangent(tangent: Number) = tap {
        internal._setTangent(tangent)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/advanced-mappings#anviladdtrajectory-1)
     */
    fun addTrajectory(trajectory: Trajectory) = tap {
        internal._addTrajectory(trajectory)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/advanced-mappings#anviladdtrajectory-2)
     */
    fun addTrajectory(trajectory: () -> Trajectory) = tap {
        internal._addTrajectory(trajectory)
    }

    // -- Markers --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/markers#anviladdtemporalmarker)
     */
    @JvmOverloads
    fun addTemporalMarker(offset: Number = 0.0, callback: MarkerCallback) = tap {
        internal._addTemporalMarker(offset, callback)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/markers#anviladddisplacementmarker)
     */
    @JvmOverloads
    fun addDisplacementMarker(offset: Number = 0.0, callback: MarkerCallback) = tap {
        internal._addDisplacementMarker(offset, callback)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/markers#anviladdspatialmarker)
     */
    fun addSpatialMarker(offsetX: Number, offsetY: Number, callback: MarkerCallback) = tap {
        internal._addSpatialMarker(offsetX, offsetY, callback)
    }

    // -- Utilities --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilsetposeestimatenow)
     */
    fun setPoseEstimateNow(pose: Pose2d) = tap {
        internal.setPoseEstimate(pose)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilsetposeestimateintemporalmarker)
     */
    fun setPoseEstimateInTemporalMarker(pose: Pose2d) = tap {
        internal.__setPoseEstimateInTemporalMarker(pose)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilinreverse)
     */
    fun inReverse(pathsToDoInReverse: AnvilConsumer) = tap {
        internal.__inReverse(pathsToDoInReverse)
    }

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvildoinreverse)
     *
     * (probably don't use this lol unless you understand how anvil works which you don't. Easy to
     * shoot yourself in the foot)
     */
    @JvmOverloads
    fun doInReverse(num: Int = 1, numPopped: AtomicInteger = AtomicInteger()) = tap {
        internal.`$doInReverse`(num, numPopped)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilnoop)
     */
    @get:JvmName("noop")
    val noop: Anvil
        get() = also { internal._noop() }

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilwithrawbuilder)
     */
    fun <T> withRawBuilder(builder: Consumer<T>) = tap {
        internal._withRawBuilder(builder)
    }

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvilgetrawbuilder)
     */
    fun <T> getRawBuilder(): T {
        return internal.`$getRawBuilder`()
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/custom-mappings#anvildotimes)
     */
    fun doTimes(times: Int, pathsToDo: AnvilCycle) = tap {
        internal.doTimes(times, pathsToDo)
    }

    // -- Constraints --

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilresetconstraints)
     */
    fun resetConstraints() = tap {
        internal._resetConstraints()
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     * 
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilsetvelconstraint-1)
     */
    fun setVelConstraint(velConstraint: TrajectoryVelocityConstraint) = tap {
        internal._setVelConstraint(velConstraint)
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     * 
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilsetvelconstraint-2)
     */
    fun setVelConstraint(maxVel: Number, maxAngularVel: Number, trackWidth: Number) = tap {
        internal._setVelConstraint(maxVel, maxAngularVel, trackWidth)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilresetvelconstraint)
     */
    fun resetVelConstraint() = tap {
        internal._resetVelConstraint()
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     * 
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilsetaccelconstraint-1)
     */
    fun setAccelConstraint(accelConstraint: TrajectoryAccelerationConstraint) = tap {
        internal._setAccelConstraint(accelConstraint)
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     * 
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilsetaccelconstraint-2)
     */
    fun setAccelConstraint(maxAccel: Number) = tap {
        internal._setAccelConstraint(maxAccel)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilresetaccelconstraint)
     */
    fun resetAccelConstraint() = tap {
        internal._resetAccelConstraint()
    }

    /**
     * __IMPORTANT:__ These units are NOT auto-converted
     * 
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilsetturnconstraint)
     */
    fun setTurnConstraint(maxAngVel: Number, maxAngAccel: Number) = tap {
        internal._setTurnConstraint(maxAngVel, maxAngAccel)
    }

    /**
     * [Link to method docs](https://blacksmithftc.vercel.app/anvil/constraints#anvilresetturnconstraint)
     */
    fun resetTurnConstraint() = tap {
        internal._resetTurnConstraint()
    }

    // -- Building, creating, running --

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/creating-and-running#running-subsequent-trajectories)
     */
    @JvmOverloads
    fun thenRun(
        nextTrajectory: (Pose2d) -> Anvil,
        configBuilder: AnvilRunConfigBuilder = AnvilRunConfig.DEFAULT
    ) = tap {
        internal.`$thenRun`(nextTrajectory, configBuilder)
    }

    /**
     * [READ DOCS FOR THIS (click me)](https://blacksmithftc.vercel.app/anvil/creating-and-running#just-building-a-trajectorysequence)
     */
    fun <T> build(): T {
        return internal.`$build`()
    }

    // -- Internal --

    // Just so Kotlin doesn't highlight 'also' as yellow, it's distracting here
    private inline fun tap(action: () -> Unit) =
        also { action() }
}
