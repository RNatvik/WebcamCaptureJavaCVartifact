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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class TCPClient implements Runnable, Publisher {

    private Thread thread;
    private InetAddress hostAddress;
    private int hostPort;
    private int timeout;
    private Queue<String> outputMessageQueue;
    private Socket socket;
    private Broker broker;
    private boolean initialized;
    private boolean connected;
    private boolean shutdown;
    private boolean terminated;

    public TCPClient(Broker broker) {
        this.thread = null;
        this.hostAddress = null;
        this.hostPort = 0;
        this.timeout = 0;
        this.outputMessageQueue = new LinkedList<>();
        this.socket = null;
        this.broker = broker;
        this.initialized = false;
        this.connected = false;
        this.shutdown = false;
        this.terminated = false;
    }

    public void initialize(String hostAddress, int hostPort, int timeout) throws UnknownHostException {
        if (!this.connected) {
            this.hostAddress = InetAddress.getByName(hostAddress);
            this.hostPort = hostPort;
            this.timeout = timeout;
            this.thread = new Thread(this);
            this.shutdown = false;
            this.terminated = false;
            this.initialized = true;
            this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                    this + " is initialized."
            )));
        }
    }

    public boolean connect() {
        boolean success = false;
        try {
            if (this.initialized && !this.connected) {
                this.socket = new Socket(this.hostAddress, this.hostPort);
                this.socket.setSoTimeout(this.timeout);
                this.connected = true;
                this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                        this + " is connected"
                )));
                this.thread.start();
                success = true;
            }
        } catch (IOException e) {

            this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                String.format("%s %s %s", this, e,
                        Arrays.toString(e.getStackTrace())
                                .replace("[", "\n     ")
                                .replace(",", "\n    ")
                                .replace("]", "\n    ")
                )
            )));
        }
        return success;
    }

    public synchronized void setOutputMessage(String command, String body) {
        if (this.connected) {
            this.outputMessageQueue.add(String.format(command + "::%s", body));
        }
    }

    public void stopConnection() {
        this.shutdown = true;
        this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                this + " stop called"
        )));
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


    private boolean shutdownProcedure() {
        this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                this + " in shutdown procedure"
        )));
        boolean success = true;
        try {
            this.socket.close();
            this.initialized = false;
            this.connected = false;
        } catch (IOException e) {
            success = false;
        }
        this.publish(this.broker, new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                this + " shutdown procedure: " + success
        )));
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

                case Topic.IMPROC_DATA:
                    JSONArray location = jsonDataObject.getJSONArray("location");
                    ImageProcessorData imProcData = new ImageProcessorData(
                            new double[]{
                                    location.getDouble(0),
                                    location.getDouble(1),
                                    location.getDouble(2),
                                    location.getDouble(3)
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
                            jsonDataObject.getDouble("setpoint"),
                            jsonDataObject.getDouble("deadBand"),
                            jsonDataObject.getDouble("maxIOutput"),
                            jsonDataObject.getBoolean("reversed")
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
                            jsonDataObject.getDouble("setpoint"),
                            jsonDataObject.getDouble("deadBand"),
                            jsonDataObject.getDouble("maxIOutput"),
                            jsonDataObject.getBoolean("reversed")
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
                            jsonDataObject.getDouble("controllerMaxOutput"),
                            jsonDataObject.getDouble("ratio")
                    );
                    message = new Message(topic, regParam);
                    break;

                case Topic.CONTROLLER_INPUT:
                    ControlInput controlInput = new ControlInput(
                            jsonDataObject.getBoolean("manualControl"),
                            jsonDataObject.getDouble("forwardSpeed"),
                            jsonDataObject.getDouble("turnSpeed")
                    );
                    message = new Message(topic, controlInput);
                    break;

                case Topic.CONSOLE_OUTPUT:
                    ConsoleOutput consoleOutput = new ConsoleOutput(jsonDataObject.getString("string"));
                    message = new Message(topic, consoleOutput);
                    break;

                case Topic.DEBUG_OUTPUT:
                    ConsoleOutput debugOutput = new ConsoleOutput(jsonDataObject.getString("string"));
                    message = new Message(topic, debugOutput);
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
                synchronized (this) {
                    while (!this.outputMessageQueue.isEmpty()) {
                        String outputMessage = this.outputMessageQueue.remove();
                        printWriter.println(outputMessage);
                        printWriter.flush();
                        this.publish(this.broker, new Message(Topic.DEBUG_OUTPUT, new ConsoleOutput(
                                outputMessage
                        )
                        ));
                    }
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
