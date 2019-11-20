package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.ControlInput;
import data.GripperControl;
import data.Topic;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;


import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Controller class controls all the necessary objects in the GUI.fxml.
 * This class also do necessarily checks, comparisons, updates and conversions.
 *
 * @author Jarl Eirik Heide.
 * @version 1.0
 * @since 30.10.2019
 */

public class Controller extends Subscriber implements Initializable {

    private String mode;
    private TCPClient tcpClient;
    private UDPClient udpClient;
    private SettingsController settingsController;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    private KeyboardInput keyboardInput;
    private GuiUpdater guiUpdater;

    @FXML
    private Button manuelBtn;
    @FXML
    private Button catchingBtn;
    @FXML
    private Button trackingBtn;
    @FXML
    private CheckBox debugCheckWindow;
    @FXML
    public Label modeText;
    @FXML
    public TextField xPos;
    @FXML
    public TextField distance;
    @FXML
    public TextField leftMotor;
    @FXML
    public TextField rightMotor;
    @FXML
    public TextArea conMessage;
    @FXML
    public ImageView imageView;
    @FXML
    public Button settingsButton;
    @FXML
    public Button helpBtn;

    /**
     * The constructor of the Controller class, constructs a Shared Recourse of a broker.
     */
    public Controller() {
        super(SharedResource.getInstance().getBroker());
    }

    /**
     * Initialize the UDP-client, TCP-client, SettingsController, KeyboardInput and ImageUpdater.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if (SharedResource.isInitialized()) {
                this.udpClient = SharedResource.getInstance().getUdpClient();
                this.tcpClient = SharedResource.getInstance().getTcpClient();
            }
            this.settingsController = new SettingsController();
            this.settingsController.startSettingsWindow();
            this.mode = "Manual";
            this.modeText.setText(mode);
            this.keyboardInput = new KeyboardInput();
            this.guiUpdater = new GuiUpdater(this.imageProperty, this.imageView, this.xPos, this.distance,
                    this.leftMotor, this.rightMotor, this.conMessage, this.debugCheckWindow, this.udpClient);
            this.ses.scheduleAtFixedRate(() -> {
                Platform.runLater(this.guiUpdater);}, 0, 40, TimeUnit.MILLISECONDS);
            conMessage.setText("Message Window:");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * When Tracking button is pressed, this method gets called. The method disable keyboard input in the KeyBoardInput.
     */
    public void tracingBtnPressed() {
        ControlInput ci = new ControlInput(false, 0, 0);
        Message message = new Message(Topic.CONTROLLER_INPUT, ci);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
        mode = "Tracking";
        modeText.setText(mode);
    }

    /**
     * This method has no function, because of project "delays".
     */
    public void catchingBtnPressed() {
        mode = "Catching";
        modeText.setText(mode);
    }

    /**
     * When Manual Mode button is pressed, this method gets called. The method able keyboard input in the KeyBoardInput.
     */
    public void manualBtnPressed() {
        ControlInput ci = new ControlInput(true, 0, 0);
        Message message = new Message(Topic.CONTROLLER_INPUT, ci);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
        mode = "Manual";
        modeText.setText(mode);
    }

    /**
     * TODO: Write some help stuff things...
     */
    public void helpBtnPressed() { }

    /**
     * Sets the settingswindow primary stage to visible.
     */
    public void openSettingsWindow() {
        settingsController.openSettingsWindow();
    }

    /**
     * Clears all the console message TextArea.
     */
    public void ClearConWindow() {
        conMessage.clear();
    }

    /**
     * Handles the KeyEvent's that are fired when the GUI window is active
     * and a button is pressed
     *
     * @param keyEvent the KeyEvent to handle
     */
    public void onKeyPressed(KeyEvent keyEvent) {
            String keysChanged = this.keyboardInput.doHandleKeyEvent(keyEvent);
            if (keysChanged != null) {

                System.out.println(keysChanged);
                if (keysChanged.contains("W") || keysChanged.contains("A")|| keysChanged.contains("S")|| keysChanged.contains("D")) {
                    ControlInput ci = this.keyboardInput.getControlInput(keysChanged);
                    Message message1 = new Message(Topic.CONTROLLER_INPUT, ci);
                    this.tcpClient.setOutputMessage("SET", message1.toJSON());
                }
                else if (keysChanged.contains("Q") || keysChanged.contains("E")) {
                    GripperControl gc = this.keyboardInput.getGripperCommand(keysChanged);
                    Message message2 = new Message(Topic.GRIPPER, gc);
                    this.tcpClient.setOutputMessage("SET", message2.toJSON());
                }
            }
    }

    /**
     * Handles the KeyEvent's that are fired when the GUI window is active
     * and a button is released
     *
     * @param keyEvent the KeyEvent to handle
     */
    public void onKeyReleased(KeyEvent keyEvent) {
        String keysChanged = this.keyboardInput.doHandleKeyEvent(keyEvent);
        if (keysChanged != null){
            ControlInput ci = this.keyboardInput.getControlInput(keysChanged);
            this.keyboardInput.getGripperCommand(keysChanged); // Remove the released key, but we dont need to send new commands
            Message message = new Message(Topic.CONTROLLER_INPUT, ci);
            this.tcpClient.setOutputMessage("SET", message.toJSON());
        }

    }


    /**
     * Try's to stop the Scheduled Exicuter, then awaits for the Exicuter to terminate, and then shuts its down.
     */
    void safeStopSceduledExicuter() {
        System.out.println("In test");
        settingsController.safeStopSceduledExicuter();
        this.ses.shutdown();
        try {
            this.ses.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.ses.shutdownNow();
    }

    /**
     * Dummy method.
     */
    @Override
    protected void doReadMessages() {
    }
}