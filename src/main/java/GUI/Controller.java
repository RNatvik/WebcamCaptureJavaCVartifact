package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.ControlInput;
import data.Data;
import data.RegulatorOutput;
import data.Topic;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;
//import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller extends Subscriber implements Initializable {

    private String mode;
    private TCPClient tcpClient;
    private UDPClient udpClient;
    private SettingsController settingsController;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private ObjectProperty<TextField> textFieldObjectProperty = new SimpleObjectProperty<TextField>();
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
     * The constructor of the Controller class.
     */
    public Controller() {
        super(SharedResource.getInstance().getBroker());
    }

    /**
     * Initialize the UDP-client, TCP-client, SettingsController, KeyboardInput and ImageUpdater.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            if(SharedResource.isInitialized()) {
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
            this.ses.scheduleAtFixedRate(this.guiUpdater, 0, 50, TimeUnit.MILLISECONDS);

            conMessage.setText("Message Window:");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tracingBtnPressed() {
        ControlInput ci = new ControlInput(false,0,0);
        Message message = new Message(Topic.CONTROLER_INPUT, ci);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
        mode = "Tracking";
        modeText.setText(mode);
    }

    public void catchingBtnPressed() {
        mode = "Catching";
        modeText.setText(mode);
    }

    public void manualBtnPressed() {
        ControlInput ci = new ControlInput(true,0,0);
        Message message = new Message(Topic.CONTROLER_INPUT, ci);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
        mode = "Manual";
        modeText.setText(mode);
    }

    public void helpBtnPressed(){
        String old = conMessage.getText() + "\n";
        conMessage.setText(old + "Dette er en test");

    }

    public void openSettingsWindow() {
        settingsController.openSettingsWindow();
    }

    public void ClearConWindow(){
        conMessage.clear();
    }

    public void onKeyPressed(KeyEvent keyEvent) {
        if (mode.equals("Manual")) {
            String keysChanged =  this.keyboardInput.doHandleKeyEvent(keyEvent);
            if (keysChanged != null){
                ControlInput ci = this.keyboardInput.getControlInput(keysChanged);
                Message message = new Message(Topic.CONTROLER_INPUT, ci);
                this.tcpClient.setOutputMessage("SET", message.toJSON());

            }
        }
    }

    public void onKeyReleased(KeyEvent keyEvent) {
        if (mode.equals("Manual")) {
            String keysChanged =  this.keyboardInput.doHandleKeyEvent(keyEvent);
            if (keysChanged != null){
                ControlInput ci = this.keyboardInput.getControlInput(keysChanged);
                Message message = new Message(Topic.CONTROLER_INPUT, ci);
                this.tcpClient.setOutputMessage("SET", message.toJSON());

            }
        }
    }

    public void test() {
        System.out.println("In test");
        this.ses.shutdown();
        try {
            this.ses.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.ses.shutdownNow();
    }

    @Override
    protected void doReadMessages() {    }
}