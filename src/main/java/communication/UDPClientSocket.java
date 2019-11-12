package communication;

import data.*;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Subscriber;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;


public class UDPClientSocket extends Subscriber implements Runnable {

    private DatagramSocket socket;
    private InetAddress clientAddress;
    private int clientPort;
    private Flag serverShutdownFlag;
    private boolean shutdown;

    public UDPClientSocket(InetAddress address, int port, Broker broker, Flag serverShutdownFlag) {
        super(broker);
        try {
            this.socket = new DatagramSocket();
            this.clientAddress = address;
            this.clientPort = port;
            this.serverShutdownFlag = serverShutdownFlag;
            this.shutdown = false;
            this.socket.setSoTimeout(5);
            this.socket.connect(this.clientAddress, this.clientPort);
            //System.out.println(this + ":: created at: " + this.socket.getLocalAddress() + " (" + this.socket.getLocalPort() + ")");
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //System.out.println(this + ":: In run");
        this.getBroker().subscribeTo(Topic.OUTPUT_IMAGE, this);
        while (!(this.serverShutdownFlag.get() || this.shutdown)) {
            try {
                this.readMessages();
                byte[] buffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(response);
                String stringResponse = new String(buffer, 0, response.getLength());
                if (stringResponse.equals("END")) {
                    this.shutdown = true;
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                shutdown = true;
            }
        }
        this.shutdownProcedure();
        //System.out.println(this + " is terminated");
    }

    private void shutdownProcedure() {
        try {
            String message = "END";
            byte[] buffer = message.getBytes();
            //System.out.println();
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


    @Override
    protected synchronized void doReadMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            Data data = message.getData();
            String topic = message.getTopic();

            if (topic.equals(Topic.OUTPUT_IMAGE)) {
                OutputImage image = data.safeCast(OutputImage.class);
                try {
                    if (image != null) {
                        BufferedImage bufferedImage = image.getImage();
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
                    } else {
                        //System.out.println(this + ":: image is null");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //System.out.println(this + ":: Topic Error");
            }
        }
    }
}
