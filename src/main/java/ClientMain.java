import communication.TCPClient;
import communication.UDPClient;
import data.ImageProcessorParameter;
import data.Topic;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import pub_sub_service.Message;

import java.net.InetAddress;

public class ClientMain {

    public static void main(String[] args) {
        Loader.load(opencv_java.class);
        TCPClient tcpClient = new TCPClient(InetAddress.getLoopbackAddress(), 1234);
        UDPClient udpClient = new UDPClient(InetAddress.getLoopbackAddress(), 2345);

        ImageProcessorParameter parameters = new ImageProcessorParameter(
                79, 125, 94, 255, 125, 255, false
        );
        Message message = new Message(Topic.IMPROC_PARAM, parameters);
        tcpClient.setOutputMessage("SET", message.toJSON());

        udpClient.startThread();
        tcpClient.startThread();
        boolean state = false;
        for (int i = 0; i < 15; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            parameters = new ImageProcessorParameter(
                    92, 113, 190, 255, 106, 204, state
            );

            message = new Message(Topic.IMPROC_PARAM, parameters);
            tcpClient.setOutputMessage("SET", message.toJSON());
            state = !state;
        }

        udpClient.stop();
        tcpClient.stop();
    }
}