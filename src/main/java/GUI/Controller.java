package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.ControlInput;
import data.Topic;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller extends Subscriber implements Initializable {

    private String mode;
    private TCPClient tcpClient;
    private UDPClient udpClient;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private ImageUpdater imageUpdater;
    private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    private KeyboardInput keyboardInput;

    @FXML
    private Button manuelBtn;
    @FXML
    private Button catchingBtn;
    @FXML
    private Button trackingBtn;
    @FXML
    public Label modeText;
    @FXML
    public ImageView imageView;
    @FXML
    public Button settingsButton;
    @FXML
    public Button helpBtn;

    SettingsController settingsController;

    public Controller() {
        super(SharedResource.getInstance().getBroker());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.udpClient = SharedResource.getInstance().getUdpClient();
            this.tcpClient = SharedResource.getInstance().getTcpClient();
            this.settingsController = new SettingsController();
            settingsController.startSettingsWindow();
            File file = new File("/loadpic.png");
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            mode = "Manual";
            modeText.setText(mode);
            keyboardInput = new KeyboardInput();
            this.imageUpdater = new ImageUpdater(this.imageProperty, this.imageView, this.udpClient);
            ses.scheduleAtFixedRate(this.imageUpdater, 0, 50, TimeUnit.MILLISECONDS);
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

    public void helpBtnPressed() {

    }

    public void openSettingsWindow() {
        settingsController.openSettingsWindow();
    }

    @Override
    protected void doReadMessages() {

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
}