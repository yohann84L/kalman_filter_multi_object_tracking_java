import org.opencv.core.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Tracker {

    private int distThresh;
    private int maxFramesToSkip;
    private int maxTraceLength;
    private int[] tracks = new int[0];
    private int trackIdCount;

    public Tracker(int distThresh, int maxFramesToSkip, int maxTraceLength, int[] tracks, int trackIdCount) {
        this.distThresh = distThresh;
        this.maxFramesToSkip = maxFramesToSkip;
        this.maxTraceLength = maxTraceLength;
        this.tracks = tracks;
        this.trackIdCount = trackIdCount;
    }

    public void Update(ArrayList<Point> detections) {
        /*Update tracks vector using following steps:
            - Create tracks if no tracks vector found
            - Calculate cost using sum of square distance
              between predicted vs detected centroids
            - Using Hungarian Algorithm assign the correct
              detected measurements to predicted tracks
              https://en.wikipedia.org/wiki/Hungarian_algorithm
            - Identify tracks with no assignment, if any
            - If tracks are not detected for long time, remove them
            - Now look for un_assigned detects
            - Start new tracks
            - Update KalmanFilter state, lastResults and tracks trace
        Args:
            detections: detected centroids of object to be tracked
        Return:
            None
        */

        // Create tracks if no tracks vector found
    }
}
