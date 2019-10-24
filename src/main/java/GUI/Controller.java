package GUI;

import communication.UDPClient;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pub_sub_service.Subscriber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller extends Subscriber implements Initializable {

    private String mode;
    private UDPClient udpClient;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();

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

    public Controller() {
        super(SharedResource.getInstance().getBroker());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.udpClient = SharedResource.getInstance().getUdpClient();
            this.settingsController = new SettingsController();
            File file = new File("/loadpic.png");
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            modeText.setText("Select Mode");
            updateImages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void helpBtnPressed() {

    }

    public void openSettingsWindow() {
        settingsController.openSettingsWindow();
    }

    public void updateImages() {
//       Task<Void> task = new Task<>() {
//            @Override
//            protected Void call() throws Exception {
//                while (true) {
//                    BufferedImage im = udpClient.getImage();
//                    System.out.println(im);
//                    if (im !=null) {
//                        Platform.runLater(() -> {
//                            final Image mainiamge = SwingFXUtils
//                                    .toFXImage(im, null);
//                            imageProperty.set(mainiamge);
//                        });
//                    }
//                }
//            }
//        };
//        Thread thread = new Thread(task);
//        thread.setDaemon(false);
//        thread.start();
//        imageView.imageProperty().bind(imageProperty);
        BufferedImage image = null;
        while (image == null) {
            image = this.udpClient.getImage();
        }
        Image im = SwingFXUtils.toFXImage(image, null);
        this.imageProperty.set(im);
        imageView.imageProperty().bind(this.imageProperty);
//        Platform.runLater(() -> {
//            this.imageProperty.set(im);
//            imageView.imageProperty().bind(this.imageProperty);
//        });
    }

    public void setModeBtnPressed() {
        if (mode != null) {
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


    @Override
    protected void readMessages() {

    }
}