package org.firstinspires.ftc.teamcodekt.opmodes.auto.mid

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class RogueMidLeftAuto : RogueMidAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun Anvil.initialGoToDeposit() = this
        .forward(132)
        .turn(-139.5)
        .lineToLinearHeading(-78.75 + poleOffset.x, -42.5 + poleOffset.y, -49)

    override fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(-80.2 + poleOffset.x, -40.8 + poleOffset.y, -39.0)
        1 -> splineTo(-79.1 + poleOffset.x, -37.0 + poleOffset.y, -36.4)
        2 -> splineTo(-76.9 + poleOffset.x, -34.5 + poleOffset.y, -33.0)
        3 -> splineTo(-76.7 + poleOffset.x, -33.5 + poleOffset.y, -26.0)
        4 -> splineTo(-76.0 + poleOffset.x, -31.5 + poleOffset.y, -23.8)
        else -> throw CycleException()
    }

    override fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-164.1, -22.0 - 1.5, 180)
        1 -> splineTo(-163.1, -19.5 - 1.7, 180)
        2 -> splineTo(-162.5, -16.2 + 0.6, 180)
        3 -> splineTo(-162.0, -14.7 + 1.7, 180)
        4 -> splineTo(-161.5, -13.8 + 2.8, 180)
        else -> throw CycleException()
    }.doInReverse()

    override fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> inReverse {
                    splineTo(-151.5, -16.8, 180)
                }
                2 -> {
                    lineToLinearHeading(-92.5, -17, 90)
                }
                3 -> {
                    strafeLeft(14)
                    splineToSplineHeading(-40, -25, 90, 180)
                }
            }

            this
        }
}
