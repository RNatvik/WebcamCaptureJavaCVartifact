package GUI;
import data.ControlInput;
import data.GripperControl;
import javafx.scene.input.KeyEvent;

/**
 * Handles input from keyboard to manually control the car.
 *
 * @author Lars Berge
 * @version 1.0
 */
public class KeyboardInput {
    // String name of active key.
    private String activeKeys;
    // Strings allowed to register.
    private String allowedKeys;

    /**
     * Initialises the class to handle the keyboardinputs
     */
    public KeyboardInput() {
        this.activeKeys = "";
        this.allowedKeys = "ASDWQE";
    }

    /**
     *
     * Returns Null if there is the KeyEvent is not form an allowed key or if the
     * @param event Key event.
     */
    public String doHandleKeyEvent(KeyEvent event) {

        String keys = null;

        String keyEventType = event.getEventType().toString().toUpperCase();

        String keyName = event.getCode().toString().toUpperCase();

        if (keyEventType.equals("KEY_PRESSED") && !this.activeKeys.contains(keyName) && this.allowedKeys.contains(keyName) ){
            this.activeKeys = this.activeKeys+keyName; //"::"+
            keys = this.activeKeys;
        }
        else if (keyEventType.equals("KEY_RELEASED") && this.activeKeys.contains(keyName) && this.allowedKeys.contains(keyName)){
            this.activeKeys = removeKey(this.activeKeys,keyName);
            keys = this.activeKeys;

        }

        return keys;
    }


    public ControlInput getControlInput(String keys) {
        double forwardSpeed = 0;
        double turnSpeed = 0;

        while (keys.contains("W") || keys.contains("A")|| keys.contains("S")|| keys.contains("D")){
            if (keys.contains("W")){
                forwardSpeed = forwardSpeed + 100;
                keys = removeKey(keys,"W");
            }
            if (keys.contains("S")){
                forwardSpeed = forwardSpeed - 100;
                keys = removeKey(keys,"S");
            }
            if (keys.contains("A")){
                turnSpeed = turnSpeed - 100;
                keys = removeKey(keys,"A");
            }
            if (keys.contains("D")){
                turnSpeed = turnSpeed + 100;
                keys = removeKey(keys,"D");
            }
        }
        return new ControlInput(true,forwardSpeed,turnSpeed);
    }

    private String removeKey(String keys,String keyToRemove) {
        return keys.replace(keyToRemove,"");
    }


    public GripperControl getGripperCommand(String keys) {
        boolean gripper = false;
        while (keys.contains("Q") || keys.contains("E")) {
            if (keys.contains("Q")) {
                gripper = true;
                keys = removeKey(keys, "Q");
            }
            if (keys.contains("E")) {
                gripper = false;
                keys = removeKey(keys, "E");
            }
        }
        System.out.println("Returning gripper");
        return new GripperControl(gripper);
    }
}