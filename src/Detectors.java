import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import javax.swing.*;
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
    private Mat outerBox = new Mat();
    private Mat outerBox2 = new Mat();
    private Mat diff_frame = null;
    private Mat tempon_frame = null;

    Size sz = new Size(640, 480);

    private int i = 0;

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

        ArrayList<Point> centers = new ArrayList<>();
        this.frame = frame;

        // Do the processImg()
        Mat imagProcess = processImg();

        // Draw the yellow rectangle
        Imgproc.rectangle(this.frame, rectLimit(VideoController.videoSize)[0], rectLimit(VideoController.videoSize)[1], yellow,4);

        // Calculate contours
        ArrayList<Rect> contours = detection_contours(imagProcess);

        if (contours.size() > 0) {
            for (Rect obj : contours) {
                int width = 12;
                int height = 26;
                if(isEnoughLarge(obj, width,height)) {
                    Point center = rectToCenter(obj);
                    centers.add(center);
                    Imgproc.rectangle(this.frame, new Point(center.x-width/2, center.y-height/2), new Point(center.x+width/2, center.y+height/2), new Scalar(0,255,0));
                    //Imgproc.rectangle(this.frame, obj.br(), obj.tl(), new Scalar(0, 255, 0), 1);
                    //Imgproc.drawMarker(this.frame, rectToCenter(obj), new Scalar(0,255,0), Imgproc.MARKER_CROSS, 20, 2, 1);
                }
            }

        }
        return centers;
    }


    private Mat processImg() {
        outerBox = new Mat(frame.size(), CvType.CV_8UC1);
        outerBox2 = new Mat(frame.size(), CvType.CV_8UC1);

        //Suppression des ombres
        Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2HSV); 	//transformation de l'image en HSV
        for (int k = 0 ; k < sz.width ; k++) { 						//parcourt de tous les pixels
            for (int h = 0 ; h < sz.height ; h++) {
                double b = outerBox.get(h, k)[2]; 					//on prend la valeur VALUE du pixel
                if( b > 100 ) {										//si le pixel est trop clair
                    outerBox2.put(h, k, 255);						//transforme le pixel en blanc => image binaire
                }
            }
        }
        //on applique un flou pour rÈcupÈrer une bonne forme pour les abeilles
        Imgproc.GaussianBlur(outerBox2, outerBox, new Size(3, 3), 0);
        //la vidÈo a traitÈ par la suite sera "outerBox"

        if (i == 0) {
            tempon_frame = new Mat(outerBox.size(), CvType.CV_8UC1);
            diff_frame = outerBox.clone();
        }

        if (i == 1) {
            Core.subtract(outerBox, tempon_frame, diff_frame);
            //int blockSize = 9;
            //int C = 2;

            int blockSize = 21;
            int C = 6;
            Imgproc.adaptiveThreshold(diff_frame, diff_frame, 255,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY_INV, blockSize, C);
        }

        i = 1;
        tempon_frame = outerBox.clone();

        return diff_frame;
    }


    public ArrayList<Rect> detection_contours(Mat outmat) {
        Mat v = new Mat();
        Mat vv = outmat.clone();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(vv, contours, v, Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 150;
        int maxAreaIdx = -1;
        Rect r = null;
        ArrayList<Rect> rect_array = new ArrayList<Rect>();

        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(contour);
            if (contourarea > maxArea) {
                // maxArea = contourarea;
                maxAreaIdx = idx;
                r = Imgproc.boundingRect(contours.get(maxAreaIdx));
                rect_array.add(r);
                //Imgproc.drawContours(this.frame, contours, maxAreaIdx, new Scalar(0,0, 255));
            }

        }

        v.release();

        return rect_array;

    }


    private static Point rectToCenter(Rect obj) {
        return new Point(obj.width/2+obj.x, obj.height/2+obj.y);
    }

    private static boolean isEnoughLarge(Rect obj, int width, int height) {
        if(obj.height >= height && obj.width >= width) {
            return true;
        } else {
            return false;
        }
    }

    private Point[] rectLimit(String videoSize) {
        Point pSupLeft = null;
        Point pInfRight = null;

        switch (videoSize) {
            case "360p" :
                pSupLeft = new Point(70, 10);
                pInfRight = new Point(570, 300);
                break;
            case "540p" :
                pSupLeft = new Point(100, 20);
                pInfRight = new Point(870, 460);
                break;
            case "720p" :
                pSupLeft = new Point(130, 30);
                pInfRight = new Point(1170, 610);
                break;
            case "1080p" :
                pSupLeft = new Point(100, 20);
                pInfRight = new Point(870, 460);
                break;
        }

        assertNotNull(pSupLeft);
        assertNotNull(pInfRight);

        return new Point[]{pSupLeft, pInfRight};
    }
}