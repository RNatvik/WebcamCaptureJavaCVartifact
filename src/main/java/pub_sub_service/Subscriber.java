package pub_sub_service;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Basic framework for a Subscriber
 * Contains the essential methods and fields needed for getting Messages from a Broker
 */
public abstract class Subscriber {

    private Queue<Message> messageQueue;
    private Broker broker;

    /**
     * Constructor for the subscriber
     * @param broker the broker to connect to
     */
    public Subscriber(Broker broker) {
        this.messageQueue = new LinkedList<>();
        this.broker = broker;
    }

    /**
     * Add a message to the subscriber's messageQueue
     * @param message the message to add.
     */
    public synchronized void pushMessage(Message message) {
        this.messageQueue.add(message);
    }

    /**
     * Returns the subscriber's message queue
     * @return the subscriber's message queue
     */
    protected synchronized Queue<Message> getMessageQueue() {
        return messageQueue;
    }

    /**
     * Returns the subscriber's broker instance
     * @return the subscriber's broker instance
     */
    protected synchronized Broker getBroker() {
        return broker;
    }

    /**
     * Synchronized wrapper method for doReadMessages.
     * Call this method to make doReadMessages synchronized
     */
    protected synchronized void readMessages() {
        doReadMessages();
    }

    /**
     * Method for handling incoming messages.
     */
    protected abstract void doReadMessages();




}
