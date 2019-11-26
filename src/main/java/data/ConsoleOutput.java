package data;

/**
 * A class for sending console messages
 */
public class ConsoleOutput extends Data {

    private String string;

    /**
     * Constructor
     *
     * @param string the message
     */
    public ConsoleOutput(String string) {
        this.string = string;
    }

    /**
     * Gets the message
     *
     * @return the message
     */
    public String getString() {
        return string;
    }
}
