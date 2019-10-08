import communication.TCPClientSocket;
import data.Circle;
import data.Data;
import data.PidParameter;

public class TestMain {

    public static void main(String[] args) {
        String result = String.format("\"type\":\"%s\"", TCPClientSocket.ERROR);
        System.out.println(result);

    }

    public static <T> T safeCast(Object o, Class<T> clazz) {
        return clazz != null && clazz.isInstance(o) ? clazz.cast(o) : null;
    }

    public static Data getSomething(int i) {
        Data data = null;
        if (i == 1) {
            data = new Circle(new int[]{1,2,3}, false);
        } else {
            data = new PidParameter(1,2,3,false);
        }
        return data;
    }
}
