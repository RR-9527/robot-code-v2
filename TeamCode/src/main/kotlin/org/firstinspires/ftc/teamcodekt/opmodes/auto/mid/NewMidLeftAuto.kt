package org.firstinspires.ftc.teamcodekt.opmodes.auto.mid

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.toIn
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.AutoData.liftOffsets
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants
import org.firstinspires.ftc.teamcodekt.components.LIFT_MID
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

@Disabled
@Autonomous
class NewMidLeftAuto : RogueBaseAuto() {
    override val startPose = GlobalUnits.pos(-91, -163, 90)

    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)

            .addTemporalMarker {
                bot.lift.targetHeight = LIFT_MID
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
        .splineTo(-78, -10.5, -25)

    private fun Anvil.goToDeposit(it: Int) = when (it) {
        0 -> splineTo(-78 + poleOffset.x, -10.5 + poleOffset.y, -25)
        1 -> splineTo(-78 + poleOffset.x, -10.5 + poleOffset.y, -25)
        2 -> splineTo(-78 + poleOffset.x, -10.5 + poleOffset.y, -25)
        3 -> splineTo(-78 + poleOffset.x, -10.5 + poleOffset.y, -25)
        4 -> splineTo(-78 + poleOffset.x, -10.5 + poleOffset.y, -25)
        else -> throw CycleException()
    }

    private fun Anvil.goToIntake(it: Int) = when (it) {
        0 -> splineTo(-182, 16, 180)
        1 -> splineTo(-182.2, 18.5, 180)
        2 -> splineTo(-182.1, 19, 180)
        3 -> splineTo(-182, 21, 180)
        4 -> splineTo(-181.3, 23, 180)
        else -> throw CycleException()
    }.doInReverse()

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-170) {
            bot.intake.disable()
        }

        .addTemporalMarker(-60) {
            bot.claw.close()
        }
//        .addTemporalMarker(90) {
//            mTelemetry.addLine("Setting pose... $tapeCorrection cm.")
//            bot.drive.poseEstimate = (Pose2d(bot.drive.poseEstimate.x, bot.drive.poseEstimate.y+tapeCorrection.toIn(), bot.drive.poseEstimate.heading))
//        }

        .addTemporalMarker(190) {
            bot.arm.setToForwardsPos()
            bot.lift.goToMid()
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
            bot.lift.goToMid()
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
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.wrist.setToBackwardsPos()
            bot.arm.targetAngle = 45.0
        }

        .addTemporalMarker(325) {
            bot.claw.openForIntakeWide()
//            bot.intake.enable()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(185) {
            bot.lift.targetHeight = liftOffsets[iterations]

            bot.arm.targetAngle = 45.0
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
