import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Detectors {

    // Rectangle Limit
    private final Point pSupLeft = new Point(100, 20);
    private final Point pInfRight = new Point(870, 460);

    // Color
    private final Scalar yellow = new Scalar(0, 255, 255);
    private final Scalar green = new Scalar(0, 255, 0);

    // List of center

    private Mat frame;

    private static Detectors instance = null;

    private BackgroundSubtractorMOG2 fgbg = Video.createBackgroundSubtractorMOG2();

    private Detectors() {
    }

    public static Detectors getInstance() {
        if (instance == null) {
            instance = new Detectors();
        }
        return instance;
    }

    public Mat getFrame() {
        return frame;
    }

    public ArrayList<Point> detect(Mat frame, double thresholdValue) {

        thresholdValue = 50;

        this.frame = frame;

        // List of the centers
        ArrayList<Point> centers = new ArrayList<>();

        // Convert BGR to GRAY
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(this.frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Perform background substraction
        Mat fgmask = new Mat();
        fgbg.apply(grayFrame, fgmask);

        // Detect edges
        Mat edges = new Mat();
        Imgproc.Canny(fgmask, edges, 50.0, 190.0);

        // Retain only edges within the threshold
        Mat tresh = new Mat();
        Imgproc.threshold(edges, tresh, 127.0, 255.0, 3);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierachy = new Mat();
        Imgproc.findContours(tresh, contours, hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int blobRadiusThresh = 10;

        Imgproc.rectangle(this.frame, pSupLeft, pInfRight, yellow,4);


        for (MatOfPoint cnt : contours) {
            MatOfPoint2f cnt2f = new MatOfPoint2f();
            cnt.convertTo(cnt2f, CvType.CV_32F);
            Point center = new Point();
            float[] radius = new float[contours.size()];
            Imgproc.minEnclosingCircle(cnt2f, center, radius);

            for(float r : radius) {
                if ((int)r > blobRadiusThresh) {
                    Imgproc.circle(this.frame, center, (int)r, green, 2);
                }
            }
            centers.add(center);
        }

        return centers;
    }
}
