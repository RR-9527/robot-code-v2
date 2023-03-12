@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.firstinspires.ftc.teamcodekt.components.chains

import ftc.rogue.blacksmith.chains.CancellableChain
import ftc.rogue.blacksmith.listeners.Listener
import ftc.rogue.blacksmith.listeners.after
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents

class FlatConeUnflipperChain(val bot: TeleOpBotComponents) : CancellableChain {
    private var isCancelled = false
    private var intakeTime = false

    override fun invokeOn(button: Listener) {
        button.onRise {
            isCancelled = false

            bot.claw.close()
            bot.lift.goToZero()

            bot.arm.setToBackwardsPos()
            bot.wrist.setToBackwardsPos()
        }

        button.onFall {
            bot.claw.openForIntakeNarrow()
            bot.intake.enable()

            intakeTime = true
        }

        Listener { intakeTime && bot.rcs.getDistance(DistanceUnit.CM) < .8 }.onRise {
            bot.intake.disable()
            intakeTime = false

            after(75).milliseconds {
                bot.claw.close()
            }

            after(250).milliseconds {
                bot.wrist.setToRestingPos()
                bot.arm.goToRest()
            }
        }
    }

    override fun cancelOn(button: Listener) = (button + { !isCancelled })
        .onRise {
            intakeTime = false
            isCancelled = true

            bot.intake.disable()
            bot.claw.close()
            bot.wrist.setToRestingPos()
            bot.arm.goToRest()
        }
        .hook()
}
