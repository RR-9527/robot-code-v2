package org.firstinspires.ftc.teamcodekt.components

import com.arcrobotics.ftclib.hardware.SimpleServo
import ftc.rogue.asp.annotations.Component
import ftc.rogue.asp.annotations.SetMethodName
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames

@Component(targetProperty = "targetAngle", methodFormat = "setTo*Pos")
object ArmComponent {
    const val tArmForwards = 138.75
    const val tArmBackwards = 42.0

    @SetMethodName("goToRest")
    const val tArmResting = 92.5

    const val tArmBackwardsLower = 42.0
    const val tArmForwardsAngled = 122.5
    const val tArmForwardsDown = 160.0

    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)

    var targetAngle = ArmResting

    fun update() {
        armServo.turnToAngle(targetAngle)
    }
}
