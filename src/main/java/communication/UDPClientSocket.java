package communication;

import data.DataStorage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPClientSocket implements Runnable{

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
        boolean shutdown = false;
        try {
            int i = 0;
            while (!shutdown) {
                String data = this.dataStorage.getData();
                byte[] buffer = data.getBytes();

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.clientAddress, this.clientPort);
                this.serverSocket.send(packet);
                i++;
                if (i > 10) {
                    shutdown = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
