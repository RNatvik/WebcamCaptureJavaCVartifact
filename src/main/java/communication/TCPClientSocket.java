package communication;

import data.*;
import org.json.JSONObject;
import pub_sub_service.Broker;
import pub_sub_service.Message;
import pub_sub_service.Publisher;
import pub_sub_service.Subscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Class for handling communication with a single TCP client
 */
public class TCPClientSocket extends Subscriber implements Runnable, Publisher {

    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Flag serverShutdown;
    private boolean shutdown;
    private boolean terminated;

    /**
     * Constructor
     *
     * @param socket         the client's socket
     * @param serverShutdown flag for whether the server is shutting down
     * @param broker         the broker to connect to the socket
     */
    public TCPClientSocket(Socket socket, Flag serverShutdown, Broker broker) {
        super(broker);
        //this.getBroker().subscribeTo(Topic.CONSOLE_OUTPUT, this);
        this.socket = socket;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.serverShutdown = serverShutdown;
            this.shutdown = false;
            this.terminated = false;
            this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                    this + ":: created at: " + this.socket.getLocalAddress() + " (" + this.socket.getPort() + ")"
            )));
        } catch (IOException e) {
            this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                    e.toString()
            )));
        }
    }

    /**
     * Stop the socket connection
     */
    public void stop() {
        this.shutdown = true;
        this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                this + " stop called"
        )));
    }

    /**
     * Check if the instance is terminated
     *
     * @return true if terminated
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * TCPClientSocket main loop
     */
    @Override
    public void run() {
        while (!(this.shutdown || this.serverShutdown.get())) {
            try {
                this.socket.setSoTimeout(20);
                this.readMessages();
                String line = this.bufferedReader.readLine();
                this.publish(this.getBroker(), new Message(Topic.DEBUG_OUTPUT, new ConsoleOutput(
                        this + " received line: " + line
                )));
                if (line != null) {
                    String[] lineParts = line.split("::");
                    String command = lineParts[0];
                    String body = lineParts[1];

                    switch (command) {
                        case "SET":
                            this.set(body);
                            break;

                        case "SUB":
                            this.sub(body);
                            break;

                        case "UNSUB":
                            this.unsub(body);
                            break;

                        default:
                            break;
                    }
                } else {
                    this.stop();
                }
            } catch (SocketTimeoutException e) {
                // Do nothing
            } catch (IOException e) {
                this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                        e.toString()
                )));
                this.stop();
            } catch (IndexOutOfBoundsException e) {
                this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                        this + " client wrote invalid syntax"
                )));
            }
        }
        this.terminated = this.shutdownProcedure();
    }

    /**
     * Handler for incoming command "SET"
     *
     * @param json the JSON string following SET command
     */
    private void set(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJson = jsonObject.getJSONObject("data");
        String topic = jsonObject.getString("topic");
        Message message = null;

        switch (topic) {

            case Topic.IMPROC_PARAM:
                ImageProcessorParameter improcParam = new ImageProcessorParameter(
                        dataJson.getInt("hueMin"),
                        dataJson.getInt("hueMax"),
                        dataJson.getInt("satMin"),
                        dataJson.getInt("satMax"),
                        dataJson.getInt("valMin"),
                        dataJson.getInt("valMax"),
                        dataJson.getBoolean("storeProcessedImage")
                );
                message = new Message(topic, improcParam);
                break;

            case Topic.PID_PARAM1:
                PidParameter param1 = new PidParameter(
                        dataJson.getDouble("kp"),
                        dataJson.getDouble("ki"),
                        dataJson.getDouble("kd"),
                        dataJson.getDouble("maxOutput"),
                        dataJson.getDouble("minOutput"),
                        dataJson.getDouble("setpoint"),
                        dataJson.getDouble("deadBand"),
                        dataJson.getDouble("maxIOutput"),
                        dataJson.getBoolean("reversed")
                );
                message = new Message(topic, param1);
                break;

            case Topic.PID_PARAM2:
                PidParameter param2 = new PidParameter(
                        dataJson.getDouble("kp"),
                        dataJson.getDouble("ki"),
                        dataJson.getDouble("kd"),
                        dataJson.getDouble("maxOutput"),
                        dataJson.getDouble("minOutput"),
                        dataJson.getDouble("setpoint"),
                        dataJson.getDouble("deadBand"),
                        dataJson.getDouble("maxIOutput"),
                        dataJson.getBoolean("reversed")
                );
                message = new Message(topic, param2);
                break;

            case Topic.REGULATOR_PARAM:
                RegulatorParameter regParam = new RegulatorParameter(
                        dataJson.getDouble("mcMinimumReverse"),
                        dataJson.getDouble("mcMaximumReverse"),
                        dataJson.getDouble("mcMinimumForward"),
                        dataJson.getDouble("mcMaximumForward"),
                        dataJson.getDouble("controllerMinOutput"),
                        dataJson.getDouble("controllerMaxOutput"),
                        dataJson.getDouble("ratio")
                );
                message = new Message(topic, regParam);
                break;

            case Topic.CONTROLLER_INPUT:
                ControlInput ci = new ControlInput(
                        dataJson.getBoolean("manualControl"),
                        dataJson.getDouble("forwardSpeed"),
                        dataJson.getDouble("turnSpeed")
                );
                message = new Message(topic, ci);
                break;

            case Topic.GRIPPER:
                GripperControl gc = new GripperControl(
                        dataJson.getBoolean("command")
                );
                message = new Message(topic, gc);
                break;


            default:
                break;
        }
        if (message != null) {
            this.publish(this.getBroker(), message);
        }
    }

    /**
     * Handler for incoming command "SUB"
     *
     * @param topic the topic to subscribe to
     */
    private void sub(String topic) {
        this.getBroker().subscribeTo(topic, this);
    }

    /**
     * Handler for incoming command "UNSUB"
     *
     * @param topic the topic to unsubscribe from
     */
    private void unsub(String topic) {
        this.getBroker().unsubscribeFrom(topic, this);
    }

    /**
     * Shutdown procedure for the client socket
     *
     * @return true if successful procedure
     */
    private boolean shutdownProcedure() {
        boolean success = false;
        try {
            this.publish(this.getBroker(), new Message(Topic.CONSOLE_OUTPUT, new ConsoleOutput(
                    this + " in shutdown procedure"
            )));
            Thread.sleep(100);
            this.readMessages();

            this.socket.close();
            success = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Publish a message to the broker
     *
     * @param broker  the message broker to publish to
     * @param message the message to publish
     */
    @Override
    public void publish(Broker broker, Message message) {
        broker.addMessage(message);
    }

    /**
     * Handler for reading messages in message queue
     */
    @Override
    protected void doReadMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            this.printWriter.println(message.toJSON());
            this.printWriter.flush();
        }
    }
}

