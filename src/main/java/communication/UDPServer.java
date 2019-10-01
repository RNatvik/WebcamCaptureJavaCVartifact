package communication;

import data.DataStorage;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class UDPServer implements Runnable {

    private Thread thread;
    private int port;
    private DataStorage dataStorage;
    private DatagramSocket serverSocket;
    private ExecutorService executorService;

    public UDPServer(int port, DataStorage dataStorage, boolean loopback, int threadPoolSize) {
        try {
            this.thread = new Thread(this);
            this.port = port;
            this.dataStorage = dataStorage;
            if (loopback) {
                this.serverSocket = new DatagramSocket(this.port, InetAddress.getLoopbackAddress());
            } else {
                this.serverSocket = new DatagramSocket(this.port, InetAddress.getLocalHost());
            }
            this.executorService = Executors.newFixedThreadPool(threadPoolSize);
            System.out.println("Server:: " + this.serverSocket.getLocalAddress() + " (" + this.serverSocket.getLocalPort() + ")");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startThread() {
        this.thread.start();
    }

    @Override
    public void run() {
        boolean shutdown = false;

        while (!shutdown) {
            try {
                DatagramPacket hello = new DatagramPacket(new byte[1], 1);
                this.serverSocket.receive(hello);
                InetAddress address = hello.getAddress();
                int port = hello.getPort();
                UDPClientSocket clientSocket = new UDPClientSocket(this.serverSocket, address, port, this.dataStorage);
                this.executorService.submit(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                shutdown = true;
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
        this.serverSocket.close();
    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer(2345, new DataStorage(), true, 3);
        server.startThread();
    }
}
