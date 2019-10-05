package data;

public class Circle extends Data{

    private int[] location;

    public Circle(int[] location, boolean initialFlag) {
        super(DataType.CIRCLE, initialFlag);
        this.location = location;
    }

    public synchronized void setLocation(int[] location) {
        this.location = location;
    }

    public synchronized int[] getLocation() {
        return this.location;
    }
}
