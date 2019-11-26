package pub_sub_service;

import java.util.*;

/**
 * This class acts as a middle-man between subscribers and publishers.
 * The Broker class is used by creating a shared Broker instance which is passed to the respective subscribers and
 * publishers. The Subscriber must call the method "subscribeTo" and pass the topic to which it wants to get notified about.
 * The Publisher must publish its variable by calling the method "addMessage". This message will be added to the
 * broker instance's messageQueue. The message queue will be broadcast to subscribers of the respective topics when
 * the run() method is called.
 */
public class Broker implements Runnable {

    private Map<String, Set<Subscriber>> topicSubscribers;
    private Queue<Message> messageQueue;

    /**
     * Instantiate a Broker object.
     */
    public Broker() {
        this.topicSubscribers = new HashMap<>();
        this.messageQueue = new LinkedList<>();
    }

    /**
     * Add a Message to the message queue
     * @param message the message to add
     */
    public synchronized void addMessage(Message message) {
        this.messageQueue.add(message);
    }

    /**
     * Adds a Subscriber to a specific topic.
     * @param topic the topic to subscribe to.
     * @param subscriber the subscriber to add to notification list of the specified topic.
     */
    public synchronized void subscribeTo(String topic, Subscriber subscriber) {
        if (this.topicSubscribers.containsKey(topic)) {
            this.topicSubscribers.get(topic).add(subscriber);
        } else {
            Set<Subscriber> set = new HashSet<>();
            set.add(subscriber);
            this.topicSubscribers.put(topic, set);
        }
        //System.out.println(subscriber + " subscribed to: " + topic);
    }

    /**
     * Unsubscribe from a specific topic.
     * @param topic the topic to unsubscribe from
     * @param subscriber the subscriber to remove from the notification list for the specified topic.
     */
    public synchronized void unsubscribeFrom(String topic, Subscriber subscriber) {
        if (this.topicSubscribers.containsKey(topic)) {
            this.topicSubscribers.get(topic).remove(subscriber);
        }
    }

    /**
     * The broker's run method. Calling this method will notify all subscribers about their subscription topics.
     */
    @Override
    public void run() {
        this.broadcast();
    }

    /**
     * Broadcast the messages added to message queue published by Publisher instances.
     */
    private synchronized void broadcast() {
        while(!this.messageQueue.isEmpty()) {
            Message message = this.messageQueue.remove();
            String topic = message.getTopic();
            if (this.topicSubscribers.containsKey(topic)) {
                for (Subscriber subscriber : this.topicSubscribers.get(topic)) {
                    subscriber.pushMessage(message);
                }
            }
        }
    }
}
