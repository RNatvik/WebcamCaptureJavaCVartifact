package pub_sub_service;

import data.Data;
import org.json.JSONObject;

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

    public String toJSON() {
        JSONObject jsonObject = new JSONObject(this);
        return jsonObject.toString();
    }
}
