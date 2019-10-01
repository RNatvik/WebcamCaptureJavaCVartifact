package communication;

import data.DataStorage;

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
        TCPServer server = new TCPServer(12345, new DataStorage(), false, 3);
        server.startThread();
    }
}
