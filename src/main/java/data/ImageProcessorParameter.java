package data;

public class ImageProcessorParameter extends Data{

    private int hueMin;
    private int hueMax;
    private int satMin;
    private int satMax;
    private int valMin;
    private int valMax;
    private boolean storeProcessedImage;

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

    public int getHueMax() {
        return hueMax;
    }

    public int getHueMin() {
        return hueMin;
    }

    public int getSatMax() {
        return satMax;
    }

    public int getSatMin() {
        return satMin;
    }

    public int getValMax() {
        return valMax;
    }

    public int getValMin() {
        return valMin;
    }

    public boolean isStoreProcessedImage() {
        return storeProcessedImage;
    }
}
