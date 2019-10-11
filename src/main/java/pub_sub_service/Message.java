package pub_sub_service;

import data.Data;
import org.json.JSONObject;

/**
 * This class represents a message to be sent between threads.
 */
public class Message {

    private String topic;
    private Data data;

    /**
     * Create a new Message
     * @param topic the message's topic
     * @param data the message data
     */
    public Message(String topic, Data data) {
        this.topic = topic;
        this.data = data;
    }

    /**
     * Returns the Message object's topic
     * @return the Message object's topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Returns the Message object's data
     * @return the Message object's data
     */
    public Data getData() {
        return data;
    }

    /**
     * Converts the Message object to a JSONObject and returns the string representation of that object.
     * @return the Message as a JSONObject string.
     */
    public String toJSON() {
        JSONObject jsonObject = new JSONObject(this);
        return jsonObject.toString();
    }
}
