import communication.TCPClient;
import communication.TCPServer;
import communication.UDPServer;
import data.ConsoleOutput;
import data.Flag;
import data.Topic;
import image_processing.Camera;
import image_processing.ImageProcessor;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        Broker serverBroker = new Broker();
        TCPServer tcpServer = new TCPServer(5678, true, 2, serverBroker);
        UDPServer udpServer = new UDPServer(2345, true, 3, serverBroker);
        ServerPublisher serverPublisher = new ServerPublisher(serverBroker);
        Flag flag = new Flag(false);
        Camera camera = new Camera(0, flag);
        ImageProcessor imageProcessor = new ImageProcessor(flag, serverBroker);

        tcpServer.startThread();
        udpServer.startThread();

        camera.start();
        imageProcessor.start(camera.getSrcIm());

        ses.scheduleAtFixedRate(camera, 0, 40, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(imageProcessor, 0, 5, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(serverPublisher, 0, 1000, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(serverBroker, 0, 5, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class Subber extends Subscriber implements Runnable {

        public Subber(Broker broker) {
            super(broker);
            this.getBroker().subscribeTo(Topic.CONSOLE_OUTPUT, this);
        }

        @Override
        protected void doReadMessages() {
            while (!this.getMessageQueue().isEmpty()) {
                Message message = this.getMessageQueue().remove();
                ConsoleOutput consoleOutput = message.getData().safeCast(ConsoleOutput.class);
                if (consoleOutput != null) {
                    System.out.println(consoleOutput.getString());
                }
            }
        }

        @Override
        public void run() {
            this.readMessages();
        }
    }

    private static class ServerPublisher implements Publisher, Runnable {

        private Broker broker;
        private int counter;

        public ServerPublisher(Broker broker) {
            this.broker = broker;
            this.counter = 0;
        }

        @Override
        public void publish(Broker broker, Message message) {
            this.broker.addMessage(message);
        }

        @Override
        public void run() {
            this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                    this + " counter: " + this.counter
            )));
            this.counter += 1;
        }
    }
}


