import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;



/**
 * This class represents a camera connected to the system.
 */
public class Camera implements Runnable {

    private VideoInputFrameGrabber grabber;
    private Frame capturedFrame;
    private Thread thread;
    private ImageStorageBox storageBox;
    private boolean shutdown;
    private boolean initialized;

    public Camera(int deviceNumber, ImageStorageBox storageBox) {
        this.grabber = new VideoInputFrameGrabber(deviceNumber);
        this.thread = new Thread(this);
        this.storageBox = storageBox;
        this.capturedFrame = new Frame();
        this.shutdown = false;
        this.initialized = false;
    }

    /**
     * Starts the camera and thread
     * Run this method once on startup.
     *
     * @return
     */
    public boolean setup() {
        boolean success = false;
        if (!initialized) {
            try {
                this.grabber.start();
                System.out.println(this.grabber.getFormat());
                System.out.println(this.grabber.getImageMode());
                this.thread.start();
                success = true;
                this.initialized = true;
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    /**
     * Closes the camera object. The run() method will exit after a shutdown procedure
     */
    public void stop() {
        this.shutdown = true;
    }

    public void run() {
        while (!this.shutdown) {
            boolean successfulCapture = this.captureImage();
            if (successfulCapture && !this.storageBox.hasUnconsumedFrame()) {
                Frame frameToStore = this.capturedFrame.clone();
                this.storageBox.putImage(frameToStore);
            }
        }
        this.shutdownProcedure();
    }

    /**
     * Attemts to grab a frame / image from the camera device
     *
     * @return grab success
     */
    private boolean captureImage() {
        boolean success = true;
        try {
            this.capturedFrame = this.grabber.grabFrame();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
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
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }


}
