package communication;

import data.DataStorage;
import data.Flag;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class UDPServer implements Runnable {

    private Thread thread;
    private int port;
    private DataStorage dataStorage;
    private DatagramSocket serverSocket;
    private ExecutorService executorService;
    private Flag shutdown;
    private boolean terminated;

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
            this.shutdown = new Flag(false);
            this.terminated = false;
            this.serverSocket.setSoTimeout(5);
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

    public void stop() {
        System.out.println("Server in stop");
        this.shutdown.set(true);
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    @Override
    public void run() {

        while (!this.shutdown.get()) {
            try {
                DatagramPacket hello = new DatagramPacket(new byte[1], 1);
                this.serverSocket.receive(hello);
                System.out.println("UDPServer:: received datagram");
                InetAddress address = hello.getAddress();
                int port = hello.getPort();
                UDPClientSocket clientSocket = new UDPClientSocket(address, port, this.dataStorage, this.shutdown);
                this.executorService.submit(clientSocket);
                System.out.println("UDPServer:: submitted client socket to executor");
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                this.shutdown.set(true);
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
        this.terminated = shutdownProcedure();
        System.out.println("Server terminated: " + this.terminated);
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
        }

        return success;
    }

//    public static void main(String[] args) {
//        UDPServer server = new UDPServer(2345, new DataStorage(), true, 3);
//        server.startThread();
//    }
}
