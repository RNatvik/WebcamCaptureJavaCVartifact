package GUI;

import communication.TCPClient;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;

public class GuiUpdater implements Runnable {
    ObjectProperty<TextField> textFieldObjectProperty;
    TextField xPos;
    TextField distance;
    TCPClient tcpClient;

    public GuiUpdater(ObjectProperty<TextField> textFieldObjectProperty, TextField xPos, TextField distance, TCPClient tcpClient){
        this.textFieldObjectProperty = textFieldObjectProperty;
        this.xPos = xPos;
        this.distance = distance;
        this.tcpClient = tcpClient;
    }

    @Override
    public void run() {
    xPos.setText("123");
    distance.setText("456");
    }
}
