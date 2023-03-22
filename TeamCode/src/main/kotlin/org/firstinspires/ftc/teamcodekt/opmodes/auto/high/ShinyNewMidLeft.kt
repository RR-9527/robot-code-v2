package org.firstinspires.ftc.teamcodekt.opmodes.auto.high

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.listeners.Listener
import ftc.rogue.blacksmith.listeners.On
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toIn
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.AutoData.liftOffsets
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants
import org.firstinspires.ftc.teamcodekt.components.LIFT_LOW
import org.firstinspires.ftc.teamcodekt.components.LIFT_MID
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Autonomous
class ShinyNewMidLeft : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)

            .addTemporalMarker {
                bot.lift.targetHeight = LIFT_LOW - 200
                bot.claw.close()
                bot.arm.setToForwardsDownPos()
                bot.wrist.setToForwardsPos()
            }

            .addTemporalMarker(1385) {
                bot.lift.targetHeight = LIFT_MID
                bot.arm.setToForwardsPos()
            }

            .initialGoToDeposit()
            .initialDeposit()

            .doTimes(NUM_CYCLES) {
                when (it) {
                    LAST_CYCLE -> fastIntakePrep(it)
                    else -> regularIntakePrep(it)
                }

                goToIntake(it)

                setPoseEstimateInTemporalMarker(-375) {
                    Pose2d(
                        bot.drive.localizer.poseEstimate.x,
                        bot.drive.localizer.poseEstimate.y + tapeCorrection.toIn(),
                        bot.drive.localizer.poseEstimate.heading,
                    )
                }

                when (it) {
                    LAST_CYCLE -> awaitFastIntake()
                    else -> awaitRegularIntake()
                }

                goToDeposit(it)
                deposit(it)
            }

            .thenRun(::parkTraj)

    private fun Anvil.initialGoToDeposit() = this
        .splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)
        1 -> splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)
        2 -> splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)
        3 -> splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)
        4 -> splineTo(-77.5 + poleOffset.x, -42.5 + poleOffset.y, -43)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-165.0, -21.0, 180)
        1 -> splineTo(-165.5, -22.5, 180)
        2 -> splineTo(-165.9, -28.0, 180)
        3 -> splineTo(-166.3, -27.5, 180)
        4 -> splineTo(-166.8, -29.25, 180)
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
            bot.lift.goToHigh()
            bot.arm.setToForwardsPos()

        }

        .addTemporalMarker(450) {
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
            bot.lift.goToHigh()
        }

        .addTemporalMarker(200) {
            bot.arm.setToForwardsPos()

        }

        .addTemporalMarker(410) {
            bot.wrist.setToForwardsPos()
        }
        .waitTime(0)



    private fun Anvil.initialDeposit() = this
        .addTemporalMarker(-120) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsDownPos()
        }
        .addTemporalMarker(-20) {
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
//            when (iterations) {
//                0 -> bot.lift.targetHeight = liftOffsets[iterations]-2
//                1 -> bot.lift.targetHeight = liftOffsets[iterations]-8
//                2 -> bot.lift.targetHeight = liftOffsets[iterations]-8
//                3 -> bot.lift.targetHeight = liftOffsets[iterations]-8
//                4 -> bot.lift.targetHeight = liftOffsets[iterations]-8
//            }
            bot.lift.targetHeight = liftOffsets[iterations]-13

            bot.wrist.setToBackwardsPos()
            bot.arm.targetAngle = 43.0
        }

        .addTemporalMarker(325) {
            bot.claw.openForIntakeWide()
//            bot.intake.enable()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.arm.targetAngle = 43.0
            bot.wrist.setToBackwardsPos()

            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }


    private fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()

            when (signalID) {
                1 -> {
                    lineToLinearHeading(-2.5, 9, 0)
                }
                2 -> {
                    lineToLinearHeading(-92.5, 9, 0)
                }
                3 -> {
                    lineToLinearHeading(-160, 9, 0)
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
