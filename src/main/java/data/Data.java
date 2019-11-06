package data;

/**
 * Parent class for process data
 */
public abstract class Data {

    /**
     * Initialize new Data object
     */
    public Data() {

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
