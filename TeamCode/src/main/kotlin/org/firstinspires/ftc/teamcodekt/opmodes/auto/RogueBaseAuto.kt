@file:Suppress("MemberVisibilityCanBePrivate")

package org.firstinspires.ftc.teamcodekt.opmodes.auto

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.outoftheboxrobotics.photoncore.PhotonCore
import com.qualcomm.hardware.lynx.LynxModule
import ftc.rogue.blacksmith.Anvil
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.internal.scheduler.Schedulables
import ftc.rogue.blacksmith.listeners.ReforgedGamepad
import ftc.rogue.blacksmith.units.DistanceUnit
import ftc.rogue.blacksmith.util.toCm
import org.firstinspires.ftc.teamcode.pipelines.TapeDetector
import org.firstinspires.ftc.teamcodekt.components.*
import org.firstinspires.ftc.teamcodekt.components.meta.createAutoBotComponents
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

abstract class RogueBaseAuto : BlackOp() {
    protected val bot by evalOnGo(::createAutoBotComponents)
    protected var signalID by Delegates.notNull<Int>()

    protected var tapeCorrection = 0.0

    protected abstract val startPose: Pose2d
    protected abstract fun mainTraj(startPose: Pose2d): Anvil

    protected var poleOffset = Vector2d()
        private set

    final override fun go() {
        PhotonCore.experimental.setMaximumParallelCommands(6)
        PhotonCore.enable()
        PhotonCore.CONTROL_HUB.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL

//        Imu.init(this)
//        Imu.start()

//        readPoleOffset()

        val startTraj = mainTraj(startPose)
        Anvil.startAutoWith(startTraj).onSchedulerLaunch()


        bot.camera.lookForwards()

        while (!opModeIsActive()) {
            mTelemetry.update()
            signalID = bot.camera.stageDetection(this) ?: 2
        }

        bot.camera.camera.stopStreaming()
//        bot.camera.setPipeline(bot.camera.tapeDetectorPipeline)

        bot.camera.lookDown()

        Scheduler.debug({ opModeIsActive() && !isStopRequested }) {
            bot.updateComponents(useLiftDeadzone = false, true)

            bot.lift.printLiftTelem()
            mTelemetry.addLine("Pole offset: x->${poleOffset.x}, y->${poleOffset.y}")
            mTelemetry.addData("Loop time", loopTime)
            mTelemetry.addData("FPS", bot.camera.camera.fps)
            mTelemetry.addData("Overhead ms", bot.camera.camera.overheadTimeMs)
            mTelemetry.addData("Pipeline ms", bot.camera.camera.pipelineTimeMs)
            mTelemetry.addData("Angle: ", bot.camera.tapeDetectorPipeline.tapeAngle)
            mTelemetry.update()

            PhotonCore.CONTROL_HUB.clearBulkCache()
        }
    }

    protected lateinit var whichPole: String

    protected fun updateTapeCorrection(){
        tapeCorrection = bot.camera.tapeDetectorPipeline.correction
    }

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

            quickLog("""
                ${whichPole.uppercase()} POLE OFFSET:
                
                ↑
                | y = $yf : ← — → x = $xf 
                ↓
            """.trimIndent())
        }

        Scheduler.nuke(Schedulables)

        poleOffset = Vector2d(x.toCm(DistanceUnit.INCHES), y.toCm(DistanceUnit.INCHES))
    }

    companion object {
        const val NUM_CYCLES = 5
        const val LAST_CYCLE = NUM_CYCLES - 1
    }
}
