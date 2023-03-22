package org.firstinspires.ftc.teamcode.pipelines;

import static org.firstinspires.ftc.teamcodekt.components.CameraKt.width;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.ArrayList;

/**
 * Class to apply a canny edge detector in an EasyOpenCV pipeline.
 */

@Config
public class TapeDetector extends OpenCvPipeline {
    public static int cannyThresh1 = 100;
    public static int cannyThresh2 = 190;
    public static int houghThresh = 50;
    public final static double minLineLen = 50;
    public static double maxLineGap = 40;
    public static double minYDist = 200;
    public static double maxXDist = 4;

    public static double adj_a = 0.000699205;
    public static double adj_b = 0.0016927;
    public static double adj_c = 0.55;

    public double tapeAngle = -1;
    public double correction = -1;

    /**
     * Telemetry object to display data to the console
     */
    private final Telemetry telemetry;

    /**
     * Constructor to assign the telemetry object and actually have telemetry work.
     * This works because the OpenCvPipeline object has this built in interface to
     * use telemetry.
     *
     * @param telemetry the input Telemetry type object.
     */
    public TapeDetector(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    /**
     * Process the frame by returning the canny edge of the input image
     *
     * @param img the input image to this pipeline.
     * @return The canny edge version of the input image.
     */
    @Override
    public Mat processFrame(Mat img) {
        return detection(img);
    }

    public Mat detection(Mat src) {
        Mat dst = new Mat(), cdstP = new Mat();

        // Edge detection
        Imgproc.Canny(src, dst, cannyThresh1, cannyThresh2, 3, false);
        Imgproc.cvtColor(dst, cdstP, Imgproc.COLOR_GRAY2BGR);

        // Probabilistic Line Transform
        Mat linesP = new Mat();
        Imgproc.HoughLinesP(dst, linesP, 1, Math.PI / 180, houghThresh, minLineLen, maxLineGap); // runs the actual detection

        ArrayList<double[]> lines = new ArrayList<>();
        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            if (Math.abs(l[3] - l[1]) > maxXDist && Math.abs(l[2] - l[0]) < minYDist) {
                lines.add(l);
            }
        }

        double[] allX = new double[lines.size() * 2];
        int insertIdx = 0;
        for (double[] line : lines) {
            allX[insertIdx] = line[0];
            insertIdx++;
            allX[insertIdx] = line[2];
            insertIdx++;
        }

        double sum = 0;
        for (double x : allX) {
            sum += x;
        }

        double biasedAverage = sum / allX.length;
        ArrayList<Double> left = new ArrayList<>();
        ArrayList<Double> right = new ArrayList<>();
        for (double x : allX) {
            if (x < biasedAverage)
                left.add(x);
            else {
                right.add(x);
            }
        }

        // Categories should now be sorted properly
        double leftAvg = listAverage(left);
        double rightAvg = listAverage(right);

        // Determine true tape center
        double pixels = (leftAvg + rightAvg) / 2;

        // for line visualization
        for (double[] l : lines)
            if ((l[2] + l[0]) / 2 < biasedAverage)
                Imgproc.line(src, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255, 0, 0), 3, Imgproc.LINE_AA, 0);
            else
                Imgproc.line(src, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, 0);

        tapeAngle = 60 * (pixels / width) - 30;
        correction = adjustment(tapeAngle) + 1;
        if (Double.isNaN(correction))
            correction = 0;

//        telemetry.addData("Estimated pixel position", avg);
//        src.release();
        cdstP.release();
        linesP.release();
        dst.release();

        return src;
    }

    public double listAverage(ArrayList<Double> data) {
        double sum = 0;
        for (Double item : data)
            sum += item;
        return sum / data.size();
    }

    public static double adjustment(double angle) {
//        if (Math.abs(angle) > 20) return adj_a * angle * angle * angle + adj_b * angle * angle;
        return adj_c * angle;
    }
}
