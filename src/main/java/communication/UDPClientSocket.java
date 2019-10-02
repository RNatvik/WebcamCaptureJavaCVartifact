package communication;

import data.DataStorage;
import data.Image;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.Frame;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class UDPClientSocket implements Runnable {

    private DatagramSocket serverSocket;
    private DataStorage dataStorage;
    private InetAddress clientAddress;
    private int clientPort;

    public UDPClientSocket(DatagramSocket serverSocket, InetAddress address, int port, DataStorage dataStorage) {
        this.serverSocket = serverSocket;
        this.dataStorage = dataStorage;
        this.clientAddress = address;
        this.clientPort = port;
    }

    @Override
    public void run() {
        System.out.println("UDPClientSocket:: In run");
        boolean shutdown = false;
        int bufferSize = 32768;
        try {
            while (!shutdown) {
                System.out.println("UDPClientSocket:: Collecting JSON image...");
                Image image = this.dataStorage.getImageToGUI();
                JSONObject data = this.getImageJSON(image);
                System.out.println("UDPClientSocket:: Collected JSON image");
                int i = 0;
                byte packetNum = 0;
                byte[] buffer = new byte[bufferSize];
                System.out.println("Data length: " + data.toString().getBytes().length);
                for (byte bt : data.toString().getBytes()) {
                    if (i == 0) {
                        buffer[0] = packetNum;
                        i += 1;
                    }

                    buffer[i] = bt;
                    i += 1;

                    if (i >= buffer.length) {
                        i = 0;
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.clientAddress, this.clientPort);
                        this.serverSocket.send(packet);
                        System.out.println("Sent packet#" + packetNum);
                        buffer = new byte[bufferSize];
                        packetNum += 1;
                    }
                }

                if (i != 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.clientAddress, this.clientPort);
                    this.serverSocket.send(packet);
                    System.out.println("Sent packet#" + packetNum);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private JSONObject getImageJSON(Image image) {
        Frame frame = image.getFrame();
        UByteBufferIndexer indexer = frame.createIndexer();
        HashMap<String, int[]> map = new HashMap<>();
        map.put("info", new int[]{frame.imageWidth, frame.imageHeight, frame.imageChannels});
        for (int x = 0; x < frame.imageWidth; x++) {
            for (int y = 0; y < frame.imageHeight; y++) {
                String key = "" + x + "," + y;
                int b = indexer.get(y, x, 0);
                int g = indexer.get(y, x, 1);
                int r = indexer.get(y, x, 2);
                map.put(key, new int[]{b, g, r});
            }
        }
        return new JSONObject(map);
    }
}
