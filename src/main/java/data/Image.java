package data;

import java.awt.image.BufferedImage;

public class Image extends Data{

    private BufferedImage image;

    public Image(boolean initialFlag) {
        super(DataType.IMAGE, initialFlag);
    }

    public synchronized BufferedImage getImage() {
        return image;
    }

    public synchronized void setImage(BufferedImage image) {
        this.image = image;
    }

}
