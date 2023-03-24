package org.firstinspires.ftc.teamcodekt.opmodes.auto.high

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import ftc.rogue.blacksmith.Anvil
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
class ShinyNewHighLeft : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .addTemporalMarker {
                bot.lift.targetHeight = LIFT_LOW - 95
                bot.claw.close()
                bot.arm.setToForwardsDownPos()
                bot.wrist.setToForwardsPos()
            }

            .addTemporalMarker(1420) {
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

//                setPoseEstimateInTemporalMarker(-250) {
//                    mTelemetry.addData("Correcting amont", "")
//                    val correction = bot.camera.tapeDetectorPipeline.correction.toIn()
//
//                    val (x, _y, h) = bot.drive.localizer.poseEstimate
//                    val y = _y + correction
//
//                    Pose2d(x, y, h)
//                }

                when (it) {
                    LAST_CYCLE -> awaitFastIntake()
                    else -> awaitRegularIntake()
                }

                goToDeposit(it)
                deposit(it)
            }

            .thenRun(::parkTraj)

    private fun Anvil.initialGoToDeposit() = this
        .splineTo(-80.5, -45.5, -41)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(-83.0 + poleOffset.x, -3 + poleOffset.y, 36)
        1 -> splineTo(-83.3 + poleOffset.x, -3 + poleOffset.y, 33)
        2 -> splineTo(-83.0 + poleOffset.x, -3.5 + poleOffset.y, 32.5)
        3 -> splineTo(-83.0 + poleOffset.x, -4 + poleOffset.y, 32)
        4 -> splineTo(-81.8 + poleOffset.x, -4 + poleOffset.y, 30.5)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(.452 + -165.1, -21.75, 180)
        1 -> splineTo(.452 + -165, -21.75, 180)
        2 -> splineTo(.452 + -165.4, -23.6, 180)
        3 -> splineTo(.452 + -165.2, -25, 180)
        4 -> splineTo(.452 + -165.2, -25.5, 180)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
//        .addTemporalMarker(-200) {
//            bot.intake.enable()
//        }
//        .addTemporalMarker(-100) {
//            bot.intake.disable()
//            bot.arm.targetAngle = 40.25
//        }
        .addTemporalMarker(-75) {
            bot.claw.close()
        }
        .addTemporalMarker(190) {
            bot.lift.goToHigh()
            bot.arm.setToForwardsPos()
        }
        .addTemporalMarker(250) {
            bot.wrist.setToForwardsPos()
        }
        .waitTime(80)

    private fun Anvil.awaitFastIntake() = this
        .addTemporalMarker(-75) {
            bot.claw.close()
        }
        .addTemporalMarker(-95) {
            bot.intake.disable()
        }
        .addTemporalMarker(-50) {
            bot.arm.targetAngle = 40.25
            bot.lift.targetHeight -= 50
        }
        .addTemporalMarker(60) {
            bot.lift.goToHigh()
        }
        .addTemporalMarker(200) {
            bot.arm.setToForwardsPos()
        }
        .addTemporalMarker(230) {
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
            bot.lift.targetHeight = liftOffsets[iterations] - 8

            bot.wrist.setToBackwardsPos()
            bot.arm.targetAngle = 43.5
        }

        .addTemporalMarker(325) {
            bot.claw.openForIntakeWide()
//            bot.intake.enable()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.arm.targetAngle = 43.5
            bot.wrist.setToBackwardsPos()

            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    private fun parkTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose) {
            resetBot()
            when (signalID) {
                1 -> {
                    lineToLinearHeading(-97.50, -20, 0)
                    setVelConstraint(100, 260.toRad(), DriveConstants.TRACK_WIDTH)
                    setAccelConstraint(80)
                    lineToLinearHeading(-160, -20, 0)
                }
                2 -> {
                    setVelConstraint(100, 260.toRad(), DriveConstants.TRACK_WIDTH)
                    setAccelConstraint(80)
                    lineToLinearHeading(-97.50, -20, 0)
                }
                3 -> {
                    lineToLinearHeading(-97.50, -20, 0)
                    setVelConstraint(100, 260.toRad(), DriveConstants.TRACK_WIDTH)
                    setAccelConstraint(80)
                    lineToLinearHeading(-40, -20, 0)
                }
            }

            this
        }

    private fun Anvil.resetBot() = this
        .addTemporalMarker(250) {
            bot.arm.goToRest()
            bot.wrist.setToRestingPos()
            bot.lift.goToZero()
            bot.claw.close()
        }
}
