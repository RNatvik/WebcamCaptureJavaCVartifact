package data;

public abstract class Data {
    public static final String PID_PARAM = "PID";
    public static final String CIRCLE = "CIRCLE";
    public static final String IMAGE = "IMAGE";

    private String type;

    public Data(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public <T> T safeCast(Class<T> clazz) {
        return clazz != null && clazz.isInstance(this) ? clazz.cast(this) : null;
    }

}
