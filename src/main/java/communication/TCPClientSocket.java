package communication;

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
    private boolean shutdown;
    private boolean terminated;


    public TCPClientSocket(Socket socket, Broker broker) {
        super(broker);
        this.socket = socket;
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
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

    }

    private String get(String variable) {
        String result = "";

        switch (variable) {

        }
        return result;
    }

    private String set(String variable, String json) {
        String reponse = "";
        JSONObject jobj = new JSONObject(json);

        switch (variable) {

        }
        return reponse;
    }

    @Override
    public void publish(Broker broker, Message message) {

    }

    @Override
    protected void readMessages() {

    }
}
