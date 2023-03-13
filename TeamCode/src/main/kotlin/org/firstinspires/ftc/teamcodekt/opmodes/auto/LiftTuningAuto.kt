
package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toIn
import ftc.rogue.blacksmith.util.toRad

@Autonomous
class LiftTuningAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(0, 0, 0)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .addTemporalMarker {
                bot.lift.goToHigh()
            }
            .forward(80)
            .addTemporalMarker {
                bot.lift.goToZero()
            }
            .back(80)
}