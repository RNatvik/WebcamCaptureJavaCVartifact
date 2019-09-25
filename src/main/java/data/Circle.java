package data;

public class Circle extends Data{

    private int[] location;

    public Circle(int[] location) {
        super(DataType.CIRCLE);
        this.location = location;
    }

    public int[] getLocation() {
        return this.location;
    }
}
