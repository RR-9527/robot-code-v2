@file:Suppress("LocalVariableName")
@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.arcrobotics.ftclib.hardware.RevIMU
import com.noahbres.meepmeep.core.exhaustive
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.util.ElapsedTime
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.BlackOp.Companion.mTelemetry
import ftc.rogue.blacksmith.annotations.ConfigKt
import ftc.rogue.blacksmith.listeners.Pulsar
import ftc.rogue.blacksmith.util.kt.invoke
import ftc.rogue.blacksmith.util.kt.maxMagnitudeAbs
import ftc.rogue.blacksmith.util.kt.pow
import ftc.rogue.blacksmith.util.kt.withDeadzone
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import java.util.*
import kotlin.math.*

@JvmField
var tiltCorrectionMult = 0.01

@JvmField
var imuAngleUsed = 1 // or 0/1, not sure which one it is... will have to test.

class Drivetrain {
    private val frontLeft  = hwMap<DcMotorEx>(DeviceNames.DRIVE_FL).apply { direction = Direction.REVERSE }
    private val frontRight = hwMap<DcMotorEx>(DeviceNames.DRIVE_FR)
    private val backLeft   = hwMap<DcMotorEx>(DeviceNames.DRIVE_BL).apply { direction = Direction.REVERSE }
    private val backRight  = hwMap<DcMotorEx>(DeviceNames.DRIVE_BR)

    private val imu = RevIMU(hwMap)

    init {
        imu.init()

        withEachMotor {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    fun drive(gamepad: Gamepad, powerMulti: Double) {
            driveRC(gamepad, powerMulti)
    }

    private fun driveRC(gamepad: Gamepad, powerMulti: Double) {
        var (x, y, r) = gamepad.getDriveSticks()
        r *= .9f

//        mTelemetry.addData("x corection", (tiltCorrectionMult * imu.angles[imuAngleUsed]).withDeadzone<Float>(.25))

        val theta = atan2(y, x)
        val power = hypot(x, y)

        val xComponent = power * cos(theta - PI / 4)
        val yComponent = power * sin(theta - PI / 4)

//        xComponent += (tiltCorrectionMult * imu.angles[imuAngleUsed]).withDeadzone<Float>(.25)

        val max = maxMagnitudeAbs<Double>(xComponent, yComponent, 1e-16)

        val powers = doubleArrayOf(
            power * (xComponent / max) + r,
            power * (yComponent / max) - r,
            power * (yComponent / max) + r,
            power * (xComponent / max) - r,
        )

        if (power + abs(r) > 1) {
            powers.mapInPlace { it / (power + abs(r)) }
        }

        val _powerMulti = if (!gamepad.isAnyJoystickTriggered()) 0.0 else powerMulti

        powers.mapInPlace { (it pow 3) * _powerMulti }

        withEachMotor {
            this.power = powers[it]
        }

//        val (a, b, c) = imu.angles
//
//        mTelemetry.addData("0rd angle", a)
//        mTelemetry.addData("1nd angle", b)
//        mTelemetry.addData("2st angle", c)
    }

    private fun DoubleArray.mapInPlace(transform: (Double) -> Double) = repeat(size) {
        this[it] = transform(this[it])
    }

    private fun withEachMotor(transformation: DcMotorEx.(Int) -> Unit) {
        frontLeft .transformation(0)
        frontRight.transformation(1)
        backLeft  .transformation(2)
        backRight .transformation(3)
    }
}
