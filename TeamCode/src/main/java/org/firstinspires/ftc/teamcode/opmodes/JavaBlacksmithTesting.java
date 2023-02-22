package org.firstinspires.ftc.teamcode.opmodes;

import com.outoftheboxrobotics.photoncore.PhotonCore;

import org.firstinspires.ftc.teamcodekt.components.Imu;
import org.firstinspires.ftc.teamcodekt.components.meta.BotComponentsKt;
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents;

import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.annotations.EvalOnGo;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;

public class JavaBlacksmithTesting extends BlackOp {
    @EvalOnGo(method = "makeReforgedGamepad1")
    private ReforgedGamepad driver;

    @EvalOnGo(method = "makeReforgedGamepad2")
    private ReforgedGamepad codriver;

    @EvalOnGo(method = "createTeleOpBotComponents", clazz = BotComponentsKt.class)
    private TeleOpBotComponents bot;

    @CreateOnGo
    private Hi hi;

    private double powerMulti = 1.0;

    @Override
    public void go() {
        PhotonCore.enable();

        Imu.INSTANCE.init(this);

        Scheduler.beforeEach(() -> powerMulti = 1.0);

        driver.dpad_up.onRise(bot.getLift()::goToHigh);
        driver.dpad_down.onRise(bot.getLift()::goToLow);

        waitForStart();

        Scheduler.debug(this, (info) -> {
            bot.getDrivetrain().drive(driver.getGamepad(), powerMulti);
            bot.updateBaseComponents();

            mTelemetry().addData("Hi isn't null?", hi != null);
            mTelemetry().addData("Loop times",  info.getLoopTime());
            mTelemetry().addData("# listeners", info.getNumHookedListeners());
            mTelemetry().addData("# msg subs",  info.getNumUniqueMessageSubs());
            mTelemetry().update();
        });
    }

    private ReforgedGamepad makeReforgedGamepad1() {
        return new ReforgedGamepad(gamepad1);
    }

    private ReforgedGamepad makeReforgedGamepad2() {
        return new ReforgedGamepad(gamepad2);
    }

    static class Hi {}
}
