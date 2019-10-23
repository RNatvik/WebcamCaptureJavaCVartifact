package GUI;

import javafx.scene.input.KeyEvent;

/**
 * Handles arrow key inputs from user, to manually control car. Can only use on key at the time, pressed key has to be
 * released before next key can be pressed.
 *
 * @author
 * @version
 */
public class KeyboardInput {
    // String name of active key.
    private String activeKey;
    // Strings allowed to register.
    private String allowedKey;

    /**
     * Initializes the class.
     */
    public KeyboardInput() {
        this.activeKey = "";
        this.allowedKey = "ASDW";
    }

    /**
     * Builds and returns string containing event type and what key is used.
     *
     * @param event Key event.
     */
    public String doHandleKeyEvent(KeyEvent event) {
        // Code of button event.
        String keyEvent = event.getEventType().toString().toUpperCase();
        // Event type(press of release).
        String keyName = event.getCode().toString().toUpperCase();
        // String to be returned.
        String string = "";

        // Activate a key if no other key is active.
        if (keyEvent.equals("KEY_PRESSED") && this.activeKey.equals("") && this.allowedKey.contains(keyName)) {
            this.activeKey = keyName;
            string = keyEvent + ":" + keyName;

            // Releases a key if the released key equals the last active key.
        } else if (keyEvent.equals("KEY_RELEASED") && keyName.equals(this.activeKey) && this.allowedKey.contains(keyName)) {
            this.activeKey = "";
            string = keyEvent + ":" + keyName;
        }

        return string;
    }
}