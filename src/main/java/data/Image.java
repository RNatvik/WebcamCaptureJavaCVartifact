package data;

import org.bytedeco.javacv.Frame;

public class Image extends Data{

    private Frame frame;

    public Image(Frame frame) {
        super(DataType.IMAGE);
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }
}
