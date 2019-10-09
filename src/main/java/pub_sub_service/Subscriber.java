package pub_sub_service;

import java.util.LinkedList;
import java.util.Queue;

public abstract class Subscriber {

    private Queue<Message> messageQueue;
    private Broker broker;

    public Subscriber(Broker broker) {
        this.messageQueue = new LinkedList<>();
        this.broker = broker;
    }

    public synchronized void pushMessage(Message message) {
        this.messageQueue.add(message);
    }

    public synchronized Queue<Message> getMessageQueue() {
        return messageQueue;
    }

    public synchronized Broker getBroker() {
        return broker;
    }

    protected abstract void readMessages();




}
