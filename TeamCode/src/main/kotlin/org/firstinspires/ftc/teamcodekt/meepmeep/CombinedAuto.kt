package org.firstinspires.ftc.teamcodekt.meepmeep

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.MeepMeep.Background
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.units.GlobalUnits
import ftc.rogue.blacksmith.util.meepmeep.MeepMeepUtil
import org.firstinspires.ftc.teamcode.roadrunner.drive.DriveConstants.*
import org.firstinspires.ftc.teamcodekt.opmodes.auto.RogueBaseAuto
import org.firstinspires.ftc.teamcodekt.util.CycleException

fun main() {
    val mm = MeepMeep(800, 60)

    val util = MeepMeepUtil(mm)

    util.persistence()
        .restore()

//    val bot = DefaultBotBuilder(mm)
//        .setColorScheme(ColorSchemeBlueDark())
//        .setConstraints(52.0, MAX_ACCEL, MAX_ANG_VEL, MAX_ANG_ACCEL, TRACK_WIDTH)
//        .setDimensions(12.0, 12.0)
//        .followTrajectorySequence(::mainTraj)

    mm.setBackground(Background.FIELD_POWERPLAY_KAI_DARK)
        .setDarkMode(true)
        .setBackgroundAlpha(0.95f)
        .addEntity(util.splineVisualizer()
            .setStartPose(76, -163)
            .setEndPose(80, -80)
            .getAndStart())
        .addEntity(util.botShadowOnHover(12.0, 12.0))
        .addEntity(util.customMouseCoords())

    mm::class.java.getMethod("requestToRemoveEntity", Entity::class.java)
        .invoke(mm, mm::class.java.getDeclaredField("DEFAULT_AXES_ENTITY").get(mm))

    mm.start()
}

private val startPose = GlobalUnits.pos(-86.25, -163, 90)

private fun mainTraj(drive: DriveShim) =
    Anvil.forgeTrajectory(drive, startPose)
        .initialGoToDeposit()
        .awaitDeposit()

        .doTimes(RogueBaseAuto.NUM_CYCLES) {
            goToIntake(it)
            awaitIntake()
            goToDeposit(it)
            awaitDeposit()
        }

        .apply {
            when (1) {
                1 -> inReverse {
                    splineTo(-143, -30, 180)
                }
                2 -> inReverse {
                    splineTo(-87, -30, 180)
                }
                3 -> inReverse {
                    splineTo(-27, -30, 90)
                }
            }
        }

        .build<TrajectorySequence>()

private fun Anvil.initialGoToDeposit() = this
    .splineTo(-77, -46, -40)

private fun Anvil.goToDeposit(it: Int) = when (it) {
    in 0..4 -> {
        splineTo(-120, -30, -0)
        splineTo(-19, -49, -28)
    }
    else -> throw CycleException()
}

private fun Anvil.goToIntake(it: Int) = when (it) {
    in 0..4 -> {
        splineTo(-120, -30, 180)
        splineTo(-157, -30, 180)
    }
    else -> throw CycleException()
}.doInReverse(2)

private fun Anvil.awaitIntake() = this
    .waitTime(350)

private fun Anvil.awaitDeposit() = this
    .waitTime(250)
