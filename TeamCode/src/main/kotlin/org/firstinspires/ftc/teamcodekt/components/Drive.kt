@file:Suppress("LocalVariableName")
@file:Config

package org.firstinspires.ftc.teamcodekt.components

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction
import com.qualcomm.robotcore.hardware.Gamepad
import ftc.rogue.blacksmith.BlackOp.Companion.hwMap
import ftc.rogue.blacksmith.util.kt.invoke
import ftc.rogue.blacksmith.util.kt.maxMagnitudeAbs
import ftc.rogue.blacksmith.util.kt.pow
import org.firstinspires.ftc.teamcodekt.components.meta.DeviceNames
import java.util.*
import kotlin.math.*

class Drivetrain {
    private val frontLeft  = hwMap<DcMotorEx>(DeviceNames.DRIVE_FL).apply { direction = Direction.REVERSE }
    private val frontRight = hwMap<DcMotorEx>(DeviceNames.DRIVE_FR)
    private val backLeft   = hwMap<DcMotorEx>(DeviceNames.DRIVE_BL).apply { direction = Direction.REVERSE }
    private val backRight  = hwMap<DcMotorEx>(DeviceNames.DRIVE_BR)

    init {
        withEachMotor {
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        }
    }

    fun drive(gamepad: Gamepad, powerMulti: Double) {
        driveRC(gamepad, powerMulti)
    }

    private fun driveRC(gamepad: Gamepad, powerMulti: Double) {
        val (x, y, _r) = gamepad.getDriveSticks()
        val r = _r * .9f

        val theta = atan2(y, x)
        val power = hypot(x, y)

        val xComponent = power * cos(theta - PI / 4)
        val yComponent = power * sin(theta - PI / 4)

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
