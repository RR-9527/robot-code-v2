@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.arcrobotics.ftclib.hardware.SimpleServo
import ftc.rogue.asp.HwComponentFile
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames

var t_ARM_FORWARDS  = 136.5
var t_ARM_BACKWARDS = 44.5
var t_ARM_RESTING   = 96.0

var t_ARM_FORWARDS_ANGLED = ARM_FORWARDS - 17
var t_ARM_BACKWARDS_LOWER = 42.0

@HwComponentFile
class Arm {
    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)

    var targetAngle = 0.0

    fun setToForwardsPos() {
        targetAngle = ARM_FORWARDS
    }

    fun setToBackwardsPos() {
        targetAngle = ARM_BACKWARDS
    }

    fun setToForwardsAngledPos() {
        targetAngle = ARM_FORWARDS_ANGLED
    }

    fun setToBackwardsPosButLikeSliiiightlyLower() {
        targetAngle = ARM_BACKWARDS_LOWER
    }

    fun setToRestingPos() {
        targetAngle = ARM_RESTING
    }

    fun update() {
        armServo.turnToAngle(targetAngle)
    }
}
