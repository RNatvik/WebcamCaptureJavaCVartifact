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

/**
 * Welcome socket for TCP communication
 * It handles requests to connect to the server and allocates a new TCPClient socket to handle
 * each client.
 */
public class TCPServer implements Runnable {

    private Thread thread;
    private int port;
    private Flag shutdownFlag;
    private Broker broker;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private boolean terminated;

    /**
     * Constructor
     *
     * @param port           the port to bind the server to
     * @param loopback       whether or not to use the local loopback address
     * @param threadPoolSize how many threads / clients can be connected at once
     * @param broker         the broker to connect the clients to
     */
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
            //System.out.println(this + ":: created socket at: " + this.serverSocket.getInetAddress() + " (" + this.serverSocket.getLocalPort() + ")");
            this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        } catch (IOException e) {
            e.printStackTrace();
            //System.out.println(this + ":: Could not initialize TCP Server");
        }
    }

    /**
     * Starts the server thread
     */
    public void startThread() {
        //System.out.println(this + ":: starting thread");
        this.thread.start();
    }

    /**
     * Stop the server
     */
    public void stop() {
        //System.out.println(this + ":: stop() called");
        this.shutdownFlag.set(true);
    }

    /**
     * Check if instance is terminated
     *
     * @return true if terminated
     */
    public boolean isTerminated() {
        return this.terminated;
    }

    /**
     * Server main loop.
     */
    @Override
    public void run() {
        while (!this.shutdownFlag.get()) {
            try {
                Socket socket = this.serverSocket.accept();
                TCPClientSocket clientSocket = new TCPClientSocket(socket, this.shutdownFlag, this.broker);
                this.executorService.submit(clientSocket);
                //System.out.println(this + ":: accepted connection. Submitted to: " + clientSocket);
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                this.shutdownFlag.set(true);
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
        this.terminated = this.shutdownProcedure();
        //System.out.println(this + ":: terminated: " + this.terminated);
    }

    /**
     * The server's shutdown procedure
     *
     * @return true if successful procedure
     */
    private boolean shutdownProcedure() {
        //System.out.println("Server in shutdown procedure");
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
