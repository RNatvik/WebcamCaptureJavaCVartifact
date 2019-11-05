package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;

import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ResourceBundle;

/**
 * The SettingsController class controls all the necessary objects in the SettingsWindow.fxml.
 * This class also do necessarily checks, comparisons and conversions.
 *
 *
 * @author  Lars Berge, Jarl Eirik Heide, Ruben Natvik and Einar Samset.
 * @version 1.0
 * @since   30.10.2019
 */

public class SettingsController extends Subscriber implements Initializable {

    private Stage primaryStage1;
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
    private Button conUdpBtn;
    @FXML
    private Button conTcpBtn;
    @FXML
    private CheckBox imProVideo;


    public SettingsController() {
        super(SharedResource.getInstance().getBroker());
    }

    /**
     * Initialize the tcp and udp shared resource.
     * @param location
     * @param resources
     */
    public void initialize(URL location, ResourceBundle resources) {
        if (SharedResource.isInitialized()) {
            this.tcpClient = SharedResource.getInstance().getTcpClient();
            this.udpClient = SharedResource.getInstance().getUdpClient();
        }
    }

    /**
     * Method is called in initialize in GUI-Controller, creates the scene and its properties from SettingsWindow.fxml.
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

    /**
     * When Settings button is pressed in GUI, its calls this method that shows and brings the settings controller
     * window to the front.
     */
    public void openSettingsWindow() {
        this.primaryStage1.show();
        this.primaryStage1.toFront();
    }

    /**
     * When Apply Forward is pressed, it calls a send-method for the Pid-Parameters with parameter
     * number one (Forward PID parameters).
     */
    public void controllerForwardApplyPressed() {
        doSendPidParameter(1);
    }
    /**
     * When Apply Turning is pressed, it calls a send-method for the Pid-Parameters with parameter
     * number one (Turning PID parameters).
     */
    public void controllerTurningApplyPressed() {
        doSendPidParameter(2);
    }

    /**
     * Not functional yeet.
     */

    public void connectButtonUDPClicked() {
        try {
            if (!this.udpClient.isRunning()) {
                this.udpClient.initialize(getIpAdr(), getUDPport());
                boolean success = this.udpClient.start();
            } else {
                this.udpClient.stop();
                boolean terminated = false;
                while (!terminated) {
                    terminated = this.udpClient.isTerminated();
                }
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Not sure works yeet.
     */
    public void connectButtonTCPClicked() {
        try {
            if (!this.tcpClient.isConnected()) {
                this.tcpClient.initialize(getIpAdr(), getTCPport(), 20);
                boolean success = this.tcpClient.connect();

            } else {
                this.tcpClient.stopConnection();
                boolean terminated = this.tcpClient.isTerminated();
                System.out.println("Tcp terminated: " + terminated);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates all PID parameters and creates a Message. Then the message is given the correct Topic and command
     * Then its sends the modified message to the setOutputMessage in TcpClient.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     */
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
        System.out.println(message.toJSON());
        this.tcpClient.setOutputMessage("SET", message.toJSON());
    }
    /**
     * Creates all Image Processor parameters and creates a Message. Then the message is given the correct Topic
     * and command. Then its sends the modified message to the setOutputMessage in TcpClient.
     */
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

    /**
     * Reads the textfield from GUI/SettingsController tab-> Controller parameters, returns proportional Gain if its
     * numerical and sets its to zero if not.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     * @return The Proportional Gain.
     */
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

    /**
     * Reads the textfield from GUI/SettingsController tab-> Controller parameters, returns Integral Gain if its
     * numerical and sets its to zero if not.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     * @return The Integral Gain.
     */
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

    /**
     * Reads the textfield from GUI/SettingsController tab-> Controller parameters, returns Derivative Gain if its
     * numerical and sets its to zero if not.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     * @return The Derivative Gain.
     */
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

    /**
     * Reads the textfield from GUI/SettingsController tab-> Controller parameters, returns Maximum Output if its
     * numerical and sets its to zero if not.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     * @return The Maximum Output.
     */
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

    /**
     * Reads the textfield from GUI/SettingsController tab-> Controller parameters, returns Minimum Output if its
     * numerical and sets its to zero if not.
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
     * @return The Minimum Output.
     */
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
     * Reads the text field from GUI/SettingsWindow tab->Controller parameters, and checks if they are
     * numerical.
     *
     * @param paramNum where 1 is Forward parameter and 2 is Turning parameter.
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

    /**
     * Compare the values of the parameters. If hmax is smaller than hmin, then hman = hmin. If hmin is bigger than
     * hmax, then hmin = hmax. The method is used on the Sliders in GUI/settingsWindow tab-> Picture.
     * @param hMin The value that is suppose to be smallest.
     * @param hMax The value that is suppose to be biggest.
     * @return
     */
    private int[] compareMinMax(double hMin, double hMax) {
        hMax = Math.max(hMax, hMin);
        hMin = Math.min(hMax, hMin);
        return new int[]{(int) hMin, (int) hMax};
    }

    /**
     * Updates the slider in GUI/SettingsWindow -> Picture, if one of the Slider is set on a illegal value and is
     * being set to a legal one automatically.
     * @param hueMinPar The actual hueMin value.
     * @param hueMaxPar The actual hueMax value.
     * @param satMinPar The actual satMin value.
     * @param satMaxPar The actual satMax value.
     * @param valMinPar The actual valMin value.
     * @param valMaxPar The actual valMax value.
     */
    private void updateSliders(
            int hueMinPar, int hueMaxPar, int satMinPar, int satMaxPar, int valMinPar, int valMaxPar) {
        hueMin.setValue(hueMinPar);
        hueMax.setValue(hueMaxPar);
        satMin.setValue(satMinPar);
        satMax.setValue(satMaxPar);
        valMin.setValue(valMinPar);
        valMax.setValue(valMaxPar);
    }

    /**
     * Checking check-boxes in GUI/SettingsWindow tab->Picture, default is set to normal video. If both
     * checkboxes is chosen
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
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void hueMaxDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void hueMinDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void satMaxDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void satMinDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void valMaxDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Slider has been clicked and released in GUI/SettingsWindow tab-> Picture.
     */
    public void valMinDragClicked(){
        doSendImageProcessorParameter();
    }
    /**
     * Sends Image Process Parameters when the Check-box has been changed in GUI/SettingsWindow tab-> Picture..
     */
    public void imgProVidClicked(){
        doSendImageProcessorParameter();
    }

    /**
     * Checks if string contains a number.
     *
     * @param str The string that's need to be checked.
     * @return true, if the string contains a number (can be decimal). False, if there is a character inside it.
     */
    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional decimal.
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
        if (isNumeric(str)) {
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
        if (isWholeNum(adrOne.getText()) && isWholeNum(adrTwo.getText()) && isWholeNum(adrThree.getText())
                && isWholeNum(adrFour.getText())) {
            if (!(adrOne.getText().isEmpty() && adrTwo.getText().isEmpty() && adrThree.getText().isEmpty()
                    && adrFour.getText().isEmpty())) {
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
    private int getUDPport() {
        String udpPort = "0";
        if (isWholeNum(UDPport.getText()) && !UDPport.getText().isEmpty()) {
            udpPort = UDPport.getText();
        }
        return Integer.parseInt(udpPort);
    }

    /**
     * Gets the text fields from GUI/Settingswindow TCP-port, checks if its empty or has characters.
     *
     * @return tcpPort. A string that contains the port number to TCP-server.
     */
    private int getTCPport() {
        String tcpPort = "0";
        if (isWholeNum(TCPport.getText()) && !TCPport.getText().isEmpty()) {
            tcpPort = TCPport.getText();
        }
        return Integer.parseInt(tcpPort);
    }

    /**
     * Adds a default value to the hueMax parameter when button is pressed.
     */
    public void hueMaxAdd() {
        double val = hueMax.getValue();
        hueMax.setValue(val + 1);
        doSendImageProcessorParameter();
    }

    public void hueMinAdd() {
        double val = hueMin.getValue();
        hueMin.setValue(val + 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the satMax parameter when button is pressed.
     */
    public void satMaxAdd() {
        double val = satMax.getValue();
        satMax.setValue(val + 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the hueMax parameter when button is pressed.
     */
    public void satMinAdd() {
        double val = satMin.getValue();
        satMin.setValue(val + 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the valMax parameter when button is pressed.
     */
    public void valMaxAdd() {
        double val = valMax.getValue();
        valMax.setValue(val + 1);
        doSendImageProcessorParameter();
    }

    public void valMinAdd() {
        double val = valMin.getValue();
        valMin.setValue(val + 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the hueMax parameter when button is pressed.
     */
    public void hueMaxSub() {
        double val = hueMax.getValue();
        hueMax.setValue(val - 1);
        doSendImageProcessorParameter();
    }

    public void hueMinSub() {
        double val = hueMin.getValue();
        hueMin.setValue(val - 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the satMax parameter when button is pressed.
     */
    public void satMaxSub() {
        double val = satMax.getValue();
        satMax.setValue(val - 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the hueMax parameter when button is pressed.
     */
    public void satMinSub() {
        double val = satMin.getValue();
        satMin.setValue(val - 1);
        doSendImageProcessorParameter();
    }
    /**
     * Adds a default value to the valMax parameter when button is pressed.
     */
    public void valMaxSub() {
        double val = valMax.getValue();
        valMax.setValue(val - 1);
        doSendImageProcessorParameter();
    }

    public void valMinSub() {
        double val = valMin.getValue();
        valMin.setValue(val - 1);
        doSendImageProcessorParameter();
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