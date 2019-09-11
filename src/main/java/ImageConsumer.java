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
    private CanvasFrame canvas3;
    private CanvasFrame canvas4;
    private CanvasFrame canvas5;
    private CanvasFrame canvas6;
    private boolean firstImage;

    public ImageConsumer(ImageStorageBox storageBox) {
        this.storageBox = storageBox;
        this.thresholder = new ImageThresholder();
        this.thread = new Thread(this);
        this.canvas = new CanvasFrame("Captured Image");
        this.canvas2 = new CanvasFrame("Blur");
        this.canvas3 = new CanvasFrame("Median");
        this.canvas4 = new CanvasFrame("Thresh");
        this.canvas5 = new CanvasFrame("Eroded");
        this.canvas6 = new CanvasFrame("Dilated");
        this.firstImage = true;
    }

    public void setup() {
        this.thresholder.setFirstRange(33, 90, 20, 255, 0, 250);
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
                    this.canvas3.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.canvas4.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.canvas5.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.canvas6.setCanvasSize(frame.imageWidth, frame.imageHeight);
                    this.firstImage = false;
                }
                Frame blurredFrame = this.thresholder.blurFilter(frame, 5);
                Frame thresholdFrame = this.thresholder.thresholdFirstRange(blurredFrame);
                Frame medFrame = this.thresholder.medFilter(thresholdFrame, 5);
                Frame erodedFrame = this.thresholder.erodeImage(medFrame, 10);
                Frame dilatedFrame = this.thresholder.dilateImage(erodedFrame, 10);
                this.canvas.showImage(frame);
                this.canvas2.showImage(blurredFrame);
                this.canvas3.showImage(medFrame);
                this.canvas4.showImage(thresholdFrame);
                this.canvas5.showImage(erodedFrame);
                this.canvas6.showImage(dilatedFrame);
            }
        }
        this.shutdownProcedure();
    }


    private void shutdownProcedure() {
        this.canvas.dispose();
        this.canvas2.dispose();
        this.canvas3.dispose();
        this.canvas4.dispose();
        this.canvas5.dispose();
        this.canvas6.dispose();
    }
}
