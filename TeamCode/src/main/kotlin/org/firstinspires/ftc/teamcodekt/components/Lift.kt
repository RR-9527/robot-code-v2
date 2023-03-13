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
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.BlackOp.Companion.mTelemetry
import ftc.rogue.blacksmith.util.kalman.KalmanFilter
import ftc.rogue.blacksmith.util.kt.clamp
import ftc.rogue.blacksmith.util.kt.invoke
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import kotlin.math.abs


// Too many fields...

@JvmField var LIFT_ZERO = 0
@JvmField var LIFT_LOW  = 250
@JvmField var LIFT_MID  = 495
@JvmField var LIFT_HIGH = 710

@JvmField var ANGLED_LIFT_LOW  = 210
@JvmField var ANGLED_LIFT_MID  = 340
@JvmField var ANGLED_LIFT_HIGH = 615

@JvmField var NORMAL_LIFT_P = 0.0005
@JvmField var NORMAL_LIFT_I = 0.0001
@JvmField var NORMAL_LIFT_D = 0.0001
@JvmField var AUTO_LIFT_P = 0.0014
@JvmField var AUTO_LIFT_I = 0.00005
@JvmField var AUTO_LIFT_D = 0.001

@JvmField var PROCESS_NOISE     = 99999999.0
@JvmField var MEASUREMENT_NOISE = 99999999.0

@JvmField var MAX_V = 20000.0
@JvmField var MAX_A = 20000.0
@JvmField var MAX_J = 20000.0

/**
 * Lift object representing the lift on our V2 robot.
 * As of 2/2, using normal PIDF with the kalman filter is very good. +-5 encoder tick accuracy
 */
class Lift {
    private val liftMotor = hwMap<DcMotorSimple>(DeviceNames.LIFT_MOTOR)

    private val liftNormalPID = PIDController(NORMAL_LIFT_P, NORMAL_LIFT_I, NORMAL_LIFT_D)

    private val liftMotionProfilePID = PIDFController(PIDCoefficients(NORMAL_LIFT_P, NORMAL_LIFT_I, NORMAL_LIFT_D))
    private val autoLiftMotionProfilePID = PIDFController(PIDCoefficients(AUTO_LIFT_P, AUTO_LIFT_I, AUTO_LIFT_D))
    private var motionProfile: MotionProfile = MotionProfileGenerator.generateSimpleMotionProfile(
        MotionState(0.0, 0.0, 0.0),
        MotionState(0.0, 0.0, 0.0),
        MAX_V,
        MAX_A,
        MAX_J
    )
    private var moveTime = ElapsedTime()

    private val liftFilter = KalmanFilter(PROCESS_NOISE, MEASUREMENT_NOISE)

    private val liftEncoder = Motor(hwMap, DeviceNames.LIFT_ENCODER)
        .apply(Motor::resetEncoder)

    private var drivenCorrection = 0.0

    var targetHeight: Int = 0
        set(height) {
            field = height
            generateMotionProfile()

        }
    var clippedHeight: Int
        get() = targetHeight
        set(height) {
            targetHeight = height.clamp(LIFT_ZERO, LIFT_HIGH)
            generateMotionProfile()

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

    fun generateMotionProfile() {
        motionProfile = MotionProfileGenerator.generateSimpleMotionProfile(
            MotionState(liftHeight.toDouble(), 0.0, 0.0),
            MotionState(targetHeight.toDouble(), 0.0, 0.0),
            MAX_V,
            MAX_A,
            MAX_J
        )
        moveTime.reset()
    }

    fun updateMotionProfile(auto: Boolean) {
        val state = motionProfile[moveTime.milliseconds()/1000]

        val correction: Double
        if(!auto) {
            liftMotionProfilePID.apply {
                targetPosition = state.x
                targetVelocity = state.v
                targetAcceleration = state.a
            }
            correction = liftMotionProfilePID.update(liftHeight.toDouble())
        }
        else {
            autoLiftMotionProfilePID.apply {
                targetPosition = state.x
                targetVelocity = state.v
                targetAcceleration = state.a
            }
            correction = autoLiftMotionProfilePID.update(liftHeight.toDouble())
        }

        liftMotor.power += correction
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
