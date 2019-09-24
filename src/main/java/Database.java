import data.Data;
import data.DataType;

import java.util.ArrayList;
import java.util.HashMap;

public class Database {

    private HashMap<DataType, ArrayList<Data>> data;

    public Database() {
        this.data = new HashMap<>();
    }


}
