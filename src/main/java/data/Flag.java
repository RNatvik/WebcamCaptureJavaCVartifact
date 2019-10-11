package data;

/**
 * A simple flag for notification across threads
 */
public class Flag {

    private boolean flag;

    /**
     * Constructs a new Flag
     * @param initialValue the flag's initial value
     */
    public Flag(boolean initialValue) {
        this.flag = initialValue;
    }

    /**
     * Returns the flag value
     * @return the flag value
     */
    public synchronized boolean get() {
        return this.flag;
    }

    /**
     * Sets the flag value
     * @param newValue new flag value
     */
    public synchronized void set(boolean newValue) {
        this.flag = newValue;
    }
}
