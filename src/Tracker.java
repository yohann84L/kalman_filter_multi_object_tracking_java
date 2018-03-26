import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 Tracker class that updates track vectors of object tracked
 Attributes:
 None
 */
public class Tracker {

    private int distThresh;
    private int maxFramesToSkip;
    private int maxTraceLength;
    private ArrayList<Track> tracks = new ArrayList<>();
    private int trackIdCount;

    public Tracker(int distThresh, int maxFramesToSkip, int maxTraceLength, int trackIdCount) {
        this.distThresh = distThresh;
        this.maxFramesToSkip = maxFramesToSkip;
        this.maxTraceLength = maxTraceLength;
        this.trackIdCount = trackIdCount;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
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
        if(tracks.size() == 0) {
            for (Point detection : detections) {
                Track track = new Track(detection, trackIdCount);
                trackIdCount++;
                tracks.add(track);
            }
        }

        // Calculate cost using sum of square distance between
        // predicted vs detected centroids
        int N = tracks.size();
        int M = detections.size();
        double[][] cost = new double[N][M]; // Cost matrix
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < M; j++) {
                Point diff = diffPoint(tracks.get(i).getPrediction(),detections.get(j));
                double distance = Math.sqrt(diff.x*diff.x + diff.y*diff.y);
                cost[i][j] = distance*0.5;
            }
        }

        // Let's average the squared ERROR
        //cost = cost.mul(0.5);
        // Using Hungarian Algorithm assign the correct detected measurements
        // to predicted tracks
        //INDArray assigment = Nd4j.zeros(N);
        //assigment.add(-1);

        int[] assigmentL = new HungarianAlgorithm(cost).execute();
        List<Integer> assigment = Lists.newArrayList(Ints.asList(assigmentL));

        //  Identify tracks with no assignment, if any
        ArrayList<Integer> unAssignedTracks = new ArrayList<>();
        for(int i = 0; i < N; i++) {
            if(assigment.get(i) != -1) {
                // check for cost distance threshold.
                // If cost is very high then un_assign (delete) the track
                if(cost[i][assigment.get(i)] > distThresh) {
                    assigment.set(i, -1);
                    unAssignedTracks.add(i);
                } else {
                    tracks.get(i).addSkippedFrames();
                }
            }
        }

        // If tracks are not detected for long time, remove them
        ArrayList<Integer> delTracks = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++) {
            if(tracks.get(i).getSkippedFrames() > maxFramesToSkip) {
                delTracks.add(i);
            }
        }
        if(delTracks.size() > 0) { // only when skipped frame exceeds max
            for(Integer id : delTracks) {
                if(id < tracks.size()) {
                    tracks.remove(id);
                    assigment.remove(id);
                } else {
                    System.out.println("ERROR: id is greater than length of tracks");
                }
            }
        }

        // Now look for un_assigned detects
        ArrayList<Integer> unAssignedDetects = new ArrayList<>();
        for(int i = 0; i < detections.size(); i++) {
            if(!assigment.contains(i)) {
                unAssignedDetects.add(i);
            }
        }

        // Start new tracks
        if(unAssignedDetects.size() != 0) {
            for (Integer unAssignedDetect : unAssignedDetects) {
                Track track = new Track(detections.get(unAssignedDetect), trackIdCount);
                trackIdCount++;
                tracks.add(track);
            }
        }

        // Update KalmanFilter state, lastResults and tracks trace
        for(int i = 0; i < assigment.size(); i++) {
            tracks.get(i).getKF().predict();

            if(assigment.get(i) != -1) {
                tracks.get(i).setSkippedFrames(0);
                tracks.get(i).setPrediction(stateVectorToPoint(tracks.get(i).getKF().correct(pointToStateVector(detections.get(assigment.get(i))), true)));
            } else {
                tracks.get(i).setPrediction(stateVectorToPoint(tracks.get(i).getKF().correct(new SimpleMatrix(new double[][]{{0},{0}}), false)));
            }

            if(tracks.get(i).getTrace().size() > maxTraceLength) {
                for(int j = 0; j < tracks.get(i).getTrace().size() - maxTraceLength; j++) {
                    tracks.get(i).delPoint(j);
                }
            }

            tracks.get(i).addPoint(tracks.get(i).getPrediction());
            tracks.get(i).getKF().setLastResult(pointToStateVector(tracks.get(i).getPrediction()));
        }
    }


    private Point diffPoint(Point A, Point B) {
        return new Point(A.x-B.x, A.y- B.y);
    }

    private Point stateVectorToPoint(SimpleMatrix u) {
        return new Point(u.get(0,0), u.get(1,0));
    }

    private SimpleMatrix pointToStateVector(Point p) {
        Double[] d = new Double[]{p.x, p.y};
        return new SimpleMatrix(new double[][]{{d[0].intValue()}, {d[1].intValue()}});
    }
}
