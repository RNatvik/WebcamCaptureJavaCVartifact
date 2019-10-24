package GUI;

import communication.UDPClient;
import data.PidParameter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pub_sub_service.Subscriber;

import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;

public class SettingsController extends Subscriber implements Initializable {


    private Stage primaryStage1;
    private PidParameter pidParameter;
    private UDPClient udpClient;

    @FXML
    private Button connectButton;
    @FXML
    private TextField portNumber;
    @FXML
    private TextField IPadr;
    @FXML
    private TextField propGainFX;
    @FXML
    private TextField intGainFX;
    @FXML
    private TextField derGainFX;
    @FXML
    private Button controllerApply;


    public SettingsController(){
        super(SharedResource.getInstance().getBroker());
        this.pidParameter = new PidParameter(1,1,1,1,1,1);

    }

    public void initialize(URL location, ResourceBundle resources){
        propGainFX.setText("1");
        intGainFX.setText("1");
        derGainFX.setText("1");
        portNumber.setText("2345");
        IPadr.setText("12345678");
    }

    /**
     * When settings is pressed, its opens a new window that contains :
     *
     */
    public void openSettingsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SettingsWindow.fxml"));
            Parent root1 = fxmlLoader.load();
            Scene scene = new Scene(root1);
            primaryStage1 = new Stage();
            primaryStage1.setScene(scene);
            primaryStage1.setTitle("camcoa");
            // Set On Close Request so that the application does not shot down when closing the extra window
            primaryStage1.setOnCloseRequest(event -> {
                primaryStage1.close();
            });
            primaryStage1.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void controllerApplyPressed(){
        pidParameter.setKp(parseToDouble(getPropGainFX()));
        pidParameter.setKi(parseToDouble(getIntGainFX()));
        pidParameter.setKd(parseToDouble(getDerGainFX()));
    }

    public void connectButtonClicked(){
        try{
            if(isWholeNum(getAddress()) && isWholeNum(getPort())){
                System.out.println("Port: **" + getPort());
                System.out.println("IP: **" + getAddress());
                //TODO: connect udpClient
                //this.udpClient.startThread(getAddress(), Integer.parseInt(getPort()));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getAddress(){
        String stringAddress = IPadr.getText();
        if(stringAddress.isEmpty()){
            stringAddress = "0";
        }

        return stringAddress;
    }

    private String getPort(){
        String port = portNumber.getText();
        if(port.isEmpty()){
            port = "0";
        }

        return port;
    }
    private String getPropGainFX(){
        String stringPropGain = propGainFX.getText();
        if(stringPropGain.isEmpty()) {
            stringPropGain = "0";
        }
        return stringPropGain;
    }
    private String getIntGainFX(){
        String stringIntGain = intGainFX.getText();
        if(stringIntGain.isEmpty()) {
            stringIntGain = "0";
        }
        return stringIntGain;
    }
    private String getDerGainFX(){
        String stringDerGain = derGainFX.getText();
        if(stringDerGain.isEmpty()) {
            stringDerGain = "0";
        }
        return stringDerGain;
    }

    public static boolean isNumeric(String str) {
        return str.matches("\\d+(\\.\\d+)?");  //match a number with optional decimal.
    }

    /**
     * Checks if string contains a whole number.
     * @param str The string that's need to be checked.
     * @return true, if the string contains a whole number. False, if there is a character inside it.
     */
    public static boolean isWholeNum(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    private double parseToDouble(String str){
        double num = 0;
        if(isNumeric(str) && (str != null)){
            num = Double.parseDouble(str);
        }
        return num;
    }

    @Override
    protected void readMessages() {

    }
}