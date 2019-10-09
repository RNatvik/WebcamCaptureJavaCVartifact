package pub_sub_service;

import java.util.*;

public class Broker implements Runnable {

    private Map<String, Set<Subscriber>> topicSubscribers;
    private Queue<Message> messageQueue;

    public Broker() {
        this.topicSubscribers = new HashMap<>();
        this.messageQueue = new LinkedList<>();
    }

    public synchronized void addMessage(Message message) {
        this.messageQueue.add(message);
    }

    public synchronized void subscribeTo(String topic, Subscriber subscriber) {
        if (this.topicSubscribers.containsKey(topic)) {
            this.topicSubscribers.get(topic).add(subscriber);
        } else {
            Set<Subscriber> set = new HashSet<>();
            set.add(subscriber);
            this.topicSubscribers.put(topic, set);
        }
        System.out.println(subscriber + " subscribed to: " + topic);
    }

    public synchronized void unsubscribeFrom(String topic, Subscriber subscriber) {
        if (this.topicSubscribers.containsKey(topic)) {
            this.topicSubscribers.get(topic).remove(subscriber);
        }
    }

    @Override
    public void run() {
        this.broadcast();
    }

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
