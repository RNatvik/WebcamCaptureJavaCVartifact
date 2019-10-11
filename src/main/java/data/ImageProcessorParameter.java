package data;

/**
 * This class contains all configuration parameters for an ImageProcessor
 */
public class ImageProcessorParameter extends Data {

    private int hueMin;
    private int hueMax;
    private int satMin;
    private int satMax;
    private int valMin;
    private int valMax;
    private boolean storeProcessedImage;

    /**
     * Constructs a new instance
     *
     * @param hueMin              color threshold lower range hue value
     * @param hueMax              color threshold upper range hue value
     * @param satMin              color threshold lower range saturation value
     * @param satMax              color threshold upper range saturation value
     * @param valMin              color threshold lower range value value
     * @param valMax              color threshold upper range value value
     * @param storeProcessedImage boolean for which image to publish to broker
     */
    public ImageProcessorParameter(int hueMin, int hueMax, int satMin, int satMax, int valMin, int valMax, boolean storeProcessedImage) {
        super(Data.IMPROC_PARAM);
        this.hueMin = hueMin;
        this.hueMax = hueMax;
        this.satMin = satMin;
        this.satMax = satMax;
        this.valMin = valMin;
        this.valMax = valMax;
        this.storeProcessedImage = storeProcessedImage;
    }

    /**
     * Returns the instance's upper range hue value
     *
     * @return the instance's upper range hue value
     */
    public int getHueMax() {
        return hueMax;
    }

    /**
     * Returns the instance's lower range hue value
     *
     * @return the instance's lower range hue value
     */
    public int getHueMin() {
        return hueMin;
    }

    /**
     * Returns the instance's upper range saturation value
     *
     * @return the instance's upper range saturation value
     */
    public int getSatMax() {
        return satMax;
    }

    /**
     * Returns the instance's lower range saturation value
     *
     * @return the instance's lower range saturation value
     */
    public int getSatMin() {
        return satMin;
    }

    /**
     * Returns the instance's upper range value value
     *
     * @return the instance's upper range value value
     */
    public int getValMax() {
        return valMax;
    }

    /**
     * Returns the instance's lower range value value
     *
     * @return the instance's lower range value value
     */
    public int getValMin() {
        return valMin;
    }

    /**
     * Returns whether or not to publish the processed image instead of the original image
     *
     * @return whether or not to publish the processed image instead of the original image
     */
    public boolean isStoreProcessedImage() {
        return storeProcessedImage;
    }
}
