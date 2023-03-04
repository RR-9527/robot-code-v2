@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.firstinspires.ftc.teamcodekt.components.chains

import ftc.rogue.blacksmith.chains.CancellableChain
import ftc.rogue.blacksmith.listeners.Listener
import ftc.rogue.blacksmith.listeners.after
import org.firstinspires.ftc.teamcodekt.components.LIFT_LOW
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents

class RegularDepositChain(val bot: TeleOpBotComponents) : CancellableChain {
    private var isCancelled = false

    override fun invokeOn(button: Listener) = (button + { bot.lift.clippedHeight > 90 })
        .onRise {
            isCancelled = false
            bot.arm.setToForwardsPos()
            bot.wrist.setToForwardsPos()
        }
        .onFall {
            if (isCancelled) {
                return@onFall
            }

            bot.lift.targetHeight = (bot.lift.targetHeight - 100).coerceAtLeast(LIFT_LOW)

            bot.claw.openForDeposit()
            bot.arm.targetAngle = 165.0

            after(400).milliseconds {
                finish()
            }

            after(400).milliseconds {
                bot.lift.goToZero()
                isCancelled = true
            }
        }
        .hook()

    override fun cancelOn(button: Listener) = (button + { !isCancelled })
        .onRise(::finish)
        .hook()

    private fun finish() {
        bot.claw.close()
        bot.arm.goToRest()
        bot.wrist.setToRestingPos()
        isCancelled = true
    }
}
