import communication.TCPClient;
import communication.UDPClient;
import data.ImageProcessorParameter;
import data.Topic;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;
import pub_sub_service.Broker;
import pub_sub_service.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMain {

    public static void main(String[] args) {
        Loader.load(opencv_java.class);
        try {
            Broker broker = new Broker();
            TCPClient tcpClient = new TCPClient("127.0.0.1", 9876, broker);

            UDPClient udpClient = new UDPClient("127.0.0.1", 2345);

            ImageProcessorParameter parameters = new ImageProcessorParameter(
                    79, 125, 94, 255, 125, 255, false
            );
            Message message = new Message(Topic.IMPROC_PARAM, parameters);
            tcpClient.setOutputMessage("SET", message.toJSON());

            udpClient.startThread();
            tcpClient.initialize();
            boolean state = false;
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                parameters = new ImageProcessorParameter(
                        52, 98, 0, 204, 52, 208, state
                );

                message = new Message(Topic.IMPROC_PARAM, parameters);
                tcpClient.setOutputMessage("SET", message.toJSON());
                state = !state;
            }

            udpClient.stop();
            tcpClient.stopConnection();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
