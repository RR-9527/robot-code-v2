@file:Suppress("MemberVisibilityCanBePrivate")

package org.firstinspires.ftc.teamcodekt.opmodes.teleop

import com.outoftheboxrobotics.photoncore.PhotonCore
import com.qualcomm.hardware.lynx.LynxModule
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.listeners.ReforgedGamepad
import org.firstinspires.ftc.teamcodekt.components.chains.*
import org.firstinspires.ftc.teamcodekt.components.meta.createTeleOpBotComponents

abstract class RogueBaseTele : BlackOp() {
    protected val driver   by createOnGo<ReforgedGamepad> { gamepad1 }
    protected val codriver by createOnGo<ReforgedGamepad> { gamepad2 }

    protected var powerMulti = 0.0

    protected val bot by evalOnGo(::createTeleOpBotComponents)

    protected val intakeChain         by createOnGo< IntakeChain         >{ bot }
    protected val regularDepositChain by createOnGo< RegularDepositChain >{ bot }
    protected val reverseDepositChain by createOnGo< ReverseDepositChain >{ bot }

    protected val coneLaunchingChain        by createOnGo< ConeLaunchingChain       >{ bot }
    protected val angledConeUnflipperChain  by createOnGo< AngledConeUnflipperChain >{ bot }
    protected val flatConeUnflipperChain    by createOnGo< FlatConeUnflipperChain   >{ bot }

    final override fun go() {
        setupPhoton()

        describeControls()

//        Imu.init(this)
//        Imu.start()

        Scheduler.beforeEach {
            powerMulti = 1.0
            PhotonCore.CONTROL_HUB.clearBulkCache()
        }

        waitForStart()

        Scheduler.debug({ opModeIsActive() && !isStopRequested }) {
            bot.drivetrain.drive(driver.gamepad, powerMulti)
            bot.updateComponents(useLiftDeadzone = true)

            bot.lift.printLiftTelem()
            mTelemetry.addData("Loop time", loopTime)
            mTelemetry.update()
        }
    }

    fun setupPhoton() {
        PhotonCore.experimental.setMaximumParallelCommands(8)
        PhotonCore.enable()
        PhotonCore.CONTROL_HUB.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
    }

    abstract fun describeControls()
}
