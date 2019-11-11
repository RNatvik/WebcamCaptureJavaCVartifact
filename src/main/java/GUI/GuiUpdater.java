package GUI;

import communication.UDPClient;
import data.ConsoleOutput;
import data.Data;
import data.RegulatorOutput;
import data.Topic;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;
import java.awt.image.BufferedImage;

public class GuiUpdater extends Subscriber implements Runnable {
    ObjectProperty<Image> imageProperty;
    ImageView imageView;
    TextField xPos;
    TextField distance;
    TextArea conMessage;
    UDPClient udpClient;
    TextField leftMotor;
    TextField rightMotor;
    CheckBox debugCheckWindow;

    public GuiUpdater(ObjectProperty<Image> imageProperty, ImageView imageView, TextField xPos, TextField distance,
                      TextField leftMotor, TextField rightMotor, TextArea conMessage, CheckBox debugCheckWindow, UDPClient udpClient) {
        super(SharedResource.getInstance().getBroker());
        this.imageProperty = imageProperty;
        this.imageView = imageView;
        this.xPos = xPos;
        this.distance = distance;
        this.conMessage = conMessage;
        this.debugCheckWindow = debugCheckWindow;
        this.udpClient = udpClient;
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.getBroker().subscribeTo(Topic.REGULATOR_OUTPUT, this);
        this.getBroker().subscribeTo(Topic.CONSOLE_OUTPUT, this);
        this.getBroker().subscribeTo(Topic.IMAGE_DATA, this);
    }

    @Override
    public void run() {
        BufferedImage image = null;
        while (image == null) {
            image = this.udpClient.getImage();
        }
        Image im = SwingFXUtils.toFXImage(image, null);
        this.imageProperty.set(im);
        imageView.imageProperty().bind(this.imageProperty);
        readMessages();
        isDebugged();
    }

    @Override
    public void doReadMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            String topic = message.getTopic();
            Data data = message.getData();

            switch (topic) {
                case Topic.REGULATOR_OUTPUT:
                    RegulatorOutput regulatorOutput = data.safeCast(RegulatorOutput.class);
                    if (regulatorOutput != null) {
                        leftMotor.setText(parseToString(regulatorOutput.getLeftMotor()));
                        rightMotor.setText(parseToString(regulatorOutput.getRightMotor()));
                    }
                    break;

                case Topic.CONSOLE_OUTPUT:
                    ConsoleOutput consoleOutput = data.safeCast(ConsoleOutput.class);
                    if (consoleOutput != null) {
                        conMessage.appendText(consoleOutput.getString());
                    }
                    break;
                case Topic.DEBUG_OUTPUT:
                    ConsoleOutput debugMessage = data.safeCast(ConsoleOutput.class);
                    if(debugMessage != null){
                        conMessage.appendText(debugMessage.getString());
                    }
                case Topic.IMAGE_DATA:

                default:
                    break;
            }
        }
    }

    public boolean isDebugged(){
        boolean debug = false;
        if(debugCheckWindow.isSelected()){
            debug = true;
            this.getBroker().subscribeTo(Topic.DEBUG_OUTPUT, this);
        }
        else{
            this.getBroker().unsubscribeFrom(Topic.DEBUG_OUTPUT, this);
        }
        return debug;
    }

    private String parseToString(double d) {
        String s = Double.toString(d);
        return s;
    }
}
