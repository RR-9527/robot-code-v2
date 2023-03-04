package org.firstinspires.ftc.teamcode.opmodes;

import com.outoftheboxrobotics.photoncore.PhotonCore;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcodekt.components.Imu;
import org.firstinspires.ftc.teamcodekt.components.meta.BotComponentsKt;
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents;

import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.annotations.EvalOnGo;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;

@TeleOp
public class JavaBlacksmithTesting extends BlackOp {
    @EvalOnGo(method = "getReforgedGamepad1")
    private ReforgedGamepad driver;

    @EvalOnGo(method = "getReforgedGamepad2")
    private ReforgedGamepad codriver;

    @EvalOnGo(method = "createTeleOpBotComponents", clazz = BotComponentsKt.class)
    private TeleOpBotComponents bot;

    @CreateOnGo(passHwMap = true)
    private SampleMecanumDrive hi;

    private double powerMulti = 1.0;

    @Override
    public void go() {
        PhotonCore.enable();

        Imu.INSTANCE.init(this);

        waitForStart();

        Scheduler.beforeEach(() -> powerMulti = 1.0);

        driver.dpad_up.onRise(bot.getLift()::goToHigh);
        driver.dpad_down.onRise(bot.getLift()::goToLow);

        Scheduler.debug(() -> opModeIsActive() && !isStopRequested(), (info) -> {
            bot.getDrivetrain().drive(driver.getGamepad(), powerMulti);
            bot.updateComponents(true);

            mTelemetry().addData("Hi isn't null?", hi != null);
            mTelemetry().addData("Loop times",  info.getLoopTime());
            mTelemetry().addData("# listeners", info.getNumHookedListeners());
            mTelemetry().addData("# msg subs",  info.getNumUniqueMessageSubs());
            mTelemetry().update();
        });
    }
}
