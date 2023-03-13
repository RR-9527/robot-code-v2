package org.firstinspires.ftc.teamcodekt.components.meta

import com.qualcomm.hardware.rev.RevColorSensorV3
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.util.kt.invoke
import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive
import org.firstinspires.ftc.teamcodekt.components.*

abstract class BaseBotComponents {
    val claw   = Claw()
    val intake = Intake()
    val arm    = Arm()
    val wrist  = Wrist()
    val lift   = Lift()

    open fun updateComponents(useLiftDeadzone: Boolean, auto: Boolean) {
        claw.update()
        arm.update()
        wrist.update()
//        lift.update(useLiftDeadzone)
        lift.updateMotionProfile(auto)
    }
}

fun createTeleOpBotComponents() =
    TeleOpBotComponents(
        hwMap(DeviceNames.COLOR_SENSOR),
        Drivetrain(),
    )

data class TeleOpBotComponents(
    val rcs: RevColorSensorV3,
    val drivetrain: Drivetrain,
) : BaseBotComponents()

fun createAutoBotComponents() =
    AutoBotComponents(
        SampleMecanumDrive(hwMap),
        Camera(),
    )

data class AutoBotComponents(
    val drive: SampleMecanumDrive,
    val camera: Camera,
) : BaseBotComponents() {
    override fun updateComponents(useLiftDeadzone: Boolean, auto: Boolean) {
        super.updateComponents(useLiftDeadzone, true)
        camera.update()
        drive.update()
    }
}
