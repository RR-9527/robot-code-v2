package org.firstinspires.ftc.teamcode.opmodes;

import com.noahbres.meepmeep.core.colorscheme.ColorManager;
import com.outoftheboxrobotics.photoncore.PhotonCore;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.roadrunner.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.roadrunner.trajectorysequence.TrajectorySequenceBuilder;
import org.firstinspires.ftc.teamcodekt.components.Imu;
import org.firstinspires.ftc.teamcodekt.components.meta.BotComponentsKt;
import org.firstinspires.ftc.teamcodekt.components.meta.TeleOpBotComponents;

import ftc.rogue.blacksmith.Anvil;
import ftc.rogue.blacksmith.BlackOp;
import ftc.rogue.blacksmith.Scheduler;
import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.annotations.EvalOnGo;
import ftc.rogue.blacksmith.listeners.ReforgedGamepad;
import kotlin.jvm.functions.Function0;

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

        Scheduler.beforeEach(() -> powerMulti = 1.0);

        driver.dpad_up.onRise(bot.getLift()::goToHigh);
        driver.dpad_down.onRise(bot.getLift()::goToLow);

        Scheduler.launchOnStart(this, () -> {
            mTelemetry().addData("Hi isn't null?", hi != null);
            mTelemetry().update();
        });
    }
}
