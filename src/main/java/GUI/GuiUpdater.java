package GUI;

import communication.TCPClient;
import data.ConsoleOutput;
import data.Data;
import data.RegulatorOutput;
import data.Topic;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;
import java.util.LinkedList;
import java.util.List;

public class GuiUpdater extends Subscriber implements Runnable {
    ObjectProperty<TextField> textFieldObjectProperty;
    TextField xPos;
    TextField distance;
    TextArea conMessage;
    TCPClient tcpClient;

    double leftMotor;
    double rightMotor;
    String key;
    String message;

    List<String> list;

    public GuiUpdater(ObjectProperty<TextField> textFieldObjectProperty, TextField xPos, TextField distance, TextArea conMessage, TCPClient tcpClient){
        super(SharedResource.getInstance().getBroker());
        this.textFieldObjectProperty = textFieldObjectProperty;
        this.xPos = xPos;
        this.distance = distance;
        this.conMessage = conMessage;
        this.tcpClient = tcpClient;
        this.leftMotor = 0;
        this.rightMotor = 0;
        this.list = new LinkedList<>();
        this.getBroker().subscribeTo(Topic.CONSOLE_OUTPUT, this);
        this.getBroker().subscribeTo(Topic.DEBUG_OUTPUT, this);
    }

    @Override
    public void run() {
        if (tcpClient.isConnected()) {
            readMessages();
        try {
                xPos.setText(parseToString(leftMotor));
                distance.setText(parseToString(rightMotor));
                //conMessage.appendText("\n" + ());
            }
        catch(NullPointerException e){}
        }
    }

    @Override
    public void doReadMessages(){
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            String topic = message.getTopic();
            Data data = message.getData();

            switch (topic) {
                case Topic.REGULATOR_OUTPUT:
                    RegulatorOutput regulatorOutput = data.safeCast(RegulatorOutput.class);
                    if (regulatorOutput != null) {
                        leftMotor = regulatorOutput.getLeftMotor();
                        rightMotor = regulatorOutput.getRightMotor();
                    }
                    break;

                case Topic.CONSOLE_OUTPUT:
                    ConsoleOutput consoleOutput = data.safeCast(ConsoleOutput.class);
                    if(consoleOutput != null){
                        list.add(consoleOutput.getString());
                    }
                default:
                    break;
            }
        }
    }
    private String parseToString(double d){
        String s = Double.toString(d);
        return s;
    }
}
