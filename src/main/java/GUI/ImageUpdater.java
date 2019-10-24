package GUI;

import communication.UDPClient;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class ImageUpdater implements Runnable {

    private ObjectProperty<Image> imageProperty;
    private ImageView imageView;
    private UDPClient udpClient;

    public ImageUpdater(ObjectProperty<Image> imageProperty, ImageView imageView, UDPClient udpClient) {
        this.imageProperty = imageProperty;
        this.imageView = imageView;
        this.udpClient = udpClient;
    }

    @Override
    public void run() {
        BufferedImage image = null;
        while (image == null) {
            image = this.udpClient.getImage();
        }
        Image im = SwingFXUtils.toFXImage(image, null);
        this.imageProperty.set(im);
        imageView.imageProperty().bind(this.imageProperty);
    }
}
