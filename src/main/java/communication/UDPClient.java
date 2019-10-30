package communication;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

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


    public UDPClient(String hostAddress, int hostPort) throws SocketException, UnknownHostException {
        this.thread = null;
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.hostPort = hostPort;
        this.socket = new DatagramSocket();
        this.shutdown = true;
        this.terminated = false;
        this.running = false;
        this.bufferedImage = null;
    }

    public void start() {
        if (!this.running && this.initialized) {
            this.thread.start();
        }
    }

    public boolean initialize(String host, int port) throws UnknownHostException {
        boolean success = false;
        if (!this.initialized) {
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

    public void stop() {
        System.out.println(this + ":: stop() called");
        this.shutdown = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        this.running = true;
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
                byte[] buffer = new byte[32768];
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

    public synchronized BufferedImage getImage() {
        return this.bufferedImage;
    }


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
