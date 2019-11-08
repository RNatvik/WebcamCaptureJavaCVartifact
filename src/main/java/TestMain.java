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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        double[] signal = new double[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        double[] kernel = new double[]{0.2, 0.2, 0.2, 0.2, 0.2};
        String id = "test";
        FilterBank filterBank = new FilterBank();
        filterBank.registerSignal(id, kernel);
        for (double s : signal) {
            double value = filterBank.passValue(id, s);
            System.out.println(value);
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
        protected void doReadMessages() {
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
                    null, new double[]{1, 2, 3, 4}
            );
            ImageProcessorParameter imageProcessorParameter = new ImageProcessorParameter(
                    1, 2, 3, 4, 5, 6, true
            );
            PidParameter pidParameter1 = new PidParameter(
                    1, 1, 1, 1, 1, 1,0,0,true
            );
            PidParameter pidParameter2 = new PidParameter(
                    2, 2, 2, 2, 2, 2,0,0,true
            );
            RegulatorOutput regulatorOutput = new RegulatorOutput(
                    15, 51
            );
            RegulatorParameter regulatorParameter = new RegulatorParameter(
                    1, 2, 3, 4, 5, 6,1
            );
            Message messageControlInput = new Message(Topic.CONTROLER_INPUT, controlInput);
            Message messageImProcParam = new Message(Topic.IMPROC_PARAM, imageProcessorParameter);
            Message messageImProcData = new Message(Topic.IMAGE_DATA, imageProcessorData);
            Message messagePid1 = new Message(Topic.PID_PARAM1, pidParameter1);
            Message messagePid2 = new Message(Topic.PID_PARAM2, pidParameter2);
            Message messageRegOut = new Message(Topic.REGULATOR_OUTPUT, regulatorOutput);
            Message messageRegParam = new Message(Topic.REGULATOR_PARAM, regulatorParameter);
            for (int i = 0; i < 3; i++) {
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


