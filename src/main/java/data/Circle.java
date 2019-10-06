package data;

public class Circle extends JSONData{

    private int[] location;

    public Circle(int[] location, boolean initialFlag) {
        super(initialFlag, Data.CIRCLE);
        this.location = location;
    }

    public synchronized void setLocation(int[] location) {
        this.location = location;
    }

    public synchronized int[] getLocation() {
        return this.location;
    }
}
