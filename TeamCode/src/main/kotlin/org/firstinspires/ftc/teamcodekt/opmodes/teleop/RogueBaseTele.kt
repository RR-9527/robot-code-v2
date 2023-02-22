@file:Suppress("MemberVisibilityCanBePrivate")

package org.firstinspires.ftc.teamcodekt.opmodes.teleop

import com.outoftheboxrobotics.photoncore.PhotonCore
import ftc.rogue.blacksmith.BlackOp
import ftc.rogue.blacksmith.Scheduler
import ftc.rogue.blacksmith.listeners.ReforgedGamepad
import org.firstinspires.ftc.teamcodekt.components.Imu
import org.firstinspires.ftc.teamcodekt.components.chains.*
import org.firstinspires.ftc.teamcodekt.components.meta.createTeleOpBotComponents

abstract class RogueBaseTele : BlackOp() {
    protected val driver   by createOnGo<ReforgedGamepad>{ gamepad1 }
    protected val codriver by createOnGo<ReforgedGamepad>{ gamepad2 }

    protected var powerMulti = 0.0

    protected val bot by evalOnGo(::createTeleOpBotComponents)

    protected val intakeChain         by createOnGo< IntakeChain         > { bot }
    protected val regularDepositChain by createOnGo< RegularDepositChain > { bot }
    protected val reverseDepositChain by createOnGo< ReverseDepositChain > { bot }
    protected val coneLaunchingChain  by createOnGo< ConeLaunchingChain  > { bot }
    protected val coneUnflipperChain  by createOnGo< ConeUnflipperChain  > { bot }

    final override fun go() {
        PhotonCore.experimental.setMaximumParallelCommands(8)
//        PhotonCore.experimental.setSinglethreadedOptimized(false)
        PhotonCore.enable()

        Imu.init(this)

        describeControls()

        Scheduler.beforeEach {
            powerMulti = 1.0
        }

        waitForStart()

        Scheduler.debug(opmode = this) {
            bot.drivetrain.drive(driver.gamepad, powerMulti)
            bot.updateBaseComponents()
            bot.lift.printLiftTelem()
            mTelemetry.addData("Loop times",  loopTime)
            mTelemetry.addData("# listeners", numHookedListeners)
            mTelemetry.addData("# msg subs",  numUniqueMessageSubs)
            mTelemetry.update()
        }
    }

    abstract fun describeControls()
}
