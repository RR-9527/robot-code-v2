package org.firstinspires.ftc.teamcodekt.opmodes.teleop

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlin.math.sign

@TeleOp
class RogueCompOp : RogueBaseTele() {
    private fun describeDriverControls() = with(bot) {
        driver.right_trigger(deadzone = .2).whileHigh {
            powerMulti = 0.5925
        }

        driver.left_trigger.whileHigh {
            powerMulti = -0.1 // Lets robot stop on a dime
        }

        angledConeUnflipperChain.invokeOn(driver.left_bumper)

        flatConeUnflipperChain.invokeOn(driver.right_bumper)
        flatConeUnflipperChain.cancelOn(driver.x)
    }

    private fun describeCodriverControls() = with(bot) {
        codriver.dpad_up   .onRise(lift::goToHigh)
        codriver.dpad_down .onRise(lift::goToZero)
        codriver.dpad_right.onRise(lift::goToMid)
        codriver.dpad_left .onRise(lift::goToLow)

        // -- TASK CHAINS --

        intakeChain.invokeOn(codriver.left_trigger)

        regularDepositChain.invokeOn(codriver.right_trigger)
        regularDepositChain.cancelOn(codriver.x)

        reverseDepositChain.invokeOn(codriver.y)
        reverseDepositChain.cancelOn(codriver.x)

        // -- MANUAL WRIST CONTROLS --

        codriver.left_stick_x.whileHigh {
            if (codriver.left_stick_x().sign < 0) {
                bot.wrist.setToForwardsPos()
            }

            if (codriver.left_stick_x().sign > 0) {
                bot.wrist.setToBackwardsPos()
            }
        }

        // -- MANUAL LIFT CONTROLS --

        val bumpersPressed = codriver.left_bumper + codriver.right_bumper

        (codriver.right_stick_y(deadzone = .2) + !bumpersPressed).whileHigh {
            bot.lift.clippedHeight += (-codriver.right_stick_y() * 10).toInt()
        }

        // -- MANUAL LIFT RESET --

        (bumpersPressed + codriver.right_stick_y(deadzone = .2)).whileHigh {
            bot.lift.targetHeight += (-codriver.right_stick_y() * 2.5).toInt()
        }

        bumpersPressed.onFall {
            bot.lift.resetEncoder()
        }
    }

    override fun describeControls() {
        describeCodriverControls()
        describeDriverControls()
    }
}
