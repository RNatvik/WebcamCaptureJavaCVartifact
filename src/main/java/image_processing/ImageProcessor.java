package image_processing;

import data.Circle;
import data.Flag;
import data.Image;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.CvMoments;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * This class is an image processor for object detection.
 * When its run() method is invoked it is designed to run until stop() method is called
 * It needs an instance of the data.Flag class, shared with a capturing device to be notified when a new image is available.
 * An instance of this class needs to have its start() method called ONCE before invoking the run() method.
 */
public class ImageProcessor implements Runnable {

    private IplImage srcIm;
    private IplImage binIm;
    private LowPassFilter filter;
    private Flag flag;
    private Image outputImage;
    private Circle outputCircle;
    private OpenCVFrameConverter.ToIplImage converter;
    private Java2DFrameConverter bufferedImageConverter;
    private boolean shutdown;
    private boolean initialized;
    private boolean terminated;

    private CanvasFrame canvas;
    private long timeTest;

    /**
     * Instance constructor
     *
     * @param flag a flag for cross thread notifications that a new image has been produced
     */
    public ImageProcessor(Flag flag, Image outputImage, Circle outputCircle) {
        this.srcIm = cvCreateImage(new CvSize(640, 480), 8, 3);
        this.binIm = cvCreateImage(new CvSize(640, 480), 8, 1);
        this.filter = new LowPassFilter(5);
        this.flag = flag;
        this.outputImage = outputImage;
        this.outputCircle = outputCircle;
        this.converter = new OpenCVFrameConverter.ToIplImage();
        this.bufferedImageConverter = new Java2DFrameConverter();
        this.shutdown = false;
        this.initialized = false;
        this.terminated = false;
        this.canvas = new CanvasFrame("image_processing.ImageProcessor");
        this.timeTest = 0;

    }

    /**
     * Start method
     * Call this method once before invoking the run() method.
     * This initializes the source image feed.
     *
     * @param srcIm the source image feed to process
     * @return true if successful, false if already initialized
     */
    public boolean start(IplImage srcIm) {
        boolean success = false;
        if (!this.initialized) {
            this.srcIm = srcIm;
            this.canvas.setCanvasSize(this.srcIm.width(), this.srcIm.height());
            this.initialized = true;
            this.shutdown = false;
            success = true;
        }
        return success;
    }

    /**
     * Stops the processor upon completion of the current loop in the run() method
     */
    public void stop() {
        this.shutdown = true;
    }

    /**
     * Used to check whether or not the the process is terminated.
     *
     * @return true if the process is terminated. False if not
     */
    public boolean isTerminated() {
        return this.terminated;
    }

    /**
     * This class' runnable method
     * This method starts a while-loop where the source image feed is processed by thresholding and filtering
     * to locate an object and paint its location on a copy of the source image.
     */
    @Override
    public void run() {
        while (!this.shutdown) {
            if (this.flag.get()) {

                IplImage image = cvCloneImage(this.srcIm);
                IplConvKernel kernel = IplConvKernel.create(5, 5, 2, 2, CV_SHAPE_ELLIPSE, null);
                this.flag.set(false);

                this.threshold(image, this.binIm);
                this.morph(this.binIm, kernel, 5, 3);
                cvSmooth(this.binIm, this.binIm, CV_GAUSSIAN, 5, 0, 0, 0); // cvSmooth(input, output, method, N, M=0, sigma1=0, sigma2=0)

                int[] location = this.getCoordinates(this.binIm);
                this.paintCircle(image, new int[]{location[0], location[1], 2});
                this.paintCircle(image, location);
//                List<int[]> locations = this.getCircles(
//                        this.binIm,
//                        1,
//                        50,
//                        50,
//                        50,
//                        10,
//                        200
//                );
//                this.paintCircle(image, locations);
                this.canvas.showImage(this.converter.convert(image));
                this.setOutputs(image, location);
                cvReleaseImage(image);
            }
        }
        if (!this.isTerminated()) {
            this.shutdownSequence();
            this.terminated = true;
            this.initialized = false;
        }
    }

    /**
     * Color threshold method
     *
     * @param image  the input image
     * @param imgbin the output image
     */
    private void threshold(IplImage image, IplImage imgbin) {
        CvSize size = new CvSize(image.width(), image.height());
        IplImage imghsv = cvCreateImage(size, 8, 3);
        cvCvtColor(image, imghsv, CV_BGR2HSV);
        cvInRangeS(imghsv, new CvScalar(79, 94, 125, 0), new CvScalar(125, 255, 255, 0), imgbin);
        cvReleaseImage(imghsv);
        cvReleaseImageHeader(imghsv);
    }

    /**
     * Dilation and Erosion method. This method fills gaps by dilation and removes noise by erosion.
     * It then restores detected object size by dilation again.
     *
     * @param image           the image to morph
     * @param kernel          the filter kernel
     * @param initialDilation layers of initial dilation to fill gaps
     * @param erosion         layers of secondary erosion to remove noise.
     *                        This parameter tells the method how many layers to erode AFTER eroding the initial dilation.
     */
    private void morph(IplImage image, IplConvKernel kernel, int initialDilation, int erosion) {
        cvDilate(image, image, kernel, initialDilation);
        cvErode(image, image, kernel, initialDilation + erosion);
        cvDilate(image, image, kernel, erosion);
    }

    /**
     * Hough circle detection method. Finds edges in a gray scale image and finds circle shaped objects
     *
     * @param image       the image to detect circles in.
     * @param accRes      the accumulator's resolution. (inverse of size)
     * @param minDist     the minimum distance between centres of detected circles
     * @param cannyThresh the upper canny edge detection threshold. Lower threshold is half of this value
     * @param votes       the number of votes needed to be qualified as a circle
     * @param rMin        the minimum circle radius to detect
     * @param rMax        the maximum circle radius to detect
     * @return a List of integer arrays containing the circles' center location and radius formatted as {x,y,r}
     */
    private List<int[]> getCircles(IplImage image, int accRes, int minDist, double cannyThresh, double votes, int rMin, int rMax) {
        CvMemStorage mem = CvMemStorage.create();
        ArrayList<int[]> circleList = new ArrayList<>();
        CvSeq circles = cvHoughCircles(
                image, //Input image
                mem, //Memory Storage
                CV_HOUGH_GRADIENT, //Detection method
                accRes, //Inverse ratio
                minDist, //Minimum distance between the centers of the detected circles
                cannyThresh, //Higher threshold for canny edge detector
                votes, //Threshold at the center detection stage
                rMin, //min radius
                rMax //max radius
        );
        for (int i = 0; i < circles.total(); i++) {
            CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));
            int x = Math.round(circle.x());
            int y = Math.round(circle.y());
            int r = Math.round(circle.z());
            circleList.add(new int[]{x, y, r});
        }
        return circleList;
    }

    /**
     * Calculates the center of mass and radius of a circle located at this center (defined by it's area).
     * When used on a color thresholded image this locates the center of mass and the area of the detected objects.
     * Note that this means that if several objects are detected they are all viewed as a single object and the area and
     * location will be skewed as a result
     *
     * @param thresholdImage the image to calculate from
     * @return integer array
     */
    private int[] getCoordinates(IplImage thresholdImage) {

        CvMoments moments = new CvMoments();
        cvMoments(thresholdImage, moments, 1);
        // cv Spatial moment : Mji=sumx,y(I(x,y)•xj•yi)
        // where I(x,y) is the intensity of the pixel (x, y).
        double momX10 = cvGetSpatialMoment(moments, 1, 0); // (x,y)
        double momY01 = cvGetSpatialMoment(moments, 0, 1);// (x,y)
        double area = cvGetCentralMoment(moments, 0, 0);
        int x = (int) (momX10 / area);
        int y = (int) (momY01 / area);
        int radius = (int) (Math.sqrt(area / Math.PI));
        int r = this.filter.passValue(radius);
        return new int[]{x, y, r, (int)area};
    }

    /**
     * Colors a red circle on the input image
     *
     * @param image     the image to color the circle on
     * @param locations a list of integer arrays containing multiple circles' {x,y,radius} information
     */
    private void paintCircle(IplImage image, List<int[]> locations) {
        for (int[] location : locations) {
            paintCircle(image, location);
        }
    }

    /**
     * Colors a red circle on the input image
     *
     * @param image    the image to color the circle on
     * @param location integer array containing a single circle's {x,y,radius} information
     */
    private void paintCircle(IplImage image, int[] location) {
        Frame frame = this.converter.convert(image);
        UByteBufferIndexer indexer = frame.createIndexer();
        int xCenter = location[0];
        int yCenter = location[1];
        int r = location[2];
        for (int i = r; i < r + 3; i++) {
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

    private void setOutputs(IplImage image, int[] location) {
        this.outputImage.setImage(this.bufferedImageConverter.getBufferedImage(this.converter.convert(image)));
        this.outputImage.setFlag(true);
        this.outputCircle.setLocation(location);
        this.outputCircle.setFlag(true);
    }

    /**
     * The shutdown sequence for this instance. To be called when shutting down.
     */
    private void shutdownSequence() {
        this.canvas.dispose();
    }
}
