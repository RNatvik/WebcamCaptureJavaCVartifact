package GUI;

import communication.TCPClient;
import communication.UDPClient;
import data.*;
import javafx.fxml.Initializable;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsControllerV2 extends Subscriber implements Initializable {

    private TCPClient tcpClient;
    private UDPClient udpClient;

    public SettingsControllerV2() {
        super(SharedResource.getInstance().getBroker());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: initialize with default values
        if (SharedResource.isInitialized()) {
            this.tcpClient = SharedResource.getInstance().getTcpClient();
            this.udpClient = SharedResource.getInstance().getUdpClient();
        }
        this.tcpClient.setOutputMessage("SUB", Topic.REGULATOR_OUTPUT);
    }

    private void doSendPidParameter1() {
        ///////// Replace with gui input //////////
        double kp = 1;
        double ki = 1;
        double kd = 1;
        double maxOutput = 1;
        double minOutput = 1;
        double setpoint = 1;
        ///////////////////////////////////////////

        PidParameter param = new PidParameter(kp, ki, kd, maxOutput, minOutput, setpoint);
        Message message = new Message(Topic.PID_PARAM1, param);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
    }

    private void doSendPidParameter2() {
        ///////// Replace with gui input //////////
        double kp = 2;
        double ki = 2;
        double kd = 2;
        double maxOutput = 2;
        double minOutput = 2;
        double setpoint = 2;
        ///////////////////////////////////////////

        PidParameter param = new PidParameter(kp, ki, kd, maxOutput, minOutput, setpoint);
        Message message = new Message(Topic.PID_PARAM2, param);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
    }

    private void doSendImageProcessorParameter() {
        ///////// Replace with gui input //////////
        int hueMin = 1;
        int hueMax = 1;
        int satMin = 1;
        int satMax = 1;
        int valMin = 1;
        int valMax = 1;
        boolean imageToStore = false;
        ///////////////////////////////////////////

        ImageProcessorParameter param = new ImageProcessorParameter(
                hueMin, hueMax, satMin, satMax, valMin, valMax, imageToStore
        );
        Message message = new Message(Topic.IMPROC_PARAM, param);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
    }

    private void doSendRegulatorParameter() {
        ///////// Replace with gui input //////////
        double mcMinimumReverse = -20;
        double mcMaximumReverse = -100;
        double mcMinimumForward = 20;
        double mcMaximumForward = 100;
        double controllerMinOutput = -200;
        double controllerMaxOutput = 200;
        ///////////////////////////////////////////

        RegulatorParameter param = new RegulatorParameter(
                mcMinimumReverse, mcMaximumReverse, mcMinimumForward,
                mcMaximumForward, controllerMinOutput, controllerMaxOutput
        );
        Message message = new Message(Topic.REGULATOR_PARAM, param);
        this.tcpClient.setOutputMessage("SET", message.toJSON());
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
