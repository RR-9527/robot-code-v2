package org.firstinspires.ftc.teamcodekt.components

import com.arcrobotics.ftclib.hardware.SimpleServo
import ftc.rogue.asp.HwComponent
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames

@HwComponent(targetProperty = "targetAngle", methodFormat = "setTo*Pos")
object ArmComponent {
    const val tArmForwards  = 136.5
    const val tArmBackwards = 44.5
    const val tArmResting   = 96.0

    const val tArmForwardsAngled = 27.5
    const val tArmBackwardsLower = 42.0

    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)

    var targetAngle = 0.0

    fun update() {
        armServo.turnToAngle(targetAngle)
    }
}
