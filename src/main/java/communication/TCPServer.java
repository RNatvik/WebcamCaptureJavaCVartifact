package communication;

import data.Circle;
import data.DataStorage;
import data.Image;
import data.PidParameter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class TCPServer implements Runnable {

    private Thread thread;
    private int port;
    private DataStorage dataStorage;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public TCPServer(int port, DataStorage dataStorage, boolean loopback, int threadPoolSize) {
        try {
            this.thread = new Thread(this);
            this.port = port;
            this.dataStorage = dataStorage;
            if (loopback) {
                this.serverSocket = new ServerSocket(this.port, 3, InetAddress.getLoopbackAddress());
            } else {
                this.serverSocket = new ServerSocket(this.port, 3, InetAddress.getLocalHost());
            }
            System.out.println("Server:: " + this.serverSocket.getInetAddress() + " (" + this.serverSocket.getLocalPort() + ")");
            this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not initialize TCP Server");
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
                Socket socket = this.serverSocket.accept();
                TCPClientSocket clientSocket = new TCPClientSocket(socket, this.dataStorage);
                this.executorService.submit(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
                shutdown = true;
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Image image = new Image(false);
        PidParameter pidParameter1 = new PidParameter(1,2,3,false);
        PidParameter pidParameter2 = new PidParameter(4,5,6,true);
        Circle circle = new Circle(new int[]{1,2,3}, false);
        DataStorage storage = new DataStorage(image, circle, pidParameter1, pidParameter2);
        TCPServer server = new TCPServer(4567, storage, false, 3);
        server.startThread();
    }
}
