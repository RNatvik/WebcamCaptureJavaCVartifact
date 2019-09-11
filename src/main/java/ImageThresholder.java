
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;

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
}
