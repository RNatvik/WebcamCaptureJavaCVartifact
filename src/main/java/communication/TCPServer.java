package communication;

import data.Flag;
import pub_sub_service.Broker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class TCPServer implements Runnable {

    private Thread thread;
    private int port;
    private Flag shutdownFlag;
    private Broker broker;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean terminated;

    public TCPServer(int port, boolean loopback, int threadPoolSize, Broker broker) {
        try {
            this.thread = new Thread(this);
            this.port = port;
            this.shutdownFlag = new Flag(false);
            this.broker = broker;
            this.terminated = false;
            if (loopback) {
                this.serverSocket = new ServerSocket(this.port, 3, InetAddress.getLoopbackAddress());
            } else {
                this.serverSocket = new ServerSocket(this.port, 3, InetAddress.getByName("192.168.0.50"));
            }
            this.serverSocket.setSoTimeout(5);
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

    public void stop() {
        this.shutdownFlag.set(true);
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    @Override
    public void run() {
        while (!this.shutdownFlag.get()) {
            try {
                Socket socket = this.serverSocket.accept();
                TCPClientSocket clientSocket = new TCPClientSocket(socket, this.shutdownFlag, this.broker);
                this.executorService.submit(clientSocket);
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                this.shutdownFlag.set(true);
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
        this.terminated = this.shutdownProcedure();
    }

    private boolean shutdownProcedure() {
        System.out.println("Server in shutdown procedure");
        boolean success = false;
        try {
            this.executorService.awaitTermination(5, TimeUnit.SECONDS);
            this.executorService.shutdown();
            this.serverSocket.close();
            success = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }
}
