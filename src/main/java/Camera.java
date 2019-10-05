import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.bytedeco.opencv.opencv_core.CvSize;
import org.bytedeco.opencv.opencv_core.IplImage;

import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;

/**
 * This class represents a webcamera connected to the device.
 * An instance of this class cannot share the webcam with another instance of this class.
 * It is designed to be used in conjunction with a ScheduledExecutorService at a fixed rate (40ms works fine).
 * It requires a an instance of the Flag class to notify others that it has grabbed a new image.
 * <p>
 * To gain access to the video input stream generated by this class call the getSrcIm() method ONCE and pass the
 * returned IplImage to the class which wants to connect to the video stream. The reason you only need to call this method once
 * is that the IplImage returned by the getSrcIm() method contains its own storage for the image data (like a producer consumer storage box).
 */
public class Camera implements Runnable {

    private VideoInputFrameGrabber grabber;
    private IplImage srcIm;
    private OpenCVFrameConverter.ToIplImage converter;
    private Flag flag;
    private boolean shutdown;
    private boolean initialized;
    private boolean terminated;

    // For testing
    private long timeTest;

    /**
     * Constructor for the class
     * @param deviceNumber the webcam device number
     * @param flag a flag for cross thread notifications that a new image has been produced
     */
    public Camera(int deviceNumber, Flag flag) {
        this.grabber = new VideoInputFrameGrabber(deviceNumber);
        this.srcIm = cvCreateImage(new CvSize(640, 480), 8, 3);
        this.converter = new OpenCVFrameConverter.ToIplImage();
        this.flag = flag;
        this.shutdown = false;
        this.initialized = false;
        this.terminated = false;

        this.timeTest = 0;
    }

    /**
     * Start method
     * Call this method once before scheduling the run() method AND / OR distributing the source image pointer
     * This initializes the video input feed and prepares the source image pointer for distribution
     * @return true if initialized without error, false if error occurred OR already initialized
     */
    public boolean start() {
        boolean success = false;
        if (!this.initialized) {
            try {
                this.grabber.start();
                System.out.println(this.grabber.getFormat());
                System.out.println(this.grabber.getImageMode());
                Frame capturedFrame = this.grabber.grabFrame();
                this.srcIm = this.converter.convert(capturedFrame);
                success = true;
                this.initialized = true;
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Makes the camera shut down. The next call to run() will start its shutdown sequence
     */
    public void stop() {
        this.shutdown = true;
    }

    /**
     * Gets the source image pointer for distribution.
     * @return IplImage containing pointer to video stream data
     */
    public IplImage getSrcIm() {
        if (this.initialized) {
            return this.srcIm;
        } else {
            return null;
        }
    }

    /**
     * Used to check whether or not the the process is terminated.
     * @return true if the process is terminated. Flase if not
     */
    public boolean isTerminated() {
        return this.terminated;
    }


    /**
     * This class' runnable method.
     * It runs once per invocation where it captures and stores new data to the source image feed.
     */
    @Override
    public void run() {

        if (!this.shutdown) {
//            long time = System.currentTimeMillis();
//            long dt = time - this.timeTest;
//            this.timeTest = time;
//            System.out.println("Camera:: " + dt);
            try {
                this.grabber.grab();
                this.flag.set(true);
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
                this.shutdown = true;
            }
        } else if (!this.terminated) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("sleep was interrupted");
            }
            this.shutdownProcedure();
        }
    }

    /**
     * The objects shutdown procedure
     *
     * @return true if all processes stopped successfully
     */
    private boolean shutdownProcedure() {
        boolean success = true;
        try {
            this.grabber.release();
            this.grabber.stop();
            this.terminated = true;
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
}
