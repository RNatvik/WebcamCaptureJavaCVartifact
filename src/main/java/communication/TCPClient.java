package communication;

import data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class TCPClient implements Runnable, Publisher {

    private Thread thread;
    private InetAddress hostAddress;
    private int hostPort;
    private String outputMessage;
    private Socket socket;
    private Broker broker;
    private boolean initialized;
    private boolean connected;
    private boolean shutdown;
    private boolean terminated;

    public TCPClient(String hostAddress, int hostPort, Broker broker) throws UnknownHostException {
        this.thread = null;
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.hostPort = hostPort;
        this.outputMessage = null;
        this.socket = null;
        this.broker = broker;
        this.initialized = false;
        this.connected = false;
        this.shutdown = false;
        this.terminated = false;
    }

    public void initialize() throws IOException {
        if (!this.initialized) {
            this.thread = new Thread(this);
            if (this.connected = this.connect()) {
                this.socket.setSoTimeout(20);
                this.shutdown = false;
                this.terminated = false;
                this.initialized = true;
                this.thread.start();
            } else {
                throw new IOException(String.format(
                        "Failed to connect to: %s(%d)",
                        this.hostAddress, this.hostPort)
                );
            }
        }
    }

    public void setHost(String hostAddress, int hostPort) throws UnknownHostException {
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.hostPort = hostPort;
    }

    public synchronized boolean setOutputMessage(String command, String body) {
        boolean success = false;
        if (this.outputMessage == null) {
            this.outputMessage = String.format(command + "::%s", body);
            success = true;
        }
        return success;
    }

    public void stopConnection() {
        this.shutdown = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isTerminated() {
        return terminated;
    }

    private boolean connect() {
        boolean success = true;
        try {
            this.socket = new Socket(this.hostAddress, this.hostPort);
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    private boolean shutdownProcedure() {
        boolean success = true;
        try {
            this.socket.close();
            this.initialized = false;
            this.connected = false;
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    private Message parseMessage(String body) {
        Message message = null;
        try {
            JSONObject jsonMessageObject = new JSONObject(body);
            JSONObject jsonDataObject = jsonMessageObject.getJSONObject("data");
            String topic = jsonMessageObject.getString("topic");
            switch (topic) {
                case Topic.IMPROC_PARAM:
                    ImageProcessorParameter imProcParam = new ImageProcessorParameter(
                            jsonDataObject.getInt("hueMin"),
                            jsonDataObject.getInt("hueMax"),
                            jsonDataObject.getInt("satMin"),
                            jsonDataObject.getInt("satMax"),
                            jsonDataObject.getInt("valMin"),
                            jsonDataObject.getInt("valMin"),
                            jsonDataObject.getBoolean("storeProcessedImage")
                    );
                    message = new Message(topic, imProcParam);
                    break;

                case Topic.IMAGE_DATA:
                    JSONArray location = jsonDataObject.getJSONArray("location");
                    ImageProcessorData imProcData = new ImageProcessorData(
                            null,
                            new int[]{
                                    location.getInt(0),
                                    location.getInt(1),
                                    location.getInt(2),
                                    location.getInt(3)
                            }
                    );
                    message = new Message(topic, imProcData);
                    break;

                case Topic.PID_PARAM1:
                    PidParameter pidParameter1 = new PidParameter(
                            jsonDataObject.getDouble("kp"),
                            jsonDataObject.getDouble("ki"),
                            jsonDataObject.getDouble("kd"),
                            jsonDataObject.getDouble("maxOutput"),
                            jsonDataObject.getDouble("minOutput"),
                            jsonDataObject.getDouble("setpoint")
                    );
                    message = new Message(topic, pidParameter1);
                    break;

                case Topic.PID_PARAM2:
                    PidParameter pidParameter2 = new PidParameter(
                            jsonDataObject.getDouble("kp"),
                            jsonDataObject.getDouble("ki"),
                            jsonDataObject.getDouble("kd"),
                            jsonDataObject.getDouble("maxOutput"),
                            jsonDataObject.getDouble("minOutput"),
                            jsonDataObject.getDouble("setpoint")
                    );
                    message = new Message(topic, pidParameter2);
                    break;

                case Topic.REGULATOR_OUTPUT:
                    RegulatorOutput regOutput = new RegulatorOutput(
                            jsonDataObject.getDouble("leftMotor"),
                            jsonDataObject.getDouble("rightMotor")
                    );
                    message = new Message(topic, regOutput);
                    break;

                case Topic.REGULATOR_PARAM:
                    RegulatorParameter regParam = new RegulatorParameter(
                            jsonDataObject.getDouble("mcMinimumReverse"),
                            jsonDataObject.getDouble("mcMaximumReverse"),
                            jsonDataObject.getDouble("mcMinimumForward"),
                            jsonDataObject.getDouble("mcMaximumForward"),
                            jsonDataObject.getDouble("controllerMinOutput"),
                            jsonDataObject.getDouble("controllerMaxOutput")
                    );
                    message = new Message(topic, regParam);
                    break;

                case Topic.CONTROLER_INPUT:
                    ControlInput controlInput = new ControlInput(
                            jsonDataObject.getBoolean("manualControl"),
                            jsonDataObject.getDouble("forwardSpeed"),
                            jsonDataObject.getDouble("turnSpeed")
                    );
                    message = new Message(topic, controlInput);
                    break;

                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    public void run() {
        BufferedReader bufferedReader = null;
        PrintWriter printWriter = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            printWriter = new PrintWriter(this.socket.getOutputStream());
        } catch (IOException e) {
            this.shutdown = true;
            e.printStackTrace();
        }

        while (!this.shutdown) {
            try {
                if (this.outputMessage != null) {
                    printWriter.println(this.outputMessage);
                    printWriter.flush();
                    this.outputMessage = null;
                }
                String body = bufferedReader.readLine();
                if (body != null) {
                    Message message = this.parseMessage(body);
                    this.publish(this.broker, message);
                } else {
                    this.shutdown = true;
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                this.shutdown = true;
                e.printStackTrace();
            }

        }

        while (!this.terminated) {
            this.terminated = this.shutdownProcedure();
        }
    }

    @Override
    public void publish(Broker broker, Message message) {
        if (message != null) {
            broker.addMessage(message);
        }
    }
}
