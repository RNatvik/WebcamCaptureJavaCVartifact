package data;

import org.json.JSONObject;

public abstract class JSONData extends Data {

    public JSONData(String type) {
        super(type);
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject(this);
        return jsonObject.toString();
    }
}
