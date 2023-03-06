package ftc.rogue.blacksmith.util.anvil

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint

interface DriveProxy {
    fun followTrajectorySequence(seq: Any)

    fun followTrajectorySequenceAsync(seq: Any)

    fun setPoseEstimate(pose: Pose2d)

    fun trajectorySequenceBuilder(startPose: Pose2d): Any

    fun getVelocityConstraint(maxVel: Double, maxAngularVel: Double, trackWidth: Double): TrajectoryVelocityConstraint

    fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint
}
