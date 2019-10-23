package data;

/**
 * Parent class for process data
 */
public abstract class Data {
    public static final String PID_PARAM = "PID";
    public static final String REG_PARAM = "REG_PARAM";
    public static final String IMAGE = "IMAGE";
    public static final String IMPROC_PARAM = "IMPROC_PARAM";
    public static final String MOTOR_CONTROL_VALUES = "MCV";
    public static final String REGULATOR_OUTPUT = "regout";

    private String type;

    /**
     * Initialize new Data object
     *
     * @param type the Data type
     */
    public Data(String type) {
        this.type = type;
    }

    /**
     * Returns the data type
     *
     * @return the data type
     */
    public String getType() {
        return type;
    }

    /**
     * Attempts to cast the Data object to an instance of a subclass
     *
     * @param clazz the subclass to cast to
     * @return subclass specified by clazz param. Returns null if Data object is not to the specified class.
     */
    public <T> T safeCast(Class<T> clazz) {
        return clazz != null && clazz.isInstance(this) ? clazz.cast(this) : null;
    }

}
