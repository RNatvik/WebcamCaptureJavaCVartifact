import data.Circle;
import data.Image;
import data.PidParameter;
import org.bytedeco.javacv.Frame;

public class Database {

    private Image imageToGUI;
    private Circle circle;
    private PidParameter pidParameter1;
    private PidParameter pidParameter2;

    public Database() {
        this.imageToGUI = new Image(new Frame());
        this.circle = new Circle(new int[]{0, 0, 0});
        this.pidParameter1 = new PidParameter(0,0,0);
        this.pidParameter2 = new PidParameter(0,0,0);
    }

    public synchronized Image getImageToGUI() {
        return imageToGUI;
    }

    public synchronized void setImageToGUI(Image imageToGUI) {
        this.imageToGUI = imageToGUI;
    }

    public synchronized Circle getCircle() {
        return circle;
    }

    public synchronized void setCircle(Circle circle) {
        this.circle = circle;
    }

    public synchronized PidParameter getPidParameter1() {
        return pidParameter1;
    }

    public synchronized void setPidParameter1(PidParameter pidParameter1) {
        this.pidParameter1 = pidParameter1;
    }

    public synchronized PidParameter getPidParameter2() {
        return pidParameter2;
    }

    public synchronized void setPidParameter2(PidParameter pidParameter2) {
        this.pidParameter2 = pidParameter2;
    }
}
