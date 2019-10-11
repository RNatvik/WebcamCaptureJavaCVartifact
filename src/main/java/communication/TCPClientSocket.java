package communication;

import data.Data;
import data.Flag;
import data.ImageProcessorParameter;
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

public class TCPClientSocket extends Subscriber implements Runnable, Publisher {

    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Flag serverShutdown;
    private boolean shutdown;
    private boolean terminated;


    public TCPClientSocket(Socket socket, Flag serverShutdown, Broker broker) {
        super(broker);
        this.socket = socket;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.serverShutdown = serverShutdown;
            this.shutdown = false;
            this.terminated = false;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create TCP client socket");
        }
    }

    public void stop() {
        this.shutdown = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void run() {
        while (!(this.shutdown || this.serverShutdown.get())) {
            try {
                this.readMessages();
                if (this.bufferedReader.ready()) {
                    String line = this.bufferedReader.readLine();
                    System.out.println(this + " received line: " + line);
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
                } else if (this.printWriter.checkError()) {
                    this.stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
                this.stop();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                System.out.println("TCPClientSocket:: client wrote invalid syntax");
            }
        }
        this.terminated = this.shutdownProcedure();
        System.out.println(this + " is terminated: " + this.terminated);
    }

    private void set(String json) {
        // TODO: Implement case for all required data types
        JSONObject jsonObject = new JSONObject(json);
        JSONObject dataJson = jsonObject.getJSONObject("data");
        String topic = jsonObject.getString("topic");
        String type = dataJson.getString("type");
        Message message = null;

        switch (type) {

            case Data.IMPROC_PARAM:
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

            case Data.PID_PARAM:
                break;

            default:
                break;
        }
        this.publish(this.getBroker(), message);
    }

    private void sub(String topic) {
        this.getBroker().subscribeTo(topic, this);
    }

    private void unsub(String topic) {
        this.getBroker().unsubscribeFrom(topic, this);
    }

    private boolean shutdownProcedure() {
        boolean success = false;
        try {
            this.socket.close();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void publish(Broker broker, Message message) {
        broker.addMessage(message);
    }

    @Override
    protected void readMessages() {
        while (!this.getMessageQueue().isEmpty()) {
            Message message = this.getMessageQueue().remove();
            this.printWriter.println(message.toJSON());
            this.printWriter.flush();
        }
    }
}

