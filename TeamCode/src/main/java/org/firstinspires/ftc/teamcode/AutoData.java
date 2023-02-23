package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

import ftc.rogue.blacksmith.annotations.CreateOnGo;
import ftc.rogue.blacksmith.annotations.EvalOnGo;
import ftc.rogue.blacksmith.internal.blackop.CreateOnGoJavaKt;

// TODO: Find better file location for this

@Config
public class AutoData {
    public static int DEPOSIT_DROP_AMOUNT = 500;

    public static int AUTO_INTAKE_LIFT_HEIGHT_1 = 115;
    public static int AUTO_INTAKE_LIFT_HEIGHT_2 = 90;
    public static int AUTO_INTAKE_LIFT_HEIGHT_3 = 83;
    public static int AUTO_INTAKE_LIFT_HEIGHT_4 = 40;
    public static int AUTO_INTAKE_LIFT_HEIGHT_5 = 0;

    public static double LOW_DEPOSIT_1 = 20;
    public static double LOW_DEPOSIT_2 = 20;
    public static double LOW_DEPOSIT_3 = 20;
    public static double LOW_DEPOSIT_4 = 20;
    public static double LOW_DEPOSIT_5 = 20;

    public static double LOW_INTAKE_1 = 21.35;
    public static double LOW_INTAKE_2 = 20;
    public static double LOW_INTAKE_3 = 20;
    public static double LOW_INTAKE_4 = 20;
    public static double LOW_INTAKE_5 = 20;

    @EvalOnGo(method = "getInstance", clazz = Barm.class)
    private Barm barm;

    public void main2() {
        System.out.println(barm);
    }
}

class Barm {
    public static Barm getInstance() {
        return new Barm();
    }
}
