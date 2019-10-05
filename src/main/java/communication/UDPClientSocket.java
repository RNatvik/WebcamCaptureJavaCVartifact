package communication;

import data.DataStorage;
import data.Flag;
import data.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;


public class UDPClientSocket implements Runnable {

    private DatagramSocket socket;
    private DataStorage dataStorage;
    private InetAddress clientAddress;
    private int clientPort;
    private Flag serverShutdownFlag;

    public UDPClientSocket(InetAddress address, int port, DataStorage dataStorage, Flag serverShutdownFlag) {
        try {
            this.socket = new DatagramSocket();
            this.dataStorage = dataStorage;
            this.clientAddress = address;
            this.clientPort = port;
            this.serverShutdownFlag = serverShutdownFlag;
            this.socket.setSoTimeout(5);
            this.socket.connect(this.clientAddress, this.clientPort);
            System.out.println("Client socket created at: " + this.socket.getLocalAddress() + " (" + this.socket.getLocalPort() + ")");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("UDPClientSocket:: In run");
        boolean shutdown = false;

        while (!(this.serverShutdownFlag.get() || shutdown)) {
            try {
                Image image = this.dataStorage.getImageToGUI();
                if (image != null) {
                    if (image.getFlag()) {
                        BufferedImage bufferedImage = image.getImage();
                        image.setFlag(false);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "jpg", baos);
                        byte[] imBuffer = baos.toByteArray();
                        DatagramPacket packet = new DatagramPacket(
                                imBuffer,
                                imBuffer.length,
                                this.clientAddress,
                                this.clientPort
                        );
                        this.socket.send(packet);
                    }
                }
                byte[] buffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(response);
                String stringResponse = new String(buffer, 0, response.getLength());
                if (stringResponse.equals("END")) {
                    shutdown = true;
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                shutdown = true;
            }
        }
        this.shutdownProcedure();
        System.out.println("ClientSocket:: end of run");
    }

    private void shutdownProcedure() {
        try {
            String message = "END";
            byte[] buffer = message.getBytes();
            System.out.println();
            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    this.clientAddress,
                    this.clientPort
            );
            this.socket.send(packet);
            this.socket.disconnect();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
