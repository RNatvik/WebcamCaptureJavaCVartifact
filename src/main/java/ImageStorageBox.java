
import org.bytedeco.javacv.Frame;

/**
 * This class is an intermediary storage for a Frame object.
 */
public class ImageStorageBox {

    private Frame image;
    private boolean unconsumedFrame;

    public ImageStorageBox() {
        this.image = new Frame();
        this.unconsumedFrame = false;
    }

    public synchronized Frame consumeImage() {
        this.unconsumedFrame = false;
        return this.image;
    }

    public synchronized void putImage(Frame image) {
        this.image = image;
        this.unconsumedFrame = true;
    }

    public synchronized boolean hasUnconsumedFrame() {
        return this.unconsumedFrame;
    }

}
