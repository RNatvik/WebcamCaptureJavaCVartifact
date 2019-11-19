package communication;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

/**
 * This class is a UDPClient, responsible for handling incoming images from the server
 */
public class UDPClient implements Runnable {

    private Thread thread;
    private InetAddress hostAddress;
    private int hostPort;
    private DatagramSocket socket;
    private boolean initialized;
    private boolean shutdown;
    private boolean terminated;
    private boolean running;
    private BufferedImage bufferedImage;

    /**
     * Constructor
     */
    public UDPClient() {
        this.thread = null;
        this.hostAddress = null;
        this.hostPort = 0;
        this.socket = null;
        this.shutdown = true;
        this.terminated = false;
        this.running = false;
        this.bufferedImage = null;
    }

    /**
     * Starts the UDP connection if initialized
     *
     * @return true if new thread started
     */
    public boolean start() {
        boolean success = false;
        if (!this.running && this.initialized) {
            this.running = true;
            this.thread.start();
            success = true;
        }
        return success;
    }

    /**
     * Initialize the UDP connection. This will not connect the client to the host.
     * Use "start" method after initialize to establish connection
     *
     * @param host the host address to connect to
     * @param port the host port to connect to
     * @return true if successful initialization
     * @throws UnknownHostException if host not found on the network
     * @throws SocketException      if socket could not be established
     */
    public boolean initialize(String host, int port) throws UnknownHostException, SocketException {
        boolean success = false;
        if (!this.running) {
            this.socket = new DatagramSocket();
            this.hostAddress = InetAddress.getByName(host);
            this.hostPort = port;
            this.thread = new Thread(this);
            this.terminated = false;
            this.shutdown = false;
            this.initialized = true;
            success = true;
        }
        return success;
    }

    /**
     * Stop the connection
     */
    public void stop() {
        System.out.println(this + ":: stop() called");
        this.shutdown = true;
    }

    /**
     * Check if instance is terminated
     *
     * @return ture if terminated
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Check if instance is initialized
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Check if client is running
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * UDPClient main loop
     */
    @Override
    public void run() {
        System.out.println(this + ":: in run");
        try {
            DatagramPacket hello = new DatagramPacket(new byte[1], 1, hostAddress, hostPort);
            this.socket.send(hello);
            this.socket.setSoTimeout(5);
        } catch (IOException e) {
            e.printStackTrace();
            this.shutdown = false;
        }
        while (!this.shutdown) {
            try {
                byte[] buffer = new byte[65536];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(response);
                this.hostAddress = response.getAddress();
                this.hostPort = response.getPort();
                String stringResponse = new String(buffer, 0, response.getLength());
                if (stringResponse.equals("END")) {
                    this.shutdown = false;
                } else {
                    this.bufferedImage = ImageIO.read(new ByteArrayInputStream(response.getData()));
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                this.shutdown = false;
                e.printStackTrace();
                System.out.println(this + ":: exiting due to IO exception");
            }
        }
        this.shutdownProcedure();
        System.out.println(this + ":: terminated: " + this.terminated);
    }

    /**
     * Access the most recent image received
     *
     * @return the most recent image received
     */
    public synchronized BufferedImage getImage() {
        return this.bufferedImage;
    }

    /**
     * The UDPClient's shutdown procedure.
     */
    private void shutdownProcedure() {
        try {
            String message = "END";
            byte[] buffer = message.getBytes();
            System.out.println();
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    this.hostAddress,
                    this.hostPort
            );
            this.socket.send(packet);
            this.socket.close();
            this.terminated = true;
            this.running = false;
            this.initialized = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
