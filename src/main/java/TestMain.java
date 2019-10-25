import communication.TCPClient;
import communication.TCPServer;
import data.*;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        try {
            Broker broker1 = new Broker();
            Broker broker2 = new Broker();
            MultiPub publisher = new MultiPub(broker1);
            Subber subber = new Subber(broker2);
            TCPServer server = new TCPServer(1234, true, 2, broker1);
            TCPServer server2 = new TCPServer(1235, true, 2, broker1);
            TCPClient client = new TCPClient("127.0.0.1", 1234, broker2);
            server2.startThread();
            server.startThread();
            Thread.sleep(3000);
            client.initialize();
            int counter = 1;
            boolean finishedSub = false;
            while (!finishedSub) {
                boolean success = false;
                switch (counter) {
                    case 1:
                        success = client.setOutputMessage("SUB", Topic.REGULATOR_OUTPUT);
                        break;
                    case 2:
                        success = client.setOutputMessage("SUB", Topic.REGULATOR_PARAM);
                        break;
                    case 3:
                        success = client.setOutputMessage("SUB", Topic.IMPROC_PARAM);
                        break;
                    case 4:
                        success = client.setOutputMessage("SUB", Topic.IMAGE_DATA);
                        break;
                    case 5:
                        success = client.setOutputMessage("SUB", Topic.PID_PARAM1);
                        break;
                    case 6:
                        success = client.setOutputMessage("SUB", Topic.PID_PARAM2);
                        break;
                    case 7:
                        success = client.setOutputMessage("SUB", Topic.CONTROLER_INPUT);
                        break;
                    default:
                        finishedSub = true;
                        break;
                }
                if (success) {
                    counter += 1;
                }
            }
            ses.scheduleAtFixedRate(broker1, 0, 50, TimeUnit.MILLISECONDS);
            ses.scheduleAtFixedRate(broker2, 0, 50, TimeUnit.MILLISECONDS);
            ses.scheduleAtFixedRate(publisher, 1, 500, TimeUnit.MILLISECONDS);
            ses.scheduleAtFixedRate(subber, 2, 200, TimeUnit.MILLISECONDS);

            client.setHost("127.0.0.1", 1235);

            Thread.sleep(10000);

            client.stopConnection();
            Thread.sleep(1000);
            boolean finished = false;
            while (!finished) {
                finished = client.isTerminated();
                System.out.print("");
            }
            client.initialize();
            finished = false;
            while (!finished) {
                finished = client.setOutputMessage("SUB", Topic.IMAGE_DATA);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class Subber extends Subscriber implements Runnable {

        public Subber(Broker broker) {
            super(broker);
            this.getBroker().subscribeTo(Topic.REGULATOR_PARAM, this);
            this.getBroker().subscribeTo(Topic.REGULATOR_OUTPUT, this);
            this.getBroker().subscribeTo(Topic.PID_PARAM1, this);
            this.getBroker().subscribeTo(Topic.PID_PARAM2, this);
            this.getBroker().subscribeTo(Topic.IMAGE_DATA, this);
            this.getBroker().subscribeTo(Topic.IMPROC_PARAM, this);
            this.getBroker().subscribeTo(Topic.CONTROLER_INPUT, this);
        }

        @Override
        protected void readMessages() {
            synchronized (this) {
                int length = this.getMessageQueue().size();
                if (length > 0) {
                    System.out.println("Subber:: " + this.getMessageQueue().size() + " in queue");
                }
                while (!this.getMessageQueue().isEmpty()) {
                    Message message = this.getMessageQueue().remove();
                }
            }
        }

        @Override
        public void run() {
            readMessages();
        }
    }

    private static class MultiPub implements Runnable, Publisher {

        private Broker broker;

        public MultiPub(Broker broker) {
            this.broker = broker;
        }

        @Override
        public void run() {
            ControlInput controlInput = new ControlInput(
                    true, 10, 20
            );
            ImageProcessorData imageProcessorData = new ImageProcessorData(
                    null, new int[]{1, 2, 3, 4}
            );
            ImageProcessorParameter imageProcessorParameter = new ImageProcessorParameter(
                    1, 2, 3, 4, 5, 6, true
            );
            PidParameter pidParameter1 = new PidParameter(
                    1, 1, 1, 1, 1, 1
            );
            PidParameter pidParameter2 = new PidParameter(
                    2, 2, 2, 2, 2, 2
            );
            RegulatorOutput regulatorOutput = new RegulatorOutput(
                    15, 51
            );
            RegulatorParameter regulatorParameter = new RegulatorParameter(
                    1, 2, 3, 4, 5, 6
            );
            Message messageControlInput = new Message(Topic.CONTROLER_INPUT, controlInput);
            Message messageImProcParam = new Message(Topic.IMPROC_PARAM, imageProcessorParameter);
            Message messageImProcData = new Message(Topic.IMAGE_DATA, imageProcessorData);
            Message messagePid1 = new Message(Topic.PID_PARAM1, pidParameter1);
            Message messagePid2 = new Message(Topic.PID_PARAM2, pidParameter2);
            Message messageRegOut = new Message(Topic.REGULATOR_OUTPUT, regulatorOutput);
            Message messageRegParam = new Message(Topic.REGULATOR_PARAM, regulatorParameter);
            for (int i = 0; i < 1; i++) {
                this.publish(broker, messageControlInput);
                this.publish(broker, messageImProcParam);
                this.publish(broker, messageImProcData);
                this.publish(broker, messagePid1);
                this.publish(broker, messagePid2);
                this.publish(broker, messageRegOut);
                this.publish(broker, messageRegParam);
            }
        }

        @Override
        public void publish(Broker broker, Message message) {
            broker.addMessage(message);
        }
    }
}


