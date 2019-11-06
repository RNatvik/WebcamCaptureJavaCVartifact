package data;

import java.awt.image.BufferedImage;

/**
 * Class containing output information from the ImageProcessor.
 */
public class ImageProcessorData extends Data {

    private BufferedImage image;
    private double[] location;

    /**
     * Constructs a new instance
     * @param image output image from processor
     * @param location detected object location
     */
    public ImageProcessorData(BufferedImage image, double[] location) {
        super(Data.IMAGE);
        this.image = image;
        this.location = location;
    }

    /**
     * Returns the instance's output image
     * @return the instance's output image
     */
    public synchronized BufferedImage getImage() {
        return image;
    }

    /**
     * Set the instance's output image
     * @param image new output image
     */
    public synchronized void setImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Returns the detected object location
     * @return the detected object location
     */
    public synchronized double[] getLocation() {
        return location;
    }

    /**
     * Set the detected object location
     * @param location new detected object location
     */
    public synchronized void setLocation(double[] location) {
        this.location = location;
    }
}
