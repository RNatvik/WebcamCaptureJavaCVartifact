package data;

import java.awt.image.BufferedImage;

/**
 * The output image from the image processor
 */
public class OutputImage extends Data {

    private BufferedImage image;

    /**
     * Constructor
     *
     * @param image the image to store
     */
    public OutputImage(BufferedImage image) {
        this.image = image;
    }

    /**
     * Gets the stored image
     *
     * @return the stored image
     */
    public BufferedImage getImage() {
        return image;
    }
}
