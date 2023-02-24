package org.firstinspires.ftc.teamcodekt.meepmeep

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.MeepMeep.Background
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.meepmeep.MeepMeepPersistence
import ftc.rogue.blacksmith.util.toRad
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants.*
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

fun main() {
    val mm = MeepMeep(800, 1000)

    MeepMeepPersistence(mm).restore()

    val bot = DefaultBotBuilder(mm)
        .setColorScheme(ColorSchemeBlueDark())
        .setConstraints(40.0, MAX_ACCEL, MAX_ANG_VEL, 250.toRad(), TRACK_WIDTH)
        .setDimensions(12.0, 12.0)
        .followTrajectorySequence(::mainTraj)

    mm.setBackground(Background.FIELD_POWERPLAY_OFFICIAL)
        .setDarkMode(true)
        .setBackgroundAlpha(0.95f)
        .addEntity(bot)
        .start()
}

val startPose = GlobalUnits.pos(-91, -163, 90)

fun mainTraj(drive: DriveShim) =
    Anvil.forgeTrajectory(drive, startPose)
        .initialGoToDeposit()
        .initialDeposit()

        .doTimes(RogueBaseAuto.NUM_CYCLES) {
            when (it) {
                RogueBaseAuto.LAST_CYCLE -> fastIntakePrep(it)
                else -> regularIntakePrep(it)
            }

            goToIntake(it)

            when (it) {
                RogueBaseAuto.LAST_CYCLE -> awaitFastIntake()
                else -> awaitRegularIntake()
            }

            goToDeposit(it)
            deposit(it)
        }

        .apply {
            resetBot()

            setPoseEstimateInTemporalMarker(internal.getCurrentEndPose())

            when (1) {
                1 -> inReverse {
                    splineTo(-160, -13.8, 180)
                }
                2 -> lineToLinearHeading(-92.5, -21, 90)
                3 -> {
                    lineToLinearHeading(-92.5, -21, 90)
                    lineToLinearHeading(-30, -16, 90)
                }
            }
        }

        .build<TrajectorySequence>()

private fun Anvil.initialGoToDeposit() = this
    .forward(132)
    .turn(-139.5)
    .lineToLinearHeading(-78.75, -42.5, -49)

private fun Anvil.goToDeposit(it: Int) = when (it) {
    0 -> splineTo(-80.2, -40.8, -39)
    1 -> splineTo(-79.1, -37, -36.4)
    2 -> splineTo(-76.9, -34.5, -33)
    3 -> splineTo(-76.7, -33.5, -26)
    4 -> splineTo(-76, -31.5, -25)
    else -> throw CycleException()
}

private fun Anvil.goToIntake(it: Int) = when (it) {
    0 -> splineTo(-161.2, -22.0, 180)
    1 -> splineTo(-160.3, -19.5, 180)
    2 -> splineTo(-160.0, -16.2, 180)
    3 -> splineTo(-160, -14.7, 180)
    4 -> splineTo(-160, -13.8, 180)
    else -> throw CycleException()
}.doInReverse()

private fun Anvil.awaitRegularIntake() = this
    .waitTime(300)

private fun Anvil.awaitFastIntake() = this
    .waitTime(120)

private fun Anvil.initialDeposit() = this

private fun Anvil.deposit(iterations: Int) = this

private fun Anvil.regularIntakePrep(iterations: Int) = this

private fun Anvil.fastIntakePrep(iterations: Int) = this

private fun Anvil.resetBot() = this