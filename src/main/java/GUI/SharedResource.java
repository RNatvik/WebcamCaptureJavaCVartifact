package GUI;

import communication.TCPClient;
import communication.UDPClient;

public class SharedResource {

    private static SharedResource instance;
    private static boolean initialized = false;

    private TCPClient tcpClient;
    private UDPClient udpClient;

    public static boolean initialize(String host, int tcpPort, int udpPort) {
        boolean success = false;
        if (!initialized) {
            instance = new SharedResource(host, tcpPort, udpPort);
            instance.getTcpClient().startThread();
            instance.getUdpClient().startThread();
            initialized = true;
            success = true;
        }
        return success;
    }

    public static boolean clear() {
        boolean success = false;
        if (initialized) {
            instance.getTcpClient().stop();
            instance.getUdpClient().stop();
            instance = null;
            initialized = false;
            success = true;
        }
        return success;
    }

    public static SharedResource getInstance() {
        return instance;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private SharedResource(String host, int tcpPort, int udpPort) {
        this.tcpClient = new TCPClient(host, tcpPort);
        this.udpClient = new UDPClient(host, udpPort);
    }

    public TCPClient getTcpClient() {
        return tcpClient;
    }

    public UDPClient getUdpClient() {
        return udpClient;
    }
}
