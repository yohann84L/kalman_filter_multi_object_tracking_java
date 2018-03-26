import org.opencv.core.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Track class for every object to be tracked
 * Attributes:
 * None
 */
public class Track {

    private int track_id;  // identification of each track object
    private KalmanF KF;  // KF instance to track this object
    private Point prediction; // predicted centroids (x,y)
    private int skippedFrames; // number of frames skipped undetected
    private ArrayList<Point> trace;  // trace path

    public Track(Point prediction, int track_id) {
        this.track_id = track_id;
        this.prediction = prediction;
        KF = new KalmanF();
        this.skippedFrames = 0;
        this.trace = new ArrayList<>();
    }

    public Point getPrediction() {
        return prediction;
    }

    public void setPrediction(Point prediction) {
        this.prediction = prediction;
    }

    public void addSkippedFrames() {
        skippedFrames += 1;
    }

    public int getSkippedFrames() {
        return skippedFrames;
    }

    public void setSkippedFrames(int skippedFrames) {
        this.skippedFrames = skippedFrames;
    }

    public KalmanF getKF() {
        return KF;
    }

    public void addPoint(Point p) {
        trace.add(p);
    }

    public void delPoint(int index) {
        trace.remove(index);
    }

    public ArrayList<Point> getTrace() {
        return trace;
    }

    public int getTrack_id() {
        return track_id;
    }
}