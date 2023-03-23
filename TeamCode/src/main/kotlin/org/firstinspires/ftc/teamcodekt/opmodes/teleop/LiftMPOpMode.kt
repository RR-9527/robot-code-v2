package org.firstinspires.ftc.teamcodekt.opmodes.teleop

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import ftc.rogue.blacksmith.listeners.Pulsar

@TeleOp
class LiftMPOpMode : RogueBaseTele() {
    override fun describeControls(): Unit = with(bot) {
        driver.dpad_up.onRise(lift::incHeight)
        driver.dpad_down.onRise(lift::decHeight)

        Pulsar(22).onPulse {
            lift.updateMotionProfile(true)
        }
    }
}
