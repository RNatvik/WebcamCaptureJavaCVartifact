package GUI;

import communication.UDPClient;
import data.*;
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

/**
 * This class represents all field in gui that is spouse to be updated based on the incoming data.
 * The class is executed by a periodic task via scheduledAtFixedRate, and the scheduler is created from a
 * ScheduledThreadPoolExecutor. Data is read at this fixed rate, and fields are updated. Streaming a series of pictures
 * sent form the udp-client to gui is also done in this class.
 *
 * @version 1.0
 * @since 20.11.2019
 */
public class GuiUpdater extends Subscriber implements Runnable {
    private ObjectProperty<Image> imageProperty;
    private ImageView imageView;
    private TextField xPos;
    private TextField distance;
    private TextArea conMessage;
    private UDPClient udpClient;
    private TextField leftMotor;
    private TextField rightMotor;
    private CheckBox debugCheckWindow;

    /**
     * The constructor of the class, it takes necessary parameters from GUI class.
     * @param imageProperty The property of the imageviewer.
     * @param imageView Imageviewer from gui controller.
     * @param xPos xPos from gui controller.
     * @param distance Distance from gui controller.
     * @param leftMotor leftMotor  from gui controller.
     * @param rightMotor rightMotor  from gui controller.
     * @param conMessage console message from gui controller.
     * @param debugCheckWindow debug check box  from gui controller.
     * @param udpClient udp client from gui controller.
     */
    GuiUpdater(ObjectProperty<Image> imageProperty, ImageView imageView, TextField xPos, TextField distance,
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

    /**
     * This method gets called an a fixed rate from a scheduled exciter. Its read the incoming messages and grabs a
     * Buffered image from udp-client. Buffered image is converted to an Image and is bind to an imageview. When debug
     * check box is true, its subscribes on debugdata, and opposite.
     */
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

    /**
     * Method for handling incoming messages.
     * While there are messages is the message que, it takes it out and makes it its own message. The message contains
     * a topic and some data. The method checks topics and sets the correct data to the gui.
     */
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

    /**
     * Checks if the debug checkbox is true or false.
     * When the checkbox is true, we choose to subscribe to DEBUG_OUTPUT; when false, unsubscribe.
     * @return true if checkbox is checked, false otherwise.
     */
    public void isDebugged(){
        if(debugCheckWindow.isSelected()){
            this.getBroker().subscribeTo(Topic.DEBUG_OUTPUT, this);
        }
        else{
            this.getBroker().unsubscribeFrom(Topic.DEBUG_OUTPUT, this);
        }
    }

    /**
     * Takes a variable of doubble, and parses it to a string. No need for checks in this method.
     * @param d The double variable you want to parse.
     * @return A string of the parameter.
     */
    private String parseToString(double d) {
        String s = Double.toString(d);
        return s;
    }
//    /**
//     * Checks if the console window contains more than hundred lines, and if it does, the l
//     */
//    private void constrainConWindow(){
//        if(conMessage.getText().split("\n", -1).length >= 100){
//            int fle = conMessage.getText().indexOf("\n");
//            conMessage.replaceText(0, fle+1, "");
//        }
//    }
}
