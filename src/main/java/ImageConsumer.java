import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import java.util.List;

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
        this.thresholder.setFirstRange(33, 80, 15, 155, 53, 200);
        this.thresholder.setSecondRange(0, 40, 200, 255, 100, 255);
        this.thread.start();
    }

    public void stop() {
        this.shutdown = true;
    }

    public void run() {
        while (!this.shutdown) {
            if (this.storageBox.hasUnconsumedFrame()) {
                long startTime = System.currentTimeMillis();
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
                Frame blurredFrame = this.thresholder.blurFilter(frame, 9);
                Frame thresholdFrame = this.thresholder.thresholdFirstRange(blurredFrame);
                Frame medFrame = this.thresholder.medFilter(thresholdFrame, 5);
                Frame erodedFrame = this.thresholder.erodeImage(medFrame, 3);
                Frame dilatedFrame = this.thresholder.dilateImage(erodedFrame, 3);
                List<int[]> locations = this.thresholder.customDetectCircle(dilatedFrame, 10, 50, 10, 0.4);

                Frame paintedFrame = frame.clone();
                UByteBufferIndexer indexer = paintedFrame.createIndexer();
                for (int[] location : locations) {
                    int xCenter = location[0];
                    int yCenter = location[1];
                    int r = location[2];
                    for (int i = r; i < r + 2; i++) {
                        int numSteps = (int) (2 * Math.PI * i);
                        for (double step = 0; step < numSteps; step++) {
                            int x = xCenter + (int) (i * Math.cos(step * 2 * Math.PI / numSteps));
                            int y = yCenter + (int) (i * Math.sin(step * 2 * Math.PI / numSteps));

                            try {
                                indexer.put(y, x, new int[]{0, 0, 255});
                            } catch (IndexOutOfBoundsException iob) {
                                System.out.println("Tried to paint out of frame");
                            }
                        }
                    }
                }
                this.canvas.showImage(dilatedFrame);
//                this.canvas2.showImage(blurredFrame);
//                this.canvas3.showImage(medFrame);
//                this.canvas4.showImage(thresholdFrame);
//                this.canvas5.showImage(erodedFrame);
                this.canvas6.showImage(paintedFrame);
                System.out.println(System.currentTimeMillis() - startTime);
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
