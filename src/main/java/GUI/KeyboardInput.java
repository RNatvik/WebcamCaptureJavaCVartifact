package GUI;
import data.ControlInput;
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
            this.activeKeys = this.activeKeys+keyName;
            System.out.println(this.activeKeys);
            keys = this.activeKeys;
        }
        else if (keyEventType.equals("KEY_RELEASED") && this.activeKeys.contains(keyName) && this.allowedKeys.contains(keyName)){
            this.activeKeys = removeKey(keyName); //this.activeKeys.replace(keyName,"");
            keys = this.activeKeys;

        }

        return keys;
    }


    public ControlInput getControlInput(String keys) {
        double forwardSpeed = 0;
        double turnSpeed = 0;

        while (!keys.isEmpty()){
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
            if (keys.contains("Q")){
                // Nothing yet, wait for gripper imp
                keys = removeKey(keys,"Q");
            }
            if (keys.contains("E")){
                // Nothing yet, wait for gripper imp
                keys = removeKey(keys,"E");
            }
        }

        return new ControlInput(true,forwardSpeed,turnSpeed);
    }


    private String removeKey(String keys) {
        return this.activeKeys.replace(keys,"");
    }

    private String removeKey(String keys,String keyToRemove) {
        return keys.replace(keyToRemove,"");
    }
}