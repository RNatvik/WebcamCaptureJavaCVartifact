package pub_sub_service;

import data.Data;

public class Message {

    private String topic;
    private Data data;

    public Message(String topic, Data data) {
        this.topic = topic;
        this.data = data;
    }

    public String getTopic() {
        return topic;
    }

    public Data getData() {
        return data;
    }
}
