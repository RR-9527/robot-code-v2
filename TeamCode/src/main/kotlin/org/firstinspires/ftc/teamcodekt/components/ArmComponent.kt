package org.firstinspires.ftc.teamcodekt.components

import com.arcrobotics.ftclib.hardware.SimpleServo
import ftc.rogue.asp.annotations.Component
import ftc.rogue.asp.annotations.SetMethodName
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames

@Component(targetProperty = "targetAngle", methodFormat = "setTo*Pos")
object ArmComponent {
    const val tArmForwards = 138.75
    const val tArmBackwards = 44.5

    @SetMethodName("goToRest")
    const val tArmResting = 96.0

    const val tArmBackwardsLower = 42.0
    const val tArmForwardsAngled = 27.5

    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)

    var targetAngle = ArmResting

    fun update() {
        armServo.turnToAngle(targetAngle)
    }
}

































//@JvmField
//var ArmForwards = 136.5
//
//@JvmField
//var ArmResting = 96.0
//
//@JvmField
//var ArmBackwards = 44.5
//
//@JvmField
//var ArmBackwardsLower = 42.0
//
//@JvmField
//var ArmForwardsAngled = 27.5
//
//class Arm {
//    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)
//
//    var targetAngle = 0.0
//
//    fun update() {
//        armServo.turnToAngle(targetAngle)
//    }
//
//    fun setToForwardsPos() {
//        targetAngle = ArmForwards
//    }
//
//    fun goToRest() {
//        targetAngle = ArmResting
//    }
//
//    fun setToBackwardsPos() {
//        targetAngle = ArmBackwards
//    }
//
//    fun setToBackwardsLowerPos() {
//        targetAngle = ArmBackwardsLower
//    }
//
//    fun setToForwardsAngledPos() {
//        targetAngle = ArmForwardsAngled
//    }
//}
