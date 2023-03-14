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
import org.firstinspires.ftc.teamcodekt.components.LIFT_HIGH_AUTO
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class AnActuallyGoodAutoOnSouthHighPole : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)

            .addTemporalMarker {
                bot.lift.goToMid()
                bot.claw.close()
                bot.arm.setToForwardsPos()
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
//        .splineTo(-94, -8, -49-90)
//        .lineTo(-93.5 + poleOffset.x, -8 + poleOffset.y)
//        .turn(-49-90)
        .splineTo(-77.5, -10.5, -25)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(8.35 + poleOffset.x, -5.5 + poleOffset.y, -34)
        1 -> splineTo(8.5 + poleOffset.x, -3.8 + poleOffset.y, -33)
        2 -> splineTo(9 + poleOffset.x, +1.5 + poleOffset.y, -33)
        3 -> splineTo(9.8 + poleOffset.x, +2.8 + poleOffset.y, -30)
        4 -> splineTo(10.5 + poleOffset.x, +6 + poleOffset.y, -30)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-178.7, 15, 180)
        1 -> splineTo(-178.2, 23, 180)
        2 -> splineTo(-178, 29.5, 180)
        3 -> splineTo(-177.7, 31.6, 180)
        4 -> splineTo(-177.3, 36, 180)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-170) {
            bot.intake.disable()
        }

        .addTemporalMarker(-60) {
            bot.claw.close()
        }

        .addTemporalMarker(190) {
            bot.arm.setToForwardsPos()
            bot.lift.targetHeight = LIFT_HIGH_AUTO
        }

        .addTemporalMarker(390) {
            bot.wrist.setToForwardsPos()
        }

        .waitTime(80)

    private fun Anvil.awaitFastIntake() = this
        .addTemporalMarker(-275) {
            bot.intake.disable()
        }

        .addTemporalMarker(-75) {
            bot.claw.close()
        }

        .addTemporalMarker(60) {
            bot.lift.targetHeight = LIFT_HIGH_AUTO
        }

        .addTemporalMarker(200) {
            bot.arm.setToForwardsPos()

        }

        .addTemporalMarker(210) {
            bot.wrist.setToForwardsPos()
        }
        .waitTime(0)


    private fun Anvil.initialDeposit() = this
        .addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsDownPos()
        }
        .addTemporalMarker(25) {
            bot.claw.openForDeposit()
//            bot.intake.enable()
        }
        .addTemporalMarker(300) {
            bot.lift.targetHeight = liftOffsets[0]
        }

    private fun Anvil.deposit(iterations: Int) = this.apply {
        addTemporalMarker(-265) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsDownPos()
        }

        val durationOffset = if (iterations < 4) -20 else -70

        addTemporalMarker(durationOffset) {
            bot.claw.openForDeposit()
        }
    }

    private fun Anvil.regularIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations] - 10

            bot.wrist.setToBackwardsPos()
            bot.arm.targetAngle = 44.0
        }

        .addTemporalMarker(325) {
            bot.claw.openForIntakeWide()
//            bot.intake.enable()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.arm.targetAngle = 40.0
            bot.wrist.setToBackwardsPos()

            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    private fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> {
                    lineToLinearHeading(-2.5, 37.5, 0)
                }
                2 -> {
                    lineToLinearHeading(-92.5, 37.5, 0)
                }
                3 -> {
                    lineToLinearHeading(-160, 37.5, 0)
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
