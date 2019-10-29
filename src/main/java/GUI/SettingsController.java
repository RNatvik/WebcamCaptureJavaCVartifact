package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;

public class SettingsController extends Subscriber implements Initializable {

    private Stage primaryStage1;
    private PidParameter pidParameter;
    private UDPClient udpClient;
    private TCPClient tcpClient;

    @FXML
    private Slider hueMax;
    @FXML
    private Slider hueMin;
    @FXML
    private Slider satMax;
    @FXML
    private Slider satMin;
    @FXML
    private Slider valMax;
    @FXML
    private Slider valMin;
    @FXML
    private TextField UDPport;
    @FXML
    private TextField TCPport;
    @FXML
    private TextField adrOne;
    @FXML
    private TextField adrTwo;
    @FXML
    private TextField adrThree;
    @FXML
    private TextField adrFour;
    @FXML
    private TextField propGainOne;
    @FXML
    private TextField intGainOne;
    @FXML
    private TextField derGainOne;
    @FXML
    private TextField contrMaxOutOne;
    @FXML
    private TextField contrMinOutOne;
    @FXML
    private TextField contrSetPointOne;
    @FXML
    private TextField propGainTwo;
    @FXML
    private TextField intGainTwo;
    @FXML
    private TextField derGainTwo;
    @FXML
    private TextField contrMaxOutTwo;
    @FXML
    private TextField contrMinOutTwo;
    @FXML
    private TextField contrSetPointTwo;
    @FXML
    private Button controllerApply;
    @FXML
    private Button connectButton;
    @FXML
    private CheckBox imProVideo;


    public SettingsController() {
        super(SharedResource.getInstance().getBroker());

    }

    public void initialize(URL location, ResourceBundle resources) {
        if (SharedResource.isInitialized()) {
            this.tcpClient = SharedResource.getInstance().getTcpClient();
            this.udpClient = SharedResource.getInstance().getUdpClient();
        }
        //this.tcpClient.setOutputMessage("SUB", Topic.REGULATOR_OUTPUT);

    }

    /**
     * When settings is pressed, its opens a new window that contains :
     * Controller parameters, Communication, Image parameters and regulator parameters.
     */
    public void startSettingsWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SettingsWindow.fxml"));
            Parent root1 = fxmlLoader.load();
            Scene scene = new Scene(root1);
            this.primaryStage1 = new Stage();
            this.primaryStage1.setScene(scene);
            this.primaryStage1.setTitle("camcoa");
            // Set On Close Request so that the application does not shot down when closing the extra window
            this.primaryStage1.setOnCloseRequest(event -> this.primaryStage1.close());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openSettingsWindow() {
        this.primaryStage1.show();
        this.primaryStage1.toFront();
    }

    public void controllerForwardApplyPressed() {
        doSendPidParameter(1);
    }
    public void controllerTurningApplyPressed() {
        doSendPidParameter(2);
    }

    public void applyPicPar() {
        doSendImageProcessorParameter();
    }

    public void connectButtonUDPClicked(){

    }
    public void connectButtonTCPClicked() {
        try {
            this.tcpClient.setHost(getIpAdr(), getTCPport());
            // also set udp client
            if (!this.tcpClient.isConnected()) {
                this.tcpClient.initialize();
                this.udpClient.startThread();
            } else {
                this.tcpClient.stopConnection();
                this.udpClient.stop();
                while (!(this.tcpClient.isTerminated() && this.udpClient.isTerminated())) {
                    // wait
                }
                this.tcpClient.initialize();
                this.udpClient.startThread();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doSendPidParameter(int paramNum) {
        double kp = parseToDouble(getPropGainFX(paramNum));
        double ki = parseToDouble(getIntGainFX(paramNum));
        double kd = parseToDouble(getDerGainFX(paramNum));
        double maxOutput = parseToDouble(getControllerMaxOut(paramNum));
        double minOutput = parseToDouble(getControllerMinOut(paramNum));
        double setpoint = parseToDouble(getControllerSetPoint(paramNum));

        PidParameter param = new PidParameter(kp, ki, kd, maxOutput, minOutput, setpoint);
        Message message = null;
        if (paramNum == 1) {
            message = new Message(Topic.PID_PARAM1, param);
        } else if (paramNum == 2) {
            message = new Message(Topic.PID_PARAM2, param);
        }
        this.tcpClient.setOutputMessage("SET", message.toJSON());
    }

    private void doSendImageProcessorParameter() {
        int[] hue = compareMinMax(hueMin.getValue(), hueMax.getValue());
        int[] sat = compareMinMax(satMin.getValue(), satMax.getValue());
        int[] val = compareMinMax(valMin.getValue(), valMax.getValue());
        updateSliders(hue[0], hue[1], sat[0], sat[1], val[0], val[1]);
        boolean imageToStore = getVideoOpt();

        ImageProcessorParameter param = new ImageProcessorParameter(
                hue[0], hue[1], sat[0], sat[1], val[0], val[1], imageToStore
        );
        Message message = new Message(Topic.IMPROC_PARAM, param);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
        System.out.println(message.toJSON());
    }


    private String getPropGainFX(int paramNum) {
        String propGain = "0";
        if (paramNum == 1) {
            propGain = propGainOne.getText();
        } if (paramNum == 2) {
            propGain = propGainTwo.getText();
        }
        if (propGain.isEmpty() && !isNumeric(propGain)) {
            propGain = "0";
        }
        return propGain;
    }

    private String getIntGainFX(int paramNum) {
        String stringIntGain = "0";
        if (paramNum == 1) {
            stringIntGain = intGainOne.getText();
        } if (paramNum == 2) {
            stringIntGain = intGainTwo.getText();
        }
        if (stringIntGain.isEmpty() && !isNumeric(stringIntGain)) {
            stringIntGain = "0";
        }
        return stringIntGain;
    }

    private String getDerGainFX(int paramNum) {
        String stringDerGain = "null";
        if (paramNum == 1) {
            stringDerGain = derGainOne.getText();
        } else if (paramNum == 2) {
            stringDerGain = derGainTwo.getText();
        }
        if (stringDerGain.isEmpty() && !isNumeric(stringDerGain)) {
            stringDerGain = "0";
        }
        return stringDerGain;
    }

    private String getControllerMaxOut(int paramNum) {
        String maxOut = "0";
        if (paramNum == 1) {
            maxOut = contrMaxOutOne.getText();
        } else if (paramNum == 2) {
            maxOut = contrMaxOutTwo.getText();
        }
        if (maxOut.isEmpty() && !isNumeric(maxOut)) {
            maxOut = "0";
        }
        return maxOut;
    }

    private String getControllerMinOut(int paramNum) {
        String minOut = "0";
        if (paramNum == 1) {
            minOut = contrMinOutOne.getText();
        } else if (paramNum == 2) {
            minOut = contrMinOutTwo.getText();
        }
        if (minOut.isEmpty() && !isNumeric(minOut)) {
            minOut = "0";
        }
        return minOut;
    }

    /**
     * Reads the text field from GUI/SettingsWindow tab->Controller parameters? and checks if they are
     * numerical.
     *
     * @param paramNum Choose if you want parameter one or two.
     * @return The set point.
     */
    private String getControllerSetPoint(int paramNum) {
        String setPoint = "0";
        if (paramNum == 1) {
            setPoint = contrSetPointOne.getText();
        } else if (paramNum == 2) {
            setPoint = contrSetPointTwo.getText();
        }
        if (setPoint.isEmpty() && !isNumeric(setPoint)) {
            setPoint = "0";
        }
        return setPoint;
    }

    private int[] compareMinMax(double hMin, double hMax) {
        hMax = Math.max(hMax, hMin);
        hMin = Math.min(hMax, hMin);
        return new int[]{(int) hMin, (int) hMax};
    }

    private void updateSliders(int hueMinPar, int hueMaxPar, int satMinPar, int satMaxPar, int valMinPar, int valMaxPar) {
        hueMin.setValue(hueMinPar);
        hueMax.setValue(hueMaxPar);
        satMin.setValue(satMinPar);
        satMax.setValue(satMaxPar);
        valMin.setValue(valMinPar);
        valMax.setValue(valMaxPar);
    }

    /**
     * Checking check-boxes in GUI/SettingsWindow tab->Picture, default is set to normal video. If both checkboxes is chosen
     * when the method is called, its goes to normal video and unchecks image processed checkbox.
     *
     * @return True, if image processed video is chosen. False, if normal video, both or non is selected.
     */
    private boolean getVideoOpt() {
        boolean imageProcessed = false;
        if(imProVideo.isSelected()){
            imageProcessed = true;
        }
        return imageProcessed;
    }

    /**
     * Checks if string contains a number.
     *
     * @param str The string that's need to be checked.
     * @return true, if the string contains a number (can be decimal). False, if there is a character inside it.
     */
    private static boolean isNumeric(String str) {
        return str.matches("\\d+(\\.\\d+)?");  //match a number with optional decimal.
    }

    /**
     * Checks if string contains a whole number.
     *
     * @param str The string that's need to be checked.
     * @return true, if the string contains a whole number. False, if there is a character inside it.
     */
    private static boolean isWholeNum(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    /**
     * Checks if string is a number and is not null, then converts string to double.
     *
     * @param str The string you want to convert.
     * @return num. The double witch the string was converted to.
     */
    private double parseToDouble(String str) {
        double num = 0;
        if (isNumeric(str) && (str.isEmpty())) {
            num = Double.parseDouble(str);
        }
        return num;
    }

    /**
     * Gets the text fields from GUI/Settingswindow IP-address and adds it together as an IP-address format.
     * Its also checks if there are any characters in the input-strings.
     *
     * @return adr. The fixed IP-address.
     */
    private String getIpAdr() {
        String adr = "0";
        if (isWholeNum(adrOne.getText()) && isWholeNum(adrTwo.getText()) && isWholeNum(adrThree.getText()) && isWholeNum(adrFour.getText())) {
            if (!(adrOne.getText().isEmpty() && adrTwo.getText().isEmpty() && adrThree.getText().isEmpty() && adrFour.getText().isEmpty())) {
                adr = adrOne.getText() + "." + adrTwo.getText() + "." + adrThree.getText() + "." + adrFour.getText();
            }
        }
        return adr;
    }

    /**
     * Gets the text fields from GUI/Settingswindow UDP-port, checks if its empty or has characters.
     *
     * @return udpPort. A string that contains the portnumber to UDP-server.
     */
    private String getUDPport() {
        String udpPort = "0";
        if (isWholeNum(UDPport.getText()) && !UDPport.getText().isEmpty()) {
            udpPort = UDPport.getText();
        }
        return udpPort;
    }

    /**
     * Gets the text fields from GUI/Settingswindow TCP-port, checks if its empty or has characters.
     *
     * @return tcpPort. A string that contains the portnumber to TCP-server.
     */
    private int getTCPport() {
        String tcpPort = "0";
        if (isWholeNum(TCPport.getText()) && !TCPport.getText().isEmpty()) {
            tcpPort = TCPport.getText();
        }
        return Integer.parseInt(tcpPort);
    }

    public void setDefaultValuesGui() {
        propGainOne.setText("1");
        intGainOne.setText("1");
        derGainOne.setText("1");
        propGainTwo.setText("1");
        intGainTwo.setText("1");
        derGainTwo.setText("1");
        TCPport.setText("2345");
        UDPport.setText("2345");
        adrOne.setText("192");
        adrTwo.setText("168");
        adrThree.setText("0");
        adrThree.setText("50");
    }

    @Override
    protected void doReadMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            String topic = message.getTopic();
            Data data = message.getData();

            switch (topic) {
                case Topic.REGULATOR_OUTPUT:
                    RegulatorOutput regulatorOutput = data.safeCast(RegulatorOutput.class);
                    if (regulatorOutput != null) {
                        regulatorOutput.getLeftMotor();
                    }
                    break;

                default:
                    break;
            }
        }
    }
}