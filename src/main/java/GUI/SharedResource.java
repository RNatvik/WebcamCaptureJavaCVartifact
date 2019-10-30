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
     * The tcp and udp client threads will not be started until they are explicitly initialized externally.
     *
     * @param brokerPeriod the broker scheduled execution period.
     * @return true if successful initialization, false if already initialized
     */
    public static boolean initialize(int brokerPeriod) {
        boolean success = false;
        if (!initialized) {
            instance = new SharedResource();
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
     */
    private SharedResource() {
        this.ses = Executors.newScheduledThreadPool(1);
        this.broker = new Broker();
        this.tcpClient = new TCPClient(this.broker);
        this.udpClient = new UDPClient();
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
