package GUI;

import communication.UDPClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Controller extends Thread implements Initializable {

    String mode;
    private UDPClient udpClient;

    @FXML
    private Button manuelBtn;
    @FXML
    private Button catchingBtn;
    @FXML
    private Button trackingBtn;
    @FXML
    private Button setModeBtn;
    @FXML
    public Label modeText;
    @FXML
    public ImageView imageView;
    @FXML
    public Button settingsButton;
    @FXML
    public Button helpBtn;

    SettingsController settingsController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.udpClient = new UDPClient(InetAddress.getByName("192.168.0.50"), 2345);
            this.settingsController = new SettingsController(this.udpClient);
            this.udpClient.startThread();
            File file = new File("/loadpic.png");
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            modeText.setText("Select Mode");
            updateImages();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public void helpBtnPressed() {

    }
    public void openSettingsWindow() {
        settingsController.openSettingsWindow();
    }
    public void updateImages() {
//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() throws Exception {
//                while (true) {
//                    InputStream im = udpClient.getBufferedImage();
//                    if (im !=null) {
//                        Platform.runLater(() -> {
//                            Image image = new Image(im);
//                            imageView.setImage(image);
//                        });
//                    }
//                }
//            }
//        };
//        Thread thread = new Thread(task);
//        thread.start();
    }
    public void setModeBtnPressed(){
        if(mode != null) {
            modeText.setText(mode);
        }
    }
    public void trackingBtnPressed() {
        mode = "Tracking";
    }

    public void catchingBtnPressed() {
        mode = "Catching";
    }

    public void manualBtnPressed() {
        mode = "Manual";
    }


}