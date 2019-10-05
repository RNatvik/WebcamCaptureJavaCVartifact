/**
 * A simple flag for notification across threads
 */
public class Flag {

    private boolean flag;

    public Flag(boolean initialValue) {
        this.flag = initialValue;
    }

    public synchronized boolean get() {
        return this.flag;
    }

    public synchronized void set(boolean newValue) {
        this.flag = newValue;
    }
}
