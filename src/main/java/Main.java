import communication.UDPClient;
import communication.UDPServer;
import data.DataStorage;

import java.net.InetAddress;

public class Main {

    public static void main(String[] args) {
        int udpPort = 2345;

        DataStorage dataStorage = new DataStorage();
        ImageStorageBox imageStorageBox = new ImageStorageBox();
        Camera camera = new Camera(0, imageStorageBox);
        ImageProcessor imageProcessor = new ImageProcessor(imageStorageBox, dataStorage);
        UDPServer udpServer = new UDPServer(udpPort, dataStorage, true, 3);
        UDPClient udpClient = new UDPClient(InetAddress.getLoopbackAddress(), udpPort);

        camera.setup();
        imageProcessor.setup();

        try {
            Thread.sleep(5000);
            udpServer.startThread();
            Thread.sleep(2000);
            udpClient.startThread();
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
