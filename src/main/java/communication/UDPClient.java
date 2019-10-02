package communication;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class UDPClient implements Runnable {

    private Thread thread;
    private InetAddress hostAddress;
    private int hostPort;
    private DatagramSocket socket;
    private boolean alive;

    private CanvasFrame canvasFrame;

    public UDPClient(InetAddress hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.hostAddress = hostAddress;
            this.hostPort = hostPort;
            this.socket = new DatagramSocket();
            this.alive = true;
            this.canvasFrame = new CanvasFrame("UDP Client");
            this.canvasFrame.setCanvasSize(640, 480);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startThread() {
        this.thread.start();
    }

    public void stop() {
        this.alive = false;
    }

    @Override
    public void run() {
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
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(response.getData()));

                Frame frame = frameConverter.getFrame(img);
                this.canvasFrame.showImage(frame);
                //String stringResponse = new String(buffer, 0, response.getLength());
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                this.alive = false;
                e.printStackTrace();
                System.out.println(Thread.currentThread() + " exiting due to IO exception");
            }
        }
        this.socket.close();
    }

    public static void main(String[] args) {

        ArrayList<UDPClient> clients = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            UDPClient client = new UDPClient(InetAddress.getLoopbackAddress(), 2345);
            clients.add(client);
            client.startThread();
        }

        System.out.println("Waiting to close");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("got next line");
        for (UDPClient client : clients) {
            System.out.println("stopping " + client);
            client.stop();
        }

    }
}
