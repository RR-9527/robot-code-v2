package org.firstinspires.ftc.teamcodekt.components

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.util.kt.invoke
import org.firstinspires.ftc.robotcore.external.navigation.Orientation

object Imu {
    private lateinit var imu: BNO055IMU

    @get:JvmStatic
    var angles = arrayOf<Float>()
        private set

    @get:JvmStatic
    var xRotationRate = 0f
        private set

    private var start = false

    fun start() {
        start = true
    }

    fun init(opmode: LinearOpMode) {
        imu = BlackOp.hwMap("imu")

        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        imu.initialize(parameters)

        Thread{
            while (!opmode.isStopRequested && (start || opmode.opModeIsActive())) {
                val (x, y, z) = imu.angularOrientation
                angles = arrayOf(x, y, z)
                xRotationRate = imu.angularVelocity.xRotationRate
            }
        }.start()
    }

    private operator fun Orientation.component1(): Float =
        firstAngle

    private operator fun Orientation.component2(): Float =
        secondAngle

    private operator fun Orientation.component3(): Float =
        thirdAngle
}
