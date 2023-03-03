@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.arcrobotics.ftclib.controller.PIDController
import com.arcrobotics.ftclib.hardware.motors.Motor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.BlackOp.Companion.mTelemetry
import ftc.rogue.blacksmith.util.kt.clamp
import ftc.rogue.blacksmith.util.kt.invoke
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import ftc.rogue.blacksmith.util.kalman.KalmanFilter
import kotlin.math.abs

// Too many fields...

@JvmField var LIFT_ZERO = 0
@JvmField var LIFT_LOW  = 240
@JvmField var LIFT_MID  = 495
@JvmField var LIFT_HIGH = 710

@JvmField var ANGLED_LIFT_LOW  = 210
@JvmField var ANGLED_LIFT_MID  = 340
@JvmField var ANGLED_LIFT_HIGH = 550

@JvmField var NORMAL_LIFT_P = 0.02
@JvmField var NORMAL_LIFT_I = 0.039
@JvmField var NORMAL_LIFT_D = 0.0003

@JvmField var PROCESS_NOISE     = 10.0
@JvmField var MEASUREMENT_NOISE = 10.0

/**
 * Lift object representing the lift on our V2 robot.
 * As of 2/2, using normal PIDF with the kalman filter is very good. +-5 encoder tick accuracy
 */
class Lift {
    private val liftMotor = hwMap<DcMotorSimple>(DeviceNames.LIFT_MOTOR)

    private val liftNormalPID = PIDController(NORMAL_LIFT_P, NORMAL_LIFT_I, NORMAL_LIFT_D)

    private val liftFilter = KalmanFilter(PROCESS_NOISE, MEASUREMENT_NOISE)

    private val liftEncoder = Motor(hwMap, DeviceNames.LIFT_ENCODER)
        .apply(Motor::resetEncoder)

    private var drivenCorrection = 0.0

    var targetHeight = 0

    var clippedHeight: Int
        get() = targetHeight
        set(height) {
            targetHeight = height.clamp(LIFT_ZERO, LIFT_HIGH)
        }

    fun goToZero() {
        targetHeight = LIFT_ZERO
    }

    fun goToLow() {
        targetHeight = LIFT_LOW
    }

    fun goToMid() {
        targetHeight = LIFT_MID
    }

    fun goToHigh() {
        targetHeight = LIFT_HIGH
    }

    fun goToAngledLow() {
        targetHeight = ANGLED_LIFT_LOW
    }

    fun goToAngledMid() {
        targetHeight = ANGLED_LIFT_MID
    }

    fun goToAngledHigh() {
        targetHeight = ANGLED_LIFT_HIGH
    }

    fun update(useDeadzone: Boolean) {
        val inDeadzone = !useDeadzone || abs(targetHeight - liftHeight) > 3

        if (inDeadzone) {
            val liftHeight   = liftHeight.toDouble()
            val targetHeight = targetHeight.toDouble()

            val correction = liftNormalPID.calculate(liftHeight, targetHeight)
            val filteredCorrection = liftFilter.filter(correction)

            drivenCorrection = filteredCorrection
            liftMotor.power = filteredCorrection
        } else {
            drivenCorrection = 0.0
            liftMotor.power = 0.0
        }
    }

    fun resetEncoder() {
        liftEncoder.resetEncoder()
    }

    private val liftHeight: Int
        get() = -liftEncoder.currentPosition

    fun printLiftTelem() {
        mTelemetry.addData("Current lift height:", liftHeight)
        mTelemetry.addData("Lift target height:", targetHeight)
        mTelemetry.addData("Driven motor power:", drivenCorrection)
    }
}
