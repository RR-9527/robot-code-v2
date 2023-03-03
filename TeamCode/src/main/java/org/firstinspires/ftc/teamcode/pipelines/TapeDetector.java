package org.firstinspires.ftc.teamcode.pipelines;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import ftc.rogue.blacksmith.util.MU;

/**
 * Class to apply a canny edge detector in an EasyOpenCV pipeline.
 */

@Config
public class TapeDetector extends OpenCvPipeline {
    public static int cannyThresh1 =  39;
    public static int cannyThresh2 = 22;
    public static int houghThresh = 5;
    public static int minLineLen = 100;
    public static int maxLineGap = 100;

    /**
     * Telemetry object to display data to the console
     */
    private final Telemetry telemetry;

    /**
     * Angle in radians of the <strong>base</strong> of the pole. Initially set to -1
     * Updates through a biased average of the average of all past values with the current value
     * to converge faster. Also means it will diverge faster if noise is gradually added.
     */
    private double angle;


    // Simple frame size in pixels determined empirically through Mat.rows() and Mat.cols().
    private static int frameWidth = 1280;
    private static int frameHeight = 760;

    /**
     * Meant to be around 6 camera FOVs to the top (estimated from a distance of ~15cm
     * Should experimentally determine based on the most optimal depositing position
     */
    private static final int PIXELS_TO_EXTRAPOLATE_AT = 6 * 720;

    /**
     * Camera offset from the center of the front of the robot
     */
    private static final double CAMERA_OFFSET = 1.2;

    /**
     * Size in centimeters of the robot on a side
     */
    private static final double ROBOT_SIZE = 40;

    private static final double FOV = 30; // Degrees here!
    private static final double FOV_MULT;

    static {
        FOV_MULT = 2 * Math.tan(Math.toRadians(FOV)) / frameWidth;
    }

    /**
     * Constructor to assign the telemetry object and actually have telemetry work.
     * This works because the OpenCvPipeline object has this built in interface to
     * use telemetry.
     *
     * @param telemetry the input Telemetry type object.
     */
    public TapeDetector(Telemetry telemetry) {
        this.telemetry = telemetry;
        angle = -1;
        frameWidth = -1;
        frameHeight = -1;
    }

    /**
     * Apply a canny edge detector to an input matrix.
     *
     * @param src is the source matrix - what is put into the function
     * @return a mat with only the edges of the main bodies given some contrast threshold.
     */
    public Mat analyze(Mat src) {
        // Define the size of the input image
        if (frameWidth == -1 && frameHeight == -1) {
            frameWidth = src.cols();
            frameHeight = src.rows();
        }
        Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(src, src, cannyThresh1, cannyThresh2);

//        Mat lines = new Mat();
//         Imgproc.HoughLines(src, lines, 1, Math.PI/180, houghThresh);
////        Imgproc.HoughLinesP(src, lines, 1, Math.PI / 180, houghThresh, minLineLen, maxLineGap);
//
//
//        telemetry.addData("Lines detected: ", lines.rows());
//        // Draw the lines
//        for (int x = 0; x < lines.rows(); x++) {
//            double[] l = lines.get(x, 0);
//            Point p0 = new Point(l[0], l[1]);
//            Point p1 = new Point(l[2], l[3]);
//
//
////                if (Math.max(p1.y, p0.y) >= src.rows() - 10 && Math.min(p1.y, p0.y) < 20) {
//                    Imgproc.line(src, p0, p1, new Scalar(255, 0, 0), 7, Imgproc.LINE_AA, 0);
////            }
//
//        }
        return src;


//        Mat gray = new Mat();
//        Mat edges = new Mat();
//
////        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
//        // Imgproc.blur(gray, gray, new Size(3, 3));
//        Imgproc.Canny(gray, edges, cannyThresh1, cannyThresh2);
//
//        Mat lines = new Mat();
////         Imgproc.HoughLines(edges, lines, 1, Math.PI/180, houghThresh);
//        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, houghThresh, minLineLen, maxLineGap);
//
//
//        Imgproc.line(src, new Point(100, 0), new Point(100, frameWidth), new Scalar(0, 0, 255),5, Imgproc.LINE_AA, 0);
//
//        // Draw the lines
//        for (int x = 0; x < lines.rows(); x++) {
//            double[] l = lines.get(x, 0);
//            Point p0 = new Point(l[0], l[1]);
//            Point p1 = new Point(l[2], l[3]);
//
////                if (Math.max(p1.y, p0.y) >= src.rows() - 10 && Math.min(p1.y, p0.y) < 20) {
//                    Imgproc.line(src, p0, p1, new Scalar(255, 0, 0), 7, Imgproc.LINE_AA, 0);
////            }
//
//        }
//
//        lines.release();
//        gray.release();
//        edges.release();
//         src.release();

//        return src;
    }

    /**
     * Process the frame by returning the canny edge of the input image
     *
     * @param img the input image to this pipeline.
     * @return The canny edge version of the input image.
     */
    @Override
    public Mat processFrame(Mat img) {
        double[] polePosAbsolute = getRepositionCoord(0, 0, Math.PI / 2, 0.3);
//        telemetry.addData("Finalized x pos", polePosAbsolute[0]);
//        telemetry.addData("Finalized y pos", polePosAbsolute[1]);

//        telemetry.update();

        return analyze(img);
    }

    private double newAngleMeasurement(Point p0, Point p1) {
        return (calculateTopXPixels(p0, p1) - 640) * 0.0469;
    }


    /**
     * Determine the x position in pixels of the TOP of the pole from the angle at the bottom of the pole.
     * Uses the inverse of the line found between two points to plug in a Y-value and get the x position.
     * PIXELS_TO_EXTRAPOLATE_AT is that y-value.
     *
     * @param p0 the first point of the line from probabalistic hough lines transform
     * @param p1 the second point of the line from probabalistic hough lines transform
     * @return the x value of the extrapolated line at height y=PIXELS_TO_EXTRAPOLATE_AT.
     */
    private double calculateTopXPixels(Point p0, Point p1) {
        double slopeInv = 1 / ((-p1.y + p0.y) / (p1.x - p0.x));
        double yInt = p1.y - slopeInv * p1.x;
        // Line is slope*x + (p1.y-slope^-1*x)
        return slopeInv * (PIXELS_TO_EXTRAPOLATE_AT - yInt) + MU.avg(p0.x, p1.x);
    }

    /**
     * Get the angle in <strong>RADIANS</strong> of an x value in pixels based on frameWidth
     * Used to
     *
     * @param xPos the x position in pixels
     * @return the angle in radians (+ is right of middle, - is left of middle)
     */
    private double pixelXAngle(double xPos) {
        return Math.atan(FOV_MULT * (xPos - (frameWidth / 2.0)));
    }


    /**
     * Determine the x/y position of the pole based on the current characteristics of the robot and
     * the passed distance from the distance sensor
     *
     * @param xOffset          the current x position of the robot
     * @param yOffset          the current y position of th robot
     * @param robotOffsetAngle the angle the robot is currently at IN RADIANS
     * @param detectedDistance the sensor measurement from the distance sensor
     * @return an array with the x and y position to move to
     */
    public double[] getRepositionCoord(double xOffset, double yOffset, double robotOffsetAngle, double detectedDistance) {
        double h = calculateH(CAMERA_OFFSET, detectedDistance, angle);
        double psi = calculatePsi(detectedDistance, h, angle);
        return new double[]{
            h * Math.cos(robotOffsetAngle + psi) + xOffset, // x - keep units consistent across everything!
            h * Math.sin(robotOffsetAngle + psi) + yOffset - ROBOT_SIZE / 2,  // y - keep units consistent across everything
        };
    }

    /**
     * Calculate the actual distance to the pole based on the measurement given
     *
     * @param x     the camera offset
     * @param r     the detected distance from the ultrasonic distance sensor
     * @param theta the angle detected from the camera
     * @return the true distance to the pole
     */
    private double calculateH(double x, double r, double theta) {
        return Math.sqrt(x * x + r * r + 2 * x * r * Math.cos(theta));
    }

    /**
     * Determine the true angle to the pole
     *
     * @param r     the detected distance from the ultrasonic distance sensor
     * @param h     the true distance from the ultrasonic distance sensor
     * @param theta the angle detected from the camera (false angle)
     * @return the true angle from the robot to the camera
     */
    private double calculatePsi(double r, double h, double theta) {
        return Math.asin(r / h * Math.sin(theta));
    }

    public double getPoleAngle() {
        return angle;
    }
}
