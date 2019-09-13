
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacpp.indexer.UByteRawIndexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;

import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class ImageThresholder {

    private CvScalar firstRangeMin, firstRangeMax;
    private CvScalar secondRangeMin, secondRangeMax;
    private OpenCVFrameConverter.ToIplImage iplConverter;
    private OpenCVFrameConverter.ToMat matConverter;

    public ImageThresholder() {
        this.firstRangeMin = new CvScalar(0,0,0,0);
        this.firstRangeMax = new CvScalar(179,255,255,0);
        this.secondRangeMin = new CvScalar(0,0,0,0);
        this.secondRangeMax = new CvScalar(179,255,255,0);
        this.iplConverter = new OpenCVFrameConverter.ToIplImage();
        this.matConverter = new OpenCVFrameConverter.ToMat();
    }

    public void setFirstRange(int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax) {
        this.firstRangeMin = new CvScalar(hueMin, satMin, valMin, 0);
        this.firstRangeMax = new CvScalar(hueMax, satMax, valMax, 0);
    }

    public void setSecondRange(int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax) {
        this.secondRangeMin = new CvScalar(hueMin, satMin, valMin, 0);
        this.secondRangeMax = new CvScalar(hueMax, satMax, valMax, 0);
    }

    public Frame thresholdFirstRange(Frame frame) {
        IplImage image = iplConverter.convert(frame);
        CvSize size = new CvSize(image.width(), image.height());
        IplImage imghsv = cvCreateImage(size, 8, 3);
        IplImage imgbin = cvCreateImage(size, 8, 1);

        cvCvtColor(image, imghsv, CV_BGR2HSV);
        cvInRangeS(imghsv, this.firstRangeMin, this.firstRangeMax, imgbin);

        return this.iplConverter.convert(imgbin);
    }

    public Frame thresholdSecondRange(Frame frame) {
        IplImage image = iplConverter.convert(frame);
        CvSize size = new CvSize(image.width(), image.height());
        IplImage imghsv = cvCreateImage(size, 8, 3);
        IplImage imgbin = cvCreateImage(size, 8, 1);

        cvCvtColor(image, imghsv, CV_BGR2HSV);
        cvInRangeS(imghsv, this.secondRangeMin, this.secondRangeMax, imgbin);

        return this.iplConverter.convert(imgbin);
    }

    public Frame thresholdBoth(Frame frame) {
        Frame bin1 = thresholdFirstRange(frame);
        Frame bin2 = thresholdSecondRange(frame);

        Mat mat1 = this.matConverter.convert(bin1);
        Mat mat2 = this.matConverter.convert(bin2);
        Mat addedMats = addPut(mat1, mat2);

        return this.matConverter.convert(addedMats);
    }

    public Frame erodeImage(Frame frame, int perimeters) {
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage erodedImage = srcImage.clone();
        for (int i = 0; i < perimeters; i++) {
            cvErode(erodedImage, erodedImage);
        }
        return this.iplConverter.convert(erodedImage);
    }

    public Frame dilateImage(Frame frame, int perimeters) {
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage dilatedImage = srcImage.clone();
        for (int i = 0; i < perimeters; i++) {
            cvDilate(dilatedImage, dilatedImage);
        }
        return this.iplConverter.convert(dilatedImage);
    }

    public Frame medFilter(Frame frame, int N) {
        Mat srcMat = this.matConverter.convert(frame);
        Mat smoothMat = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type());
        org.bytedeco.opencv.global.opencv_imgproc.medianBlur(srcMat, smoothMat, N);

        return this.matConverter.convert(smoothMat);
    }

    public Frame blurFilter(Frame frame, int N) {
        Mat srcMat = this.matConverter.convert(frame);
        Mat smoothMat = new Mat(srcMat.rows(), srcMat.cols(), srcMat.type());
        org.bytedeco.opencv.global.opencv_imgproc.blur(srcMat, smoothMat, new Size(N,N));

        return this.matConverter.convert(smoothMat);
    }

    public List<int[]> customDetectCircle(Frame frame, int rMin, int rMax, int steps, double threshold) {
        CvSize size = new CvSize(frame.imageWidth, frame.imageHeight);
        IplImage srcImage = this.iplConverter.convert(frame);
        IplImage edgeImage = cvCreateImage(size, 8, 1);
        cvCanny(srcImage, edgeImage, 0, 1);
        Mat mat = this.matConverter.convert(this.iplConverter.convert(edgeImage));

        ArrayList<int[]> points = new ArrayList<>();
        for (int r = rMin; r < rMax+1; r++) {
            for (int t = 0; t < steps; t++) {
                int[] temp = new int[3];
                temp[0] = r;
                temp[1] = (int)(r * Math.cos(2 * Math.PI * t / steps));
                temp[2] = (int)(r * Math.sin(2 * Math.PI * t / steps));
                points.add(temp);
            }
        }

        // https://www.codingame.com/playgrounds/38470/how-to-detect-circles-in-images
        Map<String, Integer> acc = new HashMap<>();
        ArrayList<int[]> edges = new ArrayList<>();
        UByteRawIndexer indexer = mat.createIndexer();
        for (int x = 0; x < mat.cols(); x++) {
            for (int y = 0; y < mat.rows(); y++) {
                if (indexer.get(y, x) > 0) {
                    edges.add(new int[]{x,y});
                }
            }
        }
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

        Map<String, Integer> sorted = acc
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        ArrayList<int[]> circles = new ArrayList<>();
        boolean first = true;
        for (String key : sorted.keySet()) {
            int v = sorted.get(key);
            String[] strings = key.split(",");
            int x = Integer.parseInt(strings[0]);
            int y = Integer.parseInt(strings[1]);
            int r = Integer.parseInt(strings[2]);
            int[] toBeNamed = new int[]{x, y, r};

            if ((double)v/steps >= threshold) {
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
        for (int[] circle : circles) {
            System.out.println("x: " + circle[0] + ", y: " + circle[1] + ", r: " + circle[2]);
        }
        return circles;
    }
}