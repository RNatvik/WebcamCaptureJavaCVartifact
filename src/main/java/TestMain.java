import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestMain {

    public static void main(String[] args) {
        try {
            InetAddress getLocal = InetAddress.getLocalHost();
            InetAddress getLoopback = InetAddress.getLoopbackAddress();
            InetAddress byName = InetAddress.getByName("raspberrypi");
            System.out.println(getLocal.toString());
            System.out.println(getLoopback.toString());
            System.out.println(byName.toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }


}
