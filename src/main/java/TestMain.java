import data.Image;

import java.awt.image.BufferedImage;

public class TestMain {

    public static void main(String[] args) {
        Image image = new Image(false);
        BufferedImage bim = image.getImage();
        System.out.println(bim);
    }
}
