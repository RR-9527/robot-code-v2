package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class RogueMidLeftAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .setVelConstraint(44, 250.toRad(), DriveConstants.TRACK_WIDTH)

            .addTemporalMarker {
                bot.lift.goToAngledMidPredeposit()
                bot.claw.close()
                bot.arm.setToForwardsAngledPos()
                bot.wrist.setToForwardsPos()
            }

            .initialGoToDeposit()
            .initialDeposit()

            .doTimes(NUM_CYCLES) {
                when (it) {
                    LAST_CYCLE -> fastIntakePrep(it)
                    else -> regularIntakePrep(it)
                }

                goToIntake(it)

                when (it) {
                    LAST_CYCLE -> awaitFastIntake()
                    else -> awaitRegularIntake()
                }

                goToDeposit(it)
                deposit(it)
            }

            .thenRun(::parkTraj)

    private fun Anvil.initialGoToDeposit() = this
        .forward(132)
        .turn(-139.5)
        .lineToLinearHeading(-78.75 + poleOffset.x, -42.5 + poleOffset.y, -49)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(-80.2 + poleOffset.x, -40.8 + poleOffset.y, -39.0)
        1 -> splineTo(-79.1 + poleOffset.x, -37.0 + poleOffset.y, -36.4)
        2 -> splineTo(-76.9 + poleOffset.x, -34.5 + poleOffset.y, -33.0)
        3 -> splineTo(-76.7 + poleOffset.x, -33.5 + poleOffset.y, -26.0)
        4 -> splineTo(-76.0 + poleOffset.x, -31.5 + poleOffset.y, -23.8)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-164.1, -22.0 - 1.5, 180)
        1 -> splineTo(-163.1, -19.5 - 1.7, 180)
        2 -> splineTo(-162.5, -16.2 + 0.6, 180)
        3 -> splineTo(-162.0, -14.7 + 1.7, 180)
        4 -> splineTo(-161.5, -13.8 + 2.8, 180)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-55) {
            bot.claw.close()
        }

        .addTemporalMarker(125) {
            bot.lift.goToAngledMidButHigher()
        }

        .addTemporalMarker(280) {
            bot.arm.setToForwardsAngledPos()
            bot.wrist.setToForwardsPos()
        }

        .waitTime(120)

    private fun Anvil.awaitFastIntake() = this
        .addTemporalMarker(-215) {
            bot.intake.disable()
        }

        .addTemporalMarker(-185) {
            bot.claw.close()
        }

        .addTemporalMarker(75) {
            bot.arm.setToForwardsAngledPos()
            bot.lift.goToAngledMidButHigher()
        }

        .addTemporalMarker(100) {
            bot.wrist.setToForwardsPos()
        }

        .waitTime(70)

    private fun Anvil.initialDeposit() = this
        .addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsPos()
        }

        .addTemporalMarker(-20) {
            bot.claw.openForDeposit()
        }

    private fun Anvil.deposit(iterations: Int) = this.apply {
        addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsPos()
        }

        val durationOffset = if (iterations < 4) -25 else -85

        addTemporalMarker(durationOffset) {
            bot.claw.openForDeposit()
        }
    }

    private fun Anvil.regularIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            bot.lift.targetHeight = liftOffsets[iterations]
            bot.wrist.setToBackwardsPos()
            bot.arm.setToBackwardsPosButLikeSliiiightlyHigher()
        }

        .addTemporalMarker(205) {
            bot.claw.openForIntakeWide()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            bot.arm.setToBackwardsPosLastCycle()
            bot.wrist.setToBackwardsPos()
        }

        .addTemporalMarker(165) {
            bot.lift.targetHeight = liftOffsets[iterations]
            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    private fun parkTraj(startPose: Pose2d) =
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

    private fun Anvil.resetBot() = this
        .addTemporalMarker {
            bot.arm.setToRestingPos()
            bot.wrist.setToRestingPos()
            bot.lift.goToZero()
            bot.claw.close()
        }
}
