@file:Suppress("ClassName")

package ftc.rogue.blacksmith.internal.proxies

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint
import ftc.rogue.blacksmith.internal.util.invokeMethodI
import java.util.*

@PublishedApi
internal class _SampleMecanumDrive(private val drive: Any) {
    fun followTrajectorySequence(seq: Any) {
        drive.invokeMethodI<Any?>("followTrajectorySequence", seq)
    }

    fun followTrajectorySequenceAsync(seq: Any) {
        drive.invokeMethodI<Any?>("followTrajectorySequenceAsync", seq)
    }

    fun setPoseEstimate(pose: Pose2d) {
        drive.invokeMethodI<Any?>("setPoseEstimate", pose)
    }

    fun trajectorySequenceBuilder(startPose: Pose2d): Any {
        return drive.invokeMethodI("trajectorySequenceBuilder", startPose)
    }

    fun getVelocityConstraint(maxVel: Number, maxAngularVel: Number, trackWidth: Number): TrajectoryVelocityConstraint {
        return drive::class.java.getMethod("getVelocityConstraint", Double::class.java, Double::class.java, Double::class.java)
            .let {
                it.invoke(null, maxVel.toDouble(), maxAngularVel.toDouble(), trackWidth.toDouble()) as TrajectoryVelocityConstraint?
                    ?: throw IllegalStateException("getVelocityConstraint not defined for this drive proxy (it returned null)")
            }
    }

    fun getAccelerationConstraint(maxAccel: Number): TrajectoryAccelerationConstraint {
        return drive::class.java.getMethod("getAccelerationConstraint", Double::class.java)
            .let {
                it.invoke(null, maxAccel.toDouble()) as TrajectoryAccelerationConstraint?
                    ?: throw IllegalStateException("getAccelerationConstraint not defined for this drive proxy (it returned null)")
            }
    }
}
