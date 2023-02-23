@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.acmerobotics.roadrunner.control.PIDCoefficients
import com.acmerobotics.roadrunner.control.PIDFController
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.arcrobotics.ftclib.controller.PIDController
import com.arcrobotics.ftclib.hardware.motors.Motor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.util.kt.clamp
import ftc.rogue.blacksmith.util.kt.invoke
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import ftc.rogue.blacksmith.util.kalman.KalmanFilter
import kotlin.math.abs

// Too many fields...

@JvmField var LIFT_ZERO = 0
@JvmField var LIFT_LOW  = 270
@JvmField var LIFT_MID  = 533
@JvmField var LIFT_HIGH = 750

@JvmField var ANGLED_LIFT_LOW  = 210
@JvmField var ANGLED_LIFT_MID  = 352
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

    fun goToAngledHigh() {
        targetHeight = ANGLED_LIFT_HIGH
    }

    fun goToAngledMidPredeposit() {
        targetHeight = ANGLED_LIFT_MID
    }

    fun goToAngledMidButHigher() {
        targetHeight = ANGLED_LIFT_MID
    }

    fun goToAngledLow() {
        targetHeight = ANGLED_LIFT_LOW
    }

    fun update() {
//        if (abs(targetHeight - liftHeight) > 3) {
//            val correction = liftNormalPID.calculate(liftHeight.toDouble(), targetHeight.toDouble())
//            val filteredCorrection = liftFilter.filter(correction)
//            drivenCorrection = filteredCorrection
//            liftMotor.power = filteredCorrection
//        } else {
//            liftMotor.power = 0.0
//            drivenCorrection = 0.0
//        }
    }

    fun resetEncoder() {
        liftEncoder.resetEncoder()
    }

    private val liftHeight: Int
        get() = -liftEncoder.currentPosition

    fun printLiftTelem() {
        BlackOp.mTelemetry.addData("Current lift height:", liftHeight)
        BlackOp.mTelemetry.addData("Lift target height:", targetHeight)
        BlackOp.mTelemetry.addData("Driven motor power:", drivenCorrection)
    }
}
