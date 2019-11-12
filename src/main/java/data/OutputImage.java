package data;

import java.awt.image.BufferedImage;

public class OutputImage extends Data {

    private BufferedImage image;

    public OutputImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }
}
