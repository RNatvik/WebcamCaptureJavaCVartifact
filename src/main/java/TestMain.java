import communication.TCPClient;
import communication.TCPServer;
import data.*;
import image_processing.FilterBank;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(4);
        Broker serverBroker = new Broker();
        Broker clientBroker = new Broker();
        TCPServer tcpServer = new TCPServer(5678, true, 2, serverBroker);
        TCPClient tcpClient = new TCPClient(clientBroker);
        ServerPublisher serverPublisher = new ServerPublisher(serverBroker);
        ClientSubber clientSubber = new ClientSubber(clientBroker);

        ses.scheduleAtFixedRate(serverBroker, 1,5, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(clientBroker, 2, 5, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(serverPublisher, 3, 500, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(clientSubber, 4, 5, TimeUnit.MILLISECONDS);

        tcpServer.startThread();
        try {
            tcpClient.initialize("127.0.0.1", 5678, 20);
            tcpClient.connect();
            tcpClient.setOutputMessage("SUB", Topic.CONSOLE_OUTPUT);
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();

            tcpClient.stopConnection();

            scanner.nextLine();
            tcpServer.stop();
            ses.shutdown();
            ses.awaitTermination(5, TimeUnit.SECONDS);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ClientSubber extends Subscriber implements Runnable {

        public ClientSubber(Broker broker) {
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


