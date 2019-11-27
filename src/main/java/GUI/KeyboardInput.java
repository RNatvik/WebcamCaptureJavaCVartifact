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
     *  Handles the keyevent and returns a string with the active keys.
     *
     * @param event the Key event fired by the pressing a button when the GUI is active
     * @return the string containing the active keys on the keyboard
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
 
    /**
     * Creates a controlinput based on the active keys
     *
     * @param keys the keys to create the controlinput to return
     * @return the controlinput to control the speed of the motors based on the input
     */
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
 
    /**
     * Creates a GripperControl object based on the active keys
     * @param keys the active keys to create the GripperControl command
     * @return the GripperControl command based on the input
     */
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
        return new GripperControl(gripper);
    }
}
