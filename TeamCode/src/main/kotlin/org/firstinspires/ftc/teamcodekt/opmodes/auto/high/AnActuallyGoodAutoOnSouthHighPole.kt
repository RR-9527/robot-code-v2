package org.firstinspires.ftc.teamcodekt.opmodes.auto.high

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.AutoData.liftOffsets
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class AnActuallyGoodAutoOnSouthHighPole : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)

            .addTemporalMarker {
                bot.lift.goToAngledMid()
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
        .lineTo(-91 + poleOffset.x, -12 + poleOffset.y)
        .turn(-49-90)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(3 + poleOffset.x, -2.5 + poleOffset.y, -30)
        1 -> splineTo(2+ poleOffset.x, -1.5 + poleOffset.y, -30)
        2 -> splineTo(4.25 + poleOffset.x, 1 + poleOffset.y, -30)
        3 -> splineTo(5 + poleOffset.x, 2 + poleOffset.y, -30)
        4 -> splineTo(3.5 + poleOffset.x, -2.5 + poleOffset.y, -30)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-179, 15, 180)
        1 -> splineTo(-179, 23, 180)
        2 -> splineTo(-178.5, 26, 180)
        3 -> splineTo(-178.5, 28, 180)
        4 -> splineTo(-178.5, 31, 180)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-170) {
            bot.intake.disable()
        }

        .addTemporalMarker {
            bot.claw.close()
        }

        .addTemporalMarker(275) {
            bot.lift.goToAngledHigh()
        }

        .addTemporalMarker(425) {
            bot.arm.setToForwardsAngledPos()
            bot.wrist.setToForwardsPos()
        }

        .waitTime(300)

    private fun Anvil.awaitFastIntake() = this
        .addTemporalMarker(-275) {
            bot.intake.disable()
        }

        .addTemporalMarker(-75) {
            bot.claw.close()
        }

        .addTemporalMarker(15) {
            bot.arm.setToForwardsAngledPos()
            bot.lift.goToAngledHigh()
        }

        .addTemporalMarker(100) {
            bot.wrist.setToForwardsPos()
        }

        .waitTime(120)

    private fun Anvil.initialDeposit() = this
        .addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsAngledPos()
        }
        .addTemporalMarker(50) {
            bot.claw.openForDeposit()
            bot.intake.enable()
        }
        .addTemporalMarker(60) {
            bot.lift.targetHeight = liftOffsets[0]
        }

    private fun Anvil.deposit(iterations: Int) = this.apply {
        addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsAngledPos()
        }

        val durationOffset = if (iterations < 4) -20 else -70

        addTemporalMarker(durationOffset) {
            bot.claw.openForDeposit()
        }
    }

    private fun Anvil.regularIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.wrist.setToBackwardsPos()
            bot.arm.setToBackwardsLowerPos()
        }

        .addTemporalMarker(325) {
            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.arm.setToBackwardsLowerPos()
            bot.wrist.setToBackwardsPos()

            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    private fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> {
                    lineToLinearHeading(-92.5, -21, 90)
                    lineToLinearHeading(-150, -16, 90)
                }
                2 -> {
                    lineToLinearHeading(-92.5, -21, 90)
                }
                3 -> {
                    lineToLinearHeading(-92.5, -21, 90)
                    lineToLinearHeading(-30, -16, 90)
                }
            }

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
