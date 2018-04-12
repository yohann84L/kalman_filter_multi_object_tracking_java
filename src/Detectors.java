import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Detectors {

    // Color
    private final Scalar yellow = new Scalar(0, 255, 255);
    private final Scalar green = new Scalar(0, 255, 0);

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

        // Blur the image to delete the noise
        Mat fgmaskBlur = new Mat();
        Imgproc.GaussianBlur(fgmask, fgmaskBlur, new Size(7,7),0);

        // Retain only edges within the threshold
        Mat thresh = new Mat();
        Imgproc.threshold(fgmaskBlur, thresh, 210, 255, Imgproc.THRESH_BINARY);

        // Detect edges
        Mat edges = new Mat();
        Imgproc.Canny(thresh, edges, 220, 255);

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierachy = new Mat();
        Imgproc.findContours(edges, contours, hierachy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        int blobRadiusThresh = 8;

        Imgproc.rectangle(this.frame, rectLimit(VideoController.videoSize)[0], rectLimit(VideoController.videoSize)[1], yellow,4);

        for (MatOfPoint cnt : contours) {
            MatOfPoint2f cnt2f = new MatOfPoint2f();
            cnt.convertTo(cnt2f, CvType.CV_32F);
            Point center = new Point();
            float[] radius = new float[contours.size()];
            Imgproc.minEnclosingCircle(cnt2f, center, radius);

            for(float r : radius) {
                if ((int)r > blobRadiusThresh && (int)r < 30) {
                    Imgproc.circle(this.frame, center, (int)r, green, 2);
                    centers.add(center);
                }
            }
        }

        return centers;
    }


    public Point[] rectLimit(String videoSize) {
        Point tl = null;
        Point br = null;

        switch (videoSize) {
            case "360p" :
                tl = new Point(70, 10);
                br = new Point(570, 300);
                break;
            case "540p" :
                tl = new Point(100, 20);
                br = new Point(870, 460);
                break;
            case "720p" :
                tl = new Point(130, 30);
                br = new Point(1170, 610);
                break;
            case "1080p" :
                tl = new Point(100, 20);
                br = new Point(870, 460);
                break;
        }

        assertNotNull(tl);
        assertNotNull(br);

        return new Point[]{tl, br};
    }
}