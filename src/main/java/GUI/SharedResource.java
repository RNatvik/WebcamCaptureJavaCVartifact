package GUI;

import communication.TCPClient;
import communication.UDPClient;
import pub_sub_service.Broker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SharedResource {

    private static SharedResource instance;
    private static boolean initialized = false;

    private TCPClient tcpClient;
    private UDPClient udpClient;
    private ScheduledExecutorService ses;
    private Broker broker;

    public static boolean initialize(String host, int tcpPort, int udpPort) {
        boolean success = false;
        if (!initialized) {
            instance = new SharedResource(host, tcpPort, udpPort);
            instance.getTcpClient().startThread();
            instance.getUdpClient().startThread();
            instance.getSes().scheduleAtFixedRate(instance.getBroker(), 0, 30, TimeUnit.MILLISECONDS);
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
        this.ses = Executors.newScheduledThreadPool(1);
        this.broker = new Broker();
    }

    public TCPClient getTcpClient() {
        return tcpClient;
    }

    public UDPClient getUdpClient() {
        return udpClient;
    }

    public ScheduledExecutorService getSes() {
        return ses;
    }

    public Broker getBroker() {
        return broker;
    }
}
