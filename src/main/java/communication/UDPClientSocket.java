package communication;

import data.DataStorage;
import data.Image;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UDPClientSocket implements Runnable {

    private DatagramSocket serverSocket;
    private DataStorage dataStorage;
    private InetAddress clientAddress;
    private int clientPort;
    private Java2DFrameConverter frameConverter;

    public UDPClientSocket(DatagramSocket serverSocket, InetAddress address, int port, DataStorage dataStorage) {
        this.serverSocket = serverSocket;
        this.dataStorage = dataStorage;
        this.clientAddress = address;
        this.clientPort = port;
        this.frameConverter = new Java2DFrameConverter();
    }

    @Override
    public void run() {
        System.out.println("UDPClientSocket:: In run");
        boolean shutdown = false;
        try {
            while (!shutdown) {
                Image image = this.dataStorage.getImageToGUI();
                BufferedImage bufferedImage = this.frameConverter.getBufferedImage(image.getFrame());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imBuffer = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(
                        imBuffer,
                        imBuffer.length,
                        this.clientAddress,
                        this.clientPort
                );
                this.serverSocket.send(packet);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] intToByte(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    private int byteToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

}
