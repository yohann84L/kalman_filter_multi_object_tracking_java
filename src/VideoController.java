import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.Slider;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the histogram creation.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 2.0 (2017-03-10)
 * @since 1.0 (2013-11-20)
 */
public class VideoController {
    // the FXML button
    @FXML
    private Button button;
    // the FXML grayscale checkbox
    @FXML
    private ImageView currentFrame;

    @FXML
    private Slider threshold;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;
    // the logo to be loaded
    private Mat logo;

    public static final String videoSize = "540p";
    private String framerateVideo = "60fps";
    private String shadow = "ombre";

    private String videoUrl = "/Users/yohannmbp/Desktop/video_test/v0/"+framerateVideo+"/"+videoSize+"_"+shadow+".mov";

    private Tracker tracker = new Tracker(200, 30, 300, 100);

    // Color
    private final Scalar blue = new Scalar(255, 0, 0);
    private final Scalar green = new Scalar(0, 255, 0);
    private final Scalar red = new Scalar(0, 0, 255);
    private final Scalar cyan = new Scalar (255, 255, 0);
    private final Scalar yellow = new Scalar(0, 255, 255);
    private final Scalar magenta = new Scalar(255, 0, 255);
    private final Scalar pink = new Scalar(255, 127, 255);
    private final Scalar purple = new Scalar(127, 0, 255);
    private final Scalar purpleblack = new Scalar(127, 0, 127);

    private final ArrayList<Scalar> trackColors = new ArrayList<>();


    /**
     * Initialize method, automatically called by @{link FXMLLoader}
     */
    public void initialize() {
        this.capture = new VideoCapture();
        this.threshold = new Slider();
        this.cameraActive = false;

        trackColors.add(blue);
        trackColors.add(green);
        trackColors.add(red);
        trackColors.add(cyan);
        trackColors.add(yellow);
        trackColors.add(magenta);
        trackColors.add(pink);
        trackColors.add(purple);
        trackColors.add(purpleblack);
    }

    /**
     * The action triggered by pushing the button on the GUI
     */
    @FXML
    protected void startCamera() {
        // set a fixed width for the frame
        this.currentFrame.setFitWidth(600);
        // preserve image ratio
        this.currentFrame.setPreserveRatio(true);

        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(videoUrl);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                      // effectively grab and process a single frame
                        Mat frame = grabFrame(threshold);
                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Image} to show
     * @param threshValue
     */
    private Mat grabFrame(Slider threshValue) {
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {

            this.capture.read(frame);

            // Detect and return centeroids of the objects in the frame
            ArrayList<Point> centers = Detectors.getInstance().detect(frame, threshValue.getValue());

            // If centroids are detected then track them
            if(centers.size() > 0) {

                // Track object using Kalman Filter
                tracker.Update(centers);

                for(int i = 0; i < tracker.getTracks().size(); i++) {
                    if(tracker.getTracks().get(i).getTrace().size() > 1) {
                        for(int j = 0; j < tracker.getTracks().get(i).getTrace().size() - 1; j++) {
                            // Draw trace line
                            Point pt1 = tracker.getTracks().get(i).getTrace().get(j);
                            Point pt2 = tracker.getTracks().get(i).getTrace().get(j+1);

                            int clr = tracker.getTracks().get(i).getTrack_id() % 9;
                            Imgproc.line(frame, pt1, pt2, trackColors.get(clr), 2);
                        }
                    }
                }

            }

            frame = Detectors.getInstance().getFrame();
        }

        //return Detectors.edges;
        return frame;
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed() {
        this.stopAcquisition();
    }

}
