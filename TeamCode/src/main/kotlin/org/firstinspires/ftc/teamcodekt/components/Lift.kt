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

@JvmField
var LIFT_ZERO = 0

@JvmField
var LIFT_LOW = 100

@JvmField
var LIFT_MID = 350

@JvmField
var LIFT_HIGH = 700

@JvmField
var ANGLED_LIFT_LOW = 120

@JvmField
var ANGLED_LIFT_MID = 300

@JvmField
var ANGLED_LIFT_HIGH = 600

@JvmField
var NORMAL_LIFT_P = 0.012

@JvmField
var NORMAL_LIFT_I = 0.029

@JvmField
var NORMAL_LIFT_D = 0.0001

@JvmField
var MOTION_PROFILE_LIFT_P = 0.022

@JvmField
var MOTION_PROFILE_LIFT_I = 0.55

@JvmField
var MOTION_PROFILE_LIFT_D = 0.0013

@JvmField
var LIFT_MAX_V = 32000.0

@JvmField
var LIFT_MAX_A = 21000.0

@JvmField
var LIFT_MAX_J = 12000.0

@JvmField
var PROCESS_NOISE = 10.0

@JvmField
var MEASUREMENT_NOISE = 10.0

/**
 * Lift object representing the lift on our V2 robot.
 * As of 2/2, using normal PIDF with the kalman filter is very good. +-5 encoder tick accuracy
 */
class Lift(private val usingMotionProfiling: Boolean) {
    private val liftMotor = hwMap<DcMotorSimple>(DeviceNames.LIFT_MOTOR)

    private val liftMotionProfilePID = PIDFController(
        PIDCoefficients(
            MOTION_PROFILE_LIFT_P,
            MOTION_PROFILE_LIFT_I,
            MOTION_PROFILE_LIFT_D
        )
    )

    private val liftNormalPID = PIDController(NORMAL_LIFT_P, NORMAL_LIFT_I, NORMAL_LIFT_D)

    private val liftFilter = KalmanFilter(PROCESS_NOISE, MEASUREMENT_NOISE)

    private val liftEncoder = Motor(hwMap, DeviceNames.LIFT_ENCODER)
        .apply(Motor::resetEncoder)

    private lateinit var profile: MotionProfile

    private val motionTime = ElapsedTime()

    private var drivenCorrection = 0.0

    init {
//        liftNormalPID.targetAcceleration = 0.0
//        liftNormalPID.targetVelocity = 0.0
        if (usingMotionProfiling) {
            regenMotionProfile(0)
        }
    }

    var targetHeight = 0
        set(height) {
            field = height

            if (usingMotionProfiling) {
                regenMotionProfile(field)
            }
        }

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

    fun goToAngledMid() {
        targetHeight = ANGLED_LIFT_MID
    }

    fun goToAngledMidPredeposit() {
        targetHeight = ANGLED_LIFT_MID + 50
    }

    fun goToAngledMidButHigher() {
        targetHeight = ANGLED_LIFT_MID + 85
    }

    fun goToAngledLow() {
        targetHeight = ANGLED_LIFT_LOW
    }

    fun update() {
        if (usingMotionProfiling) {
            val state = profile[motionTime.microseconds()]

            liftMotionProfilePID.apply {
                targetPosition = state.x
                targetVelocity = state.v
                targetAcceleration = state.a
            }

            var correction = liftMotionProfilePID.update(liftHeight.toDouble(), liftVelocity)
            if (liftHeight < 10 && targetHeight == LIFT_ZERO)
                correction = 0.0
            liftMotor.power = correction
        } else {
            if (abs(liftHeight - targetHeight) < 6) {
                drivenCorrection = 0.0
                liftMotor.power = 0.0
            } else {
//                liftNormalPID.targetPosition = targetHeight.toDouble()
//                val correction = liftNormalPID.update(liftHeight.toDouble(), liftVelocity)
                val correction = liftNormalPID.calculate(liftHeight.toDouble(), targetHeight.toDouble())
                val filteredCorrection = liftFilter.filter(correction)
                drivenCorrection = filteredCorrection
                liftMotor.power = filteredCorrection

            }
        }
    }

    fun resetEncoder() {
        liftEncoder.resetEncoder()
    }

    private var twoPrevVel = 0.0
    private var onePrevVel = 0.0

    private var twoPrevTime = 0L
    private var onePrevTime = 0L

    private fun regenMotionProfile(targetHeight: Int) {
        motionTime.reset()
        // TODO: add exception handler for comp
        profile = MotionProfileGenerator.generateSimpleMotionProfile(
            MotionState(liftHeight.toDouble(), liftVelocity, liftAccel),
            MotionState(targetHeight.toDouble(), 0.0, 0.0),
            LIFT_MAX_V, LIFT_MAX_A, LIFT_MAX_J
        )
    }

    private val liftHeight: Int
        get() = -liftEncoder.currentPosition

    private val liftVelocity: Double
        get() {
            twoPrevVel = onePrevVel
            onePrevVel = liftEncoder.correctedVelocity

            twoPrevTime = onePrevTime
            onePrevTime = System.currentTimeMillis()
            return liftEncoder.correctedVelocity
        }

    private val liftAccel: Double
        get() {
            // TODO: Check if this actually works. a bit sus imo but idk might work or be close enough
            if (twoPrevTime == 0L || onePrevTime == 0L)
                return 0.0

            return 1000 * ((onePrevVel - twoPrevVel) / (onePrevTime - twoPrevTime))
        }

    fun printLiftTelem() {
        BlackOp.mTelemetry.addData("Current lift height:", liftHeight)
        BlackOp.mTelemetry.addData("Lift target height:", targetHeight)
        BlackOp.mTelemetry.addData("Driven motor power:", drivenCorrection)
    }

    private fun ElapsedTime.microseconds(): Double {
        return this.milliseconds() / 1000
    }
}
