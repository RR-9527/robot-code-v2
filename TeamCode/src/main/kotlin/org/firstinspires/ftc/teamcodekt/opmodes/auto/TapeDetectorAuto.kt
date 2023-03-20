
package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toIn

@Autonomous
class TapeDetectorAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(0,0, 0)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .back(0.01)
//            .addTemporalMarker(-25) {
////                updateTapeCorrection()
//            }
//            .addTemporalMarker(-15) {
////                bot.drive.poseEstimate = (Pose2d(bot.drive.poseEstimate.x, bot.drive.poseEstimate.y+tapeCorrection.toIn(), bot.drive.poseEstimate.heading))
//            }



    private fun Anvil.resetBot() = this
        .addTemporalMarker {
            bot.arm.goToRest()
            bot.wrist.setToRestingPos()
            bot.lift.goToZero()
            bot.claw.close()
        }
}
