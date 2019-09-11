
import org.bytedeco.javacv.Frame;


public class ImageStorageBox {

    private Frame image;
    private boolean unconsumedFrame;

    public ImageStorageBox() {
        this.image = new Frame();
        this.unconsumedFrame = false;
    }

    public synchronized Frame consumeImage() {
        System.out.println("Consuming image");
        this.unconsumedFrame = false;
        return this.image;
    }

    public synchronized void putImage(Frame image) {
        System.out.println("Storing image");
        this.image = image;
        this.unconsumedFrame = true;
    }

    public synchronized boolean hasUnconsumedFrame() {
        return this.unconsumedFrame;
    }

}
