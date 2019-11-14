package data;

public class GripperControl extends Data {
    boolean command; // if false open, if true close


    public GripperControl(boolean command) {
        this.command = command;
    }

    public boolean isCommand() {
        return command;
    }
}
