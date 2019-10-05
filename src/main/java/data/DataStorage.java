package data;

public class DataStorage {

    private Image imageToGUI;
    private Circle circle;
    private PidParameter pidParameter1;
    private PidParameter pidParameter2;

    public DataStorage(Image imageToGUI, Circle circle, PidParameter pidParameter1, PidParameter pidParameter2) {
        this.imageToGUI = imageToGUI;
        this.circle = circle;
        this.pidParameter1 = pidParameter1;
        this.pidParameter2 = pidParameter2;
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

    public synchronized String getData() {
        return "This is a string for testing communication and shit.";
    }
}
