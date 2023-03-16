package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;

// TODO: Find better file location for this

@Config
public class AutoData {
    public static int DEPOSIT_DROP_AMOUNT = 150;

    public static int AUTO_INTAKE_LIFT_HEIGHT_1 = 125;
    public static int AUTO_INTAKE_LIFT_HEIGHT_2 = 109;
    public static int AUTO_INTAKE_LIFT_HEIGHT_3 = 75;
    public static int AUTO_INTAKE_LIFT_HEIGHT_4 = 45;
    public static int AUTO_INTAKE_LIFT_HEIGHT_5 = 0;

    public static final int[] liftOffsets = {
        AUTO_INTAKE_LIFT_HEIGHT_1,
        AUTO_INTAKE_LIFT_HEIGHT_2,
        AUTO_INTAKE_LIFT_HEIGHT_3,
        AUTO_INTAKE_LIFT_HEIGHT_4,
        AUTO_INTAKE_LIFT_HEIGHT_5
    };

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
}
