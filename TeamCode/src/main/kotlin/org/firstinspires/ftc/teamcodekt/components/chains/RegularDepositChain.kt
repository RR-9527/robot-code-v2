@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.firstinspires.ftc.teamcodekt.components.chains

import ftc.rogue.blacksmith.chains.CancellableChain
import ftc.rogue.blacksmith.listeners.Listener
import ftc.rogue.blacksmith.listeners.after
import org.firstinspires.ftc.teamcodekt.components.LIFT_LOW
import org.firstinspires.ftc.teamcodekt.components.LIFT_MID
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents

class RegularDepositChain(val bot: TeleOpBotComponents) : CancellableChain {
    private var isCancelled = false

    override fun invokeOn(button: Listener) = (button + { bot.lift.clippedHeight > 50 })
        .onRise {
            isCancelled = false
            bot.arm.setToDepositMode()
            bot.wrist.setToForwardsPos()
        }
        .onFall {
            if (isCancelled) {
                return@onFall
            }

            if(bot.lift.targetHeight > LIFT_LOW)
                bot.lift.targetHeight = (bot.lift.targetHeight - 100)

            bot.arm.setToForwardsPos()


            after(210).milliseconds {
                bot.claw.openForDeposit()
            }

            after(395).milliseconds {
                finish()
            }

            after(425).milliseconds {
                bot.lift.goToZero()
            }
        }
        .hook()

    override fun cancelOn(button: Listener) = (button + { !isCancelled })
        .onRise(::finish)
        .hook()

    private fun finish() {
        bot.claw.close()
        bot.arm.setToRestingPos()
        bot.wrist.setToRestingPos()
        isCancelled = true
    }
}
