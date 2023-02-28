package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants

abstract class RogueMidAuto : RogueBaseAuto() {
    override fun mainTraj(startPose: Pose2d) =
        Anvil.forgeTrajectory(bot.drive, startPose)
            .initialSetup()
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
                deposit()
            }

            .thenRun(::parkTraj)

    abstract fun Anvil.initialGoToDeposit(): Anvil

    abstract fun Anvil.goToDeposit(it: Int): Anvil

    abstract fun Anvil.goToIntake(it: Int): Anvil

    abstract fun parkTraj(startPose: Pose2d): Anvil

    private fun Anvil.initialSetup() = this
        .setVelConstraint(44, 250.toRad(), DriveConstants.TRACK_WIDTH)

        .addTemporalMarker {
            bot.lift.goToAngledMid()
            bot.claw.close()
            bot.arm.setToForwardsAngledPos()
            bot.wrist.setToForwardsPos()
        }

    private fun Anvil.awaitRegularIntake() = this
        .addTemporalMarker(-55) {
            bot.claw.close()
        }

        .addTemporalMarker(125) {
            bot.lift.goToAngledMid()
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
            bot.lift.goToAngledMid()
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

    private fun Anvil.deposit() = this.apply {
        addTemporalMarker(-165) {
            bot.lift.targetHeight -= AutoData.DEPOSIT_DROP_AMOUNT
            bot.arm.setToForwardsPos()
        }

        addTemporalMarker(-25) {
            bot.claw.openForDeposit()
        }
    }

    private fun Anvil.regularIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            bot.lift.targetHeight = liftOffsets[iterations]
            bot.wrist.setToBackwardsPos()
            bot.arm.setToBackwardsPosButLikeSliiiightlyLower()
        }

        .addTemporalMarker(205) {
            bot.claw.openForIntakeWide()
        }

    private fun Anvil.fastIntakePrep(iterations: Int) = this
        .addTemporalMarker(65) {
            bot.arm.setToBackwardsPosButLikeSliiiightlyLower()
            bot.wrist.setToBackwardsPos()
        }

        .addTemporalMarker(165) {
            bot.lift.targetHeight = liftOffsets[iterations]
            bot.claw.openForIntakeNarrow()
            bot.intake.enable()
        }

    protected fun Anvil.resetBot() = this
        .addTemporalMarker {
            bot.arm.setToRestingPos()
            bot.wrist.setToRestingPos()
            bot.lift.goToZero()
            bot.claw.close()
        }
}
