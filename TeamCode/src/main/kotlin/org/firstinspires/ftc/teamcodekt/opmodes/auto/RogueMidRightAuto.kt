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
class RogueMidRightAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .setVelConstraint(40, 250.toRad(), DriveConstants.TRACK_WIDTH)

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
        .turn(142.5)
        .lineToLinearHeading(81.5 + poleOffset.x, -44.7 + poleOffset.y, 180 + 40)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(80.00 + poleOffset.x, -43.5 + poleOffset.y, 224.5)
        1 -> splineTo(78.13 + poleOffset.x, -42.0 + poleOffset.y, 223.5)
        2 -> splineTo(79.10 + poleOffset.x, -41.9 + poleOffset.y, 220.5)
        3 -> splineTo(76.50 + poleOffset.x, -39.3 + poleOffset.y, 218.3)
        4 -> splineTo(76.70 + poleOffset.x, -38.8 + poleOffset.y, 212.5)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(162.3 - 2.1, -26.9, 0)
        1 -> splineTo(161.5 - 2.1, -25.4, 0)
        2 -> splineTo(160.2 - 2.2, -23.9, 0)
        3 -> splineTo(160.4 - 2.3, -23.4, 0)
        4 -> splineTo(159.6 - 2.3, -22.9, 0)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-35) {
            bot.claw.close()
        }

        .addTemporalMarker(175) {
            bot.lift.goToAngledMidButHigher()
        }

        .addTemporalMarker(300) {
            bot.arm.setToForwardsAngledPos()
            bot.wrist.setToForwardsPos()
        }

        .waitTime(120)

    private fun Anvil.awaitFastIntake() = this
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

        .addTemporalMarker(-50) {
            bot.claw.openForDeposit()
        }

    private fun Anvil.deposit(iterations: Int) = this.apply {
        addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsPos()
        }

        val durationOffset = if (iterations < 4) -50 else -100

        addTemporalMarker(durationOffset) {
            bot.claw.openForDeposit()
        }
    }

    private fun Anvil.regularIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            when (iterations) {
                0 -> bot.lift.targetHeight = liftOffsets[iterations]
                1 -> bot.lift.targetHeight = liftOffsets[iterations]
                2 -> bot.lift.targetHeight = liftOffsets[iterations]
                3 -> bot.lift.targetHeight = liftOffsets[iterations]
            }

            bot.wrist.setToBackwardsPos()
            bot.arm.setToBackwardsPosButLikeSliiiightlyHigher()
        }

        .addTemporalMarker(205) {
            bot.claw.openForIntakeNarrow()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            bot.arm.setToBackwardsPosLastCycle()
            bot.wrist.setToBackwardsPos()
        }

        .addTemporalMarker(165) {
            bot.lift.targetHeight = liftOffsets[iterations]
            bot.claw.openForIntakeNarrow()
        }

    private fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> {
                    lineToLinearHeading(95.5, -23, -90)
                    lineToLinearHeading(37, -23, -90)
                }
                2 -> inReverse {
                    splineTo(147.9, -25.9, 0)
                    turn(-90)
                }
                3 -> {
                    lineToLinearHeading(95.5, -23, -90)
                    lineToLinearHeading(150, -23, -90)
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
