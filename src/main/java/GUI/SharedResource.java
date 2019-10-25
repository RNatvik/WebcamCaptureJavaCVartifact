package GUI;

import communication.TCPClient;
import communication.UDPClient;
import pub_sub_service.Broker;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A singleton class for sharing variables between controllers for application GUI
 */
public class SharedResource {

    private static SharedResource instance;
    private static boolean initialized = false;

    private TCPClient tcpClient;
    private UDPClient udpClient;
    private ScheduledExecutorService ses;
    private Broker broker;

    /**
     * Initialize the SharedResource singleton instance
     * The broker will be started and scheduled at a fixed rate.
     * The tcp and udp client threads will not be started until they are explicitly called externally.
     *
     * @param host    the target host for client sockets.
     * @param tcpPort the target host port for tcp client socket.
     * @param udpPort the target host port for udp client socket.
     * @param brokerPeriod the broker scheduled execution period.
     * @return true if successful initialization, false if already initialized
     * @throws UnknownHostException if host is not valid.
     */
    public static boolean initialize(String host, int tcpPort, int udpPort, int brokerPeriod) throws UnknownHostException {
        boolean success = false;
        if (!initialized) {
            instance = new SharedResource(host, tcpPort, udpPort);
            instance.getSes().scheduleAtFixedRate(instance.getBroker(), 0, brokerPeriod, TimeUnit.MILLISECONDS);
            initialized = true;
            success = true;
        }
        return success;
    }

    /**
     * Stops all processes for the singleton instance and sets it to null
     * @return true if instance was previously initialized and stopped successfully.
     */
    public static boolean clear() {
        boolean success = false;
        if (initialized) {
            instance.getTcpClient().stopConnection();
            instance.getUdpClient().stop();
            instance.getSes().shutdown();
            instance = null;
            initialized = false;
            success = true;
        }
        return success;
    }

    /**
     * Returns the singleton instance of the SharedResource class
     * @return the singleton instance of the SharedResource class
     */
    public static SharedResource getInstance() {
        return instance;
    }

    /**
     * Checks whether the singleton is initialized
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Constructor
     * @param host target host address
     * @param tcpPort target tcp server port
     * @param udpPort target udp server port
     * @throws UnknownHostException if not valid host
     */
    private SharedResource(String host, int tcpPort, int udpPort) throws UnknownHostException {
        this.ses = Executors.newScheduledThreadPool(1);
        this.broker = new Broker();
        this.tcpClient = new TCPClient(host, tcpPort, this.broker);
        this.udpClient = new UDPClient(host, udpPort);
    }

    /**
     * Returns the instance's tcp client object
     * @return the instance's tcp client object
     */
    public TCPClient getTcpClient() {
        return tcpClient;
    }

    /**
     * Returns the instance's udp client object
     * @return the instance's udp client object
     */
    public UDPClient getUdpClient() {
        return udpClient;
    }

    /**
     * Returns the instance's scheduled executor service
     * @return the instance's scheduled executor service
     */
    public ScheduledExecutorService getSes() {
        return ses;
    }

    /**
     * Returns the instance's broker object
     * @return the instance's broker object
     */
    public Broker getBroker() {
        return broker;
    }
}
