import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Track class for every object to be tracked
 * Attributes:
 * None
 */
public class Track {


    private int track_id;  // identification of each track object
    private KalmanFilter KF;  // KF instance to track this object
    private INDArray prediction; // predicted centroids (x,y)
    private int skipped_frames; // number of frames skipped undetected
    private INDArray trace;  // trace path
}
