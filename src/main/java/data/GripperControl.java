package data;

/**
 * Data class for controlling the gripper
 */
public class GripperControl extends Data {
    boolean command; // if false open, if true close

    /**
     * Constructor
     *
     * @param command true to close, false to open
     */
    public GripperControl(boolean command) {
        this.command = command;
    }

    /**
     * Gets the command
     *
     * @return the command
     */
    public boolean isCommand() {
        return command;
    }
}
