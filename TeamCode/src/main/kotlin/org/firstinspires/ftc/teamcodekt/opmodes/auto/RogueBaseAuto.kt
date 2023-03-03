@file:Suppress("MemberVisibilityCanBePrivate")

package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.outoftheboxrobotics.photoncore.PhotonCore
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.listeners.ReforgedGamepad
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.util.toCm
import org.firstinspires.ftc.teamcode.AutoData
import org.firstinspires.ftc.teamcodekt.components.*
import org.firstinspires.ftc.teamcodekt.components.meta.createAutoBotComponents
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

abstract class RogueBaseAuto : BlackOp() {
    protected val bot by evalOnGo(::createAutoBotComponents)
    protected var signalID by Delegates.notNull<Int>()

    protected abstract val startPose: Pose2d
    protected abstract fun mainTraj(startPose: Pose2d): Anvil

    protected var doBeforeInit: () -> Unit = {}
    protected var doDuringInit: () -> Unit = {}

    protected var aprilTagDetection = true

    protected var poleOffset = Vector2d()
        private set

    final override fun go() {
        PhotonCore.experimental.setMaximumParallelCommands(8)
        PhotonCore.enable()

        Imu.init(this)
        Imu.start()

//        readPoleOffset()

        val startTraj = mainTraj(startPose)
        Anvil.startAutoWith(startTraj).onSchedulerLaunch()

        doBeforeInit()
        while (!opModeIsActive()){
            doDuringInit()
            if(aprilTagDetection)
                signalID = bot.camera.stageDetection(this) ?: 2
        }



        Scheduler.debug(opmode = this) {
            bot.updateBaseComponents(useLiftDeadzone = false)
            bot.drive.update()
            mTelemetry.addLine("Pole offset: x->${poleOffset.x}, y->${poleOffset.y}")
            mTelemetry.addData("Loop time", loopTime)
            mTelemetry.update()
        }
    }

    protected lateinit var whichPole: String

    private fun readPoleOffset() {
        if (!::whichPole.isInitialized)
            return

        val driver = ReforgedGamepad(gamepad1)

        var x = 0.0
        var y = 0.0

        Scheduler.launchManually({ !gamepad1.a }) {
            driver.dpad_right.onRise { x += .25 }
            driver.dpad_left .onRise { x -= .25 }
            driver.dpad_up   .onRise { y += .25 }
            driver.dpad_down .onRise { y -= .25 }

            val xf = (if (x > 0) "+" else "-") + "%4.2f".format(x.absoluteValue)
            val yf = (if (y > 0) "+" else "-") + "%4.2f".format(y.absoluteValue)

            telemetry.addLine("""
                ${whichPole.uppercase()} POLE OFFSET:
                
                ↑
                | y = $yf : ← — → x = $xf 
                ↓
            """.trimIndent())

            telemetry.update()
        }

        driver.dpad_right.destroy()
        driver.dpad_left .destroy()
        driver.dpad_up   .destroy()
        driver.dpad_down .destroy()

        poleOffset = Vector2d(x.toCm(DistanceUnit.INCHES), y.toCm(DistanceUnit.INCHES))
    }

    companion object {
        const val NUM_CYCLES = 5
        const val LAST_CYCLE = NUM_CYCLES - 1

        @JvmStatic
        protected val liftOffsets = intArrayOf(
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_1,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_2,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_3,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_4,
            AutoData.AUTO_INTAKE_LIFT_HEIGHT_5,
        )
    }
}
