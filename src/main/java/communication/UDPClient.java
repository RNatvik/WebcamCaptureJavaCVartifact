package communication;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

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
    private boolean alive;
    private boolean terminated;

    private CanvasFrame canvasFrame;

    public UDPClient(InetAddress hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.hostAddress = hostAddress;
            this.hostPort = hostPort;
            this.socket = new DatagramSocket();
            this.alive = true;
            this.terminated = false;
            this.canvasFrame = new CanvasFrame("UDP Client");
            this.canvasFrame.setCanvasSize(640, 480);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public UDPClient(String hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.hostAddress = InetAddress.getByName(hostAddress);
            this.hostPort = hostPort;
            this.socket = new DatagramSocket();
            this.alive = true;
            this.terminated = false;
            this.canvasFrame = new CanvasFrame("UDP Client");
            this.canvasFrame.setCanvasSize(640, 480);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void startThread() {
        System.out.println(this + ":: starting thread");
        this.thread.start();
    }

    public void stop() {
        System.out.println(this + ":: stop() called");
        this.alive = false;
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void run() {
        System.out.println(this + ":: in run");
        int counter = 0;
        try {
            DatagramPacket hello = new DatagramPacket(new byte[1], 1, hostAddress, hostPort);
            this.socket.send(hello);
            this.socket.setSoTimeout(5);
        } catch (IOException e) {
            e.printStackTrace();
            this.alive = false;
        }
        Java2DFrameConverter frameConverter = new Java2DFrameConverter();
        while (this.alive) {
            try {
                byte[] buffer = new byte[32768];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(response);
                this.hostAddress = response.getAddress();
                this.hostPort = response.getPort();
                String stringResponse = new String(buffer, 0, response.getLength());
                if (stringResponse.equals("END")) {
                    this.alive = false;
                } else {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(response.getData()));
                    Frame frame = frameConverter.getFrame(img);
                    this.canvasFrame.showImage(frame);
                    counter += 1;
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                this.alive = false;
                e.printStackTrace();
                System.out.println(this + ":: exiting due to IO exception");
            }
        }
        this.shutdownProcedure();
        System.out.println(this + ":: terminated: " + this.terminated + " " + counter);
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
            this.canvasFrame.dispose();
            this.terminated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
