import data.Image;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;


/**
 * This class represents an image processor.
 */
public class ImageProcessor implements Runnable {

    private ImageStorageBox storageBox;
    private Database database;
    private Thread thread;
    private CvScalar firstRangeMin, firstRangeMax;
    private CvScalar secondRangeMin, secondRangeMax;
    private OpenCVFrameConverter.ToIplImage iplConverter;
    private OpenCVFrameConverter.ToMat matConverter;
    private boolean shutdown;

    // Testing purposes //
    private CanvasFrame canvas;
    private CanvasFrame canvas2;
    private boolean firstImage;

    public ImageProcessor(ImageStorageBox storageBox, Database database) {
        this.storageBox = storageBox;
        this.database = new Database();
        this.thread = new Thread(this);
        this.firstRangeMin = new CvScalar(0, 0, 0, 0);
        this.firstRangeMax = new CvScalar(179, 255, 255, 0);
        this.secondRangeMin = new CvScalar(0, 0, 0, 0);
        this.secondRangeMax = new CvScalar(179, 255, 255, 0);
        this.iplConverter = new OpenCVFrameConverter.ToIplImage();
        this.matConverter = new OpenCVFrameConverter.ToMat();
        this.canvas = new CanvasFrame("Canvas 1");
        this.canvas2 = new CanvasFrame("Canvas 2");
        this.firstImage = true;
    }

    public void setup() {
        this.thread.start();
        this.setFirstRange(38, 82, 0, 167, 36, 200);
        this.setSecondRange(0, 40, 200, 255, 100, 255);
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
                    this.firstImage = false;
                }
                Frame processedFrame = this.blurFilter(frame, 9);
                processedFrame = this.thresholdFirstRange(processedFrame);
                processedFrame = this.medFilter(processedFrame, 5);
                processedFrame = this.dilateImage(processedFrame, 7);
                processedFrame = this.erodeImage(processedFrame, 14);
                processedFrame = this.dilateImage(processedFrame, 7);
                processedFrame = this.edgeDetectCanny(processedFrame);
                List<int[]> locations = this.customDetectCircle(processedFrame, 15, 20, 20, 0.4);

                Frame paintedFrame = paintCircles(frame, locations);
                this.database.setImageToGUI(new Image(paintedFrame));
                this.canvas.showImage(paintedFrame);
                this.canvas2.showImage(processedFrame);
                // System.out.println(System.currentTimeMillis() - startTime);
            }
        }
        this.shutdownProcedure();
    }

    /**
     * Paint red circles in frame for given location and size
     *
     * @param frame     the frame to paint the circles on
     * @param locations list of locations in frame to paint the circles formatted as [x,y,r] where x and y are the
     *                  circle center position and r is the circles radius
     * @return painted frame
     */
    private Frame paintCircles(Frame frame, List<int[]> locations) {
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
                    }
                }
            }
        }
        return paintedFrame;
    }

    /**
     * Set the first color range
     *
     * @param hueMin minimum hue in range
     * @param hueMax maximum hue in range
     * @param satMin minimum saturation in range
     * @param satMax maximum saturation in range
     * @param valMin minimum value in range
     * @param valMax maximum value in range
     */
    private void setFirstRange(int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax) {
        this.firstRangeMin = new CvScalar(hueMin, satMin, valMin, 0);
        this.firstRangeMax = new CvScalar(hueMax, satMax, valMax, 0);
    }

    /**
     * Set the second color range
     *
     * @param hueMin minimum hue in range
     * @param hueMax maximum hue in range
     * @param satMin minimum saturation in range
     * @param satMax maximum saturation in range
     * @param valMin minimum value in range
     * @param valMax maximum value in range
     */
    private void setSecondRange(int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax) {
        this.secondRangeMin = new CvScalar(hueMin, satMin, valMin, 0);
        this.secondRangeMax = new CvScalar(hueMax, satMax, valMax, 0);
    }

    /**
     * Preforms color thresholding on the input frame
     *
     * @param frame the frame to threshold
     * @return binary color thresholded frame
     */
    private Frame thresholdFirstRange(Frame frame) {
        IplImage image = iplConverter.convert(frame);
        CvSize size = new CvSize(image.width(), image.height());
        IplImage imghsv = cvCreateImage(size, 8, 3);
        IplImage imgbin = cvCreateImage(size, 8, 1);

        cvCvtColor(image, imghsv, CV_BGR2HSV);
        cvInRangeS(imghsv, this.firstRangeMin, this.firstRangeMax, imgbin);

        return this.iplConverter.convert(imgbin);
    }

    /**
     * Preforms color thresholding on the input frame
     *
     * @param frame the frame to threshold
     * @return binary color thresholded frame
     */
    private Frame thresholdSecondRange(Frame frame) {
        IplImage image = iplConverter.convert(frame);
        CvSize size = new CvSize(image.width(), image.height());
        IplImage imghsv = cvCreateImage(size, 8, 3);
        IplImage imgbin = cvCreateImage(size, 8, 1);

        cvCvtColor(image, imghsv, CV_BGR2HSV);
        cvInRangeS(imghsv, this.secondRangeMin, this.secondRangeMax, imgbin);

        return this.iplConverter.convert(imgbin);
    }

    /**
     * Preforms color thresholding on the input frame using both internal ranges
     * Useful only for detecting multiple colors OR the color red.
     *
     * @param frame the frame to threshold
     * @return binary color thresholded frame
     */
    private Frame thresholdBoth(Frame frame) {
        Frame bin1 = thresholdFirstRange(frame);
        Frame bin2 = thresholdSecondRange(frame);

        Mat mat1 = this.matConverter.convert(bin1);
        Mat mat2 = this.matConverter.convert(bin2);
        Mat addedMats = addPut(mat1, mat2);

        return this.matConverter.convert(addedMats);
    }

    /**
     * Slims (erodes) edges in an image
     *
     * @param frame      the frame to erode
     * @param perimeters number of edge perimeters to remove
     * @return eroded frame
     */
    private Frame erodeImage(Frame frame, int perimeters) {
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage erodedImage = srcImage.clone();
        for (int i = 0; i < perimeters; i++) {
            cvErode(erodedImage, erodedImage);
        }
        return this.iplConverter.convert(erodedImage);
    }

    /**
     * Expands (dilates) edges in an image
     *
     * @param frame      the frame to dilate
     * @param perimeters number of perimiters to add
     * @return expanded frame
     */
    private Frame dilateImage(Frame frame, int perimeters) {
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage dilatedImage = srcImage.clone();
        for (int i = 0; i < perimeters; i++) {
            cvDilate(dilatedImage, dilatedImage);
        }
        return this.iplConverter.convert(dilatedImage);
    }

    /**
     * Median Filter. Finds the median value of pixel values encompassed by the NxN filter mask.
     *
     * @param frame the input image
     * @param N     mask size
     * @return median filtered Frame
     */
    private Frame medFilter(Frame frame, int N) {
        Mat srcMat = this.matConverter.convert(frame);
        Mat smoothMat = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type());
        org.bytedeco.opencv.global.opencv_imgproc.medianBlur(srcMat, smoothMat, N);

        return this.matConverter.convert(smoothMat);
    }

    /**
     * Blur the input frame
     *
     * @param frame input frame to blur
     * @param N     mask size
     * @return blurred frame
     */
    private Frame blurFilter(Frame frame, int N) {
        Mat srcMat = this.matConverter.convert(frame);
        Mat smoothMat = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type());
        org.bytedeco.opencv.global.opencv_imgproc.blur(srcMat, smoothMat, new Size(N, N));

        return this.matConverter.convert(smoothMat);
    }

    /**
     * Use canny method to detect edges in input frame.
     *
     * @param frame the frame to scan.
     * @return binary frame with only detected edges.
     */
    private Frame edgeDetectCanny(Frame frame) {
        CvSize size = new CvSize(frame.imageWidth, frame.imageHeight);
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage edgeImage = cvCreateImage(size, 8, 1);
        cvCanny(srcImage, edgeImage, 0, 1);
        return this.iplConverter.convert(edgeImage);
    }

    /**
     * Finds circle objects in the input frame with specified radius range
     *
     * @param frame     the frame to scan
     * @param rMin      minimum circle radius to detect
     * @param rMax      maximum circle radius to detect
     * @param steps     amount of points to match to a circle
     * @param threshold edge strength threshold (redundant for binary images: set to 1)
     * @return list of detected circles formatted as [x, y, r] where x the circle center column position, y is the
     * circle center row position and r is the detected circle radius.
     */
    private List<int[]> customDetectCircle(Frame frame, int rMin, int rMax, int steps, double threshold) {
        Mat mat = this.matConverter.convert(frame);
        long pointsTimerStart = System.currentTimeMillis();
        ArrayList<int[]> points = new ArrayList<>();
        for (int r = rMin; r < rMax + 1; r++) {
            for (int t = 0; t < steps; t++) {
                int[] temp = new int[3];
                temp[0] = r;
                temp[1] = (int) (r * Math.cos(2 * Math.PI * t / steps));
                temp[2] = (int) (r * Math.sin(2 * Math.PI * t / steps));
                points.add(temp);
            }
        }

        // Start of time intensive process
        long findEdgesTimerStart = System.currentTimeMillis();
        ArrayList<int[]> edges = new ArrayList<>();
        UByteRawIndexer indexer = mat.createIndexer();
        for (int x = 0; x < mat.cols(); x++) {
            for (int y = 0; y < mat.rows(); y++) {
                if (indexer.get(y, x) > 0) {
                    edges.add(new int[]{x, y});
                }
            }
        }
        // end of time intensive process

        long accStartTime = System.currentTimeMillis();
        Map<String, Integer> acc = new HashMap<>();
        for (int[] edge : edges) {
            int x = edge[0];
            int y = edge[1];
            for (int[] point : points) {
                int r = point[0];
                int a = x - point[1];
                int b = y - point[2];
                String key = a + "," + b + "," + r;
                if (acc.containsKey(key)) {
                    int tempVal = acc.get(key);
                    acc.put(key, tempVal + 1);
                } else {
                    acc.put(key, 1);
                }
            }
        }

        long sortAccStartTime = System.currentTimeMillis();
        Map<String, Integer> sorted = acc
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        long findCirclesStartTime = System.currentTimeMillis();
        ArrayList<int[]> circles = new ArrayList<>();
        boolean first = true;
        for (String key : sorted.keySet()) {
            int v = sorted.get(key);
            String[] strings = key.split(",");
            int x = Integer.parseInt(strings[0]);
            int y = Integer.parseInt(strings[1]);
            int r = Integer.parseInt(strings[2]);
            int[] toBeNamed = new int[]{x, y, r};

            if ((double) v / steps >= threshold) {
                boolean add = true;
                if (first) {
                    first = false;
                    circles.add(toBeNamed);
                } else {
                    for (int[] circle : circles) {
                        int xc = circle[0];
                        int yc = circle[1];
                        int rc = circle[2];
                        if (!(Math.pow((x - xc), 2) + Math.pow((y - yc), 2) > Math.pow(rc, 2))) {
                            add = false;
                        }
                    }
                    if (add) {
                        circles.add(toBeNamed);
                    }
                }

            }

        }
        long findCriclesEndTime = System.currentTimeMillis();
//        System.out.println("Setup points: " + (findEdgesTimerStart - pointsTimerStart));
//        System.out.println("Find edges: " + (accStartTime - findEdgesTimerStart));
//        System.out.println("Accumulate: " + (sortAccStartTime - accStartTime));
//        System.out.println("Sort acc: " + (findCirclesStartTime - sortAccStartTime));
//        System.out.println("Find circles: " + (findCriclesEndTime - findCirclesStartTime));
        return circles;
    }

    /**
     * Shut down procedure. Called whenever the thread is stopped.
     */
    private void shutdownProcedure() {
        this.canvas.dispose();
        this.canvas2.dispose();
    }
}
