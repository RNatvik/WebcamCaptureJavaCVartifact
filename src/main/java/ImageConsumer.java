import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

public class ImageConsumer implements Runnable {

    private ImageStorageBox storageBox;
    private ImageThresholder thresholder;
    private Thread thread;
    private boolean shutdown;

    // Testing purposes //
    private CanvasFrame canvas;
    private CanvasFrame canvas2;
    private boolean firstImage;

    public ImageConsumer(ImageStorageBox storageBox) {
        this.storageBox = storageBox;
        this.thresholder = new ImageThresholder();
        this.thread = new Thread(this);
        this.canvas = new CanvasFrame("cam");
        this.canvas2 = new CanvasFrame("reference");
        this.firstImage = true;
    }

    public void setup() {
        this.thresholder.setFirstRange(140, 179, 200, 255, 100, 255);
        this.thresholder.setSecondRange(0, 40, 200, 255, 100, 255);
        this.thread.start();
    }

    public void stop() {
        this.shutdown = true;
    }

    public void run() {
        while (!this.shutdown) {
            if (this.storageBox.hasUnconsumedFrame()) {
                Frame frame = this.storageBox.consumeImage();
                if (this.firstImage) {
                    this.canvas.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.canvas2.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.firstImage = false;
                }
                Frame frameToShow = this.thresholder.thresholdBoth(frame);
                this.canvas.showImage(frameToShow, false);
                this.canvas2.showImage(frame);
            }
        }
        this.shutdownProcedure();
    }


    private void shutdownProcedure() {
        this.canvas.dispose();
        this.canvas2.dispose();
    }
}
