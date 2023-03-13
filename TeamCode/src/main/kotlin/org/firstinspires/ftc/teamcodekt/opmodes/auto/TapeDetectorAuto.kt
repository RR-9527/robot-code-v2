package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants

@Autonomous
class TapeDetectorAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(0,0, 0)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .setVelConstraint(40, 250.toRad(), DriveConstants.TRACK_WIDTH)
//            .splineTo(0, 1, 0);
            .forward(0.05)
            .thenRun(::moveTraj)

    fun moveTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            strafeRight(tapeCorrection)
            this
        }

    private fun Anvil.resetBot() = this
        .addTemporalMarker {
            bot.arm.goToRest()
            bot.wrist.setToRestingPos()
            bot.lift.goToZero()
            bot.claw.close()
        }
}
