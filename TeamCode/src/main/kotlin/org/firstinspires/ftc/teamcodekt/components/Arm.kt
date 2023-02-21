@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.arcrobotics.ftclib.hardware.SimpleServo
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames

@JvmField
var ARM_FORWARDS = 136.5
@JvmField
var ARM_BACKWARDS = 44.5
@JvmField
var ARM_RESTING = 96.0
@JvmField
var ARM_DEP_ANGLED = 122.5
@JvmField
var ARM_DEP_FLAT = 140.5

class Arm {
    private val armServo = SimpleServo(hwMap, DeviceNames.ARM_SERVO, 0.0, 180.0)

    var targetAngle = ARM_RESTING
        set(value) {
            field = value
        }

    fun setToForwardsPos() {
        targetAngle = ARM_FORWARDS
    }

    fun setToBackwardsPos() {
        targetAngle = ARM_BACKWARDS
    }

    fun setToForwardsAngledPos() {
        targetAngle = ARM_FORWARDS - 23
    }

    fun setToBackwardsPosButLikeSliiiightlyHigher() {
        targetAngle = 42.0
    }

    fun setToBackwardsPosLastCycle() {
        targetAngle = 39.5
    }

    fun setToRestingPos() {
        targetAngle = ARM_RESTING
    }

    fun update() {
        armServo.turnToAngle(targetAngle)
    }
}
