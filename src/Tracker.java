public class Track {

    private trackId = trackIdCount  // identification of each track object
    KF = KalmanFilter()  // KF instance to track this object
    prediction = np.asarray(prediction)  // predicted centroids (x,y)
    skipped_frames = 0  // number of frames skipped undetected
    trace = []  // trace path
}
