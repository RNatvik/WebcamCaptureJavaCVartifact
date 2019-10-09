package data;

import java.awt.image.BufferedImage;

public class ImageProcessorData extends Data {

    private BufferedImage image;
    private int[] location;

    public ImageProcessorData(BufferedImage image, int[] location) {
        super(Data.IMAGE);
        this.image = image;
        this.location = location;
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    public synchronized void setImage(BufferedImage image) {
        this.image = image;
    }

    public synchronized int[] getLocation() {
        return location;
    }

    public synchronized void setLocation(int[] location) {
        this.location = location;
    }
}
