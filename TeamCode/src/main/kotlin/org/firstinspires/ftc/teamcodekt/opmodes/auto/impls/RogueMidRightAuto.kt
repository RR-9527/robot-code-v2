package org.firstinspires.ftc.teamcodekt.opmodes.auto.impls

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueMidAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class RogueMidRightAuto : RogueMidAuto() {
    override val startPose = GlobalUnits.pos(91, -163, 90)

    override fun Anvil.initialGoToDeposit() = this
        .forward(132)
        .turn(142.5)
        .lineToLinearHeading(79.7 + poleOffset.x, -43.2 + poleOffset.y, 220)

    override fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(80.00 - .85 + poleOffset.x, -42.9 + poleOffset.y, 221.5)
        1 -> splineTo(78.13 - .85 + poleOffset.x, -42.0 + poleOffset.y, 220.5)
        2 -> splineTo(78.80 - .05 + poleOffset.x, -41.9 + poleOffset.y, 219.5)
        3 -> splineTo(75.40 - .05 + poleOffset.x, -39.3 + poleOffset.y, 218.3)
        4 -> splineTo(75.50 - .05 + poleOffset.x, -38.8 + poleOffset.y, 213.5)
        else -> throw CycleException()
    }

    override fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(161.5 - 0.0, -28.1 - 1.2, 0)
        1 -> splineTo(160.4 - 0.7, -26.9 - 2.2, 0)
        2 -> splineTo(159.0 - 0.7, -25.4 - 2.2, 0)
        3 -> splineTo(159.1 - 0.1, -24.4 - 2.2, 0)
        4 -> splineTo(158.3 + 0.1, -22.9 - 2.2, 0)
        else -> throw CycleException()
    }.doInReverse()

    override fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> {
                    strafeRight(14)
                    splineToSplineHeading(30, -27, 90, 180)
                }
                2 -> {
                    lineToLinearHeading(78.5, -23, 90)
                }
                3 -> inReverse {
                    splineToSplineHeading(139.9, -25.9, 90, 0)
                }
            }

            this
        }
}
