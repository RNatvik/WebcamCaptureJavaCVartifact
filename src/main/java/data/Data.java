package data;

public abstract class Data {

    private DataType type;
    private Flag flag;

    public Data(DataType type, boolean initialFlag) {
        this.type = type;
        this.flag = new Flag(initialFlag);
    }

    public DataType getType() {
        return this.type;
    }

    public boolean getFlag() {
        return this.flag.get();
    }

    public void setFlag(boolean state) {
        this.flag.set(state);
    }
}
