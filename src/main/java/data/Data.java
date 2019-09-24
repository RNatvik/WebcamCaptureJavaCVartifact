package data;

public abstract class Data {

    private DataType type;

    public Data(DataType type) {
        this.type = type;
    }

    public DataType getType() {
        return this.type;
    }
}
