@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.firstinspires.ftc.teamcodekt.components.chains

import com.acmerobotics.dashboard.config.Config
import ftc.rogue.blacksmith.chains.Chain
import ftc.rogue.blacksmith.listeners.Listener
import ftc.rogue.blacksmith.listeners.after
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents

@Config
class ConeUnflipperChain(val bot: TeleOpBotComponents) : Chain {
    companion object {
        @JvmField
        var flipperTargetHeight = 113

        @JvmField
        var flipperTargetAngle = 27.5
    }

    private var isRunning = false

    override fun invokeOn(button: Listener) {
        button.onRise {
            bot.claw.openForIntakeNarrow()
            bot.intake.enable()

            bot.lift.targetHeight = flipperTargetHeight

            bot.arm.targetAngle = flipperTargetAngle
            bot.wrist.setToRestingPos()

            isRunning = true
        }

        button.onFall {
            if (!isRunning) {
                return@onFall
            }

            bot.intake.disable()
            bot.claw.close()

            isRunning = false

            bot.wrist.setToRestingPos()
            bot.arm.setToRestingPos()
            bot.lift.goToZero()
        }

        Listener { isRunning && bot.rcs.getDistance(DistanceUnit.CM) < .8 }.onRise {
            bot.intake.disable()
            isRunning = false

            after(30).milliseconds {
                bot.claw.close()
            }

            after(35).milliseconds {
                bot.claw.close()
            }

            after(250).milliseconds {
                bot.wrist.setToRestingPos()
                bot.arm.setToRestingPos()
                bot.lift.goToZero()
            }
        }
    }
}
