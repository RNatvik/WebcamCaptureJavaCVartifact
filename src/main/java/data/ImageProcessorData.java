package data;


/**
 * Class containing output information from the ImageProcessor.
 */
public class ImageProcessorData extends Data {

    private double[] location;

    /**
     * Constructs a new instance
     *
     * @param location detected object location
     */
    public ImageProcessorData(double[] location) {
        this.location = location;
    }

    /**
     * Returns the detected object location
     *
     * @return the detected object location
     */
    public synchronized double[] getLocation() {
        return location;
    }

    /**
     * Set the detected object location
     *
     * @param location new detected object location
     */
    public synchronized void setLocation(double[] location) {
        this.location = location;
    }
}
