package data;

public abstract class Data {
    public static final String PID_PARAM = "PID";
    public static final String CIRCLE = "CIRCLE";
    public static final String IMAGE = "IMAGE";

    private Flag flag;
    private String type;

    public Data(boolean initialFlag, String type) {
        this.flag = new Flag(initialFlag);
        this.type = type;
    }

    public boolean getFlag() {
        return this.flag.get();
    }

    public void setFlag(boolean state) {
        this.flag.set(state);
    }

    public String getType() {
        return type;
    }

    public <T> T safeCast(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

}
