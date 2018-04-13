import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import static org.junit.Assert.assertNotNull;

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

    @FXML
    private Label entrancesText;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;
    // the logo to be loaded
    private Mat logo;

    public static final String videoSize = "1080p";
    private int framerate = 60;
    private String framerateVideo = framerate+"fps";
    private String shadow = "ombre";

    private int slowFactor = 1;

    private String videoUrl = "/Users/yohannmbp/Desktop/video_test/v0/"+framerateVideo+"/"+videoSize+"_"+shadow+".mov";

    private Tracker tracker = new Tracker(250, 30, 1, 100);

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

    private final Rect rect = new Rect(Detectors.getInstance().rectLimit(VideoController.videoSize)[0],
            Detectors.getInstance().rectLimit(VideoController.videoSize)[1]);

    private ArrayList<Integer> entrancesId = new ArrayList<>();
    private ArrayList<Integer> departuresId = new ArrayList<>();
    private ArrayList<Integer> flybyId = new ArrayList<>();
    private ArrayList<Integer> idInArea = new ArrayList<>();

    private ArrayList<Integer> entrancesIdDrawn = new ArrayList<>();
    private ArrayList<Integer> departuresIdDrawn = new ArrayList<>();
    private ArrayList<Integer> flybyIdDrawn = new ArrayList<>();

    private ArrayList<Integer> crossBottomId = new ArrayList<>();
    private ArrayList<Integer> crossTopId = new ArrayList<>();
    private ArrayList<Integer> crossLeftId = new ArrayList<>();
    private ArrayList<Integer> crossRightId = new ArrayList<>();


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
        this.currentFrame.setFitWidth(900);
        // preserve image ratio
        this.currentFrame.setPreserveRatio(true);

        this.entrancesText = new Label("Entrances : " + entrancesId.size());

        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(videoUrl);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 16 ms (60 frames/sec)
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
                this.timer.scheduleAtFixedRate(frameGrabber, 0, slowFactor*1000/framerate, TimeUnit.MILLISECONDS);

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

                            // Count
                            Integer trackId = tracker.getTracks().get(i).getTrack_id();
                            count(trackId, pt1, pt2);

                            int clr = tracker.getTracks().get(i).getTrack_id() % 9;
                            Imgproc.line(frame, pt1, pt2, trackColors.get(clr), 2);

                            //frame = drawTrace(entrancesIdDrawn, green, frame);
                            //frame = drawTrace(departuresIdDrawn,red, frame);
                            //frame = drawTrace(flybyIdDrawn, blue, frame);
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
                System.out.println("Entrances : " + entrancesId.size()+"   ||   "+"Departures : "+departuresId.size()+"   ||   "+"Flyby : "+flybyId.size());

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

    private Mat drawTrace(ArrayList<Integer> listId, Scalar color, Mat fr) {
        if(listId.size() > 0) {
            for(int id : listId) {
                Track selectedTrack = tracker.getTracks().stream().filter(track -> track.getTrack_id() == id).findFirst().get();
                System.out.println(selectedTrack);
                for(int k = 0; k < selectedTrack.getTrace().size() -1 ; k++) {
                    Imgproc.line(fr, selectedTrack.getTrace().get(k), selectedTrack.getTrace().get(k+1), color, 2);
                }
            }
            listId.clear();
        }

        return fr;
    }

    private void count(int trackId, Point pt1, Point pt2) {
        /////////// Entrances in area ///////////
        if(Counter.getInstance(rect).crossBottomIn(pt1, pt2, rect) && !crossBottomId.contains(trackId)) {
            crossBottomId.add(trackId);
        } else if(Counter.getInstance(rect).crossTopIn(pt1, pt2, rect) && !crossTopId.contains(trackId)) {
            crossTopId.add(trackId);
        } else if(Counter.getInstance(rect).crossLeftIn(pt1, pt2, rect) && !crossLeftId.contains(trackId)) {
            crossLeftId.add(trackId);
        } else if(Counter.getInstance(rect).crossRightIn(pt1, pt2, rect) && !crossRightId.contains(trackId)) {
            crossRightId.add(trackId);
        }

        /////////// Entrances ///////////
        if(Counter.getInstance(rect).crossBottomOut(pt1, pt2, rect)) {
            if(crossLeftId.contains(trackId) || crossRightId.contains(trackId) || crossTopId.contains(trackId)) {
                entrancesId.add(trackId);
                entrancesIdDrawn.add(trackId);
            }
        }

        /////////// Departures ///////////
        if((Counter.getInstance(rect).crossLeftOut(pt1, pt2, rect)
                || Counter.getInstance(rect).crossRightOut(pt1, pt2, rect)
                || Counter.getInstance(rect).crossTopOut(pt1, pt2, rect)) && crossBottomId.contains(trackId)) {
            departuresId.add(trackId);
            departuresIdDrawn.add(trackId);
        }

        /////////// Flyby ///////////
        // Left TO (Top or Right)
        if(crossLeftId.contains(trackId)
                && (Counter.getInstance(rect).crossTopOut(pt1, pt2, rect)
                || Counter.getInstance(rect).crossRightOut(pt1, pt2, rect))) {
            flybyId.add(trackId);
            flybyIdDrawn.add(trackId);
        }
        // Right TO (Top or Left)
        if(crossRightId.contains(trackId)
                && (Counter.getInstance(rect).crossTopOut(pt1, pt2, rect)
                || Counter.getInstance(rect).crossLeftOut(pt1, pt2, rect))) {
            flybyId.add(trackId);
            flybyIdDrawn.add(trackId);
        }
        // Top TO (Left or Right)
        if(crossTopId.contains(trackId) && (Counter.getInstance(rect).crossLeftOut(pt1, pt2, rect)
                || Counter.getInstance(rect).crossRightOut(pt1, pt2, rect))) {
            flybyId.add(trackId);
            flybyIdDrawn.add(trackId);
        }
    }

}
