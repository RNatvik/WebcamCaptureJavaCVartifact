package GUI;

import communication.UDPClient;
import data.*;
import image_processing.ImageProcessor;
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
import java.util.Arrays;

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
        this.getBroker().subscribeTo(Topic.IMPROC_DATA, this);
    }

    @Override
    public void run() {
        readMessages();
        BufferedImage image = this.udpClient.getImage();

        if (image != null) {
            Image im = SwingFXUtils.toFXImage(image, null);
            this.imageProperty.set(im);
            imageView.imageProperty().bind(this.imageProperty);
        }
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
                        conMessage.appendText("\n" + consoleOutput.getString());
                    }
                    break;
                case Topic.DEBUG_OUTPUT:
                    ConsoleOutput debugMessage = data.safeCast(ConsoleOutput.class);
                    if(debugMessage != null){
                        conMessage.appendText("\n" + debugMessage.getString());
                    }
                case Topic.IMPROC_DATA:
                    ImageProcessorData imageProcessorData = data.safeCast(ImageProcessorData.class);
                    if(imageProcessorData != null){
                        distance.setText(parseToString(Math.round(imageProcessorData.getLocation()[2])));
                        xPos.setText(parseToString(Math.round(imageProcessorData.getLocation()[0])));
                    }
                default:
                    break;
            }
        }
    }

    public boolean isDebugged(){
        boolean debug;
        if(debugCheckWindow.isSelected()){
            this.getBroker().subscribeTo(Topic.DEBUG_OUTPUT, this);
            debug = true;
        }
        else{
            this.getBroker().unsubscribeFrom(Topic.DEBUG_OUTPUT, this);
            debug = false;
        }
        return debug;
    }

    private String parseToString(double d) {
        String s = Double.toString(d);
        return s;
    }
}
